package com.example.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FileItem
import com.example.data.repository.CsbouiraRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarksViewModel(private val repository: CsbouiraRepository) : ViewModel() {
    val bookmarkedFiles: StateFlow<List<FileItem>> = repository.getBookmarkedFiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val downloadedUrls: StateFlow<Set<String>> = repository.getDownloadsFlow()
        .map { list -> list.map { it.url }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

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
