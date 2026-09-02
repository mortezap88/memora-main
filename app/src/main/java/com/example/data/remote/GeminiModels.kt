package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @param:Json(name = "contents") val contents: List<ContentItem>,
    @param:Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @param:Json(name = "systemInstruction") val systemInstruction: ContentItem? = null
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    @param:Json(name = "role") val role: String? = null,
    @param:Json(name = "parts") val parts: List<PartItem>
)

@JsonClass(generateAdapter = true)
data class PartItem(
    @param:Json(name = "text") val text: String? = null,
    @param:Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @param:Json(name = "mimeType") val mimeType: String,
    @param:Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @param:Json(name = "temperature") val temperature: Float? = null,
    @param:Json(name = "topP") val topP: Float? = null,
    @param:Json(name = "topK") val topK: Int? = null,
    @param:Json(name = "thinkingConfig") val thinkingConfig: ThinkingConfig? = null
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    @param:Json(name = "thinkingBudget") val thinkingBudget: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @param:Json(name = "candidates") val candidates: List<CandidateItem>? = null,
    @param:Json(name = "error") val error: ApiError? = null
)

@JsonClass(generateAdapter = true)
data class CandidateItem(
    @param:Json(name = "content") val content: ContentItem? = null,
    @param:Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @param:Json(name = "code") val code: Int? = null,
    @param:Json(name = "message") val message: String? = null,
    @param:Json(name = "status") val status: String? = null
)
