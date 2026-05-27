package com.example.ui.screens.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CsbouiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ViewerViewModel(private val repository: CsbouiraRepository) : ViewModel() {
    private val _localPath = MutableStateFlow<String?>(null)
    val localPath: StateFlow<String?> = _localPath

    fun checkLocalDownload(url: String) {
        viewModelScope.launch {
            _localPath.value = repository.getDownloadedPath(url)
        }
    }
}
