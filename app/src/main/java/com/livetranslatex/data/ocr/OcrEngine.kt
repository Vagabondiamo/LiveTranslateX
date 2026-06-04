package com.livetranslatex.data.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class OcrBlock(
    val text: String,
    val boundingBox: android.graphics.Rect?
)

@Singleton
class OcrEngine @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /** Restituisce tutto il testo come stringa unica */
    suspend fun recognizeText(bitmap: Bitmap): String {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            ""
        }
    }

    /** Restituisce blocchi di testo con bounding box (per overlay camera) */
    suspend fun recognizeBlocks(bitmap: Bitmap): List<OcrBlock> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            result.textBlocks
                .filter { it.text.isNotBlank() }
                .map { block ->
                    OcrBlock(
                        text = block.text,
                        boundingBox = block.boundingBox
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Riconosce testo in una singola linea/area */
    suspend fun recognizeRegion(bitmap: Bitmap, region: android.graphics.Rect): String {
        return try {
            val cropped = Bitmap.createBitmap(
                bitmap,
                region.left.coerceAtLeast(0),
                region.top.coerceAtLeast(0),
                region.width().coerceAtMost(bitmap.width - region.left),
                region.height().coerceAtMost(bitmap.height - region.top)
            )
            recognizeText(cropped)
        } catch (e: Exception) {
            ""
        }
    }
}
