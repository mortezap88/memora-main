package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.MessageDigest

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String, // Normalized lowercase unique username
    val displayName: String,
    val passwordHash: String,
    val role: String = "STUDENT", // "STUDENT" or "INSTRUCTOR"
    val avatarColorHex: String = "#A855F7",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
