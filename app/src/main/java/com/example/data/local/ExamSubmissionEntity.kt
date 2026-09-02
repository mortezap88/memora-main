package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "exam_submissions")
data class ExamSubmissionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val examId: String,
    val examTitle: String,
    val studentUsername: String,
    val studentDisplayName: String,
    val answersJson: String, // JSON array of [{questionId, questionText, answerText}]
    val submittedAt: Long = System.currentTimeMillis(),
    val aiDigestSummary: String? = null, // Gemini AI digested analysis of student understanding
    val aiStrengths: String? = null,
    val aiWeaknesses: String? = null,
    val aiRecommendedScore: String? = null,
    val teacherFeedback: String? = null,
    val status: String = "SUBMITTED" // "SUBMITTED", "AI_DIGESTED", "REVIEWED"
)
