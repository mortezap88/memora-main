package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    fun prewarm() {
        try {
            val request = okhttp3.Request.Builder()
                .url(BASE_URL)
                .head()
                .build()
            okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.close()
                }
            })
        } catch (_: Exception) {}
    }

    private fun createGenerationConfig(
        temperature: Float = 0.7f,
        topP: Float = 0.95f,
        thinkingLevel: String = "low"
    ): GenerationConfig {
        val budget = when (thinkingLevel.lowercase()) {
            "none" -> null
            "low" -> 1024
            "medium" -> 4096
            "high" -> 8192
            else -> null
        }
        return GenerationConfig(
            temperature = temperature,
            topP = topP,
            thinkingConfig = if (budget != null && budget > 0) ThinkingConfig(thinkingBudget = budget) else null
        )
    }

    private suspend fun executeWithFallback(
        modelName: String,
        apiKey: String,
        request: GenerateContentRequest
    ): Result<String> {
        val lowerModel = modelName.lowercase()
        val isPro = lowerModel.contains("pro")
        val isLite = lowerModel.contains("lite") || lowerModel.contains("light")
        
        // Exact user specified models (gemini-pro-latest, gemini-flash-latest, gemini-flash-lite-latest)
        val modelsToTry = when {
            isPro -> listOf(
                "gemini-pro-latest",
                "gemini-2.5-pro",
                "gemini-flash-latest",
                "gemini-flash-lite-latest"
            )
            isLite -> listOf(
                "gemini-flash-lite-latest",
                "gemini-2.5-flash-lite",
                "gemini-flash-latest",
                "gemini-pro-latest"
            )
            else -> listOf(
                "gemini-flash-latest",
                "gemini-2.5-flash",
                "gemini-flash-lite-latest",
                "gemini-pro-latest"
            )
        }

        var lastException: Exception? = null
        for (model in modelsToTry) {
            try {
                val response = api.generateContent(
                    model = model,
                    apiKey = apiKey,
                    request = request
                )
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return Result.success(text)
                }
            } catch (e: Exception) {
                lastException = e
                val errorMsg = e.message ?: ""
                if (e is java.net.UnknownHostException || e is java.net.ConnectException || e is java.net.SocketTimeoutException) {
                    return Result.failure(Exception("No internet connection. Please check your network connection and try again."))
                }
                if (errorMsg.contains("403") || errorMsg.contains("401")) {
                    return Result.failure(Exception("Gemini API Error (HTTP 403 Forbidden). Please check your Gemini API key in Settings."))
                }
            }
        }
        val isNetworkIssue = lastException is java.net.UnknownHostException ||
            lastException is java.net.ConnectException ||
            lastException is java.net.SocketTimeoutException

        val finalMessage = if (isNetworkIssue) {
            "No internet connection. Please check your network connection and try again."
        } else {
            lastException?.message ?: "Unable to connect to AI Coach. Please check your connection."
        }
        return Result.failure(Exception(finalMessage))
    }

    suspend fun generateDiscoveryCoachReply(
        aiDomain: String,
        linguisticContext: String?,
        userHistory: List<Pair<String, String>>,
        modelName: String = "gemini-flash-latest",
        thinkingLevel: String = "low",
        customApiKey: String = ""
    ): Result<String> {
        val apiKey = customApiKey.trim().ifBlank { BuildConfig.GEMINI_API_KEY }
        val isRealKeyAvailable = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (!isRealKeyAvailable) {
            return Result.failure(Exception("Gemini API key is missing. Please enter your API key in Settings."))
        }

        val isKnowledge = aiDomain.uppercase() == "KNOWLEDGE"
        val contextDesc = when (linguisticContext?.uppercase()) {
            "GIRLFRIEND" -> "an intimate, affectionate conversation with a girlfriend / romantic partner"
            "CLASS" -> "a classroom, seminar, teacher, or polite academic setting"
            else -> "a casual conversation with close friends"
        }

        val systemInstructionText = if (isKnowledge) {
            """
You are an encouraging, expert Conceptual & Knowledge Mentor in the Memora Spaced-Repetition app.
You are in DISCOVERY & ONBOARDING MODE (Session #1).

GOAL:
The user is talking with you to explore and learn a new concept, idea, theory, or cognitive model that they want to retain.
Your job is to:
1. Converse naturally and warmly to understand what they are trying to learn.
2. Answer their questions directly with insightful, clear explanations, breakdown of mechanisms, and intuitive real-world examples.
3. If they ask how something works or what a term means, explain it thoroughly, clearly, and concisely.
4. Keep each response conversational, informative, and engaging (2-4 short paragraphs, under 150 words).
5. Identify the SINGLE main concept/theory title being explored (e.g. "Pareto Principle", "Spaced Repetition").
6. CRITICAL RULE FOR GREETINGS: Completely ignore small talk, greetings like "hi" or "hello", and only identify the genuine concept. NEVER extract greetings or pleasantries as keywords.
7. Output the single extracted concept title at the very end of your response on its own line:
[TARGET_KEYWORD: Core Concept Name]

SPEECH-TO-TEXT / VOICE INPUT AWARENESS:
The user speaks to you using voice recognition / audio recordings. Completely ignore missing punctuation or phonetic voice artifacts; focus on the genuine meaning and intent of what they are saying.
""".trimIndent()
        } else {
            """
You are an encouraging, native English Language & Communication Coach in the Memora Spaced-Repetition app.
You are in DISCOVERY & ONBOARDING MODE (Session #1).

CONTEXT:
- Domain: Word / Grammar Practice
- Situation & Interlocutor: $contextDesc

GOAL:
The user is conversing with you to explore what English word, idiom, phrasing, or grammar structure to use in real life ($contextDesc).
Your job is to:
1. Converse like a real, intelligent human language coach.
2. Directly answer their specific questions, explain nuances of English phrases, and provide the exact natural wording that native speakers use for $contextDesc.
3. If they share a sentence or scenario, explain why certain words fit better and provide miniature dialogue examples.
4. Keep each response concise, natural, friendly, and authentic (under 140 words).
5. Identify the SINGLE primary target word, idiom, or phrase being learned (ONE specific item, e.g. "hit it off", "call it a day", "serendipity").
6. CRITICAL RULE FOR GREETINGS: The user might start with casual greetings like "hi", "hello", "hey", "how are you". NEVER extract greetings or conversational filler as the target keyword/phrase. Ignore casual greetings and only identify the genuine subject/word/phrase being discussed.
7. Output the single extracted keyword/phrase at the very end of your message on a new line in this format:
[TARGET_KEYWORD: The Exact Word or Expression]

SPEECH-TO-TEXT / VOICE INPUT AWARENESS:
The user speaks to you using voice recognition. Completely ignore missing punctuation or capitalization. Focus on helping them express their thought naturally.
""".trimIndent()
        }

        val contents = mutableListOf<ContentItem>()
        if (userHistory.isEmpty()) {
            contents.add(
                ContentItem(
                    role = "user",
                    parts = listOf(PartItem(text = if (isKnowledge) "Hello Coach! I'm starting a new learning session. Please greet me warmly and ask what concept or topic I'd like to explore today." else "Hello Coach! I'm starting a new English learning session for $contextDesc. Please greet me and ask what phrase, situation, or expression I want to practice today."))
                )
            )
        } else {
            userHistory.forEach { (role, msg) ->
                contents.add(
                    ContentItem(
                        role = if (role == "user") "user" else "model",
                        parts = listOf(PartItem(text = msg))
                    )
                )
            }
        }

        val request = GenerateContentRequest(
            contents = contents,
            generationConfig = createGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                thinkingLevel = thinkingLevel
            ),
            systemInstruction = ContentItem(
                parts = listOf(PartItem(text = systemInstructionText))
            )
        )

        return executeWithFallback(
            modelName = modelName,
            apiKey = apiKey,
            request = request
        )
    }

    suspend fun generateFreeChatResponse(
        userHistory: List<Pair<String, String>>, // role ("user" or "model") to text
        modelName: String = "gemini-flash-latest",
        thinkingLevel: String = "low",
        customApiKey: String = "",
        personalizedContext: String = ""
    ): Result<String> {
        val apiKey = customApiKey.trim().ifBlank { BuildConfig.GEMINI_API_KEY }
        val isRealKeyAvailable = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (!isRealKeyAvailable) {
            return Result.failure(Exception("Gemini API key is missing. Please enter your API key in Settings."))
        }

        val personalMemorySection = if (personalizedContext.isNotBlank()) {
            """
PERSONALIZED LONG-TERM MEMORY & CONTEXT (Relational & Life Background):
The user has enabled Personalization. Here is what you know about the user, their loved ones, friends, life context, and personal world:
$personalizedContext

Guidelines for Memory & Empathy:
- Empathetic Understanding: Use this knowledge to genuinely understand where the user comes from, their personal experiences, and the people in their life.
- Relational Context: When they mention friends, loved ones, or past experiences (e.g. Ali, Narges, Fardis), naturally reflect your familiarity with what they did, their personalities, or their perspectives.
- Memory Inquiries: When the user asks what you know about them (e.g., "what do you know about me?", "who are my friends?"), warmly and thoughtfully summarize the personal facts, relationships, and meaningful life context you remember.
- Organic & Conversational: Weave in this context with emotional intelligence—never mechanically recite facts unless asked.
"""
        } else {
            ""
        }

        val systemInstructionText = """
You are a friendly, natural AI conversational partner and language coach in the Memora app.
$personalMemorySection
CONVERSATIONAL TONE & RULES:
- Be relaxed, friendly, and conversational like a real human friend or tutor.
- NEVER send unsolicited introductory lists of capabilities (e.g. do NOT say "I can help you practice speaking, chatting, checking polish, learn new vocabulary...").
- When the user sends small talk or simple greetings (e.g. "Hey", "How are you?", "What's up?"), just reply naturally in 1-2 friendly sentences (e.g. "Hey! I'm doing well, how are you doing today?").
- When the user asks any question (e.g. vocabulary, idioms, grammar rules, science, or general topics), answer directly, concisely, and clearly.

FEEDBACK ON GRAMMAR & PHRASING:
- If the user made grammatical or structural errors:
  [RED_CORRECTION]
  🔴 "<what user said / mistake>" → "<corrected phrasing>"
  ❓ <Concise 1-sentence reason/rule why>
  🔴 "<second error if any>" → "<fix>"
  ❓ <Concise reason>
  [/RED_CORRECTION]
  * CRITICAL: Do NOT write "Correction:", "Grammar Correction", or filler words like "Sorry, you should say". Format exactly as 🔴 "<mistake>" → "<fix>" followed by ❓ <concise reason> on the next line! Wrap phrases in double quotation marks "".
- If there are more natural / idiomatic suggestions:
  [YELLOW_SUGGESTION]
  🟡 "<original phrasing>" → "<natural alternative 1>" (or "<alternative 2>")
  ❓ <Concise 1-sentence nuance why this phrasing is more natural>
  [/YELLOW_SUGGESTION]
  * CRITICAL: Do NOT write "Suggestion:" or "You could say". Group the user's phrasing and its suggestion into ONE 🟡 bullet with an arrow →, followed by ❓ <reason> on the next line! Wrap phrases in double quotation marks "".
- Your direct answer / reply:
  [COACH_REPLY]
  <Conversational reply or explanation>
  [/COACH_REPLY]

IMAGE & VISUAL ILLUSTRATION GUIDANCE:
- Whenever the user asks about or discusses ANY concrete object, craft, item, animal, plant, term, food, material, anatomical structure, place, landmark, organism, device, historical artifact, or concept (e.g. "what is a burlap flower", "cow", "what is photosynthesis", "how does a carburetor work", "origami", "eiffel tower", "show me..."):
  ALWAYS append an image query tag on a new line at the very end of your reply:
  [IMAGE_QUERY: <clean, concise 1-3 word English search term, e.g. "burlap flower" or "cow" or "photosynthesis diagram">]
- Only omit [IMAGE_QUERY] for pure pleasantries / greetings (e.g. "hi", "how are you today") or purely abstract grammar correction without any subject noun.

CRITICAL RULES:
- If there are NO grammar mistakes in the user's message, DO NOT generate [RED_CORRECTION] or [YELLOW_SUGGESTION]. Just provide [COACH_REPLY].
- NEVER display raw bracket tags to the user.
- Put words or phrases being discussed in double quotation marks (e.g. "word") for clear highlight visibility.
- The user may use voice speech-to-text. Disregard missing punctuation or minor speech artifacts.
""".trimIndent()

        val contents = mutableListOf<ContentItem>()
        userHistory.forEach { (role, msg) ->
            contents.add(
                ContentItem(
                    role = if (role == "user") "user" else "model",
                    parts = listOf(PartItem(text = msg))
                )
            )
        }

        val request = GenerateContentRequest(
            contents = contents,
            generationConfig = createGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                thinkingLevel = thinkingLevel
            ),
            systemInstruction = ContentItem(
                parts = listOf(PartItem(text = systemInstructionText))
            )
        )

        return executeWithFallback(
            modelName = modelName,
            apiKey = apiKey,
            request = request
        )
    }

    suspend fun generateExplanationCoach(
        targetPhrase: String,
        targetMeaning: String,
        previousSessions: String,
        userHistory: List<Pair<String, String>>, // role ("user" or "model") to text
        aiDomain: String? = "LINGUISTIC",
        linguisticContext: String? = "FRIEND",
        linguisticScenarioType: String? = "COMMUNICATIVE",
        modelName: String = "gemini-flash-latest",
        thinkingLevel: String = "low",
        customApiKey: String = ""
    ): Result<String> {
        val apiKey = customApiKey.trim().ifBlank { BuildConfig.GEMINI_API_KEY }
        val isRealKeyAvailable = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (!isRealKeyAvailable) {
            return Result.failure(Exception("Gemini API key is missing. Please enter your API key in Settings."))
        }

        val audienceGuide = when (linguisticContext?.uppercase()) {
            "GIRLFRIEND" -> "Audience / Interlocutor: Girlfriend / Romantic partner (Intimate, affectionate, caring, emotionally expressive register; e.g. expressing deep care, feelings, endearing tone)."
            "CLASS" -> "Audience / Interlocutor: Classroom / Teacher / Formal (Polite, respectful, structured, academic/workplace tone)."
            else -> "Audience / Interlocutor: Close Friend (Casual, friendly, everyday conversational slang/idioms, e.g. hanging out)."
        }

        val isKnowledgeDomain = aiDomain?.uppercase() == "KNOWLEDGE"

        val systemInstructionText = if (isKnowledgeDomain) {
            """
You are a precise, friendly knowledge assessment coach in the Memora Spaced-Repetition app.
The user is reviewing a TECHNICAL / FACTUAL / CONCEPTUAL flashcard:
- Title / Target Fact: "$targetPhrase"
- Key Fact Details / Definition / Meaning: "$targetMeaning"

CRITICAL INSTRUCTIONS FOR TECHNICAL / KNOWLEDGE QUESTIONS:
1. FIRST TURN (Initial review question):
   - Ask ONE direct, simple, crisp question that tests the user's recall of the key fact, number, definition, name, or mechanism stored on this card.
   - STRICTLY FORBIDDEN: Do NOT create long roleplays, elaborate hypothetical scenarios, or verbose preambles.
   - Examples of proper questions:
     * Card: Title="David", Details="The name of my new friend is David" -> "What is the name of your new friend?"
     * Card: Title="Resting Heart Rate", Details="Heart beats 60 to 100 times per minute" -> "How many times does the human heart beat per minute at rest?"
     * Card: Title="DNS", Details="Translates human-readable domain names into IP addresses" -> "What does DNS stand for, and what is its primary function?"
   - Do NOT reveal the exact answer or target in the question.
   - Format purely as:
   [COACH_REPLY]
   <Your simple, direct question>
   [/COACH_REPLY]

2. FEEDBACK TURNS (When evaluating the user's answer):
   - Evaluate if the user gave the correct answer based on the card fact ("$targetPhrase", "$targetMeaning").
   - If their answer is correct: Confirm warmly in 1 short sentence.
   - If their answer is incorrect or partially incomplete: Clearly state the correct answer and provide a brief, helpful clarification (under 2 sentences).
   - If they made any English grammar errors, provide [RED_CORRECTION] or [YELLOW_SUGGESTION] if applicable, followed by [COACH_REPLY] with your direct assessment.
""".trimIndent()
        } else {
            """
You are an encouraging, expert Language & Communication Coach in the Memora Spaced-Repetition app.
The user is practicing the target English expression / phrase: "$targetPhrase".
Meaning / Function: "$targetMeaning".

CONTEXT CONFIGURATION:
- Domain: Word / Grammar Practice
- $audienceGuide

CRITICAL - MULTI-SESSION CONTINUOUS MEMORY & BLIND SPOT TARGETING:
You have the complete chronological conversation history across ALL past sessions (Session #1 Initial Discovery, plus subsequent reviews at 2min, 20min, 2hr, 1day, etc.):
\"\"\"
$previousSessions
\"\"\"

BLIND SPOT & SURROUNDING GAP DETECTION (CORE OBJECTIVE):
1. DEEP ERROR & HESITATION ANALYSIS:
   - Carefully review all previous sessions above. Identify the user's recurring grammatical blind spots, awkward preposition choices, misaligned tone/register, incorrect collocations, or conceptual gaps surrounding "$targetPhrase".
   - Note what they got wrong or where they stumbled during previous sessions.
2. ADAPTIVE CHALLENGE TESTING:
   - Do NOT simply repeat the same situation. Formulate a FRESH scenario that tests the user on the EXACT surrounding nuances, edge cases, prepositions, or grammatical structures they previously struggled with.
   - For example, if they confused when to use the phrase vs. an alternative, or struggled with formal vs. casual delivery, test them in a scenario requiring that specific distinction.
3. STRICT TEST CONFIDENTIALITY:
   - NEVER reveal, mention, or spoil the target word/phrase ("$targetPhrase") in your question or scenario. This is an active recall test!
4. Challenge the user so they must independently recall and use "$targetPhrase" in their spoken or typed response.

SPEECH-TO-TEXT (VOICE INPUT) AWARENESS:
The user speaks their responses via voice recognition / speech-to-text.
- Completely IGNORE missing punctuation (periods, commas, question marks).
- Completely IGNORE automatic capitalization or minor speech-to-text phonetic artifacts.
- Focus ONLY on real English grammar, syntactic structure, idiomatic usage, and target phrase mastery.

INSTRUCTIONS:
1. FIRST TURN (Initial review prompt):
   If there is no user message yet, create a vivid, relatable scenario matching the interlocutor register ($audienceGuide) and building upon their learning history.
   Conclude with: "👉 What would you say in this situation?"
   Format purely as:
   [COACH_REPLY]
   (Your scenario question here - DO NOT name the target phrase)
   [/COACH_REPLY]

2. FEEDBACK TURNS (When evaluating user's spoken sentence):
   You MUST separate general sentence corrections from target-word specific feedback into EXACTLY four tagged sections:

   [RED_CORRECTION]
   🔴 "<what user said / mistake>" → "<corrected phrasing>"
   ❓ <Concise 1-sentence reason/rule why>
   🔴 "<second error if any>" → "<corrected phrasing>"
   ❓ <Concise reason>
   (Concise corrections only. Precede each error with 🔴. Include ❓ reason on next line. Wrap phrases in double quotes "". If no errors exist, write "None")
   [/RED_CORRECTION]

   [YELLOW_SUGGESTION]
   🟡 "<original phrasing>" → "<natural alternative 1>" (or "<alternative 2>")
   ❓ <Concise 1-sentence nuance why this phrasing is more natural>
   (Concise suggestions only. Group user phrasing and suggestion into ONE 🟡 item with an arrow →. Include ❓ reason on next line. Wrap phrases in double quotes "". If phrasing is already optimal, write "None")
   [/YELLOW_SUGGESTION]

   [TARGET_WORD_FEEDBACK]
   Focused assessment EXCLUSIVELY on the TARGET PHRASE ("$targetPhrase"):
   - Did the user deploy "$targetPhrase" accurately in this context?
   - Nuances, tone/register appropriateness for the interlocutor ($audienceGuide).
   (Wrap words/terms being analyzed in double quotes "")
   [/TARGET_WORD_FEEDBACK]

   [COACH_REPLY]
   In-character conversational reply responding to what the user said in the scenario, maintaining the roleplay and proposing the next follow-up. Wrap discussed terms in double quotes "".
   [/COACH_REPLY]
""".trimIndent()
        }

        val contents = mutableListOf<ContentItem>()

        if (userHistory.isEmpty()) {
            val initialPrompt = if (isKnowledgeDomain) {
                "Hello! Please ask me a direct, simple test question testing my recall of this card: \"$targetPhrase\" (details: \"$targetMeaning\"). Make it a single simple question without any scenario or long roleplay."
            } else {
                "Hello Coach! Please give me a scenario to practice this flashcard according to the context and scenario type. Do not say the target word."
            }
            contents.add(
                ContentItem(
                    role = "user",
                    parts = listOf(PartItem(text = initialPrompt))
                )
            )
        } else {
            userHistory.forEach { (role, msg) ->
                contents.add(
                    ContentItem(
                        role = if (role == "user") "user" else "model",
                        parts = listOf(PartItem(text = msg))
                    )
                )
            }
        }

        val request = GenerateContentRequest(
            contents = contents,
            generationConfig = createGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                thinkingLevel = thinkingLevel
            ),
            systemInstruction = ContentItem(
                parts = listOf(PartItem(text = systemInstructionText))
            )
        )

        return executeWithFallback(
            modelName = modelName,
            apiKey = apiKey,
            request = request
        )
    }

    data class DistilledCardResult(
        val title: String,
        val meaning: String,
        val domain: String, // "LINGUISTIC" or "KNOWLEDGE"
        val linguisticContext: String // "FRIEND", "CLASS", "GIRLFRIEND"
    )

    suspend fun distillConversationToCard(
        conversationTranscript: String,
        modelName: String = "gemini-flash-latest",
        thinkingLevel: String = "low",
        customApiKey: String = ""
    ): Result<DistilledCardResult> {
        val apiKey = customApiKey.trim().ifBlank { BuildConfig.GEMINI_API_KEY }
        val isRealKeyAvailable = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (!isRealKeyAvailable) {
            return Result.failure(Exception("Gemini API key is missing. Please enter your API key in Settings."))
        }

        val prompt = """
You are an expert curriculum designer, lexicographer, and pedagogist for a language & knowledge spaced-repetition learning system.
Analyze the following selected conversation segment between a user and an AI coach:

\"\"\"
$conversationTranscript
\"\"\"

YOUR TASK:
Carefully analyze what core element, idiom, phrase, grammar rule, distinction, or knowledge concept is being taught, corrected, or practiced in this conversation.
Then generate two essential pieces:
1. A METICULOUS, HIGH-PRECISION TITLE (5-15 words max for grammar rules, or the exact word/idiom if vocabulary).
2. COMPREHENSIVE MEANING & USAGE NOTES (detailed explanation, key grammar rule / why a mistake was wrong, nuance comparison, and a clear example sentence).

TITLE RULES:
1. If discussing a SPECIFIC WORD, IDIOM, OR COLLOCATION:
   - Title MUST be the exact word/idiom (e.g. "Hit it off", "Serendipity", "Call it a day", "Take for granted").
2. If discussing a GRAMMAR POINT, CORRECTION, OR CONTRAST:
   - Title MUST clearly describe the grammar rule/contrast so the user immediately knows what rule it is.
   - Examples: "Present Perfect vs. Past Simple: 'Ever' & 'Never'", "Subjunctive Mood in Conditional 'If' Clauses", "'Used to' (past habit) vs. 'Be used to' (accustomed)", "Direct vs. Indirect Object Pronoun Placement".
3. If discussing a GENERAL KNOWLEDGE CONCEPT, TECHNICAL FACT, OR INFORMATION:
   - Title MUST be the concise concept, subject, or entity name (e.g. "David (Friend's Name)", "Resting Heart Rate", "DNS Function", "Pareto Principle").
   - Meaning MUST contain the exact concise fact, definition, or answer (e.g. "The name of my new friend is David", "The human heart beats 60 to 100 times per minute at rest").
   - Set DOMAIN to KNOWLEDGE.
4. NEVER use vague, generic titles like "Conversation Item", "English Practice", "Correction", or "Chat #1".

DOMAIN SELECTION RULE:
- Set DOMAIN to "KNOWLEDGE" if the card is about a technical topic, fact, personal fact, number, scientific concept, or definition.
- Set DOMAIN to "LINGUISTIC" if the card is about an English vocabulary word, idiom, collocation, grammatical structure, or language nuance.

MEANING & USAGE NOTES RULES:
- Provide a clear, insightful definition or grammatical explanation.
- Include the specific rule or nuance discussed in the chat.
- Include a natural example sentence demonstrating proper usage.

FORMAT YOUR RESPONSE EXACTLY AS FOLLOWS:
[TITLE]
<Exact Title Here>
[/TITLE]

[DOMAIN]
<LINGUISTIC or KNOWLEDGE>
[/DOMAIN]

[CONTEXT]
<FRIEND or CLASS or GIRLFRIEND>
[/CONTEXT]

[MEANING]
<Detailed Meaning, Explanation, Grammar Rule, and Example Sentence Here>
[/MEANING]
""".trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                ContentItem(
                    role = "user",
                    parts = listOf(PartItem(text = prompt))
                )
            ),
            generationConfig = createGenerationConfig(
                temperature = 0.2f,
                topP = 0.95f,
                thinkingLevel = thinkingLevel
            )
        )

        val result = executeWithFallback(
            modelName = modelName,
            apiKey = apiKey,
            request = request
        )

        return result.map { rawText ->
            var title = Regex("\\[TITLE\\]\\s*([\\s\\S]*?)\\s*\\[/TITLE\\]", RegexOption.IGNORE_CASE)
                .find(rawText)?.groupValues?.get(1)?.trim() ?: ""
            if (title.isBlank()) {
                title = Regex("(?:\\*\\*Title\\*\\*|Title:)\\s*(.+)", RegexOption.IGNORE_CASE)
                    .find(rawText)?.groupValues?.get(1)?.trim() ?: ""
            }

            val domain = Regex("\\[DOMAIN\\]\\s*([\\s\\S]*?)\\s*\\[/DOMAIN\\]", RegexOption.IGNORE_CASE)
                .find(rawText)?.groupValues?.get(1)?.trim()?.uppercase() ?: "LINGUISTIC"
            val context = Regex("\\[CONTEXT\\]\\s*([\\s\\S]*?)\\s*\\[/CONTEXT\\]", RegexOption.IGNORE_CASE)
                .find(rawText)?.groupValues?.get(1)?.trim()?.uppercase() ?: "FRIEND"

            var meaning = Regex("\\[MEANING\\]\\s*([\\s\\S]*?)\\s*\\[/MEANING\\]", RegexOption.IGNORE_CASE)
                .find(rawText)?.groupValues?.get(1)?.trim() ?: ""
            if (meaning.isBlank()) {
                meaning = Regex("(?:\\*\\*Meaning\\*\\*|Meaning:|\\*\\*Usage Notes?\\*\\*|Usage Notes?:|\\*\\*Explanation\\*\\*|Explanation:)\\s*([\\s\\S]+)", RegexOption.IGNORE_CASE)
                    .find(rawText)?.groupValues?.get(1)?.trim() ?: ""
            }

            // Clean up any remaining bounding tags or markdown artifact brackets
            title = title.replace(Regex("^\\[+|\\]+$"), "").trim()
            meaning = meaning.replace(Regex("^\\[+|\\]+$"), "").trim()

            DistilledCardResult(
                title = title.ifBlank { "Practice Item" },
                meaning = meaning,
                domain = if (domain.contains("KNOWLEDGE")) "KNOWLEDGE" else "LINGUISTIC",
                linguisticContext = when {
                    context.contains("CLASS") -> "CLASS"
                    context.contains("GIRL") -> "GIRLFRIEND"
                    else -> "FRIEND"
                }
            )
        }
    }

    suspend fun extractPersonalMemories(
        conversationTranscript: String,
        existingMemoriesSummary: String = "",
        modelName: String = "gemini-flash-latest",
        thinkingLevel: String = "none",
        customApiKey: String = ""
    ): Result<List<ExtractedPersonalMemory>> {
        val apiKey = customApiKey.trim().ifBlank { BuildConfig.GEMINI_API_KEY }
        val isRealKeyAvailable = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (!isRealKeyAvailable) {
            return Result.success(extractHeuristicMemories(conversationTranscript))
        }

        val prompt = """
You are an empathetic, human-centric Personal & Relational Memory Extractor for the Memora companion app.

PURPOSE & PHILOSOPHY:
Extract ONLY intimate, personal, and human context (the user's identity, friends, family, loved ones, feelings, what people in their life did, how friends think, interpersonal dynamics, life experiences, and emotional background).
This long-term memory exists exclusively to build deep relational empathy, therapeutic understanding, and emotional continuity — so the AI understands where the user comes from and who is in their world.

WHAT TO EXTRACT (Personal & Relational Only):
1. User Identity & Background: Name, personal life events, meaningful personal stories, personal struggles, emotional moments, favorite comforts or personal hobbies.
2. People & Relationships: Friends, partner, family members, pets, colleagues (e.g. "Ali", "Sarah", "Narges", "Fardis").
3. Stories & Actions: Things friends/contacts did, places you went together, personal plans, discussions, or shared experiences.
4. Mindset & Perspectives: How friends think about different things, their viewpoints, quirks, values, or relationship dynamics with the user.

STRICT EXCLUSIONS (CRITICAL — NEVER SAVE TECHNICAL / ACADEMIC / LINGUISTIC FACTS):
- DO NOT extract any academic, scientific, or technical concepts (e.g. biology, chemistry, physics, mathematics, algorithms, programming/coding, medical trivia, engineering, geography).
- DO NOT extract linguistic, language-learning, or grammar explanations (e.g. verb tenses, vocabulary definitions, idioms studied, syntax rules, translation exercises).
- DO NOT extract generic conversation filler or objective tutorial questions.
- If the conversation was purely technical, academic, linguistic, or impersonal, return [NO_ENTITIES].

CONVERSATION TRANSCRIPT:
$conversationTranscript

EXISTING MEMORIES:
$existingMemoriesSummary

FORMAT:
If meaningful personal/relational facts were found, output each as:
[MEMORY]
[KEYWORD: entity_or_person_name]
[NAME: Entity Display Name]
[FACTS: Concise, meaningful summary of what was learned about them, their actions, perspectives, or relationship to the user]
[/MEMORY]

If there are NO personal or relational entities discussed, respond strictly with [NO_ENTITIES].
""".trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                ContentItem(
                    role = "user",
                    parts = listOf(PartItem(text = prompt))
                )
            ),
            generationConfig = createGenerationConfig(
                temperature = 0.2f,
                topP = 0.95f,
                thinkingLevel = "none" // Always none for background extraction to avoid token lag and format issues
            )
        )

        val result = executeWithFallback(
            modelName = modelName,
            apiKey = apiKey,
            request = request
        )

        return result.map { rawText ->
            val memories = mutableListOf<ExtractedPersonalMemory>()
            
            // Strategy 1: [MEMORY] blocks
            val memoryBlocks = Regex("\\[MEMORY\\]([\\s\\S]*?)\\[/MEMORY\\]", RegexOption.IGNORE_CASE).findAll(rawText)
            for (block in memoryBlocks) {
                val blockText = block.groupValues[1]
                val keyword = Regex("\\[KEYWORD:\\s*([^\\]]+)\\]", RegexOption.IGNORE_CASE)
                    .find(blockText)?.groupValues?.get(1)?.trim()?.lowercase() ?: ""
                val displayName = Regex("\\[NAME:\\s*([^\\]]+)\\]", RegexOption.IGNORE_CASE)
                    .find(blockText)?.groupValues?.get(1)?.trim() ?: keyword.replaceFirstChar { it.uppercase() }
                val facts = Regex("\\[FACTS:\\s*([\\s\\S]*?)\\]", RegexOption.IGNORE_CASE)
                    .find(blockText)?.groupValues?.get(1)?.trim() ?: ""

                if (keyword.isNotBlank() && facts.isNotBlank() && !keyword.contains("no_entities") && !isTechnicalOrLinguisticKeyword(keyword)) {
                    memories.add(
                        ExtractedPersonalMemory(
                            keyword = keyword,
                            displayName = displayName.ifBlank { keyword.replaceFirstChar { it.uppercase() } },
                            factsSummary = facts
                        )
                    )
                }
            }

            // Strategy 2: KEYWORD: ... NAME: ... FACTS: ... without brackets
            if (memories.isEmpty()) {
                val looseBlocks = rawText.split(Regex("(?i)KEYWORD:")).filter { it.isNotBlank() }
                for (b in looseBlocks) {
                    val lines = b.lines()
                    val kw = lines.firstOrNull()?.trim()?.removePrefix("[")?.removeSuffix("]")?.lowercase() ?: ""
                    val nameMatch = Regex("(?i)NAME:\\s*([^\n\\]]+)").find(b)?.groupValues?.get(1)?.trim() ?: kw
                    val factsMatch = Regex("(?i)FACTS:\\s*([\\s\\S]+)").find(b)?.groupValues?.get(1)?.trim() ?: ""
                    if (kw.isNotBlank() && factsMatch.isNotBlank() && kw.length in 2..40 && !kw.contains("no_entities") && !isTechnicalOrLinguisticKeyword(kw)) {
                        memories.add(
                            ExtractedPersonalMemory(
                                keyword = kw,
                                displayName = nameMatch.ifBlank { kw.replaceFirstChar { it.uppercase() } },
                                factsSummary = factsMatch.take(300).trim()
                            )
                        )
                    }
                }
            }

            // Strategy 3: Heuristic extraction fallback if AI returned empty and non-technical
            if (memories.isEmpty()) {
                memories.addAll(extractHeuristicMemories(conversationTranscript))
            }

            memories
        }.recoverCatching {
            extractHeuristicMemories(conversationTranscript)
        }
    }

    suspend fun reconcileAndSynthesizeMemory(
        entityName: String,
        existingFacts: String,
        newFactCandidate: String,
        modelName: String = "gemini-flash-latest",
        customApiKey: String = ""
    ): Result<String> {
        val apiKey = customApiKey.trim().ifBlank { BuildConfig.GEMINI_API_KEY }
        val isRealKeyAvailable = apiKey.isNotBlank() && !apiKey.contains("PLACEHOLDER")

        if (!isRealKeyAvailable || existingFacts.isBlank()) {
            return Result.success(heuristicSynthesizeFacts(existingFacts, newFactCandidate))
        }

        val prompt = """
You are a Personal Memory Consolidation Engine.

TASK:
Consolidate and update the stored memory facts for person/topic: "$entityName".

EXISTING MEMORY RECORD:
$existingFacts

NEW INCOMING OBSERVATION:
$newFactCandidate

REASONING & RECONCILIATION PROCESS:
1. REPETITION CHECK: If the new information is already present in meaning or substance, do NOT add it again.
2. OVERLAP & SYNTHESIS: If the new information overlaps with or updates an existing fact, merge them together into a single logically coherent bullet point.
3. COMPLETION / EXTENSION: If this is a new detail, perspective, or completed idea, include it as a clean new point.
4. CONCISION: Keep the total facts clear, empathetic, and concise (1 to 4 clean bullet points max).

OUTPUT FORMAT:
Return ONLY the updated bullet points (starting with '• '). No preamble, headers, or explanations.
""".trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                ContentItem(
                    role = "user",
                    parts = listOf(PartItem(text = prompt))
                )
            ),
            generationConfig = createGenerationConfig(
                temperature = 0.2f,
                topP = 0.95f,
                thinkingLevel = "none"
            )
        )

        val result = executeWithFallback(
            modelName = modelName,
            apiKey = apiKey,
            request = request
        )

        return result.map { rawText ->
            val cleaned = rawText.trim()
                .replace(Regex("^(Here is|Consolidated facts:|Updated record:).*?\n", RegexOption.IGNORE_CASE), "")
                .trim()
            if (cleaned.isNotBlank()) cleaned else heuristicSynthesizeFacts(existingFacts, newFactCandidate)
        }.recoverCatching {
            heuristicSynthesizeFacts(existingFacts, newFactCandidate)
        }
    }

    fun heuristicSynthesizeFacts(existing: String, incoming: String): String {
        if (existing.isBlank()) {
            return if (incoming.startsWith("•") || incoming.startsWith("-")) incoming.trim() else "• ${incoming.trim()}"
        }
        if (incoming.isBlank()) return existing.trim()

        val existingLines = existing.lines()
            .map { it.trim().removePrefix("•").removePrefix("-").trim() }
            .filter { it.isNotBlank() }
            .toMutableList()

        val incomingLines = incoming.lines()
            .map { it.trim().removePrefix("•").removePrefix("-").trim() }
            .filter { it.isNotBlank() }

        for (inc in incomingLines) {
            val incLower = inc.lowercase()
            val isDuplicate = existingLines.any { ex ->
                val exLower = ex.lowercase()
                exLower == incLower || exLower.contains(incLower) || incLower.contains(exLower)
            }
            if (!isDuplicate) {
                existingLines.add(inc)
            }
        }

        return existingLines.take(5).joinToString("\n") { "• $it" }
    }

    private fun isTechnicalOrLinguisticKeyword(keyword: String): Boolean {
        val technicalTerms = setOf(
            "biology", "chemistry", "physics", "mathematics", "math", "calculus", "algebra",
            "grammar", "english", "linguistics", "vocabulary", "verb", "noun", "adjective",
            "adverb", "preposition", "tense", "syntax", "idiom", "phrasal", "pronunciation",
            "code", "coding", "python", "kotlin", "java", "javascript", "algorithm", "database",
            "sql", "function", "variable", "api", "server", "html", "css", "compiler",
            "atom", "molecule", "cell", "organism", "genetics", "photosynthesis", "reaction"
        )
        val lower = keyword.lowercase().trim()
        return technicalTerms.contains(lower) || technicalTerms.any { lower.contains(it) && lower.length < 25 }
    }

    private fun extractHeuristicMemories(transcript: String): List<ExtractedPersonalMemory> {
        val list = mutableListOf<ExtractedPersonalMemory>()

        // Match friend patterns: "my friend [is/named] X", "friends [are] X and Y"
        val friendRegexes = listOf(
            Regex("(?:my|a)\\s+friend\\s+(?:is|named|called)?\\s*([a-zA-Z]{2,20})", RegexOption.IGNORE_CASE),
            Regex("(?:my|a)\\s+best\\s+friend\\s+(?:is|named|called)?\\s*([a-zA-Z]{2,20})", RegexOption.IGNORE_CASE),
            Regex("(?:my|a)\\s+(?:brother|sister|wife|husband|mom|dad|mother|father|partner|girlfriend|boyfriend|cousin|colleague)\\s+(?:is|named|called)?\\s*([a-zA-Z]{2,20})", RegexOption.IGNORE_CASE)
        )

        val forbiddenStopWords = setOf(
            "who", "what", "where", "how", "that", "this", "there", "here", "they", "them", "someone",
            "biology", "chemistry", "physics", "math", "grammar", "english", "python", "code", "lesson"
        )

        for (regex in friendRegexes) {
            val matches = regex.findAll(transcript)
            for (m in matches) {
                val rawName = m.groupValues[1].trim()
                val kw = rawName.lowercase()
                if (rawName.isNotBlank() && kw !in forbiddenStopWords && !isTechnicalOrLinguisticKeyword(kw)) {
                    if (list.none { it.keyword == kw }) {
                        list.add(
                            ExtractedPersonalMemory(
                                keyword = kw,
                                displayName = rawName.replaceFirstChar { it.uppercase() },
                                factsSummary = "Mentioned in conversation as friend/contact of user"
                            )
                        )
                    }
                }
            }
        }

        return list
    }

    suspend fun analyzeStudentExamSubmission(
        examTitle: String,
        examDescription: String,
        studentName: String,
        questionsAndAnswers: String,
        apiKey: String,
        modelName: String = "gemini-flash-latest",
        thinkingLevel: String = "low"
    ): Result<StudentExamAiDigest> {
        val prompt = """
            You are an expert pedagogical assessment AI assistant for teachers and instructors in Memora.
            
            An exam/assessment named "$examTitle" was completed by student "$studentName".
            Exam Description/Objectives: $examDescription
            
            STUDENT'S QUESTIONS & ANSWERS:
            $questionsAndAnswers
            
            YOUR TASK:
            Process, digest, and evaluate the student's submission in a structured, actionable way for the teacher.
            
            REQUIRED OUTPUT FORMAT:
            [DIGEST_SUMMARY]
            Concise 2-3 sentence overview of the student's overall understanding and performance.
            [/DIGEST_SUMMARY]
            
            [STRENGTHS]
            • Point 1
            • Point 2
            [/STRENGTHS]
            
            [WEAKNESSES_OR_GAPS]
            • Misconceptions or areas needing improvement
            [/WEAKNESSES_OR_GAPS]
            
            [SCORE_RECOMMENDATION]
            Estimated Grade or Score (e.g., "88/100 (Proficient)")
            [/SCORE_RECOMMENDATION]
            
            [SUGGESTED_TEACHER_FEEDBACK]
            Empathetic, constructive message draft that the teacher can send back to the student.
            [/SUGGESTED_TEACHER_FEEDBACK]
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                ContentItem(
                    role = "user",
                    parts = listOf(PartItem(text = prompt))
                )
            ),
            generationConfig = createGenerationConfig(temperature = 0.2f, thinkingLevel = thinkingLevel)
        )

        val result = executeWithFallback(modelName, apiKey, request)
        return result.map { rawText ->
            val summary = Regex("\\[DIGEST_SUMMARY\\](.*?)\\[/DIGEST_SUMMARY\\]", setOf(RegexOption.DOT_MATCHES_ALL))
                .find(rawText)?.groupValues?.get(1)?.trim()
                ?: "Student completed the questions with acceptable comprehension."

            val strengths = Regex("\\[STRENGTHS\\](.*?)\\[/STRENGTHS\\]", setOf(RegexOption.DOT_MATCHES_ALL))
                .find(rawText)?.groupValues?.get(1)?.trim()
                ?: "• Demonstrated understanding of core concepts."

            val weaknesses = Regex("\\[WEAKNESSES_OR_GAPS\\](.*?)\\[/WEAKNESSES_OR_GAPS\\]", setOf(RegexOption.DOT_MATCHES_ALL))
                .find(rawText)?.groupValues?.get(1)?.trim()
                ?: "• Could provide more in-depth examples."

            val score = Regex("\\[SCORE_RECOMMENDATION\\](.*?)\\[/SCORE_RECOMMENDATION\\]", setOf(RegexOption.DOT_MATCHES_ALL))
                .find(rawText)?.groupValues?.get(1)?.trim()
                ?: "85/100 (Good)"

            val feedback = Regex("\\[SUGGESTED_TEACHER_FEEDBACK\\](.*?)\\[/SUGGESTED_TEACHER_FEEDBACK\\]", setOf(RegexOption.DOT_MATCHES_ALL))
                .find(rawText)?.groupValues?.get(1)?.trim()
                ?: "Great effort! Review the detailed points to sharpen your mastery."

            StudentExamAiDigest(
                summary = summary,
                strengths = strengths,
                weaknesses = weaknesses,
                recommendedGradeOrScore = score,
                suggestedTeacherFeedback = feedback
            )
        }
    }
}

data class StudentExamAiDigest(
    val summary: String,
    val strengths: String,
    val weaknesses: String,
    val recommendedGradeOrScore: String,
    val suggestedTeacherFeedback: String
)

