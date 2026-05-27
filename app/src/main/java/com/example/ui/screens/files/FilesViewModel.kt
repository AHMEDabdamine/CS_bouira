package com.example.ui.screens.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FileItem
import com.example.data.repository.CsbouiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FilesViewModel(private val repository: CsbouiraRepository) : ViewModel() {
    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _moduleTitle = MutableStateFlow("")
    val moduleTitle: StateFlow<String> = _moduleTitle

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    val bookmarkedIds: StateFlow<Set<String>> = repository.getBookmarkedFiles()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val downloadedUrls: StateFlow<Set<String>> = repository.getDownloadsFlow()
        .map { list -> list.map { it.url }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun loadModuleFiles(yearName: String, semester: Int, moduleName: String) {
        _moduleTitle.value = moduleName
        _isLoading.value = true
        viewModelScope.launch {
            repository.getFilesFlow(yearName, semester, moduleName).collect {
                _files.value = it
                _isLoading.value = false
            }
        }
    }

    fun toggleBookmark(file: FileItem) {
        viewModelScope.launch {
            repository.toggleBookmark(file)
        }
    }

    fun downloadFile(file: FileItem) {
        viewModelScope.launch {
            repository.downloadFile(file)
        }
    }

    fun deleteDownload(url: String) {
        viewModelScope.launch {
            repository.deleteDownload(url)
        }
    }
}
