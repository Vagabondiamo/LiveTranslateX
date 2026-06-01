package com.livetranslatex.domain.model

import android.graphics.Rect

data class TextBlock(
    val text: String,
    val bounds: Rect
)

data class TranslationResult(
    val original: String,
    val translated: String,
    val bounds: Rect
)

enum class OcrEngineType {
    ML_KIT,
    PADDLE_OCR
}

enum class TranslatorType {
    OFFLINE_ML_KIT,
    DEEPL,
    OPENAI
}

data class AppSettings(
    val ocrEngine: OcrEngineType = OcrEngineType.ML_KIT,
    val translatorType: TranslatorType = TranslatorType.OFFLINE_ML_KIT,
    val sourceLanguage: String = "ja",
    val targetLanguage: String = "it",
    val captureIntervalMs: Long = 500L,
    val deeplApiKey: String = "",
    val openAiApiKey: String = ""
)
