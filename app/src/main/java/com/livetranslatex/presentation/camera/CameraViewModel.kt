package com.livetranslatex.presentation.camera

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livetranslatex.data.ocr.OcrEngine
import com.livetranslatex.data.translator.TranslatorEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class TextBlock(
    val original: String,
    val translated: String,
    val rect: Rect
)

data class CameraUiState(
    val textBlocks: List<TextBlock> = emptyList(),
    val lastTranslation: String = "",
    val isProcessing: Boolean = false,
    val sourceLang: String = "JA",
    val targetLang: String = "IT"
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val ocrEngine: OcrEngine,
    private val translatorEngine: TranslatorEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val supportedLangs = listOf("IT", "EN", "JA", "ZH", "KO", "FR", "ES", "DE")
    private var processingJob: Job? = null

    // Throttle: processa max 1 frame ogni 500ms
    private var lastProcessTime = 0L

    fun processFrame(bitmap: Bitmap) {
        val now = System.currentTimeMillis()
        if (now - lastProcessTime < 500) return
        if (_uiState.value.isProcessing) return
        lastProcessTime = now

        processingJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isProcessing = true) }
            try {
                val blocks = ocrEngine.recognizeBlocks(bitmap)
                if (blocks.isEmpty()) {
                    _uiState.update { it.copy(isProcessing = false, textBlocks = emptyList()) }
                    return@launch
                }

                val translatedBlocks = blocks.map { block ->
                    val translated = translatorEngine.translate(block.text)
                    TextBlock(
                        original = block.text,
                        translated = translated,
                        rect = block.boundingBox ?: Rect()
                    )
                }

                val fullTranslation = translatedBlocks.joinToString("\n") { it.translated }

                _uiState.update {
                    it.copy(
                        textBlocks = translatedBlocks,
                        lastTranslation = fullTranslation,
                        isProcessing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    fun stopProcessing() {
        processingJob?.cancel()
    }

    fun toggleSourceLang() {
        val langs = supportedLangs
        val current = _uiState.value.sourceLang
        val next = langs[(langs.indexOf(current) + 1) % langs.size]
        _uiState.update { it.copy(sourceLang = next) }
    }

    fun toggleTargetLang() {
        val langs = supportedLangs
        val current = _uiState.value.targetLang
        val next = langs[(langs.indexOf(current) + 1) % langs.size]
        _uiState.update { it.copy(targetLang = next) }
    }
}
