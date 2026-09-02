package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notepads")
data class NotepadEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Note",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0,
    val ownerUsername: String = ""
)
