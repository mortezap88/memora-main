package com.example.data.local

import androidx.room.Entity

@Entity(
    tableName = "personal_memories",
    primaryKeys = ["keyword", "ownerUsername"]
)
data class PersonalMemoryEntity(
    val keyword: String, // Normalized lowercase keyword / entity name e.g. "morteza", "ali", "pizza"
    val ownerUsername: String = "",
    val displayName: String, // Capitalized / original entity name e.g. "Morteza", "Ali"
    val factsSummary: String, // Extracted bullet points / knowledge notes about this entity
    val lastUpdated: Long = System.currentTimeMillis()
)
