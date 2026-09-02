package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.AppStats
import com.example.data.local.AuthSession
import com.example.data.local.ExamEntity
import com.example.data.local.ExamSubmissionEntity
import com.example.data.local.FlashcardEntity
import com.example.data.local.MemoraSettings
import com.example.data.local.NotepadEntity
import com.example.data.local.PersonalMemoryEntity
import com.example.data.local.SpacedRepetitionStages
import com.example.data.local.UserEntity
import com.example.data.remote.ExtractedPersonalMemory
import com.example.data.remote.GeminiClient
import com.example.data.repository.FlashcardRepository
import com.example.data.repository.StudentStatsReport
import com.example.data.speech.AudioRecordState
import com.example.data.speech.AudioRecorderManager
import com.example.data.speech.SpeechRecognitionState
import com.example.data.speech.SpeechToTextManager
import com.example.data.tts.TtsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class ReviewAction {
    PROMOTE_NEXT,
    RESET_TIMER,
    MARK_MASTERED
}

data class ReviewSessionState(
    val cardsQueue: List<FlashcardEntity>,
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val completedCount: Int = 0,
    val promotedCount: Int = 0,
    val resetCount: Int = 0,
    val masteredCount: Int = 0,
    val isFinished: Boolean = false
) {
    val currentCard: FlashcardEntity? get() = cardsQueue.getOrNull(currentIndex)
    val totalCards: Int get() = cardsQueue.size
    val progress: Float get() = if (totalCards > 0) currentIndex.toFloat() / totalCards.toFloat() else 0f
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" or "model" or "system"
    val content: String,
    val imageUrl: String? = null,
    val imageTitle: String? = null,
    val imageSource: String? = null,
    val candidateImages: List<com.example.data.remote.SearchImageResult> = emptyList(),
    val currentImageIndex: Int = 0,
    val isSearchingImage: Boolean = false,
    val imageSearchQuery: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiCoachSessionState(
    val card: FlashcardEntity,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCreationMode: Boolean = false,
    val suggestedTitle: String = "",
    val suggestedMeaning: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class MemoraViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val preferences = AppPreferences(application)
    private val repository = FlashcardRepository(database, preferences)
    val ttsManager = TtsManager(application)
    val speechManager = SpeechToTextManager(application)
    val audioRecorder = AudioRecorderManager(application)

    val authSession: StateFlow<AuthSession> = preferences.authSessionFlow
    val stats: StateFlow<AppStats> = preferences.statsFlow
    val settings: StateFlow<MemoraSettings> = preferences.settingsFlow
    val isTtsPlaying: StateFlow<Boolean> = ttsManager.isPlaying
    val speechState: StateFlow<SpeechRecognitionState> = speechManager.state
    val speechPartialText: StateFlow<String> = speechManager.partialText
    val speechSoundLevel: StateFlow<Float> = speechManager.soundLevel

    val audioRecordState: StateFlow<AudioRecordState> = audioRecorder.state
    val audioRecordDuration: StateFlow<Int> = audioRecorder.durationSeconds
    val audioRecordAmplitude: StateFlow<Float> = audioRecorder.amplitude

    // User-Isolated Flashcard Flows
    val allCards: StateFlow<List<FlashcardEntity>> = authSession.flatMapLatest { session ->
        if (session.username.isBlank()) flowOf(emptyList())
        else repository.getAllCardsForUser(session.username)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCards: StateFlow<List<FlashcardEntity>> = authSession.flatMapLatest { session ->
        if (session.username.isBlank()) flowOf(emptyList())
        else repository.getActiveCardsForUser(session.username)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masteredCards: StateFlow<List<FlashcardEntity>> = authSession.flatMapLatest { session ->
        if (session.username.isBlank()) flowOf(emptyList())
        else repository.getMasteredCardsForUser(session.username)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotepads: StateFlow<List<NotepadEntity>> = authSession.flatMapLatest { session ->
        if (session.username.isBlank()) flowOf(emptyList())
        else repository.getAllNotepadsForUser(session.username)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemories: StateFlow<List<PersonalMemoryEntity>> = authSession.flatMapLatest { session ->
        if (session.username.isBlank()) flowOf(emptyList())
        else repository.getAllMemoriesForUserFlow(session.username)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedNotepadId = MutableStateFlow<String?>(null)

    // Free AI Chat State (User Isolated)
    private val _freeChatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val freeChatMessages: StateFlow<List<ChatMessage>> = _freeChatMessages.asStateFlow()

    private val _isFreeChatLoading = MutableStateFlow(false)
    val isFreeChatLoading: StateFlow<Boolean> = _isFreeChatLoading.asStateFlow()

    // Search and filter state
    val searchQuery = MutableStateFlow("")
    val selectedTypeFilter = MutableStateFlow<String?>(null)
    val selectedStageFilter = MutableStateFlow<Int?>(null)

    // Filtered active cards
    val filteredCards = combine(
        activeCards,
        searchQuery,
        selectedTypeFilter,
        selectedStageFilter
    ) { cards, query, type, stage ->
        cards.filter { card ->
            val matchesQuery = query.isBlank() ||
                card.titleContent.contains(query, ignoreCase = true) ||
                card.descriptionContent.contains(query, ignoreCase = true)
            val matchesType = type == null || card.itemType == type
            val matchesStage = stage == null || card.currentStageId == stage
            matchesQuery && matchesType && matchesStage
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Review Session State
    private val _reviewSession = MutableStateFlow<ReviewSessionState?>(null)
    val reviewSession: StateFlow<ReviewSessionState?> = _reviewSession.asStateFlow()

    // AI Coach Chat Session State
    private val _aiCoachSession = MutableStateFlow<AiCoachSessionState?>(null)
    val aiCoachSession: StateFlow<AiCoachSessionState?> = _aiCoachSession.asStateFlow()

    // Message / Toast banner state
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Real-time clock ticker
    val currentTimeFlow: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(1000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    // --- TEACHER / MONITOR MODE FLOWS ---
    val allStudents: StateFlow<List<UserEntity>> = repository.getAllStudentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudentReports: StateFlow<List<StudentStatsReport>> = allStudents.flatMapLatest { students ->
        flow {
            val reports = students.map { student ->
                repository.getStudentStatsSummary(student.username)
            }
            emit(reports)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExams: StateFlow<List<ExamEntity>> = repository.getAllExamsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentExams: StateFlow<List<ExamEntity>> = authSession.flatMapLatest { session ->
        if (session.username.isBlank()) flowOf(emptyList())
        else repository.getExamsForStudentFlow(session.username)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubmissions: StateFlow<List<ExamSubmissionEntity>> = repository.getAllSubmissionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myStudentSubmissions: StateFlow<List<ExamSubmissionEntity>> = authSession.flatMapLatest { session ->
        if (session.username.isBlank()) flowOf(emptyList())
        else repository.getSubmissionsForStudentFlow(session.username)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CLOUD SYNC STATE & CONTROLS ---
    private val _cloudSyncState = MutableStateFlow(com.example.data.remote.supabase.CloudSyncState.SYNCED)
    val cloudSyncState: StateFlow<com.example.data.remote.supabase.CloudSyncState> = _cloudSyncState.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    fun triggerCloudSync() {
        viewModelScope.launch {
            _cloudSyncState.value = com.example.data.remote.supabase.CloudSyncState.SYNCING
            val res = repository.syncAllCloudData(authSession.value.username)
            if (res.isSuccess) {
                _cloudSyncState.value = com.example.data.remote.supabase.CloudSyncState.SYNCED
                _lastSyncTimestamp.value = System.currentTimeMillis()
            } else {
                _cloudSyncState.value = com.example.data.remote.supabase.CloudSyncState.ERROR
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.ensurePresetInstructors()
            loadUserChatHistory()
            triggerCloudSync()
        }
    }

    private fun loadUserChatHistory() {
        val user = authSession.value.username
        val json = preferences.getFreeChatJson(user)
        if (!json.isNullOrBlank()) {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<ChatMessage>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ChatMessage(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            role = obj.optString("role", "user"),
                            content = obj.optString("content", ""),
                            imageUrl = if (obj.has("imageUrl")) obj.optString("imageUrl") else null,
                            imageTitle = if (obj.has("imageTitle")) obj.optString("imageTitle") else null,
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _freeChatMessages.value = list
            } catch (_: Exception) {
                _freeChatMessages.value = emptyList()
            }
        } else {
            _freeChatMessages.value = emptyList()
        }
    }

    private fun persistFreeChatMessages() {
        val user = authSession.value.username
        if (user.isBlank()) return
        val list = _freeChatMessages.value
        try {
            val array = JSONArray()
            list.takeLast(60).forEach { msg ->
                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("role", msg.role)
                    put("content", msg.content)
                    msg.imageUrl?.let { put("imageUrl", it) }
                    msg.imageTitle?.let { put("imageTitle", it) }
                    put("timestamp", msg.timestamp)
                }
                array.put(obj)
            }
            preferences.saveFreeChatJson(user, array.toString())
        } catch (_: Exception) {}
    }

    // --- AUTHENTICATION ACTIONS ---
    fun signUp(
        username: String,
        passwordPlain: String,
        displayName: String,
        onResult: (Result<UserEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.registerStudent(username, passwordPlain, displayName)
            res.onSuccess {
                loadUserChatHistory()
                showMessage("🎉 Welcome to Memora, ${it.displayName}!")
            }
            onResult(res)
        }
    }

    fun signIn(
        username: String,
        passwordPlain: String,
        onResult: (Result<UserEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.authenticateUser(username, passwordPlain)
            res.onSuccess {
                loadUserChatHistory()
                showMessage("Welcome back, ${it.displayName}!")
            }
            onResult(res)
        }
    }

    fun signOut() {
        preferences.clearAuthSession()
        _freeChatMessages.value = emptyList()
        _reviewSession.value = null
        _aiCoachSession.value = null
        showMessage("Logged out.")
    }

    // --- MENTOR USER PROVISIONING & ROSTER MANAGEMENT ---
    fun createAccountByMentor(
        username: String,
        passwordPlain: String,
        displayName: String,
        role: String,
        onResult: (Result<UserEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.createAccountByMentor(username, passwordPlain, displayName, role)
            res.onSuccess {
                val roleTitle = if (it.role == "INSTRUCTOR") "Mentor" else "Student"
                showMessage("✅ Added new $roleTitle: ${it.displayName} (@${it.username})")
            }
            onResult(res)
        }
    }

    fun updateAccountByMentor(
        username: String,
        displayName: String,
        newPasswordPlain: String?,
        onResult: (Result<UserEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.updateAccountByMentor(username, displayName, newPasswordPlain)
            res.onSuccess {
                showMessage("✅ Updated account @${it.username}")
            }
            onResult(res)
        }
    }

    fun deleteAccountByMentor(
        username: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val currentLoggedUser = authSession.value.username
            if (username.equals(currentLoggedUser, ignoreCase = true)) {
                onResult(Result.failure(IllegalArgumentException("You cannot delete the currently logged-in account.")))
                return@launch
            }
            val res = repository.deleteAccountByMentor(username)
            res.onSuccess {
                showMessage("🗑️ Removed account @$username")
            }
            onResult(res)
        }
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // --- REVIEW & FLASHCARD ACTIONS ---
    fun startGlobalReview() {
        val now = System.currentTimeMillis()
        val dueList = activeCards.value.filter { it.dueTimestamp <= now }
        val queue = if (dueList.isNotEmpty()) dueList else activeCards.value
        if (queue.isEmpty()) {
            showMessage("No cards available to review in your library.")
            return
        }
        _reviewSession.value = ReviewSessionState(cardsQueue = queue)
    }

    fun startStageReview(stageId: Int) {
        val stageCards = activeCards.value.filter { it.currentStageId == stageId }
        val now = System.currentTimeMillis()
        val dueCards = stageCards.filter { it.dueTimestamp <= now }
        val queue = if (dueCards.isNotEmpty()) dueCards else if (stageCards.isNotEmpty()) stageCards else activeCards.value
        if (queue.isEmpty()) {
            val stageName = SpacedRepetitionStages.getStage(stageId).name
            showMessage("No cards currently in $stageName to review.")
            return
        }
        if (queue.size == 1 && (queue.first().itemType == "EXPLANATION" || queue.first().outputSubtype == "AI_COACH")) {
            startAiCoachSession(queue.first())
            return
        }
        _reviewSession.value = ReviewSessionState(cardsQueue = queue)
    }

    fun startSingleCardReview(card: FlashcardEntity) {
        if (card.itemType == "EXPLANATION" || card.outputSubtype == "AI_COACH") {
            startAiCoachSession(card)
        } else {
            _reviewSession.value = ReviewSessionState(cardsQueue = listOf(card))
        }
    }

    fun flipCard() {
        _reviewSession.value?.let { current ->
            _reviewSession.value = current.copy(isFlipped = !current.isFlipped)
        }
    }

    fun handleReviewAction(action: ReviewAction, targetStageId: Int? = null) {
        val current = _reviewSession.value ?: return
        val card = current.currentCard ?: return

        viewModelScope.launch {
            when (action) {
                ReviewAction.PROMOTE_NEXT -> {
                    repository.promoteCard(card, targetStageId)
                    advanceReviewSession(current, promoted = 1, reset = 0, mastered = 0)
                }
                ReviewAction.RESET_TIMER -> {
                    repository.resetCardTimer(card)
                    advanceReviewSession(current, promoted = 0, reset = 1, mastered = 0)
                }
                ReviewAction.MARK_MASTERED -> {
                    repository.markAsMastered(card)
                    advanceReviewSession(current, promoted = 0, reset = 0, mastered = 1)
                }
            }
        }
    }

    private fun advanceReviewSession(
        session: ReviewSessionState,
        promoted: Int,
        reset: Int,
        mastered: Int
    ) {
        val nextIdx = session.currentIndex + 1
        if (nextIdx >= session.cardsQueue.size) {
            _reviewSession.value = session.copy(
                currentIndex = nextIdx,
                isFinished = true,
                completedCount = session.completedCount + 1,
                promotedCount = session.promotedCount + promoted,
                resetCount = session.resetCount + reset,
                masteredCount = session.masteredCount + mastered
            )
        } else {
            _reviewSession.value = session.copy(
                currentIndex = nextIdx,
                isFlipped = false,
                completedCount = session.completedCount + 1,
                promotedCount = session.promotedCount + promoted,
                resetCount = session.resetCount + reset,
                masteredCount = session.masteredCount + mastered
            )
        }
    }

    fun endReviewSession() {
        _reviewSession.value = null
        ttsManager.stop()
    }

    // --- AI COACH SESSIONS ---
    fun startDiscoveryCoachSession(aiDomain: String, linguisticContext: String) {
        val user = authSession.value.username
        val isKnowledge = aiDomain.uppercase() == "KNOWLEDGE"
        val placeholderTitle = if (isKnowledge) "New Concept" else "Target Expression"
        val placeholderMeaning = if (isKnowledge) "Exploring key intuition & mechanism" else "Natural expression for $linguisticContext"
        val tempCard = FlashcardEntity(
            id = java.util.UUID.randomUUID().toString(),
            titleContent = placeholderTitle,
            descriptionContent = placeholderMeaning,
            itemType = "EXPLANATION",
            outputSubtype = "AI_COACH",
            aiDomain = aiDomain,
            linguisticContext = linguisticContext,
            currentStageId = if (isKnowledge) 2 else 0,
            dueTimestamp = System.currentTimeMillis() + (if (isKnowledge) 2 * 3600 * 1000L else 2 * 60 * 1000L),
            audioPronunciationText = "",
            createdAt = System.currentTimeMillis(),
            lastReviewedAt = System.currentTimeMillis(),
            ownerUsername = user
        )

        _aiCoachSession.value = AiCoachSessionState(
            card = tempCard,
            messages = emptyList(),
            isLoading = true,
            isCreationMode = true,
            suggestedTitle = "",
            suggestedMeaning = ""
        )

        viewModelScope.launch {
            try {
                val result = GeminiClient.generateDiscoveryCoachReply(
                    aiDomain = aiDomain,
                    linguisticContext = linguisticContext,
                    userHistory = emptyList(),
                    modelName = settings.value.geminiModel,
                    thinkingLevel = settings.value.thinkingLevel,
                    customApiKey = settings.value.geminiApiKey
                )

                result.onSuccess { greeting ->
                    _aiCoachSession.value = _aiCoachSession.value?.copy(
                        isLoading = false,
                        errorMessage = null,
                        messages = listOf(ChatMessage(role = "model", content = greeting))
                    )
                }.onFailure { error ->
                    _aiCoachSession.value = _aiCoachSession.value?.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to connect to AI Coach",
                        messages = listOf(
                            ChatMessage(
                                role = "model",
                                content = "Hi! Let's explore together. What specific situation or question do you want to practice?"
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                _aiCoachSession.value = _aiCoachSession.value?.copy(
                    isLoading = false,
                    errorMessage = e.message,
                    messages = listOf(
                        ChatMessage(
                            role = "model",
                            content = "Hi! Let's practice. What would you like to discuss?"
                        )
                    )
                )
            }
        }
    }

    fun startAiCoachSession(card: FlashcardEntity) {
        val initialHistory = parseHistoryToMessages(card.previousSessions)
        _aiCoachSession.value = AiCoachSessionState(
            card = card,
            messages = initialHistory,
            isLoading = false,
            errorMessage = null,
            isCreationMode = false
        )

        if (initialHistory.isEmpty()) {
            val domain = card.aiDomain ?: "LINGUISTIC"
            val context = card.linguisticContext ?: "General"
            _aiCoachSession.value = _aiCoachSession.value?.copy(
                messages = listOf(
                    ChatMessage(
                        role = "model",
                        content = if (domain.uppercase() == "KNOWLEDGE") {
                            "Ready to review '${card.titleContent}'. How would you explain this in your own words?"
                        } else {
                            "Hi! Let's practice using '${card.titleContent}' in a $context scenario."
                        }
                    )
                )
            )
        }
    }

    fun sendAiCoachMessage(userText: String) {
        val currentSession = _aiCoachSession.value ?: return
        if (userText.isBlank()) return

        val userMessage = ChatMessage(role = "user", content = userText.trim())
        val updatedMessages = currentSession.messages + userMessage

        _aiCoachSession.value = currentSession.copy(
            messages = updatedMessages,
            isLoading = true,
            errorMessage = null
        )

        val card = currentSession.card
        val domain = card.aiDomain ?: "LINGUISTIC"
        val context = card.linguisticContext ?: "General"

        viewModelScope.launch {
            val historyForAi = updatedMessages.map { it.role to it.content }
            val memories = if (settings.value.personalizationEnabled) repository.getAllMemoriesListForUser(authSession.value.username) else emptyList()

            val result = if (currentSession.isCreationMode) {
                GeminiClient.generateDiscoveryCoachReply(
                    aiDomain = domain,
                    linguisticContext = context,
                    userHistory = historyForAi,
                    modelName = settings.value.geminiModel,
                    thinkingLevel = settings.value.thinkingLevel,
                    customApiKey = settings.value.geminiApiKey
                )
            } else {
                val memoryContext = memories.joinToString("\n") { "• ${it.displayName}: ${it.factsSummary}" }
                GeminiClient.generateExplanationCoach(
                    targetPhrase = card.titleContent,
                    targetMeaning = card.descriptionContent,
                    previousSessions = memoryContext,
                    userHistory = historyForAi,
                    aiDomain = domain,
                    linguisticContext = context,
                    modelName = settings.value.geminiModel,
                    thinkingLevel = settings.value.thinkingLevel,
                    customApiKey = settings.value.geminiApiKey
                )
            }

            result.onSuccess { aiReply ->
                val newMessages = updatedMessages + ChatMessage(role = "model", content = aiReply)
                _aiCoachSession.value = _aiCoachSession.value?.copy(
                    messages = newMessages,
                    isLoading = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                _aiCoachSession.value = _aiCoachSession.value?.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to get AI response"
                )
            }
        }
    }

    fun closeAiCoachSession() {
        _aiCoachSession.value = null
        ttsManager.stop()
    }

    // --- FREE AI CHAT ---
    fun sendFreeChatMessage(text: String) {
        if (text.isBlank()) return
        val user = authSession.value.username
        val userMsg = ChatMessage(role = "user", content = text.trim())
        val updated = _freeChatMessages.value + userMsg
        _freeChatMessages.value = updated
        _isFreeChatLoading.value = true
        persistFreeChatMessages()

        viewModelScope.launch {
            val historyForAi = updated.map { it.role to it.content }
            val memories = if (settings.value.personalizationEnabled) repository.getAllMemoriesListForUser(user) else emptyList()
            val personalContext = memories.joinToString("\n") { "• ${it.displayName}: ${it.factsSummary}" }

            val result = GeminiClient.generateFreeChatResponse(
                userHistory = historyForAi,
                modelName = settings.value.geminiModel,
                thinkingLevel = settings.value.thinkingLevel,
                customApiKey = settings.value.geminiApiKey,
                personalizedContext = personalContext
            )

            result.onSuccess { aiText: String ->
                _freeChatMessages.value = updated + ChatMessage(role = "model", content = aiText)
                _isFreeChatLoading.value = false
                persistFreeChatMessages()

                if (settings.value.personalizationEnabled) {
                    extractAndSaveRelationalMemories(updated.takeLast(8).joinToString("\n") { "${it.role}: ${it.content}" })
                }
            }.onFailure { err ->
                _freeChatMessages.value = updated + ChatMessage(role = "model", content = "Error: ${err.message}")
                _isFreeChatLoading.value = false
            }
        }
    }

    fun clearFreeChat() {
        _freeChatMessages.value = emptyList()
        preferences.clearFreeChatJson(authSession.value.username)
        showMessage("Chat history cleared.")
    }

    private fun extractAndSaveRelationalMemories(transcript: String) {
        val user = authSession.value.username
        if (user.isBlank()) return
        viewModelScope.launch {
            val currentMems = repository.getAllMemoriesListForUser(user)
            val existingSummary = currentMems.joinToString("\n") { "• ${it.displayName}: ${it.factsSummary}" }
            val extractResult = GeminiClient.extractPersonalMemories(
                conversationTranscript = transcript,
                existingMemoriesSummary = existingSummary,
                modelName = settings.value.geminiModel,
                customApiKey = settings.value.geminiApiKey
            )
            extractResult.onSuccess { extracted ->
                extracted.forEach { mem ->
                    repository.insertOrUpdateMemory(
                        PersonalMemoryEntity(
                            keyword = mem.keyword,
                            ownerUsername = user,
                            displayName = mem.displayName,
                            factsSummary = mem.factsSummary
                        )
                    )
                }
            }
        }
    }

    // --- CARD CRUD (USER ISOLATED) ---
    fun saveCard(card: FlashcardEntity) {
        val user = authSession.value.username
        viewModelScope.launch {
            repository.insertCard(card.copy(ownerUsername = user))
            showMessage("Flashcard saved successfully!")
        }
    }

    fun deleteCard(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.deleteCard(card)
            showMessage("Card deleted.")
        }
    }

    fun createCardWithImage(imageUrl: String, title: String) {
        val user = authSession.value.username
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val cleanTitle = title.trim().ifBlank { "Visual Concept" }
            val newCard = FlashcardEntity(
                id = java.util.UUID.randomUUID().toString(),
                titleContent = cleanTitle,
                descriptionContent = "Visual concept for $cleanTitle",
                itemType = "IMAGE",
                cardCategory = "OUTPUT",
                outputSubtype = "IMAGE_TO_WORD",
                aiDomain = "KNOWLEDGE",
                currentStageId = 0,
                dueTimestamp = now + (2 * 60 * 1000L),
                createdAt = now,
                imageUriOrBase64 = imageUrl,
                audioPronunciationText = cleanTitle,
                lastReviewedAt = now,
                ownerUsername = user
            )
            repository.insertCard(newCard)
            showMessage("✨ Created flashcard for '$cleanTitle'!")
        }
    }

    fun createQuickMemo(title: String, memoContent: String) {
        val user = authSession.value.username
        viewModelScope.launch {
            val entity = FlashcardEntity(
                id = java.util.UUID.randomUUID().toString(),
                titleContent = title.trim(),
                descriptionContent = memoContent.trim(),
                itemType = "TEXT",
                currentStageId = 0,
                dueTimestamp = System.currentTimeMillis() + (2 * 60 * 1000L),
                audioPronunciationText = title.trim(),
                createdAt = System.currentTimeMillis(),
                lastReviewedAt = System.currentTimeMillis(),
                ownerUsername = user
            )
            repository.insertCard(entity)
            showMessage("Study memo saved!")
        }
    }

    fun unmasterCard(card: FlashcardEntity, targetStageId: Int = 0) {
        viewModelScope.launch {
            repository.unmasterCard(card, targetStageId)
            showMessage("Card moved back to active stage.")
        }
    }

    fun resetCardTimer(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.resetCardTimer(card)
            showMessage("Review timer reset for ${card.titleContent}")
        }
    }

    // --- NOTEPAD (USER ISOLATED) ---
    fun createNotepad(title: String) {
        val user = authSession.value.username
        val newNote = NotepadEntity(title = title.ifBlank { "Untitled Note" }, content = "", ownerUsername = user)
        viewModelScope.launch {
            repository.insertNotepad(newNote)
            selectedNotepadId.value = newNote.id
            showMessage("New note created.")
        }
    }

    fun saveNotepad(notepad: NotepadEntity) {
        val user = authSession.value.username
        viewModelScope.launch {
            repository.insertNotepad(notepad.copy(ownerUsername = user))
            showMessage("Note saved.")
        }
    }

    fun deleteNotepad(notepad: NotepadEntity) {
        viewModelScope.launch {
            repository.deleteNotepad(notepad)
            showMessage("Note deleted.")
        }
    }

    // --- PERSONAL MEMORY ENTITIES ---
    fun savePersonalMemory(keyword: String, displayName: String, factsSummary: String) {
        val user = authSession.value.username
        viewModelScope.launch {
            repository.insertOrUpdateMemory(
                PersonalMemoryEntity(
                    keyword = keyword.trim().lowercase(),
                    ownerUsername = user,
                    displayName = displayName.trim().ifBlank { keyword },
                    factsSummary = factsSummary.trim()
                )
            )
            showMessage("Personal memory saved for '$displayName'!")
        }
    }

    fun deletePersonalMemory(keyword: String) {
        val user = authSession.value.username
        viewModelScope.launch {
            repository.deleteMemory(keyword, user)
            showMessage("Memory entity deleted.")
        }
    }

    fun clearAllMemories() {
        val user = authSession.value.username
        viewModelScope.launch {
            repository.clearAllMemoriesForUser(user)
            showMessage("All personal memories cleared.")
        }
    }

    // --- EXAMS & TEACHER MONITORING ENGINE ---
    fun createExam(
        title: String,
        description: String,
        targetTopic: String,
        questionsJson: String,
        assignedToUsername: String = "ALL"
    ) {
        val teacher = authSession.value.username
        viewModelScope.launch {
            val exam = ExamEntity(
                title = title.trim(),
                description = description.trim(),
                targetTopic = targetTopic.trim().ifBlank { "General" },
                questionsJson = questionsJson,
                assignedToUsername = assignedToUsername,
                createdByTeacher = teacher
            )
            repository.createExam(exam)
            showMessage("🎓 Exam '${exam.title}' published successfully!")
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            repository.deleteExam(exam)
            showMessage("Exam deleted.")
        }
    }

    fun submitStudentExam(
        examId: String,
        examTitle: String,
        answersJson: String
    ) {
        val student = authSession.value.username
        val studentName = authSession.value.displayName
        viewModelScope.launch {
            val submission = ExamSubmissionEntity(
                examId = examId,
                examTitle = examTitle,
                studentUsername = student,
                studentDisplayName = studentName,
                answersJson = answersJson,
                submittedAt = System.currentTimeMillis(),
                status = "SUBMITTED"
            )
            repository.submitExam(submission)
            showMessage("✅ Exam submitted successfully! Your teacher & AI will review it.")
        }
    }

    fun digestStudentSubmission(
        submission: ExamSubmissionEntity,
        examDescription: String = "Comprehensive assessment of student mastery"
    ) {
        viewModelScope.launch {
            showMessage("🔍 AI is digesting student answers...")
            val result = com.example.data.remote.GeminiClient.api // or direct service call
            try {
                // Parse questions and answers into readable string
                val parsedText = buildReadableAnswersString(submission.answersJson)
                val aiDigestResult = com.example.data.remote.GeminiClient.analyzeStudentExamSubmission(
                    examTitle = submission.examTitle,
                    examDescription = examDescription,
                    studentName = submission.studentDisplayName,
                    questionsAndAnswers = parsedText,
                    apiKey = settings.value.geminiApiKey,
                    modelName = settings.value.geminiModel,
                    thinkingLevel = settings.value.thinkingLevel
                )

                aiDigestResult.onSuccess { digest ->
                    val updated = submission.copy(
                        aiDigestSummary = digest.summary,
                        aiStrengths = digest.strengths,
                        aiWeaknesses = digest.weaknesses,
                        aiRecommendedScore = digest.recommendedGradeOrScore,
                        teacherFeedback = submission.teacherFeedback ?: digest.suggestedTeacherFeedback,
                        status = "AI_DIGESTED"
                    )
                    repository.updateExamSubmission(updated)
                    showMessage("✨ AI Assessment & Digest generated for @${submission.studentUsername}!")
                }.onFailure { error ->
                    showMessage("AI Digest failed: ${error.message}")
                }
            } catch (e: Exception) {
                showMessage("Error generating AI digest: ${e.message}")
            }
        }
    }

    fun sendTeacherFeedback(submission: ExamSubmissionEntity, feedbackText: String) {
        viewModelScope.launch {
            val updated = submission.copy(
                teacherFeedback = feedbackText.trim(),
                status = "REVIEWED"
            )
            repository.updateExamSubmission(updated)
            showMessage("✉️ Feedback sent to student @${submission.studentUsername}!")
        }
    }

    private fun buildReadableAnswersString(answersJson: String): String {
        return try {
            val array = JSONArray(answersJson)
            val sb = StringBuilder()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val q = obj.optString("question", "Question ${i + 1}")
                val a = obj.optString("answer", "No answer provided")
                sb.append("Q: ").append(q).append("\n")
                sb.append("Student Answer: ").append(a).append("\n\n")
            }
            sb.toString()
        } catch (_: Exception) {
            answersJson
        }
    }

    fun updateSettings(newSettings: MemoraSettings) {
        preferences.updateSettings(newSettings)
        showMessage("Settings updated.")
    }

    private fun parseHistoryToMessages(historyJson: String): List<ChatMessage> {
        val list = mutableListOf<ChatMessage>()
        if (historyJson.isBlank() || historyJson == "[]") return list
        try {
            val array = JSONArray(historyJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChatMessage(
                        role = obj.optString("role", "user"),
                        content = obj.optString("content", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        speechManager.shutdown()
        audioRecorder.shutdown()
    }
}
