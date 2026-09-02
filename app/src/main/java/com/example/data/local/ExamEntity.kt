package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetTopic: String = "General",
    val questionsJson: String, // JSON array of questions: [{"id":"1", "question":"...", "maxScore":10}]
    val assignedToUsername: String = "ALL", // "ALL" or specific student username
    val createdByTeacher: String,
    val createdAt: Long = System.currentTimeMillis(),
    val deadlineTimestamp: Long? = null
)
