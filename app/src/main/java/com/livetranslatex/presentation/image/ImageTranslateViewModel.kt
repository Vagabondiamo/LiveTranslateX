package com.livetranslatex.presentation.image

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livetranslatex.data.ocr.OcrEngine
import com.livetranslatex.data.translator.TranslatorEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImageUiState(
    val bitmap: Bitmap? = null,
    val originalText: String = "",
    val translatedText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ImageTranslateViewModel @Inject constructor(
    private val ocrEngine: OcrEngine,
    private val translatorEngine: TranslatorEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageUiState())
    val uiState: StateFlow<ImageUiState> = _uiState.asStateFlow()

    fun processImage(bitmap: Bitmap) {
        _uiState.update { it.copy(bitmap = bitmap, isLoading = true, error = null,
            originalText = "", translatedText = "") }
        viewModelScope.launch {
            try {
                val original = ocrEngine.recognizeText(bitmap)
                if (original.isBlank()) {
                    _uiState.update { it.copy(isLoading = false, error = "Nessun testo trovato nell'immagine") }
                    return@launch
                }
                val translated = translatorEngine.translate(original)
                _uiState.update { it.copy(isLoading = false, originalText = original, translatedText = translated) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Errore") }
            }
        }
    }

    fun reset() { _uiState.value = ImageUiState() }

    fun copyToClipboard(context: Context, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("traduzione", text))
    }
}
