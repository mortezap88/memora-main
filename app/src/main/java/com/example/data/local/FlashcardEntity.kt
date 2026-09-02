package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val titleContent: String,
    val descriptionContent: String,
    val itemType: String = "TEXT", // Legacy compatibility: "TEXT", "IMAGE", "AUDIO", "EXPLANATION"
    val cardCategory: String = "OUTPUT", // "INPUT" (Passive single-sided) or "OUTPUT" (Active testing)
    val outputSubtype: String = "WORD_TO_SOUND", // "WORD_TO_SOUND", "SOUND_TO_WRITING", "IMAGE_TO_WORD", "AI_COACH", "NONE"
    val aiDomain: String? = null, // "LINGUISTIC" or "KNOWLEDGE"
    val linguisticContext: String? = null, // "GIRLFRIEND", "FRIEND", "CLASS"
    val linguisticScenarioType: String? = null, // "COMMUNICATIVE", "LOGICAL", "DESCRIPTIVE"
    val currentStageId: Int = 0, // 0 to 7 (2m, 20m, 2h, 1d, 3d, 1w, 2w, 1mo)
    val dueTimestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val isMastered: Boolean = false,
    val masteredAt: Long? = null,
    val imageUriOrBase64: String? = null,
    val audioPronunciationText: String? = null,
    val previousSessions: String = "[]", // JSON array of past conversation/review histories
    val totalReviewsCount: Int = 0,
    val lastReviewedAt: Long? = null,
    val ownerUsername: String = ""
) {
    fun countPastSessions(): Int {
        val history = previousSessions.trim()
        if (history.isBlank() || history == "[]") return 0
        if (history.startsWith("[") && history.endsWith("]")) {
            val jsonCount = Regex("""\{\s*"date"""").findAll(history).count()
            if (jsonCount > 0) return jsonCount
        }
        val sessionHeaderCount = Regex("""(?:---)?\s*Session\s*#\d+""", RegexOption.IGNORE_CASE).findAll(history).count()
        if (sessionHeaderCount > 0) return sessionHeaderCount
        return history.split("\n\n").filter { it.isNotBlank() }.size.coerceAtLeast(1)
    }
}

enum class CardCategory(val displayName: String) {
    INPUT("Input (Passive Review)"),
    OUTPUT("Output (Active Recall)")
}

enum class OutputSubtype(val displayName: String, val shortLabel: String) {
    WORD_TO_SOUND("Word → Sound", "Pronounce"),
    SOUND_TO_WRITING("Sound → Writing", "Spelling"),
    IMAGE_TO_WORD("Image → Word", "Visual"),
    AI_COACH("AI Interactive Coach", "AI Coach")
}

enum class AiDomain(val displayName: String) {
    LINGUISTIC("Word / Grammar"),
    KNOWLEDGE("Knowledge")
}

enum class LinguisticContext(val displayName: String, val emoji: String, val description: String) {
    CLASS("Classroom", "🎓", "Classroom and academic context"),
    FRIEND("Friend", "🤝", "Casual conversations with friends"),
    GIRLFRIEND("Girlfriend", "💖", "Intimate, personal conversations")
}

enum class LinguisticScenarioType(val displayName: String, val description: String) {
    COMMUNICATIVE("Communicative Dialogue", "Character script scenario with dialogue turns"),
    LOGICAL("Logical / Cause & Effect", "Connectives: because, so, as a result of"),
    DESCRIPTIVE("Descriptive / Spatial", "Spatial: on top of, under, or demeanor description")
}

data class StageInfo(
    val id: Int,
    val name: String,
    val shortName: String,
    val intervalMillis: Long,
    val description: String
)

object SpacedRepetitionStages {
    val STAGES = listOf(
        StageInfo(0, "2 Minutes", "2m", 2 * 60 * 1000L, "Immediate recall"),
        StageInfo(1, "20 Minutes", "20m", 20 * 60 * 1000L, "Short-term consolidation"),
        StageInfo(2, "2 Hours", "2h", 2 * 60 * 60 * 1000L, "Intra-day retention"),
        StageInfo(3, "1 Day", "1d", 24 * 60 * 60 * 1000L, "Next day review"),
        StageInfo(4, "3 Days", "3d", 3 * 24 * 60 * 60 * 1000L, "Multi-day buffer"),
        StageInfo(5, "1 Week", "1w", 7 * 24 * 60 * 60 * 1000L, "Weekly reinforcement"),
        StageInfo(6, "2 Weeks", "2w", 14 * 24 * 60 * 60 * 1000L, "Fortnightly retention"),
        StageInfo(7, "1 Month", "1mo", 30 * 24 * 60 * 60 * 1000L, "Long-term mastery")
    )

    fun getStage(id: Int): StageInfo {
        return STAGES.getOrElse(id.coerceIn(0, STAGES.lastIndex)) { STAGES[0] }
    }

    fun getNextStageId(currentId: Int): Int? {
        return if (currentId < STAGES.lastIndex) currentId + 1 else null
    }
}
