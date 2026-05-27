package com.example.ui.screens.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Module
import com.example.data.repository.CsbouiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ModulesViewModel(private val repository: CsbouiraRepository) : ViewModel() {
    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules

    private val _yearTitle = MutableStateFlow("")
    val yearTitle: StateFlow<String> = _yearTitle

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadYearModules(yearName: String, semester: Int) {
        _yearTitle.value = yearName
        _isLoading.value = true
        viewModelScope.launch {
            repository.getModulesFlow(yearName).collect { list ->
                _modules.value = list
                _isLoading.value = false
            }
        }
    }
}
