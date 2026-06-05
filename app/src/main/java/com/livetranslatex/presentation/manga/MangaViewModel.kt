package com.livetranslatex.presentation.manga

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livetranslatex.data.ocr.OcrEngine
import com.livetranslatex.data.translator.TranslatorEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TranslationBlock(val original: String, val translated: String, val x: Int, val y: Int)
data class MangaPage(val bitmap: Bitmap, val isLoading: Boolean = false,
    val isTranslated: Boolean = false, val translations: List<TranslationBlock> = emptyList())
data class MangaUiState(val pages: List<MangaPage> = emptyList())

@HiltViewModel
class MangaViewModel @Inject constructor(
    private val ocrEngine: OcrEngine,
    private val translatorEngine: TranslatorEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(MangaUiState())
    val uiState: StateFlow<MangaUiState> = _uiState.asStateFlow()

    fun addPage(bitmap: Bitmap) {
        _uiState.update { it.copy(pages = it.pages + MangaPage(bitmap)) }
    }

    fun removePage(index: Int) {
        _uiState.update { it.copy(pages = it.pages.toMutableList().also { l -> l.removeAt(index) }) }
    }

    fun translatePage(index: Int) {
        updatePage(index) { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val page = _uiState.value.pages[index]
                val blocks = ocrEngine.recognizeBlocks(page.bitmap)
                val translated = blocks.mapNotNull { block ->
                    if (block.text.isBlank()) null
                    else TranslationBlock(block.text, translatorEngine.translate(block.text),
                        block.boundingBox?.left ?: 0, block.boundingBox?.top ?: 0)
                }
                updatePage(index) { it.copy(isLoading = false, isTranslated = true, translations = translated) }
            } catch (e: Exception) {
                updatePage(index) { it.copy(isLoading = false) }
            }
        }
    }

    fun translateAll() = _uiState.value.pages.indices.forEach { translatePage(it) }

    private fun updatePage(index: Int, transform: (MangaPage) -> MangaPage) {
        val pages = _uiState.value.pages.toMutableList()
        if (index < pages.size) { pages[index] = transform(pages[index]) }
        _uiState.update { it.copy(pages = pages) }
    }
}
