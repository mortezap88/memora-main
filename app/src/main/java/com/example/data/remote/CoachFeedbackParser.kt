package com.example.data.remote

data class ParsedCoachResponse(
    val redCorrection: String? = null,
    val yellowSuggestion: String? = null,
    val targetWordFeedback: String? = null,
    val conversationalReply: String = "",
    val imageQuery: String? = null
) {
    val hasStructuredFeedback: Boolean
        get() = !redCorrection.isNullOrBlank() || !yellowSuggestion.isNullOrBlank() || !targetWordFeedback.isNullOrBlank()
}

object CoachFeedbackParser {
    private val TAG_REGEX = Regex("\\[/?(COACH_REPLY|RED_CORRECTION|YELLOW_SUGGESTION|TARGET_WORD_FEEDBACK|TARGET_KEYWORD|DISCOVERY_TARGET|IMAGE_QUERY)[^\\]]*\\]", RegexOption.IGNORE_CASE)

    fun extractImageQuery(text: String): String? {
        val match = Regex("\\[IMAGE_QUERY:\\s*([^\\]]+)\\]", RegexOption.IGNORE_CASE).find(text)
        return match?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    fun detectVisualIntent(userText: String, responseText: String? = null): String? {
        val text = userText.trim().replace(Regex("[?!.]+$"), "").trim()
        if (text.isBlank()) return null

        // 1. Direct explicit image request patterns
        val explicitPatterns = listOf(
            Regex("(?:show\\s+(?:me\\s+)?(?:an?\\s+)?(?:image|picture|photo|diagram|illustration)\\s+of\\s+)(.+)", RegexOption.IGNORE_CASE),
            Regex("(?:find\\s+(?:an?\\s+)?(?:image|picture|photo|diagram|illustration)\\s+(?:of|for)\\s+)(.+)", RegexOption.IGNORE_CASE),
            Regex("(?:what\\s+does\\s+(?:an?\\s+|the\\s+)?(.+?)\\s+look\\s+like)", RegexOption.IGNORE_CASE),
            Regex("(?:how\\s+does\\s+(?:an?\\s+|the\\s+)?(.+?)\\s+look)", RegexOption.IGNORE_CASE),
            Regex("(?:image|picture|photo|diagram|illustration|look\\s+like)\\s+(?:of\\s+)?(.+)", RegexOption.IGNORE_CASE)
        )
        for (pattern in explicitPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val candidate = cleanTerm(match.groupValues[1])
                if (candidate.isNotBlank() && candidate.length > 1) {
                    return candidate
                }
            }
        }

        // 2. Definitional / Subject inquiries (e.g. "what is a burlap flower", "what are succulents", "tell me about...", "explain...")
        val definitionalPatterns = listOf(
            Regex("^(?:what\\s+is|what's|what\\s+are|what\\s+was|what\\s+were)\\s+(?:an?\\s+|the\\s+)?(.+)", RegexOption.IGNORE_CASE),
            Regex("^(?:tell\\s+me\\s+about|explain|describe|define|meaning\\s+of)\\s+(?:an?\\s+|the\\s+)?(.+)", RegexOption.IGNORE_CASE),
            Regex("^(?:how\\s+to\\s+(?:make|draw|build|create|fold|grow))\\s+(?:an?\\s+|the\\s+)?(.+)", RegexOption.IGNORE_CASE)
        )
        for (pattern in definitionalPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val candidate = cleanTerm(match.groupValues[1])
                if (candidate.isNotBlank() && candidate.length > 1 && !isPureSmallTalk(candidate)) {
                    return candidate
                }
            }
        }

