package com.livetranslatex.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livetranslatex.data.database.TranslationHistory
import com.livetranslatex.data.repository.TranslationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TranslationRepository
) : ViewModel() {

    val history: StateFlow<List<TranslationHistory>> =
        repository.getHistory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(item: TranslationHistory) {
        viewModelScope.launch { repository.delete(item) }
    }

    fun deleteAll() {
        viewModelScope.launch { repository.deleteAll() }
    }
}
