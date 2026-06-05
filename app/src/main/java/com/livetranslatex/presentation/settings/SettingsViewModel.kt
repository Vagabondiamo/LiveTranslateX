package com.livetranslatex.presentation.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsState(
    val engineIndex: Int = 0,          // 0=MLKit, 1=DeepL, 2=OpenAI
    val sourceLang: String = "Giapponese",
    val targetLang: String = "Italiano",
    val deeplKey: String = "",
    val openaiKey: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun setEngine(index: Int)        { _state.update { it.copy(engineIndex = index) } }
    fun setSourceLang(lang: String)  { _state.update { it.copy(sourceLang = lang) } }
    fun setTargetLang(lang: String)  { _state.update { it.copy(targetLang = lang) } }
    fun setDeeplKey(key: String)     { _state.update { it.copy(deeplKey = key) } }
    fun setOpenaiKey(key: String)    { _state.update { it.copy(openaiKey = key) } }
}
