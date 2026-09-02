package com.example.data.local

/**
 * Preconfigured authorized Mentor accounts.
 *
 * Mentors and Students sign in with their assigned username & password.
 * Mentors can provision new Student and Mentor accounts inside Settings.
 */
data class PresetInstructor(
    val username: String,
    val defaultPasswordPlain: String,
    val fullName: String,
    val avatarColorHex: String = "#8B5CF6"
)

object PresetInstructorRegistry {
    val PRESET_INSTRUCTORS = listOf(
        PresetInstructor(
            username = "mentor",
            defaultPasswordPlain = "mentor123",
            fullName = "Lead Mentor",
            avatarColorHex = "#8B5CF6"
        ),
        PresetInstructor(
            username = "instructor",
            defaultPasswordPlain = "teacher123",
            fullName = "Lead Mentor",
            avatarColorHex = "#8B5CF6"
        ),
        PresetInstructor(
            username = "teacher",
            defaultPasswordPlain = "teacher123",
            fullName = "Assistant Mentor",
            avatarColorHex = "#A855F7"
        )
    )

    fun isReservedInstructorUsername(username: String): Boolean {
        val clean = username.trim().lowercase()
        return PRESET_INSTRUCTORS.any { it.username.equals(clean, ignoreCase = true) }
    }
}

