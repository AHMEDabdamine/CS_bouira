package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Year
import com.example.data.repository.CsbouiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeViewModel(private val repository: CsbouiraRepository) : ViewModel() {
    val years: StateFlow<List<Year>> = repository.getYearsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedYear = MutableStateFlow<String?>(null)
    val selectedYear: StateFlow<String?> = _selectedYear

    val currentDate: String = run {
        val date = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.FRENCH)
        date.format(formatter).replaceFirstChar { it.uppercase() }
    }

    fun onYearTap(yearName: String) {
        _selectedYear.value = when {
            yearName.isEmpty() -> null
            _selectedYear.value == yearName -> null
            else -> yearName
        }
    }

    fun onSemesterSelected() {
        _selectedYear.value = null
    }

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
