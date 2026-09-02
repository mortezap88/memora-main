package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalMemoryDao {
    @Query("SELECT * FROM personal_memories WHERE ownerUsername = :username ORDER BY lastUpdated DESC")
    fun getAllMemoriesFlowForUser(username: String): Flow<List<PersonalMemoryEntity>>

    @Query("SELECT * FROM personal_memories WHERE ownerUsername = :username ORDER BY lastUpdated DESC")
    suspend fun getAllMemoriesForUser(username: String): List<PersonalMemoryEntity>

    @Query("SELECT * FROM personal_memories ORDER BY lastUpdated DESC")
    fun getAllMemoriesFlow(): Flow<List<PersonalMemoryEntity>>

    @Query("SELECT * FROM personal_memories ORDER BY lastUpdated DESC")
    suspend fun getAllMemories(): List<PersonalMemoryEntity>

    @Query("SELECT * FROM personal_memories WHERE keyword = :keyword AND ownerUsername = :username LIMIT 1")
    suspend fun getMemoryByKeywordForUser(keyword: String, username: String): PersonalMemoryEntity?

    @Query("SELECT COUNT(*) FROM personal_memories WHERE ownerUsername = :username")
    suspend fun getMemoriesCountForUser(username: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMemory(memory: PersonalMemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(memories: List<PersonalMemoryEntity>)

    @Query("DELETE FROM personal_memories WHERE keyword = :keyword AND ownerUsername = :username")
    suspend fun deleteMemoryByKeywordForUser(keyword: String, username: String)

    @Query("DELETE FROM personal_memories WHERE ownerUsername = :username")
    suspend fun clearAllMemoriesForUser(username: String)

    @Query("DELETE FROM personal_memories")
    suspend fun clearAllMemories()
}
