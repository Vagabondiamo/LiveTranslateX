package com.livetranslatex.data.ocr

import android.graphics.Bitmap
import android.graphics.Rect
import com.livetranslatex.domain.model.TextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface OcrEngine {
    suspend fun recognize(bitmap: Bitmap): List<TextBlock>
}

class MlKitOcrEngine @Inject constructor() : OcrEngine {

    private val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val japaneseRecognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    private val chineseRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    private val koreanRecognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    var sourceLanguage: String = "en"

    override suspend fun recognize(bitmap: Bitmap): List<TextBlock> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = when (sourceLanguage) {
            "ja" -> japaneseRecognizer
            "zh" -> chineseRecognizer
            "ko" -> koreanRecognizer
            else -> latinRecognizer
        }
        val result = recognizer.process(image).await()
        return result.textBlocks.map { block ->
            TextBlock(
                text = block.text,
                bounds = block.boundingBox ?: Rect()
            )
        }
    }
}

// Stub: PaddleOCR via JNI or ONNX Runtime (advanced - future implementation)
class PaddleOcrEngine @Inject constructor() : OcrEngine {
    override suspend fun recognize(bitmap: Bitmap): List<TextBlock> {
        // TODO: Integrate PaddleOCR via JNI or ONNX Runtime for manga/CJK
        // Placeholder returns empty list until native lib is integrated
        return emptyList()
    }
}
