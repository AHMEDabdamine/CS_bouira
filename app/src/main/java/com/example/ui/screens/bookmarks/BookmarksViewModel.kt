package com.example.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DownloadEntity
import com.example.data.model.FileItem
import com.example.data.repository.CsbouiraRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarksViewModel(private val repository: CsbouiraRepository) : ViewModel() {
    val bookmarkedFiles: StateFlow<List<FileItem>> = repository.getBookmarkedFiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val downloads: StateFlow<List<DownloadEntity>> = repository.getDownloadsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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
