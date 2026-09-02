package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppStats(
    val streak: Int = 0,
    val lastReviewDate: String = "",
    val totalReviews: Int = 0
)

data class MemoraSettings(
    val fontSizeScale: Float = 1.0f, // 0.85f (Small), 1.0f (Medium), 1.15f (Large), 1.3f (Extra Large)
    val geminiModel: String = "gemini-flash-latest",
    val thinkingLevel: String = "low", // "none", "low", "medium", "high"
    val geminiApiKey: String = "",
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val ttsSpeechRate: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val personalizationEnabled: Boolean = true,
    val userName: String = "",
    val hasPromptedUserName: Boolean = false
)

data class AuthSession(
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val displayName: String = "",
    val role: String = "STUDENT" // "STUDENT" or "INSTRUCTOR"
)

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("memora_preferences", Context.MODE_PRIVATE)

    private val _authSessionFlow = MutableStateFlow(loadAuthSession())
    val authSessionFlow: StateFlow<AuthSession> = _authSessionFlow.asStateFlow()

    private val _statsFlow = MutableStateFlow(loadStats())
    val statsFlow: StateFlow<AppStats> = _statsFlow.asStateFlow()

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<MemoraSettings> = _settingsFlow.asStateFlow()

    private fun loadAuthSession(): AuthSession {
        val username = prefs.getString("auth_username", "") ?: ""
        val displayName = prefs.getString("auth_display_name", "") ?: ""
        val role = prefs.getString("auth_role", "STUDENT") ?: "STUDENT"
        return AuthSession(
            isLoggedIn = username.isNotBlank(),
            username = username,
            displayName = displayName.ifBlank { username },
            role = role
        )
    }

    fun saveAuthSession(username: String, displayName: String, role: String) {
        prefs.edit {
            putString("auth_username", username.trim().lowercase())
            putString("auth_display_name", displayName.trim().ifBlank { username })
            putString("auth_role", role)
        }
        _authSessionFlow.value = AuthSession(
            isLoggedIn = true,
            username = username.trim().lowercase(),
            displayName = displayName.trim().ifBlank { username },
            role = role
        )
        refreshUserStats()
    }

    fun clearAuthSession() {
        prefs.edit {
            remove("auth_username")
            remove("auth_display_name")
            remove("auth_role")
        }
        _authSessionFlow.value = AuthSession(isLoggedIn = false)
        refreshUserStats()
    }

    fun refreshUserStats() {
        _statsFlow.value = loadStats()
    }

    private fun loadStats(): AppStats {
        val user = _authSessionFlow.value.username.ifBlank { "global" }
        return AppStats(
            streak = prefs.getInt("streak_$user", 1),
            lastReviewDate = prefs.getString("last_review_date_$user", "") ?: "",
            totalReviews = prefs.getInt("total_reviews_$user", 0)
        )
    }

    private fun loadSettings(): MemoraSettings {
        return MemoraSettings(
            fontSizeScale = prefs.getFloat("font_size_scale", 1.0f),
            geminiModel = prefs.getString("gemini_model", "gemini-flash-latest") ?: "gemini-flash-latest",
            thinkingLevel = prefs.getString("thinking_level", "low") ?: "low",
            geminiApiKey = prefs.getString("gemini_api_key", "") ?: "",
            themeMode = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM",
            ttsSpeechRate = prefs.getFloat("tts_speech_rate", 1.0f),
            ttsPitch = prefs.getFloat("tts_pitch", 1.0f),
            personalizationEnabled = prefs.getBoolean("personalization_enabled", true),
            userName = prefs.getString("user_name", "") ?: "",
            hasPromptedUserName = prefs.getBoolean("has_prompted_user_name", false)
        )
    }

    fun recordReviewCompleted() {
        val user = _authSessionFlow.value.username.ifBlank { "global" }
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentStats = _statsFlow.value
        val lastDate = currentStats.lastReviewDate
        val newStreak = when {
            lastDate == today -> currentStats.streak
            isYesterday(lastDate) -> currentStats.streak + 1
            else -> 1
        }
        val newTotal = currentStats.totalReviews + 1

        prefs.edit {
            putInt("streak_$user", newStreak)
            putString("last_review_date_$user", today)
            putInt("total_reviews_$user", newTotal)
        }

        _statsFlow.value = AppStats(newStreak, today, newTotal)
    }

    private fun isYesterday(dateStr: String): Boolean {
        if (dateStr.isEmpty()) return false
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return false
            val diffMillis = System.currentTimeMillis() - date.time
            val daysDiff = diffMillis / (1000 * 60 * 60 * 24)
            return daysDiff in 1..2
        } catch (_: Exception) {
            return false
        }
    }

    fun updateSettings(newSettings: MemoraSettings) {
        prefs.edit {
            putFloat("font_size_scale", newSettings.fontSizeScale)
            putString("gemini_model", newSettings.geminiModel)
            putString("thinking_level", newSettings.thinkingLevel)
            putString("gemini_api_key", newSettings.geminiApiKey)
            putString("theme_mode", newSettings.themeMode)
            putFloat("tts_speech_rate", newSettings.ttsSpeechRate)
            putFloat("tts_pitch", newSettings.ttsPitch)
            putBoolean("personalization_enabled", newSettings.personalizationEnabled)
            putString("user_name", newSettings.userName)
            putBoolean("has_prompted_user_name", newSettings.hasPromptedUserName)
        }

        _settingsFlow.value = newSettings
    }

    fun resetStats() {
        val user = _authSessionFlow.value.username.ifBlank { "global" }
        prefs.edit {
            putInt("streak_$user", 0)
            putString("last_review_date_$user", "")
            putInt("total_reviews_$user", 0)
        }
        _statsFlow.value = AppStats(0, "", 0)
    }

    fun restoreStats(streak: Int, lastReviewDate: String, totalReviews: Int) {
        val user = _authSessionFlow.value.username.ifBlank { "global" }
        prefs.edit {
            putInt("streak_$user", streak)
            putString("last_review_date_$user", lastReviewDate)
            putInt("total_reviews_$user", totalReviews)
        }
        _statsFlow.value = AppStats(streak, lastReviewDate, totalReviews)
    }

    fun hasSeededInitialData(): Boolean {
        return prefs.getBoolean("has_seeded_initial_data", false)
    }

    fun setHasSeededInitialData(seeded: Boolean) {
        prefs.edit {
            putBoolean("has_seeded_initial_data", seeded)
        }
    }

    fun saveFreeChatJson(username: String, json: String) {
        val key = "persisted_free_chat_messages_${username.ifBlank { "default" }}"
        prefs.edit {
            putString(key, json)
        }
    }

    fun getFreeChatJson(username: String): String? {
        val key = "persisted_free_chat_messages_${username.ifBlank { "default" }}"
        return prefs.getString(key, null)
    }

    fun clearFreeChatJson(username: String) {
        val key = "persisted_free_chat_messages_${username.ifBlank { "default" }}"
        prefs.edit {
            remove(key)
        }
    }
}
