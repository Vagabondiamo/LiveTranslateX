package com.livetranslatex.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "translation_history")
data class TranslationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val original: String,
    val translated: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val date: Long = System.currentTimeMillis()
)

@Dao
interface TranslationHistoryDao {
    @Query("SELECT * FROM translation_history ORDER BY date DESC")
    fun getAll(): Flow<List<TranslationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TranslationHistory)

    @Delete
    suspend fun delete(item: TranslationHistory)

    @Query("DELETE FROM translation_history")
    suspend fun deleteAll()
}

@Database(entities = [TranslationHistory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun translationHistoryDao(): TranslationHistoryDao
}