        // 3. Short standalone entity/noun queries (e.g. "cow", "burlap flower", "mitochondria", "cherry blossom")
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size in 1..4 && !isPureSmallTalk(text)) {
            val candidate = cleanTerm(text)
            if (candidate.isNotBlank() && candidate.length > 1) {
                return candidate
            }
        }

        // 4. Try extracting from AI response opening sentence if available (e.g., "A burlap flower is a rustic...")
        if (!responseText.isNullOrBlank()) {
            val stripped = stripAllTags(responseText).trim()
            val firstSentence = stripped.split(".", "\n", "?", "!").firstOrNull { it.isNotBlank() }?.trim()
            if (!firstSentence.isNullOrBlank()) {
                val entityMatch = Regex("^(?:An?|The)\\s+([a-zA-Z0-9\\s-]{2,40}?)\\s+(?:is|are|was|were|refers to|represents|means)\\s+", RegexOption.IGNORE_CASE).find(firstSentence)
                if (entityMatch != null) {
                    val candidate = cleanTerm(entityMatch.groupValues[1])
                    if (candidate.isNotBlank() && candidate.length > 1 && !isPureSmallTalk(candidate)) {
                        return candidate
                    }
                }
            }
        }

        return null
    }

    private fun cleanTerm(raw: String): String {
        return raw.trim()
            .replace(Regex("^(a|an|the)\\s+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[?.!,\"]+$"), "")
            .trim()
    }

    private fun isPureSmallTalk(term: String): Boolean {
        val t = term.lowercase().trim()
        val smallTalkList = setOf(
            "hi", "hello", "hey", "how are you", "what's up", "good morning", "good evening",
            "thank you", "thanks", "ok", "okay", "yes", "no", "bye", "goodbye", "cool",
            "great", "nice", "practice", "chat", "start", "help", "who are you", "what can you do"
        )
        return smallTalkList.contains(t) || t.startsWith("how are you") || t.startsWith("my name is")
    }

    fun stripAllTags(text: String): String {
        return text.replace(TAG_REGEX, "").trim()
    }

    private fun formatRedItems(text: String): String {
        val cleaned = stripAllTags(text)
            .replace(Regex("^(🔴\\s*)?(Grammar\\s+)?Correction(s)?(\\s*:)?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^Sorry,\\s*you should say this like:\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^You made a mistake:\\s*", RegexOption.IGNORE_CASE), "")
            .trim()

        val lines = cleaned.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return cleaned
        return lines.joinToString("\n") { line ->
            when {
                line.startsWith("🔴") -> line
                line.startsWith("❓") || line.startsWith("?") || line.startsWith("Why:", ignoreCase = true) || line.startsWith("Reason:", ignoreCase = true) -> {
                    val content = line.replace(Regex("^(❓|\\?|Why:|Reason:)\\s*", RegexOption.IGNORE_CASE), "").trim()
                    "❓ $content"
                }
                line.startsWith("- ") || line.startsWith("• ") || line.startsWith("* ") -> "🔴 " + line.substring(2).trim()
                Regex("^\\d+[.)]\\s*").containsMatchIn(line) -> line.replace(Regex("^\\d+[.)]\\s*"), "🔴 ")
                line.startsWith("(") && line.endsWith(")") -> "❓ " + line.removeSurrounding("(", ")").trim()
                line.contains("→") || line.contains("->") -> "🔴 $line"
                else -> line
            }
        }
    }

    private fun formatYellowItems(text: String): String {
        val cleaned = stripAllTags(text)
            .replace(Regex("^(🟡\\s*)?(Alternative\\s+)?Suggestion(s)?(\\s*:)?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^You could say:\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^Better phrasing:\\s*", RegexOption.IGNORE_CASE), "")
            .trim()

        val lines = cleaned.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return cleaned
        return lines.joinToString("\n") { line ->
            when {
                line.startsWith("🟡") -> line
                line.startsWith("❓") || line.startsWith("?") || line.startsWith("Why:", ignoreCase = true) || line.startsWith("Reason:", ignoreCase = true) -> {
                    val content = line.replace(Regex("^(❓|\\?|Why:|Reason:)\\s*", RegexOption.IGNORE_CASE), "").trim()
                    "❓ $content"
                }
                line.startsWith("- ") || line.startsWith("• ") || line.startsWith("* ") -> "🟡 " + line.substring(2).trim()
                Regex("^\\d+[.)]\\s*").containsMatchIn(line) -> line.replace(Regex("^\\d+[.)]\\s*"), "🟡 ")
                line.startsWith("(") && line.endsWith(")") -> "❓ " + line.removeSurrounding("(", ")").trim()
                line.contains("→") || line.contains("->") -> "🟡 $line"
                else -> line
            }
        }
    }

    fun parse(rawText: String): ParsedCoachResponse {
        val targetKeywordRegex = Regex("\\[TARGET_KEYWORD:[^\\]]+\\]", RegexOption.IGNORE_CASE)
        val sanitizedRaw = rawText.replace(targetKeywordRegex, "").trim()

        val redRegex = Regex("\\[RED_CORRECTION\\]([\\s\\S]*?)(?:\\[/RED_CORRECTION\\]|\$)", RegexOption.IGNORE_CASE)
        val yellowRegex = Regex("\\[YELLOW_SUGGESTION\\]([\\s\\S]*?)(?:\\[/YELLOW_SUGGESTION\\]|\$)", RegexOption.IGNORE_CASE)
        val targetRegex = Regex("\\[TARGET_WORD_FEEDBACK\\]([\\s\\S]*?)(?:\\[/TARGET_WORD_FEEDBACK\\]|\$)", RegexOption.IGNORE_CASE)
        val replyRegex = Regex("\\[COACH_REPLY\\]([\\s\\S]*?)(?:\\[/COACH_REPLY\\]|\$)", RegexOption.IGNORE_CASE)

        val hasRedTag = sanitizedRaw.contains("[RED_CORRECTION]", ignoreCase = true)
        val hasYellowTag = sanitizedRaw.contains("[YELLOW_SUGGESTION]", ignoreCase = true)
        val hasTargetTag = sanitizedRaw.contains("[TARGET_WORD_FEEDBACK]", ignoreCase = true)
        val hasReplyTag = sanitizedRaw.contains("[COACH_REPLY]", ignoreCase = true)

        if (hasRedTag || hasYellowTag || hasTargetTag || hasReplyTag) {
            val redMatch = redRegex.find(sanitizedRaw)?.groupValues?.get(1)?.trim()
            val yellowMatch = yellowRegex.find(sanitizedRaw)?.groupValues?.get(1)?.trim()
            val targetMatch = targetRegex.find(sanitizedRaw)?.groupValues?.get(1)?.trim()
            val replyMatch = replyRegex.find(sanitizedRaw)?.groupValues?.get(1)?.trim()

            val cleanRed = if (redMatch.isNullOrBlank() ||
                redMatch.equals("NONE", ignoreCase = true) ||
                redMatch.contains("No grammatical error", ignoreCase = true) ||
                redMatch.contains("No error", ignoreCase = true) ||
                redMatch.equals("N/A", ignoreCase = true)
            ) {
                null
            } else formatRedItems(redMatch)

            val cleanYellow = if (yellowMatch.isNullOrBlank() ||
                yellowMatch.equals("NONE", ignoreCase = true) ||
                yellowMatch.equals("N/A", ignoreCase = true)
            ) null else formatYellowItems(yellowMatch)

            val cleanTarget = if (targetMatch.isNullOrBlank() ||
                targetMatch.equals("NONE", ignoreCase = true) ||
                targetMatch.equals("N/A", ignoreCase = true)
            ) null else stripAllTags(targetMatch)

            val cleanReply = if (!replyMatch.isNullOrBlank()) {
                stripAllTags(replyMatch)
            } else {
                // If replyMatch was null or blank, strip out red, yellow, target blocks from sanitizedRaw
                var remainder = sanitizedRaw
                if (hasRedTag) remainder = remainder.replace(redRegex, "")
                if (hasYellowTag) remainder = remainder.replace(yellowRegex, "")
                if (hasTargetTag) remainder = remainder.replace(targetRegex, "")
                stripFeedbackArtifacts(stripAllTags(remainder))
            }

            return ParsedCoachResponse(
                redCorrection = cleanRed,
                yellowSuggestion = cleanYellow,
                targetWordFeedback = cleanTarget,
                conversationalReply = stripFeedbackArtifacts(cleanReply),
                imageQuery = extractImageQuery(rawText)
            )
        }

        // Markdown / Emoji fallback parser: Check for emoji/heading markers
        val lines = sanitizedRaw.lines()
        val redLines = mutableListOf<String>()
        val yellowLines = mutableListOf<String>()
        val targetLines = mutableListOf<String>()
        val replyLines = mutableListOf<String>()

        var currentSection = 0 // 0: None/Reply, 1: Red, 2: Yellow, 3: Target, 4: Reply

        for (line in lines) {
            val trimmed = line.trim()
            val lower = trimmed.lowercase()

            when {
                lower.startsWith("🔴") || lower.contains("red correction") || lower.contains("grammar correction") || lower.contains("errors:") -> {
                    currentSection = 1
                    if (trimmed.startsWith("🔴") || trimmed.contains("→") || trimmed.contains("->")) {
                        redLines.add(trimmed)
                    }
                }
                lower.startsWith("🟡") || lower.contains("yellow suggestion") || lower.contains("better phrasing") || lower.contains("suggestions:") -> {
                    currentSection = 2
                    if (trimmed.startsWith("🟡") || trimmed.contains("→") || trimmed.contains("->")) {
                        yellowLines.add(trimmed)
                    }
                }
                lower.startsWith("🎯") || lower.contains("target word") || lower.contains("target concept") || lower.contains("target expression") -> {
                    currentSection = 3
                    if (trimmed.isNotBlank()) {
                        targetLines.add(trimmed)
                    }
                }
                lower.startsWith("💬") || lower.contains("coach reply") || lower.contains("conversation:") || lower.contains("roleplay:") -> {
                    currentSection = 4
                    val content = trimmed.replace(Regex("^(💬|coach reply:|conversation:|roleplay:)\\s*", RegexOption.IGNORE_CASE), "").trim()
                    if (content.isNotBlank()) {
                        replyLines.add(content)
                    }
                }
                else -> {
                    val isFeedbackDetail = trimmed.startsWith("❓") || trimmed.startsWith("?") ||
                            lower.startsWith("why:") || lower.startsWith("reason:") ||
                            (trimmed.startsWith("(") && trimmed.endsWith(")"))

                    if (isFeedbackDetail) {
                        when (currentSection) {
                            1 -> redLines.add(trimmed)
                            2 -> yellowLines.add(trimmed)
                            3 -> targetLines.add(trimmed)
                            else -> replyLines.add(trimmed)
                        }
                    } else if (trimmed.isBlank()) {
                        // Empty line
                        if (currentSection == 4) replyLines.add("")
                    } else {
                        // Regular text paragraph
                        if (currentSection == 1 || currentSection == 2 || currentSection == 3) {
                            // If we hit a standard text paragraph after red/yellow bullets, it's the conversational reply!
                            currentSection = 4
                            replyLines.add(trimmed)
                        } else {
                            replyLines.add(trimmed)
                        }
                    }
                }
            }
        }

        val red = if (redLines.isNotEmpty()) formatRedItems(redLines.joinToString("\n").trim()) else null
        val yellow = if (yellowLines.isNotEmpty()) formatYellowItems(yellowLines.joinToString("\n").trim()) else null
        val target = if (targetLines.isNotEmpty()) stripAllTags(targetLines.joinToString("\n").trim()) else null
        val reply = if (replyLines.isNotEmpty()) {
            stripFeedbackArtifacts(stripAllTags(replyLines.joinToString("\n").trim()))
        } else {
            // If nothing in replyLines, check if raw has non-feedback text
            stripFeedbackArtifacts(stripAllTags(sanitizedRaw.trim()))
        }

        return ParsedCoachResponse(
            redCorrection = red,
            yellowSuggestion = yellow,
            targetWordFeedback = target,
            conversationalReply = reply
        )
    }

    /**
     * Ensures conversational reply is never polluted with red/yellow bullets or feedback tags
     */
    private fun stripFeedbackArtifacts(text: String): String {
        val lines = text.lines()
        val clean = lines.filterNot { line ->
            val t = line.trim()
            val l = t.lowercase()
            t.startsWith("🔴") || t.startsWith("🟡") ||
                (t.startsWith("❓") && (lines.any { it.trim().startsWith("🔴") || it.trim().startsWith("🟡") })) ||
                l.startsWith("grammar correction") || l.startsWith("alternative suggestion") ||
                l.startsWith("red correction") || l.startsWith("yellow suggestion")
        }
        return clean.joinToString("\n").trim()
    }
}
