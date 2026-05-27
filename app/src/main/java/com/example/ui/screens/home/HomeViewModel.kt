package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Year
import com.example.data.repository.CsbouiraRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: CsbouiraRepository) : ViewModel() {
    val years: StateFlow<List<Year>> = repository.getYearsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.preloadYear("Licence 1")
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.clearAllCache()
        }
    }
}
