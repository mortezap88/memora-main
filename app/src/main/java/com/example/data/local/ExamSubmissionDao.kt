package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamSubmissionDao {
    @Query("SELECT * FROM exam_submissions ORDER BY submittedAt DESC")
    fun getAllSubmissionsFlow(): Flow<List<ExamSubmissionEntity>>

    @Query("SELECT * FROM exam_submissions WHERE studentUsername = :username ORDER BY submittedAt DESC")
    fun getSubmissionsForStudentFlow(username: String): Flow<List<ExamSubmissionEntity>>

    @Query("SELECT * FROM exam_submissions WHERE examId = :examId ORDER BY submittedAt DESC")
    fun getSubmissionsForExamFlow(examId: String): Flow<List<ExamSubmissionEntity>>

    @Query("SELECT * FROM exam_submissions WHERE id = :id LIMIT 1")
    suspend fun getSubmissionById(id: String): ExamSubmissionEntity?

    @Query("SELECT * FROM exam_submissions WHERE examId = :examId AND studentUsername = :username LIMIT 1")
    suspend fun getSubmissionForStudent(examId: String, username: String): ExamSubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: ExamSubmissionEntity)

    @Update
    suspend fun updateSubmission(submission: ExamSubmissionEntity)

    @Delete
    suspend fun deleteSubmission(submission: ExamSubmissionEntity)

    @Query("DELETE FROM exam_submissions WHERE id = :id")
    suspend fun deleteSubmissionById(id: String)
}
