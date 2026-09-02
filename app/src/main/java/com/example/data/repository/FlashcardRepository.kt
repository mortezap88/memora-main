package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.ExamEntity
import com.example.data.local.ExamSubmissionEntity
import com.example.data.local.FlashcardEntity
import com.example.data.local.MemoraSettings
import com.example.data.local.NotepadEntity
import com.example.data.local.PersonalMemoryEntity
import com.example.data.local.PresetInstructorRegistry
import com.example.data.local.SpacedRepetitionStages
import com.example.data.local.UserEntity
import com.example.ui.viewmodel.ReviewAction
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class RawAppBackup(
    val version: Int = 1,
    val exportDate: Long = System.currentTimeMillis(),
    val appName: String = "Memora",
    val items: List<FlashcardEntity> = emptyList(),
    val notepads: List<NotepadEntity> = emptyList(),
    val streak: Int = 0,
    val totalReviews: Int = 0,
    val lastReviewDate: String = "",
    val settings: MemoraSettings? = null
)

class FlashcardRepository(
    private val database: AppDatabase,
    private val preferences: AppPreferences
) {
    private val userDao = database.userDao()
    private val dao = database.flashcardDao()
    private val notepadDao = database.notepadDao()
    private val personalMemoryDao = database.personalMemoryDao()
    private val examDao = database.examDao()
    private val examSubmissionDao = database.examSubmissionDao()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val supabase = com.example.data.remote.supabase.SupabaseClient()

    // --- USER MANAGEMENT & AUTH ---
    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    fun getAllStudentsFlow(): Flow<List<UserEntity>> = userDao.getAllStudentsFlow()

    suspend fun syncAllCloudData(currentUsername: String? = null): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var syncedItemsCount = 0

            // 1. Sync Users
            val remoteUsersResult = supabase.fetchAllUsers()
            if (remoteUsersResult.isSuccess) {
                val remoteUsers = remoteUsersResult.getOrNull() ?: emptyList()
                val localUsers = userDao.getAllUsersList()
                
                // Save new remote users to Room
                remoteUsers.forEach { ru ->
                    val existing = userDao.getUserByUsername(ru.username)
                    if (existing == null) {
                        userDao.insertUser(ru)
                        syncedItemsCount++
                    } else if (ru.lastLoginAt > existing.lastLoginAt || ru.displayName != existing.displayName || ru.passwordHash != existing.passwordHash) {
                        userDao.updateUser(ru)
                    }
                }

                // Push any local users not yet in remote
                val missingOnRemote = localUsers.filter { lu -> remoteUsers.none { it.username == lu.username } }
                if (missingOnRemote.isNotEmpty()) {
                    supabase.upsertUsers(missingOnRemote)
                }
            }

            // 2. Sync Exams
            val remoteExamsResult = supabase.fetchAllExams()
            if (remoteExamsResult.isSuccess) {
                val remoteExams = remoteExamsResult.getOrNull() ?: emptyList()
                remoteExams.forEach { exam ->
                    examDao.insertExam(exam)
                    syncedItemsCount++
                }
            }

            // 3. Sync Exam Submissions
            val remoteSubmissionsResult = supabase.fetchAllExamSubmissions()
            if (remoteSubmissionsResult.isSuccess) {
                val remoteSubs = remoteSubmissionsResult.getOrNull() ?: emptyList()
                remoteSubs.forEach { sub ->
                    val existing = examSubmissionDao.getSubmissionForStudent(sub.examId, sub.studentUsername)
                    if (existing == null) {
                        examSubmissionDao.insertSubmission(sub)
                        syncedItemsCount++
                    } else if (sub.submittedAt >= existing.submittedAt) {
                        examSubmissionDao.updateSubmission(sub)
                    }
                }
            }

            // 4. Sync Flashcards if a user is logged in
            if (!currentUsername.isNullOrBlank()) {
                val remoteCardsResult = supabase.fetchFlashcards(currentUsername)
                if (remoteCardsResult.isSuccess) {
                    val remoteCards = remoteCardsResult.getOrNull() ?: emptyList()
                    val localCards = dao.getAllCardsListForUser(currentUsername)

                    remoteCards.forEach { rc ->
                        val localMatch = localCards.find { it.id == rc.id }
                        if (localMatch == null) {
                            dao.insertCard(rc)
                            syncedItemsCount++
                        }
                    }

                    // Push local cards to remote
                    localCards.forEach { lc ->
                        supabase.upsertFlashcard(lc)
                    }
                }
            }

            Result.success(syncedItemsCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensurePresetInstructors() = withContext(Dispatchers.IO) {
        PresetInstructorRegistry.PRESET_INSTRUCTORS.forEach { preset ->
            val clean = preset.username.trim().lowercase()
            val existing = userDao.getUserByUsername(clean)
            if (existing == null) {
                val instructorEntity = UserEntity(
                    username = clean,
                    displayName = preset.fullName,
                    passwordHash = UserEntity.hashPassword(preset.defaultPasswordPlain),
                    role = "INSTRUCTOR",
                    avatarColorHex = preset.avatarColorHex,
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                userDao.insertUser(instructorEntity)
            } else if (existing.role != "INSTRUCTOR") {
                userDao.updateUser(existing.copy(role = "INSTRUCTOR"))
            }
        }
    }

    suspend fun registerStudent(
        username: String,
        passwordPlain: String,
        displayName: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        if (cleanUsername.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Username must be at least 3 characters."))
        }
        if (!cleanUsername.all { it.isLetterOrDigit() || it == '_' || it == '.' }) {
            return@withContext Result.failure(IllegalArgumentException("Username can only contain letters, numbers, underscores, and dots."))
        }
        if (passwordPlain.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 4 characters."))
        }
        if (PresetInstructorRegistry.isReservedInstructorUsername(cleanUsername)) {
            return@withContext Result.failure(IllegalArgumentException("Username '@$cleanUsername' is reserved for instructors. Please use Sign In."))
        }

        val exists = userDao.checkUsernameExists(cleanUsername)
        if (exists > 0) {
            return@withContext Result.failure(IllegalArgumentException("Username '@$cleanUsername' is already taken. Please choose another."))
        }

        val colors = listOf("#A855F7", "#3B82F6", "#10B981", "#EC4899", "#F59E0B", "#6366F1")
        val randomColor = colors[Math.abs(cleanUsername.hashCode()) % colors.size]

        val newUser = UserEntity(
            username = cleanUsername,
            displayName = displayName.trim().ifBlank { cleanUsername.replaceFirstChar { it.uppercase() } },
            passwordHash = UserEntity.hashPassword(passwordPlain),
            role = "STUDENT",
            avatarColorHex = randomColor,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        userDao.insertUser(newUser)
        preferences.saveAuthSession(newUser.username, newUser.displayName, newUser.role)
        Result.success(newUser)
    }

    suspend fun authenticateUser(
        username: String,
        passwordPlain: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        var user = userDao.getUserByUsername(cleanUsername)

        // If it's a preset instructor that hasn't been initialized yet, seed it automatically
        if (user == null && PresetInstructorRegistry.isReservedInstructorUsername(cleanUsername)) {
            val preset = PresetInstructorRegistry.PRESET_INSTRUCTORS.firstOrNull { it.username.equals(cleanUsername, ignoreCase = true) }
            if (preset != null) {
                val instructorEntity = UserEntity(
                    username = cleanUsername,
                    displayName = preset.fullName,
                    passwordHash = UserEntity.hashPassword(preset.defaultPasswordPlain),
                    role = "INSTRUCTOR",
                    avatarColorHex = preset.avatarColorHex,
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                userDao.insertUser(instructorEntity)
                user = instructorEntity
            }
        }

        if (user == null) {
            // Attempt cloud sync to fetch account if registered on mentor's phone
            try {
                val remoteUsers = supabase.fetchAllUsers().getOrNull()
                remoteUsers?.forEach { userDao.insertUser(it) }
                user = userDao.getUserByUsername(cleanUsername)
            } catch (_: Exception) {}
        }

        if (user == null) {
            return@withContext Result.failure(IllegalArgumentException("No account found with username '@$cleanUsername'."))
        }

        val hash = UserEntity.hashPassword(passwordPlain)
        if (user.passwordHash != hash) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect password for '@$cleanUsername'."))
        }

        val updated = user.copy(lastLoginAt = System.currentTimeMillis())
        userDao.updateUser(updated)
        try { supabase.upsertUser(updated) } catch (_: Exception) {}
        preferences.saveAuthSession(updated.username, updated.displayName, updated.role)
        Result.success(updated)
    }

    suspend fun getUser(username: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByUsername(username.trim().lowercase())
    }

    suspend fun createAccountByMentor(
        username: String,
        passwordPlain: String,
        displayName: String,
        role: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        if (cleanUsername.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Username must be at least 3 characters."))
        }
        if (!cleanUsername.all { it.isLetterOrDigit() || it == '_' || it == '.' }) {
            return@withContext Result.failure(IllegalArgumentException("Username can only contain letters, numbers, underscores, and dots."))
        }
        if (passwordPlain.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 4 characters."))
        }

        val exists = userDao.checkUsernameExists(cleanUsername)
        if (exists > 0) {
            return@withContext Result.failure(IllegalArgumentException("Username '@$cleanUsername' is already in use."))
        }

        val normalizedRole = if (role.equals("INSTRUCTOR", ignoreCase = true) || role.equals("MENTOR", ignoreCase = true)) "INSTRUCTOR" else "STUDENT"
        val colors = listOf("#8B5CF6", "#3B82F6", "#10B981", "#EC4899", "#F59E0B", "#6366F1", "#14B8A6")
        val randomColor = colors[Math.abs(cleanUsername.hashCode()) % colors.size]

        val newUser = UserEntity(
            username = cleanUsername,
            displayName = displayName.trim().ifBlank { cleanUsername.replaceFirstChar { it.uppercase() } },
            passwordHash = UserEntity.hashPassword(passwordPlain),
            role = normalizedRole,
            avatarColorHex = randomColor,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = 0L
        )
        userDao.insertUser(newUser)
        try { supabase.upsertUser(newUser) } catch (_: Exception) {}
        Result.success(newUser)
    }

    suspend fun updateAccountByMentor(
        username: String,
        displayName: String,
        newPasswordPlain: String? = null
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        val existing = userDao.getUserByUsername(cleanUsername)
            ?: return@withContext Result.failure(IllegalArgumentException("Account '@$cleanUsername' not found."))

        val newHash = if (!newPasswordPlain.isNullOrBlank()) {
            if (newPasswordPlain.length < 4) {
                return@withContext Result.failure(IllegalArgumentException("New password must be at least 4 characters."))
            }
            UserEntity.hashPassword(newPasswordPlain)
        } else {
            existing.passwordHash
        }

        val updated = existing.copy(
            displayName = displayName.trim().ifBlank { existing.displayName },
            passwordHash = newHash
        )
        userDao.updateUser(updated)
        try { supabase.upsertUser(updated) } catch (_: Exception) {}
        Result.success(updated)
    }

    suspend fun deleteAccountByMentor(username: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        userDao.deleteUser(cleanUsername)
        try { supabase.deleteUser(cleanUsername) } catch (_: Exception) {}
        Result.success(Unit)
    }

    // --- USER-ISOLATED DATA FLOWS ---
    fun getAllCardsForUser(username: String): Flow<List<FlashcardEntity>> = dao.getAllCardsForUser(username)
    fun getActiveCardsForUser(username: String): Flow<List<FlashcardEntity>> = dao.getActiveCardsForUser(username)
    fun getMasteredCardsForUser(username: String): Flow<List<FlashcardEntity>> = dao.getMasteredCardsForUser(username)
    fun getDueCardsForUser(username: String, now: Long = System.currentTimeMillis()): Flow<List<FlashcardEntity>> = dao.getDueCardsForUser(username, now)
    fun getCardsByStageForUser(username: String, stageId: Int): Flow<List<FlashcardEntity>> = dao.getCardsByStageForUser(username, stageId)

    fun getAllNotepadsForUser(username: String): Flow<List<NotepadEntity>> = notepadDao.getAllNotepadsForUser(username)
    fun getAllMemoriesForUserFlow(username: String): Flow<List<PersonalMemoryEntity>> = personalMemoryDao.getAllMemoriesFlowForUser(username)

    // Global / Teacher overview
    val allCardsGlobal: Flow<List<FlashcardEntity>> = dao.getAllCards()

    suspend fun getAllMemoriesListForUser(username: String): List<PersonalMemoryEntity> = withContext(Dispatchers.IO) {
        personalMemoryDao.getAllMemoriesForUser(username)
    }

    suspend fun insertOrUpdateMemory(memory: PersonalMemoryEntity) = withContext(Dispatchers.IO) {
        personalMemoryDao.insertOrUpdateMemory(memory)
    }

    suspend fun insertOrUpdateMemories(memories: List<PersonalMemoryEntity>) = withContext(Dispatchers.IO) {
        personalMemoryDao.insertOrUpdateAll(memories)
    }

    suspend fun deleteMemory(keyword: String, username: String) = withContext(Dispatchers.IO) {
        personalMemoryDao.deleteMemoryByKeywordForUser(keyword, username)
    }

    suspend fun clearAllMemoriesForUser(username: String) = withContext(Dispatchers.IO) {
        personalMemoryDao.clearAllMemoriesForUser(username)
    }

    suspend fun getStudentStatsSummary(username: String): StudentStatsReport = withContext(Dispatchers.IO) {
        val cardsCount = dao.getCardsCountForUser(username)
        val masteredCount = dao.getMasteredCardsCountForUser(username)
        val memoriesCount = personalMemoryDao.getMemoriesCountForUser(username)
        val user = userDao.getUserByUsername(username)
        StudentStatsReport(
            username = username,
            displayName = user?.displayName ?: username,
            role = user?.role ?: "STUDENT",
            avatarColor = user?.avatarColorHex ?: "#A855F7",
            totalCards = cardsCount,
            masteredCards = masteredCount,
            masteryPercentage = if (cardsCount > 0) (masteredCount * 100) / cardsCount else 0,
            memoryEntitiesCount = memoriesCount,
            lastActiveTimestamp = user?.lastLoginAt ?: System.currentTimeMillis()
        )
    }

    suspend fun insertCards(cards: List<FlashcardEntity>) = withContext(Dispatchers.IO) {
        dao.insertCards(cards)
    }

    suspend fun insertCard(card: FlashcardEntity) = withContext(Dispatchers.IO) {
        dao.insertCard(card)
    }

    suspend fun updateCard(card: FlashcardEntity) = withContext(Dispatchers.IO) {
        dao.updateCard(card)
    }

    suspend fun deleteCard(card: FlashcardEntity) = withContext(Dispatchers.IO) {
        dao.deleteCard(card)
    }

    suspend fun deleteCardById(id: String) = withContext(Dispatchers.IO) {
        dao.deleteCardById(id)
    }

    suspend fun promoteCard(card: FlashcardEntity, targetStageId: Int? = null) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val nextStage = targetStageId ?: SpacedRepetitionStages.getNextStageId(card.currentStageId)

        if (nextStage == null || nextStage > SpacedRepetitionStages.STAGES.lastIndex) {
            markAsMastered(card)
        } else {
            val stageInfo = SpacedRepetitionStages.getStage(nextStage)
            val updated = card.copy(
                currentStageId = nextStage,
                dueTimestamp = now + stageInfo.intervalMillis,
                totalReviewsCount = card.totalReviewsCount + 1,
                lastReviewedAt = now
            )
            dao.updateCard(updated)
            preferences.recordReviewCompleted()
        }
    }

    suspend fun resetCardTimer(card: FlashcardEntity) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val stageInfo = SpacedRepetitionStages.getStage(card.currentStageId)
        val updated = card.copy(
            dueTimestamp = now + stageInfo.intervalMillis,
            totalReviewsCount = card.totalReviewsCount + 1,
            lastReviewedAt = now
        )
        dao.updateCard(updated)
        preferences.recordReviewCompleted()
    }

    suspend fun markAsMastered(card: FlashcardEntity) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val updated = card.copy(
            isMastered = true,
            masteredAt = now,
            totalReviewsCount = card.totalReviewsCount + 1,
            lastReviewedAt = now
        )
        dao.updateCard(updated)
        preferences.recordReviewCompleted()
    }

    suspend fun unmasterCard(card: FlashcardEntity, targetStageId: Int = 0) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val stageInfo = SpacedRepetitionStages.getStage(targetStageId)
        val updated = card.copy(
            isMastered = false,
            masteredAt = null,
            currentStageId = targetStageId,
            dueTimestamp = now + stageInfo.intervalMillis
        )
        dao.updateCard(updated)
    }

    suspend fun completeAiCoachReview(
        cardId: String,
        sessionNote: String,
        action: ReviewAction,
        targetStageId: Int? = null
    ) = withContext(Dispatchers.IO) {
        val card = dao.getCardById(cardId) ?: return@withContext
        val currentSessions = card.previousSessions.trim()
        val updatedSessions = if (currentSessions.isBlank() || currentSessions == "[]") {
            sessionNote.trim()
        } else {
            "$currentSessions\n\n${sessionNote.trim()}"
        }
        val cardWithUpdatedSessions = card.copy(previousSessions = updatedSessions)
        when (action) {
            ReviewAction.PROMOTE_NEXT -> promoteCard(cardWithUpdatedSessions, targetStageId)
            ReviewAction.RESET_TIMER -> {
                if (targetStageId != null) {
                    promoteCard(cardWithUpdatedSessions, targetStageId)
                } else {
                    resetCardTimer(cardWithUpdatedSessions)
                }
            }
            ReviewAction.MARK_MASTERED -> markAsMastered(cardWithUpdatedSessions)
        }
    }

    // --- NOTEPAD OPERATIONS ---
    suspend fun insertNotepad(notepad: NotepadEntity) = withContext(Dispatchers.IO) {
        notepadDao.insertNotepad(notepad)
    }

    suspend fun updateNotepad(notepad: NotepadEntity) = withContext(Dispatchers.IO) {
        notepadDao.updateNotepad(notepad)
    }

    suspend fun deleteNotepad(notepad: NotepadEntity) = withContext(Dispatchers.IO) {
        notepadDao.deleteNotepad(notepad)
    }

    suspend fun deleteNotepadById(id: String) = withContext(Dispatchers.IO) {
        notepadDao.deleteNotepadById(id)
    }

    // --- EXAMS & MONITORING SYSTEM ---
    fun getAllExamsFlow(): Flow<List<ExamEntity>> = examDao.getAllExamsFlow()
    fun getExamsForStudentFlow(username: String): Flow<List<ExamEntity>> = examDao.getExamsForStudentFlow(username)

    suspend fun createExam(exam: ExamEntity) = withContext(Dispatchers.IO) {
        examDao.insertExam(exam)
        try { supabase.upsertExam(exam) } catch (_: Exception) {}
    }

    suspend fun deleteExam(exam: ExamEntity) = withContext(Dispatchers.IO) {
        examDao.deleteExam(exam)
        try { supabase.deleteExam(exam.id) } catch (_: Exception) {}
    }

    fun getAllSubmissionsFlow(): Flow<List<ExamSubmissionEntity>> = examSubmissionDao.getAllSubmissionsFlow()
    fun getSubmissionsForStudentFlow(username: String): Flow<List<ExamSubmissionEntity>> = examSubmissionDao.getSubmissionsForStudentFlow(username)
    fun getSubmissionsForExamFlow(examId: String): Flow<List<ExamSubmissionEntity>> = examSubmissionDao.getSubmissionsForExamFlow(examId)

    suspend fun submitExam(submission: ExamSubmissionEntity) = withContext(Dispatchers.IO) {
        examSubmissionDao.insertSubmission(submission)
        try { supabase.upsertExamSubmission(submission) } catch (_: Exception) {}
    }

    suspend fun updateExamSubmission(submission: ExamSubmissionEntity) = withContext(Dispatchers.IO) {
        examSubmissionDao.updateSubmission(submission)
        try { supabase.upsertExamSubmission(submission) } catch (_: Exception) {}
    }

    suspend fun getSubmissionForStudent(examId: String, username: String): ExamSubmissionEntity? = withContext(Dispatchers.IO) {
        examSubmissionDao.getSubmissionForStudent(examId, username)
    }

    // --- BACKUP & RESTORE ---
    suspend fun exportJsonBackup(username: String): String = withContext(Dispatchers.IO) {
        val adapter = moshi.adapter(RawAppBackup::class.java).indent("  ")
        val allCards = dao.getAllCardsListForUser(username)
        val allNotepads = notepadDao.getAllNotepadsListForUser(username)
        val stats = preferences.statsFlow.value
        val settings = preferences.settingsFlow.value
        val backup = RawAppBackup(
            version = 1,
            exportDate = System.currentTimeMillis(),
            appName = "Memora",
            items = allCards,
            notepads = allNotepads,
            streak = stats.streak,
            totalReviews = stats.totalReviews,
            lastReviewDate = stats.lastReviewDate,
            settings = settings
        )
        adapter.toJson(backup)
    }
}

data class StudentStatsReport(
    val username: String,
    val displayName: String,
    val role: String,
    val avatarColor: String,
    val totalCards: Int,
    val masteredCards: Int,
    val masteryPercentage: Int,
    val memoryEntitiesCount: Int,
    val lastActiveTimestamp: Long
)
