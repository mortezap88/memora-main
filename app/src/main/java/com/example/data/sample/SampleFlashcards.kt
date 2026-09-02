package com.example.data.sample

import com.example.data.local.FlashcardEntity
import java.util.UUID

object SampleFlashcards {
    fun getInitialSamples(): List<FlashcardEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // 1. Output: AI Coach - Linguistic (Friend Context, Communicative Script Scenario)
            FlashcardEntity(
                id = "sample_card_ai_friend_1",
                titleContent = "Bite the bullet",
                descriptionContent = "To face a difficult situation with courage and force oneself to do something unpleasant or painful.",
                itemType = "EXPLANATION",
                cardCategory = "OUTPUT",
                outputSubtype = "AI_COACH",
                aiDomain = "LINGUISTIC",
                linguisticContext = "FRIEND",
                linguisticScenarioType = "COMMUNICATIVE",
                currentStageId = 0, // 2m stage (Due now)
                dueTimestamp = now - 1000L,
                createdAt = now - 120000L,
                previousSessions = """[{"date":"Creation","notes":"Initial creation chat: Student wanted practice with everyday dialogue script when facing difficult decisions."}]"""
            ),

            // 2. Output: Word to Sound (Pronunciation / Phonetic Test)
            FlashcardEntity(
                id = "sample_card_word_to_sound_1",
                titleContent = "Ubiquitous",
                descriptionContent = "/juːˈbɪk.wə.təs/ — Present, appearing, or found everywhere simultaneously.",
                itemType = "AUDIO",
                cardCategory = "OUTPUT",
                outputSubtype = "WORD_TO_SOUND",
                currentStageId = 0,
                dueTimestamp = now - 5000L,
                createdAt = now - 100000L,
                audioPronunciationText = "Ubiquitous"
            ),

            // 3. Output: Sound to Writing (Spelling & Dictation Test)
            FlashcardEntity(
                id = "sample_card_sound_to_writing_1",
                titleContent = "Ephemeral",
                descriptionContent = "Spelling: E-P-H-E-M-E-R-A-L. Meaning: Lasting for a very short time; fleeting or transitory.",
                itemType = "AUDIO",
                cardCategory = "OUTPUT",
                outputSubtype = "SOUND_TO_WRITING",
                currentStageId = 1, // 20m stage
                dueTimestamp = now - 8000L,
                createdAt = now - 200000L,
                audioPronunciationText = "Ephemeral"
            ),

            // 4. Input Card: Passive recognition (Single-sided review without flip)
            FlashcardEntity(
                id = "sample_card_input_1",
                titleContent = "Ebbinghaus Spaced Repetition Principle",
                descriptionContent = "Memory decay is steepest right after initial exposure. Reviewing at increasing intervals (2m, 20m, 2h, 1d, 3d, 1w, 2w, 1mo) resets the forgetting curve and transitions information into permanent retention.",
                itemType = "TEXT",
                cardCategory = "INPUT",
                outputSubtype = "NONE",
                currentStageId = 1,
                dueTimestamp = now - 15000L,
                createdAt = now - 250000L
            ),

            // 5. Output: AI Coach - Linguistic (Girlfriend / Intimate Context)
            FlashcardEntity(
                id = "sample_card_ai_gf_1",
                titleContent = "To mean the world to someone",
                descriptionContent = "To be extraordinarily important, cherished, and deeply loved by someone.",
                itemType = "EXPLANATION",
                cardCategory = "OUTPUT",
                outputSubtype = "AI_COACH",
                aiDomain = "LINGUISTIC",
                linguisticContext = "GIRLFRIEND",
                linguisticScenarioType = "COMMUNICATIVE",
                currentStageId = 2, // 2 Hours stage
                dueTimestamp = now - 2000L,
                createdAt = now - 400000L,
                previousSessions = """[{"date":"Interval 1 (20m)","notes":"Practiced romantic and supportive response when comforting girlfriend after work."}]"""
            ),

            // 6. Output: AI Coach - Knowledge / Conceptual Domain (Sequence & Facts)
            FlashcardEntity(
                id = "sample_card_ai_knowledge_1",
                titleContent = "Action Potential Depolarization",
                descriptionContent = "When threshold is reached, voltage-gated Na+ channels open rapidly, causing sodium influx and reversing membrane polarity from -70mV to +30mV before repolarization occurs.",
                itemType = "EXPLANATION",
                cardCategory = "OUTPUT",
                outputSubtype = "AI_COACH",
                aiDomain = "KNOWLEDGE",
                currentStageId = 2,
                dueTimestamp = now + (30 * 60 * 1000L),
                createdAt = now - 500000L
            ),

            // 7. Output: Image to Word Card
            FlashcardEntity(
                id = "sample_card_image_1",
                titleContent = "Synaptic Transmission",
                descriptionContent = "The biological process by which a neuron communicates with a target cell across a synapse via neurotransmitter release.",
                itemType = "IMAGE",
                cardCategory = "OUTPUT",
                outputSubtype = "IMAGE_TO_WORD",
                currentStageId = 3, // 1 Day stage
                dueTimestamp = now + (4 * 60 * 60 * 1000L),
                createdAt = now - 600000L,
                imageUriOrBase64 = null
            ),

            // 8. Output: AI Coach - Linguistic (Class / Formal & Polite Context)
            FlashcardEntity(
                id = "sample_card_ai_class_1",
                titleContent = "To shed light on",
                descriptionContent = "To clarify, explain, or provide new insight on a complex topic in an academic or formal setting.",
                itemType = "EXPLANATION",
                cardCategory = "OUTPUT",
                outputSubtype = "AI_COACH",
                aiDomain = "LINGUISTIC",
                linguisticContext = "CLASS",
                linguisticScenarioType = "LOGICAL",
                currentStageId = 4, // 3 Days stage
                dueTimestamp = now + (2 * 24 * 60 * 60 * 1000L),
                createdAt = now - 700000L
            ),

            // 9. Mastered Card
            FlashcardEntity(
                id = "sample_card_mastered_1",
                titleContent = "Serendipity",
                descriptionContent = "The occurrence and development of events by chance in a happy or beneficial way.",
                itemType = "TEXT",
                cardCategory = "OUTPUT",
                outputSubtype = "WORD_TO_SOUND",
                currentStageId = 7,
                dueTimestamp = now + (28 * 24 * 60 * 60 * 1000L),
                createdAt = now - 1000000L,
                isMastered = true,
                masteredAt = now - 3600000L,
                totalReviewsCount = 8
            )
        )
    }
}
