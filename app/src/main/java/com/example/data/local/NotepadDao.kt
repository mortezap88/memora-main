package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotepadDao {
    @Query("SELECT * FROM notepads WHERE ownerUsername = :username ORDER BY orderIndex ASC, createdAt ASC")
    fun getAllNotepadsForUser(username: String): Flow<List<NotepadEntity>>

    @Query("SELECT * FROM notepads ORDER BY orderIndex ASC, createdAt ASC")
    fun getAllNotepads(): Flow<List<NotepadEntity>>

    @Query("SELECT * FROM notepads WHERE id = :id LIMIT 1")
    suspend fun getNotepadById(id: String): NotepadEntity?

    @Query("SELECT COUNT(*) FROM notepads WHERE ownerUsername = :username")
    suspend fun getNotepadsCountForUser(username: String): Int

    @Query("SELECT COUNT(*) FROM notepads")
    suspend fun getNotepadsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotepad(notepad: NotepadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotepads(notepads: List<NotepadEntity>)

    @Update
    suspend fun updateNotepad(notepad: NotepadEntity)

    @Delete
    suspend fun deleteNotepad(notepad: NotepadEntity)

    @Query("SELECT * FROM notepads WHERE ownerUsername = :username ORDER BY orderIndex ASC, createdAt ASC")
    suspend fun getAllNotepadsListForUser(username: String): List<NotepadEntity>

    @Query("SELECT * FROM notepads ORDER BY orderIndex ASC, createdAt ASC")
    suspend fun getAllNotepadsList(): List<NotepadEntity>

    @Query("DELETE FROM notepads WHERE ownerUsername = :username")
    suspend fun clearAllNotepadsForUser(username: String)

    @Query("DELETE FROM notepads")
    suspend fun clearAllNotepads()

    @Query("DELETE FROM notepads WHERE id = :id")
    suspend fun deleteNotepadById(id: String)
}
