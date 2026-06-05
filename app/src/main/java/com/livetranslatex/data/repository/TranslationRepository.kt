package com.livetranslatex.data.repository

import com.livetranslatex.data.database.TranslationHistory
import com.livetranslatex.data.database.TranslationHistoryDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationRepository @Inject constructor(
    private val dao: TranslationHistoryDao
) {
    fun getHistory(): Flow<List<TranslationHistory>> = dao.getAll()

    suspend fun save(
        originalText: String,
        translatedText: String,
        sourceLang: String = "auto",
        targetLang: String = "it"
    ) {
        dao.insert(
            TranslationHistory(
                originalText = originalText,
                translatedText = translatedText,
                sourceLang = sourceLang,
                targetLang = targetLang
            )
        )
    }

    suspend fun deleteById(id: Int) = dao.deleteById(id)
    suspend fun deleteAll() = dao.deleteAll()
}
