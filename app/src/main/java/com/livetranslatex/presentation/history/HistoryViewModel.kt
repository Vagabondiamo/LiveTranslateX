package com.livetranslatex.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livetranslatex.data.repository.TranslationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TranslationRepository
) : ViewModel() {
    val history = repository.getHistory()

    fun clearHistory() = viewModelScope.launch {
        repository.clearHistory()
    }
}
