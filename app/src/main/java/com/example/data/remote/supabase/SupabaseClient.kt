package com.example.data.remote.supabase

import com.example.data.local.ExamEntity
import com.example.data.local.ExamSubmissionEntity
import com.example.data.local.FlashcardEntity
import com.example.data.local.UserEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseClient(
    private val supabaseUrl: String = SupabaseConfig.DEFAULT_SUPABASE_URL,
    private val anonKey: String = SupabaseConfig.DEFAULT_ANON_KEY
) {
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private fun buildRequest(
        endpoint: String,
        method: String = "GET",
        body: String? = null,
        preferHeader: String? = null
    ): Request {
        val url = if (endpoint.startsWith("http")) endpoint else "${supabaseUrl.trimEnd('/')}/rest/v1/$endpoint"
        val builder = Request.Builder()
            .url(url)
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .addHeader("Content-Type", "application/json")

        if (preferHeader != null) {
            builder.addHeader("Prefer", preferHeader)
        }

        when (method.uppercase()) {
            "GET" -> builder.get()
            "POST" -> builder.post((body ?: "{}").toRequestBody(JSON_MEDIA_TYPE))
            "PATCH" -> builder.patch((body ?: "{}").toRequestBody(JSON_MEDIA_TYPE))
            "DELETE" -> builder.delete((body?.toRequestBody(JSON_MEDIA_TYPE)))
        }

        return builder.build()
    }

    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val req = buildRequest("users?select=username&limit=1")
            okHttpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful || resp.code == 200 || resp.code == 206) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Supabase connection returned HTTP ${resp.code}: ${resp.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // USERS SYNC
    // ==========================================
    suspend fun fetchAllUsers(): Result<List<UserEntity>> = withContext(Dispatchers.IO) {
        try {
            val req = buildRequest("users?select=*")
            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to fetch users: HTTP ${resp.code}"))
                }
                val bodyStr = resp.body?.string() ?: "[]"
                val jsonArray = JSONArray(bodyStr)
                val users = mutableListOf<UserEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val username = obj.optString("username", "")
                    if (username.isNotBlank()) {
                        users.add(
                            UserEntity(
                                username = username.lowercase(),
                                displayName = obj.optString("display_name", username),
                                passwordHash = obj.optString("password_hash", ""),
                                role = obj.optString("role", "STUDENT"),
                                avatarColorHex = obj.optString("avatar_color_hex", "#8B5CF6"),
                                createdAt = obj.optLong("created_at", System.currentTimeMillis()),
                                lastLoginAt = obj.optLong("last_login_at", 0L)
                            )
                        )
                    }
                }
                Result.success(users)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertUser(user: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("username", user.username.lowercase())
                put("display_name", user.displayName)
                put("password_hash", user.passwordHash)
                put("role", user.role)
                put("avatar_color_hex", user.avatarColorHex)
                put("created_at", user.createdAt)
                put("last_login_at", user.lastLoginAt)
            }
            val req = buildRequest(
                endpoint = "users",
                method = "POST",
                body = json.toString(),
                preferHeader = "resolution=merge-duplicates"
            )
            okHttpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful || resp.code in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Upsert user failed: HTTP ${resp.code} ${resp.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertUsers(users: List<UserEntity>): Result<Unit> = withContext(Dispatchers.IO) {
        if (users.isEmpty()) return@withContext Result.success(Unit)
        try {
            val arr = JSONArray()
            users.forEach { u ->
                arr.put(JSONObject().apply {
                    put("username", u.username.lowercase())
                    put("display_name", u.displayName)
                    put("password_hash", u.passwordHash)
                    put("role", u.role)
                    put("avatar_color_hex", u.avatarColorHex)
                    put("created_at", u.createdAt)
                    put("last_login_at", u.lastLoginAt)
                })
            }
            val req = buildRequest(
                endpoint = "users",
                method = "POST",
                body = arr.toString(),
                preferHeader = "resolution=merge-duplicates"
            )
            okHttpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful || resp.code in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Upsert users failed: HTTP ${resp.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(username: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val req = buildRequest(
                endpoint = "users?username=eq.${username.lowercase()}",
                method = "DELETE"
            )
            okHttpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful || resp.code in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Delete user failed: HTTP ${resp.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // EXAMS SYNC
    // ==========================================
    suspend fun fetchAllExams(): Result<List<ExamEntity>> = withContext(Dispatchers.IO) {
        try {
            val req = buildRequest("exams?select=*")
            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to fetch exams: HTTP ${resp.code}"))
                }
                val bodyStr = resp.body?.string() ?: "[]"
                val jsonArray = JSONArray(bodyStr)
                val exams = mutableListOf<ExamEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    exams.add(
                        ExamEntity(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            title = obj.optString("title", "Exam"),
                            description = obj.optString("description", ""),
                            targetTopic = obj.optString("target_topic", "General"),
                            questionsJson = obj.optString("questions_json", "[]"),
                            assignedToUsername = obj.optString("assigned_to_username", "ALL"),
                            createdByTeacher = obj.optString("created_by_username", obj.optString("created_by_teacher", "mentor")),
                            createdAt = obj.optLong("created_at", System.currentTimeMillis()),
                            deadlineTimestamp = if (obj.has("deadline_timestamp") && !obj.isNull("deadline_timestamp")) obj.optLong("deadline_timestamp") else null
                        )
                    )
                }
                Result.success(exams)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertExam(exam: ExamEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", exam.id)
                put("title", exam.title)
                put("created_by_username", exam.createdByTeacher)
                put("assigned_to_username", exam.assignedToUsername)
                put("questions_json", exam.questionsJson)
                put("created_at", exam.createdAt)
                if (exam.deadlineTimestamp != null) {
                    put("due_date", exam.deadlineTimestamp)
                }
            }
            val req = buildRequest(
                endpoint = "exams",
                method = "POST",
                body = json.toString(),
                preferHeader = "resolution=merge-duplicates"
            )
            okHttpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful || resp.code in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Upsert exam failed: HTTP ${resp.code} ${resp.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExam(examId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val req = buildRequest(
                endpoint = "exams?id=eq.$examId",
                method = "DELETE"
            )
            okHttpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful || resp.code in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Delete exam failed: HTTP ${resp.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // EXAM SUBMISSIONS SYNC (STUDENT -> MENTOR)
    // ==========================================
    suspend fun fetchAllExamSubmissions(): Result<List<ExamSubmissionEntity>> = withContext(Dispatchers.IO) {
        try {
            val req = buildRequest("exam_submissions?select=*")
            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to fetch submissions: HTTP ${resp.code}"))
                }
                val bodyStr = resp.body?.string() ?: "[]"
                val jsonArray = JSONArray(bodyStr)
                val submissions = mutableListOf<ExamSubmissionEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    submissions.add(
                        ExamSubmissionEntity(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            examId = obj.optString("exam_id", ""),
                            examTitle = obj.optString("exam_title", "Exam"),
                            studentUsername = obj.optString("student_username", ""),
                            studentDisplayName = obj.optString("student_name", obj.optString("student_display_name", "")),
                            answersJson = obj.optString("answers_json", "[]"),
                            submittedAt = obj.optLong("submitted_at", System.currentTimeMillis()),
                            aiDigestSummary = if (obj.has("ai_digest_summary") && !obj.isNull("ai_digest_summary")) obj.optString("ai_digest_summary") else null,
                            aiStrengths = if (obj.has("ai_strengths") && !obj.isNull("ai_strengths")) obj.optString("ai_strengths") else null,
                            aiWeaknesses = if (obj.has("ai_weaknesses") && !obj.isNull("ai_weaknesses")) obj.optString("ai_weaknesses") else null,
                            aiRecommendedScore = if (obj.has("score")) obj.optString("score") else null,
                            teacherFeedback = if (obj.has("mentor_feedback") && !obj.isNull("mentor_feedback")) obj.optString("mentor_feedback") else null,
                            status = if (obj.has("reviewed_at") && obj.optLong("reviewed_at") > 0) "REVIEWED" else "SUBMITTED"
                        )
                    )
                }
                Result.success(submissions)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertExamSubmission(sub: ExamSubmissionEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", sub.id)
                put("exam_id", sub.examId)
                put("student_username", sub.studentUsername)
                put("student_name", sub.studentDisplayName)
                put("answers_json", sub.answersJson)
                put("submitted_at", sub.submittedAt)
                put("score", sub.aiRecommendedScore?.toDoubleOrNull() ?: 0.0)
                put("mentor_feedback", sub.teacherFeedback ?: "")
                if (sub.status == "REVIEWED") {
                    put("reviewed_at", System.currentTimeMillis())
                }
            }
            val req = buildRequest(
                endpoint = "exam_submissions",
                method = "POST",
                body = json.toString(),
                preferHeader = "resolution=merge-duplicates"
            )
            okHttpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful || resp.code in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Upsert submission failed: HTTP ${resp.code} ${resp.body?.string()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // FLASHCARDS SYNC
    // ==========================================
    suspend fun fetchFlashcards(username: String? = null): Result<List<FlashcardEntity>> = withContext(Dispatchers.IO) {
        try {
            val endpoint = if (!username.isNullOrBlank()) {
                "flashcards?username=eq.${username.lowercase()}"
            } else {
                "flashcards?select=*"
            }
            val req = buildRequest(endpoint)
            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to fetch flashcards: HTTP ${resp.code}"))
                }
                val bodyStr = resp.body?.string() ?: "[]"
                val jsonArray = JSONArray(bodyStr)
                val cards = mutableListOf<FlashcardEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    cards.add(
                        FlashcardEntity(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            titleContent = obj.optString("source_term", obj.optString("title_content", "Card")),
                            descriptionContent = obj.optString("target_term", obj.optString("description_content", "")),
                            itemType = obj.optString("item_type", "TEXT"),
                            currentStageId = obj.optInt("current_stage", 0),
                            dueTimestamp = obj.optLong("next_review_at", System.currentTimeMillis()),
                            createdAt = obj.optLong("created_at", System.currentTimeMillis()),
                            isMastered = obj.optBoolean("is_mastered", false),
                            ownerUsername = obj.optString("username", username ?: "")
                        )
                    )
                }
                Result.success(cards)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertFlashcard(card: FlashcardEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", card.id)
                put("username", card.ownerUsername.lowercase())
                put("item_type", card.itemType)
                put("source_language", "EN")
                put("target_language", "FA")
                put("source_term", card.titleContent)
                put("target_term", card.descriptionContent)
                put("current_stage", card.currentStageId)
                put("next_review_at", card.dueTimestamp)
                put("created_at", card.createdAt)
                put("updated_at", System.currentTimeMillis())
                put("is_mastered", card.isMastered)
                put("total_reviews", card.totalReviewsCount)
            }
            val req = buildRequest(
                endpoint = "flashcards",
                method = "POST",
                body = json.toString(),
                preferHeader = "resolution=merge-duplicates"
            )
            okHttpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful || resp.code in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Upsert flashcard failed: HTTP ${resp.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
