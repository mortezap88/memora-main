package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE ownerUsername = :username ORDER BY createdAt DESC")
    fun getAllCardsForUser(username: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE ownerUsername = :username AND isMastered = 0 ORDER BY dueTimestamp ASC")
    fun getActiveCardsForUser(username: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE ownerUsername = :username AND isMastered = 1 ORDER BY masteredAt DESC")
    fun getMasteredCardsForUser(username: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE ownerUsername = :username AND isMastered = 0 AND dueTimestamp <= :now ORDER BY dueTimestamp ASC")
    fun getDueCardsForUser(username: String, now: Long = System.currentTimeMillis()): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE ownerUsername = :username AND isMastered = 0 AND currentStageId = :stageId ORDER BY dueTimestamp ASC")
    fun getCardsByStageForUser(username: String, stageId: Int): Flow<List<FlashcardEntity>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE ownerUsername = :username")
    suspend fun getCardsCountForUser(username: String): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE ownerUsername = :username AND isMastered = 1")
    suspend fun getMasteredCardsCountForUser(username: String): Int

    @Query("SELECT * FROM flashcards WHERE ownerUsername = :username ORDER BY createdAt DESC")
    suspend fun getAllCardsListForUser(username: String): List<FlashcardEntity>

    // Global / Teacher overview
    @Query("SELECT * FROM flashcards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE isMastered = 0 ORDER BY dueTimestamp ASC")
    fun getActiveCards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE isMastered = 1 ORDER BY masteredAt DESC")
    fun getMasteredCards(): Flow<List<FlashcardEntity>>

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun getCardsCount(): Int

    @Query("SELECT * FROM flashcards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: String): FlashcardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashcardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashcardEntity>)

    @Update
    suspend fun updateCard(card: FlashcardEntity)

    @Delete
    suspend fun deleteCard(card: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteCardById(id: String)

    @Query("DELETE FROM flashcards WHERE ownerUsername = :username")
    suspend fun clearAllCardsForUser(username: String)

    @Query("DELETE FROM flashcards")
    suspend fun clearAllCards()
}
