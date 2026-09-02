package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExamEntity
import com.example.data.local.FlashcardEntity
import com.example.ui.components.ThemedAppBackground
import com.example.ui.theme.CoachPurple
import com.example.ui.theme.MemoraTheme
import com.example.ui.viewmodel.MemoraViewModel

enum class MainTab {
    DASHBOARD,
    NOTEPAD,
    CHAT,
    MONITOR,
    SETTINGS,
    CARDS
}

private fun getTabOrder(tab: MainTab): Int = when (tab) {
    MainTab.DASHBOARD -> 0
    MainTab.NOTEPAD -> 1
    MainTab.CHAT -> 2
    MainTab.MONITOR -> 3
    MainTab.SETTINGS -> 4
    MainTab.CARDS -> 0
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainAppScreen(
    viewModel: MemoraViewModel
) {
    val authSession by viewModel.authSession.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val activeCards by viewModel.activeCards.collectAsState()
    val masteredCards by viewModel.masteredCards.collectAsState()
    val filteredCards by viewModel.filteredCards.collectAsState()
    val allNotepads by viewModel.allNotepads.collectAsState()
    val selectedNotepadId by viewModel.selectedNotepadId.collectAsState()
    val freeChatMessages by viewModel.freeChatMessages.collectAsState()
    val isFreeChatLoading by viewModel.isFreeChatLoading.collectAsState()
    val isTtsPlaying by viewModel.isTtsPlaying.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val speechPartialText by viewModel.speechPartialText.collectAsState()
    val speechSoundLevel by viewModel.speechSoundLevel.collectAsState()
    val audioRecordState by viewModel.audioRecordState.collectAsState()
    val audioRecordDuration by viewModel.audioRecordDuration.collectAsState()
    val audioRecordAmplitude by viewModel.audioRecordAmplitude.collectAsState()
    val allMemories by viewModel.allMemories.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val cloudSyncState by viewModel.cloudSyncState.collectAsState()

    val studentExams by viewModel.studentExams.collectAsState()
    val myStudentSubmissions by viewModel.myStudentSubmissions.collectAsState()

    val reviewSession by viewModel.reviewSession.collectAsState()
    val aiCoachSession by viewModel.aiCoachSession.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedTypeFilter.collectAsState()
    val selectedStage by viewModel.selectedStageFilter.collectAsState()
    val currentTime by viewModel.currentTimeFlow.collectAsState()

    var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    var isChatKeyboardExpanded by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<FlashcardEntity?>(null) }
    var showAddEditSheet by remember { mutableStateOf(false) }
    var examToTake by remember { mutableStateOf<ExamEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // If user is not authenticated, show AuthScreen
    if (!authSession.isLoggedIn) {
        MemoraTheme(
            themeMode = settings.themeMode,
            dynamicColor = false
        ) {
            AuthScreen(
                onSignIn = { username, password, onResult ->
                    viewModel.signIn(username, password) { onResult(it.map { u -> u as Any }) }
                }
            )
        }
        return
    }

    val isInstructor = authSession.role == "INSTRUCTOR"
    val orderedTabs = remember(isInstructor) {
        if (isInstructor) {
            listOf(MainTab.DASHBOARD, MainTab.NOTEPAD, MainTab.CHAT, MainTab.MONITOR, MainTab.SETTINGS)
        } else {
            listOf(MainTab.DASHBOARD, MainTab.NOTEPAD, MainTab.CHAT, MainTab.SETTINGS)
        }
    }

    var totalDragX by remember { mutableStateOf(0f) }

    MemoraTheme(
        themeMode = settings.themeMode,
        dynamicColor = false
    ) {
        ThemedAppBackground(
            themeMode = settings.themeMode,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    // LAYER 1: Full-Screen Tab Content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .pointerInput(currentTab) {
                                detectHorizontalDragGestures(
                                    onDragStart = { totalDragX = 0f },
                                    onDragEnd = {
                                        val swipeThreshold = 60.dp.toPx()
                                        val currentIndex = orderedTabs.indexOf(currentTab)
                                        if (currentIndex != -1) {
                                            if (totalDragX < -swipeThreshold && currentIndex < orderedTabs.size - 1) {
                                                currentTab = orderedTabs[currentIndex + 1]
                                            } else if (totalDragX > swipeThreshold && currentIndex > 0) {
                                                currentTab = orderedTabs[currentIndex - 1]
                                            }
                                        }
                                        totalDragX = 0f
                                    },
                                    onDragCancel = { totalDragX = 0f },
                                    onHorizontalDrag = { _, dragAmount -> totalDragX += dragAmount }
                                )
                            }
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                val isMovingForward = getTabOrder(targetState) > getTabOrder(initialState)
                                if (isMovingForward) {
                                    (slideInHorizontally(
                                        initialOffsetX = { fullWidth -> fullWidth },
                                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                    ) + fadeIn(
                                        animationSpec = tween(durationMillis = 280, easing = LinearEasing)
                                    )).togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> -fullWidth },
                                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                        ) + fadeOut(
                                            animationSpec = tween(durationMillis = 280, easing = LinearEasing)
                                        )
                                    )
                                } else {
                                    (slideInHorizontally(
                                        initialOffsetX = { fullWidth -> -fullWidth },
                                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                    ) + fadeIn(
                                        animationSpec = tween(durationMillis = 280, easing = LinearEasing)
                                    )).togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth },
                                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                        ) + fadeOut(
                                            animationSpec = tween(durationMillis = 280, easing = LinearEasing)
                                        )
                                    )
                                }
                            },
                            label = "main_tab_horizontal_slide",
                            modifier = Modifier.fillMaxSize()
                        ) { targetTab ->
                            when (targetTab) {
                                MainTab.DASHBOARD -> DashboardScreen(
                                    stats = stats,
                                    activeCards = activeCards,
                                    masteredCards = masteredCards,
                                    studentExams = studentExams,
                                    studentSubmissions = myStudentSubmissions,
                                    onOpenExam = { examToTake = it },
                                    onStartGlobalReview = { viewModel.startGlobalReview() },
                                    onStartStageReview = { stageId -> viewModel.startStageReview(stageId) },
                                    onAddNewCard = {
                                        cardToEdit = null
                                        showAddEditSheet = true
                                    },
                                    currentTime = currentTime,
                                    fontScale = settings.fontSizeScale
                                )

                                MainTab.CARDS -> CardsManagementScreen(
                                    cards = filteredCards,
                                    searchQuery = searchQuery,
                                    onSearchChange = { viewModel.searchQuery.value = it },
                                    selectedType = selectedType,
                                    onTypeSelect = { viewModel.selectedTypeFilter.value = it },
                                    selectedStage = selectedStage,
                                    onStageSelect = { viewModel.selectedStageFilter.value = it },
                                    onAddNewCard = {
                                        cardToEdit = null
                                        showAddEditSheet = true
                                    },
                                    onEditCard = { card ->
                                        cardToEdit = card
                                        showAddEditSheet = true
                                    },
                                    onDeleteCard = { card -> viewModel.deleteCard(card) },
                                    onStartReview = { card ->
                                        if (card.itemType == "EXPLANATION") {
                                            viewModel.startAiCoachSession(card)
                                        } else {
                                            viewModel.startSingleCardReview(card)
                                        }
                                    },
                                    onResetTimer = { card -> viewModel.resetCardTimer(card) },
                                    onSpeak = { text -> viewModel.ttsManager.speak(text) },
                                    onStopTts = { viewModel.ttsManager.stop() },
                                    isTtsPlaying = isTtsPlaying,
                                    currentTime = currentTime,
                                    fontScale = settings.fontSizeScale
                                )

                                MainTab.NOTEPAD -> NotepadScreen(
                                    notepads = allNotepads,
                                    selectedNotepadId = selectedNotepadId,
                                    onSelectNotepad = { noteId -> viewModel.selectedNotepadId.value = noteId },
                                    onCreateNotepad = { title -> viewModel.createNotepad(title) },
                                    onRenameNotepad = { pad, title -> viewModel.saveNotepad(pad.copy(title = title)) },
                                    onUpdateContent = { pad, content -> viewModel.saveNotepad(pad.copy(content = content)) },
                                    onDeleteNotepad = { viewModel.deleteNotepad(it) },
                                    onSpeak = { text -> viewModel.ttsManager.speak(text) },
                                    onStopTts = { viewModel.ttsManager.stop() },
                                    isTtsPlaying = isTtsPlaying,
                                    fontScale = settings.fontSizeScale
                                )

                                MainTab.CHAT -> FreeChatScreen(
                                    messages = freeChatMessages,
                                    isLoading = isFreeChatLoading,
                                    onSendMessage = { viewModel.sendFreeChatMessage(it) },
                                    onClearChat = { viewModel.clearFreeChat() },
                                    onSpeak = { text -> viewModel.ttsManager.speak(text) },
                                    onStopTts = { viewModel.ttsManager.stop() },
                                    isTtsPlaying = isTtsPlaying,
                                    speechState = speechState,
                                    speechPartialText = speechPartialText,
                                    audioRecordState = audioRecordState,
                                    onStartVoiceRecording = { viewModel.audioRecorder.startRecording() },
                                    onStopVoiceRecording = { onTranscribed ->
                                        viewModel.audioRecorder.stopAndTranscribe(
                                            selectedModel = settings.geminiModel,
                                            customApiKey = settings.geminiApiKey,
                                            onResult = onTranscribed
                                        )
                                    },
                                    onCancelVoiceRecording = { viewModel.audioRecorder.cancelRecording() },
                                    onResetVoiceRecording = { viewModel.audioRecorder.resetState() },
                                    onAddCardFromChat = { domain, contextCode, title, meaning, transcript ->
                                        // add card
                                    },
                                    onInsertImageToNotepad = { imageUrl, caption ->
                                        // insert image
                                    },
                                    onCreateCardWithImage = { imageUrl, title ->
                                        viewModel.createCardWithImage(imageUrl, title)
                                    },
                                    onRequestImageForMessage = { _, _ -> },
                                    onCycleNextImage = { _ -> },
                                    settings = settings,
                                    isKeyboardExpanded = isChatKeyboardExpanded,
                                    onKeyboardExpandedChange = { isChatKeyboardExpanded = it },
                                    fontScale = settings.fontSizeScale
                                )

                                MainTab.MONITOR -> TeacherMonitorScreen(
                                    viewModel = viewModel
                                )

                                 MainTab.SETTINGS -> SettingsScreen(
                                    settings = settings,
                                    onUpdateSettings = { viewModel.updateSettings(it) },
                                    onResetToDefault = { },
                                    onClearAll = { },
                                    onExportBackup = { callback -> },
                                    onImportBackup = { json, fullRestore -> },
                                    onTestTts = { text -> viewModel.ttsManager.speak(text) },
                                    authSession = authSession,
                                    onSignOut = { viewModel.signOut() },
                                    allUsers = allUsers,
                                    onCreateAccount = { u, p, d, r, cb ->
                                        viewModel.createAccountByMentor(u, p, d, r, cb)
                                    },
                                    onUpdateAccount = { u, d, p, cb ->
                                        viewModel.updateAccountByMentor(u, d, p, cb)
                                    },
                                    onDeleteAccount = { u, cb ->
                                        viewModel.deleteAccountByMentor(u, cb)
                                    },
                                    masteredCards = masteredCards,
                                    cards = filteredCards,
                                    searchQuery = searchQuery,
                                    onSearchChange = { viewModel.searchQuery.value = it },
                                    selectedType = selectedType,
                                    onTypeSelect = { viewModel.selectedTypeFilter.value = it },
                                    selectedStage = selectedStage,
                                    onStageSelect = { viewModel.selectedStageFilter.value = it },
                                    onAddNewCard = {
                                        cardToEdit = null
                                        showAddEditSheet = true
                                    },
                                    onEditCard = { card ->
                                        cardToEdit = card
                                        showAddEditSheet = true
                                    },
                                    onStartReview = { card ->
                                        if (card.itemType == "EXPLANATION") {
                                             viewModel.startAiCoachSession(card)
                                        } else {
                                             viewModel.startSingleCardReview(card)
                                        }
                                    },
                                    onResetTimer = { card -> viewModel.resetCardTimer(card) },
                                    onUnmasterCard = { card, targetStage -> viewModel.unmasterCard(card, targetStage) },
                                    onDeleteCard = { card -> viewModel.deleteCard(card) },
                                    onSpeak = { text -> viewModel.ttsManager.speak(text) },
                                    onStopTts = { viewModel.ttsManager.stop() },
                                    isTtsPlaying = isTtsPlaying,
                                    currentTime = currentTime,
                                    memories = allMemories,
                                    onDeleteMemory = { keyword -> viewModel.deletePersonalMemory(keyword) },
                                    onClearAllMemories = { viewModel.clearAllMemories() },
                                    onSaveMemory = { kw, name, facts -> viewModel.savePersonalMemory(kw, name, facts) },
                                    cloudSyncState = cloudSyncState,
                                    onTriggerCloudSync = { viewModel.triggerCloudSync() }
                                )
                            }
                        }
                    }
                }

                // LAYER 2: Floating Bottom Navigation Bar
                val shouldShowBottomNav = reviewSession == null && aiCoachSession == null

                AnimatedVisibility(
                    visible = shouldShowBottomNav,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    val isDarkNav = MaterialTheme.colorScheme.background.luminance() < 0.5f

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = if (isDarkNav) Color(0xFF12141F).copy(alpha = 0.94f) else Color(0xFFFFFFFF).copy(alpha = 0.95f),
                            border = BorderStroke(
                                1.2.dp,
                                if (isDarkNav) Color.White.copy(alpha = 0.18f) else Color(0xFFE2E8F0)
                            ),
                            shadowElevation = if (isDarkNav) 6.dp else 3.dp,
                            tonalElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .clip(RoundedCornerShape(24.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val navTabs = remember(isInstructor) {
                                    if (isInstructor) {
                                        listOf(
                                            Triple(MainTab.DASHBOARD, "Stages", Icons.Default.Dashboard to Icons.Outlined.Dashboard),
                                            Triple(MainTab.NOTEPAD, "Notepad", Icons.Default.EditNote to Icons.Outlined.EditNote),
                                            Triple(MainTab.CHAT, "AI Chat", Icons.Default.ChatBubble to Icons.Outlined.ChatBubbleOutline),
                                            Triple(MainTab.MONITOR, "Mentor", Icons.Default.School to Icons.Outlined.School),
                                            Triple(MainTab.SETTINGS, "Settings", Icons.Default.Settings to Icons.Outlined.Settings)
                                        )
                                    } else {
                                        listOf(
                                            Triple(MainTab.DASHBOARD, "Stages", Icons.Default.Dashboard to Icons.Outlined.Dashboard),
                                            Triple(MainTab.NOTEPAD, "Notepad", Icons.Default.EditNote to Icons.Outlined.EditNote),
                                            Triple(MainTab.CHAT, "AI Chat", Icons.Default.ChatBubble to Icons.Outlined.ChatBubbleOutline),
                                            Triple(MainTab.SETTINGS, "Settings", Icons.Default.Settings to Icons.Outlined.Settings)
                                        )
                                    }
                                }

                                navTabs.forEach { (tab, label, iconPair) ->
                                    val isSelected = currentTab == tab
                                    val activeColor = if (tab == MainTab.MONITOR) CoachPurple else MaterialTheme.colorScheme.primary
                                    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                currentTab = tab
                                                isChatKeyboardExpanded = false
                                            }
                                            .padding(vertical = 3.dp)
                                            .testTag("nav_tab_${label.lowercase()}")
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) iconPair.first else iconPair.second,
                                            contentDescription = label,
                                            tint = if (isSelected) activeColor else inactiveColor,
                                            modifier = Modifier.size(22.dp)
                                        )

                                        Spacer(modifier = Modifier.height(1.dp))

                                        Text(
                                            text = label,
                                            fontSize = (10.5 * settings.fontSizeScale).sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) activeColor else inactiveColor,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Snackbar Host
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 96.dp)
                )

                // Fullscreen Review Overlay
                AnimatedVisibility(
                    visible = reviewSession != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    reviewSession?.let { session ->
                        ReviewScreen(
                            sessionState = session,
                            onFlip = { viewModel.flipCard() },
                            onAction = { action, targetStageId ->
                                viewModel.handleReviewAction(action, targetStageId)
                            },
                            onCloseSession = { viewModel.endReviewSession() },
                            onStartAiCoach = { card ->
                                viewModel.startAiCoachSession(card)
                            },
                            onSpeak = { text -> viewModel.ttsManager.speak(text) },
                            onStopTts = { viewModel.ttsManager.stop() },
                            isTtsPlaying = isTtsPlaying,
                            fontScale = settings.fontSizeScale
                        )
                    }
                }

                // Fullscreen AI Coach Chat Session Overlay
                AnimatedVisibility(
                    visible = aiCoachSession != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    aiCoachSession?.let { session ->
                        ExplanationAiSessionScreen(
                            sessionState = session,
                            onSendMessage = { msg -> viewModel.sendAiCoachMessage(msg) },
                            onCompleteSession = { action, stageId ->
                                viewModel.handleReviewAction(action, stageId)
                                viewModel.closeAiCoachSession()
                            },
                            onSaveCreationCard = { title, meaning ->
                                viewModel.saveCard(
                                    session.card.copy(
                                        titleContent = title,
                                        descriptionContent = meaning
                                    )
                                )
                                viewModel.closeAiCoachSession()
                            },
                            onCloseSession = {
                                viewModel.closeAiCoachSession()
                            },
                            onSpeak = { text -> viewModel.ttsManager.speak(text) },
                            onStopTts = { viewModel.ttsManager.stop() },
                            isTtsPlaying = isTtsPlaying,
                            speechState = speechState,
                            speechPartialText = speechPartialText,
                            speechSoundLevel = speechSoundLevel,
                            onStartSpeech = { viewModel.speechManager.startListening() },
                            onStopSpeech = { viewModel.speechManager.stopListening() },
                            onResetSpeech = { viewModel.speechManager.resetState() },
                            audioRecordState = audioRecordState,
                            audioRecordDuration = audioRecordDuration,
                            audioRecordAmplitude = audioRecordAmplitude,
                            onStartAudioRecording = { viewModel.audioRecorder.startRecording() },
                            onStopAudioRecording = { onTranscribed ->
                                viewModel.audioRecorder.stopAndTranscribe(
                                    selectedModel = settings.geminiModel,
                                    customApiKey = settings.geminiApiKey,
                                    onResult = onTranscribed
                                )
                            },
                            onCancelAudioRecording = { viewModel.audioRecorder.cancelRecording() },
                            onResetAudioRecording = { viewModel.audioRecorder.resetState() },
                            onAddCardFromChat = { domain, contextCode, title, meaning, transcript ->
                                // add card
                            },
                            settings = settings,
                            fontScale = settings.fontSizeScale,
                            onUpdateApiKey = { key ->
                                viewModel.updateSettings(settings.copy(geminiApiKey = key))
                            },
                            onRetry = {
                                session.messages.lastOrNull { it.role == "user" }?.let { lastUserMsg ->
                                    viewModel.sendAiCoachMessage(lastUserMsg.content)
                                }
                            }
                        )
                    }
                }

                // Student Exam Dialog
                examToTake?.let { exam ->
                    val existingSub = myStudentSubmissions.firstOrNull { it.examId == exam.id }
                    StudentExamDialog(
                        exam = exam,
                        existingSubmission = existingSub,
                        onDismiss = { examToTake = null },
                        onSubmitAnswers = { answersJson ->
                            viewModel.submitStudentExam(exam.id, exam.title, answersJson)
                            examToTake = null
                        }
                    )
                }

                // Add/Edit Card BottomSheet
                if (showAddEditSheet) {
                    AddEditCardBottomSheet(
                        cardToEdit = cardToEdit,
                        onDismiss = { showAddEditSheet = false },
                        onSave = { card -> viewModel.saveCard(card) },
                        onTestSpeak = { text -> viewModel.ttsManager.speak(text) },
                        onStartDiscoverySession = { domain, context ->
                            viewModel.startDiscoveryCoachSession(domain, context)
                        },
                        fontScale = settings.fontSizeScale
                    )
                }
            }
        }
    }
}
