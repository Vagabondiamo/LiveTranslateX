package com.livetranslatex.presentation.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livetranslatex.data.translator.TranslationEngine
import com.livetranslatex.data.translator.TranslatorEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "settings")

data class SettingsUiState(
    val selectedEngine: Int = 0,
    val sourceLang: String = "JA",
    val targetLang: String = "IT",
    val deeplKey: String = "",
    val openAiKey: String = "",
    val darkOverlay: Boolean = true,
    val autoHide: Boolean = true,
    val saved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val translatorEngine: TranslatorEngine
) : ViewModel() {

    companion object {
        val KEY_ENGINE = intPreferencesKey("engine")
        val KEY_SOURCE = stringPreferencesKey("source_lang")
        val KEY_TARGET = stringPreferencesKey("target_lang")
        val KEY_DEEPL = stringPreferencesKey("deepl_key")
        val KEY_OPENAI = stringPreferencesKey("openai_key")
        val KEY_DARK_OVERLAY = booleanPreferencesKey("dark_overlay")
        val KEY_AUTO_HIDE = booleanPreferencesKey("auto_hide")
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            context.dataStore.data.first().let { prefs ->
                _uiState.update {
                    it.copy(
                        selectedEngine = prefs[KEY_ENGINE] ?: 0,
                        sourceLang = prefs[KEY_SOURCE] ?: "JA",
                        targetLang = prefs[KEY_TARGET] ?: "IT",
                        deeplKey = prefs[KEY_DEEPL] ?: "",
                        openAiKey = prefs[KEY_OPENAI] ?: "",
                        darkOverlay = prefs[KEY_DARK_OVERLAY] ?: true,
                        autoHide = prefs[KEY_AUTO_HIDE] ?: true
                    )
                }
                applyToEngine()
            }
        }
    }

    fun setEngine(idx: Int) = _uiState.update { it.copy(selectedEngine = idx, saved = false) }
    fun setSourceLang(lang: String) = _uiState.update { it.copy(sourceLang = lang, saved = false) }
    fun setTargetLang(lang: String) = _uiState.update { it.copy(targetLang = lang, saved = false) }
    fun setDeeplKey(key: String) = _uiState.update { it.copy(deeplKey = key, saved = false) }
    fun setOpenAiKey(key: String) = _uiState.update { it.copy(openAiKey = key, saved = false) }
    fun setDarkOverlay(v: Boolean) = _uiState.update { it.copy(darkOverlay = v, saved = false) }
    fun setAutoHide(v: Boolean) = _uiState.update { it.copy(autoHide = v, saved = false) }

    fun saveSettings() {
        viewModelScope.launch {
            val s = _uiState.value
            context.dataStore.edit { prefs ->
                prefs[KEY_ENGINE] = s.selectedEngine
                prefs[KEY_SOURCE] = s.sourceLang
                prefs[KEY_TARGET] = s.targetLang
                prefs[KEY_DEEPL] = s.deeplKey
                prefs[KEY_OPENAI] = s.openAiKey
                prefs[KEY_DARK_OVERLAY] = s.darkOverlay
                prefs[KEY_AUTO_HIDE] = s.autoHide
            }
            applyToEngine()
            _uiState.update { it.copy(saved = true) }
        }
    }

    private fun applyToEngine() {
        val s = _uiState.value
        translatorEngine.engine = when (s.selectedEngine) {
            1 -> TranslationEngine.DEEPL
            2 -> TranslationEngine.OPENAI
            else -> TranslationEngine.MLKIT
        }
        translatorEngine.sourceLang = s.sourceLang.lowercase()
        translatorEngine.targetLang = s.targetLang.lowercase()
        translatorEngine.deeplApiKey = s.deeplKey
        translatorEngine.openAiApiKey = s.openAiKey
    }
}
