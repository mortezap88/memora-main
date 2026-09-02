package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        FlashcardEntity::class,
        NotepadEntity::class,
        PersonalMemoryEntity::class,
        ExamEntity::class,
        ExamSubmissionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun notepadDao(): NotepadDao
    abstract fun personalMemoryDao(): PersonalMemoryDao
    abstract fun examDao(): ExamDao
    abstract fun examSubmissionDao(): ExamSubmissionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "memora_flashcards.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
