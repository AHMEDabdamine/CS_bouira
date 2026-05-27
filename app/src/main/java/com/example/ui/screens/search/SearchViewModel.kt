package com.example.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FileItem
import com.example.data.repository.CsbouiraRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: CsbouiraRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<FileItem>> = _query
        .debounce(300)
        .flatMapLatest { q -> repository.searchFilesFlow(q) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
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
