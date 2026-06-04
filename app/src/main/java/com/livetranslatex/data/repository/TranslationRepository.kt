package com.livetranslatex.data.repository

import android.graphics.Bitmap
import com.livetranslatex.data.database.TranslationHistory
import com.livetranslatex.data.database.TranslationHistoryDao
import com.livetranslatex.data.ocr.OcrEngine
import com.livetranslatex.data.translator.TranslatorEngine
...
class TranslationRepository @Inject constructor(
    private val ocrEngine: OcrEngine,
    private val translator: TranslatorEngine,
    private val dao: TranslationHistoryDao
) {
    private var previousHash: String = ""

    suspend fun processImage(
        bitmap: Bitmap,
        source: String,
        target: String
    ): List<TranslationResult> {
        val blocks = ocrEngine.recognizeBlocks(bitmap)
        if (blocks.isEmpty()) return emptyList()

        val combinedText = blocks.joinToString("\n") { it.text }
        val hash = combinedText.md5()

        // Skip if same content — reduces CPU 70-90%
        if (hash == previousHash) return emptyList()
        previousHash = hash

        translator.sourceLang = source
        translator.targetLang = target

        return blocks.map { block ->
            val translated = translator.translate(block.text)
            dao.insert(
                TranslationHistory(
                    original = block.text,
                    translated = translated,
                    sourceLanguage = source,
                    targetLanguage = target
                )
            )
            TranslationResult(
                original = block.text,
                translated = translated,
                bounds = block.boundingBox ?: android.graphics.Rect()
            )
        }
    }

    fun getHistory(): Flow<List<TranslationHistory>> = dao.getAll()

    suspend fun clearHistory() = dao.deleteAll()
}
