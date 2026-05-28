package com.example.ui.screens.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FileItem
import com.example.data.repository.CsbouiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewerViewModel(private val repository: CsbouiraRepository) : ViewModel() {
    private val _localPath = MutableStateFlow<String?>(null)
    val localPath: StateFlow<String?> = _localPath

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress

    private val _fileList = MutableStateFlow<List<FileItem>>(emptyList())
    val fileList: StateFlow<List<FileItem>> = _fileList.asStateFlow()

    private val _currentFileIndex = MutableStateFlow(0)
    val currentFileIndex: StateFlow<Int> = _currentFileIndex.asStateFlow()

    val currentFile: FileItem?
        get() = _fileList.value.getOrNull(_currentFileIndex.value)

    val hasNext: Boolean
        get() = _currentFileIndex.value < _fileList.value.size - 1

    val hasPrevious: Boolean
        get() = _currentFileIndex.value > 0

    fun setFiles(files: List<FileItem>, startIndex: Int) {
        _fileList.value = files
        _currentFileIndex.value = startIndex.coerceIn(0, (files.size - 1).coerceAtLeast(0))
    }

    fun goToNext(): FileItem? {
        if (!hasNext) return null
        _currentFileIndex.value++
        val file = currentFile
        file?.let { checkLocalDownload(it.url) }
        return file
    }

    fun goToPrevious(): FileItem? {
        if (!hasPrevious) return null
        _currentFileIndex.value--
        val file = currentFile
        file?.let { checkLocalDownload(it.url) }
        return file
    }

    fun goToIndex(index: Int): FileItem? {
        if (index !in _fileList.value.indices) return null
        _currentFileIndex.value = index
        val file = currentFile
        file?.let { checkLocalDownload(it.url) }
        return file
    }

    fun checkLocalDownload(url: String) {
        viewModelScope.launch {
            _localPath.value = repository.getDownloadedPath(url)
        }
    }

    fun downloadFile(name: String, url: String) {
        if (_isDownloading.value) return
        viewModelScope.launch {
            _isDownloading.value = true
            _downloadProgress.value = 0f
            val fileItem = FileItem(
                id = url, moduleId = "", name = name,
                type = "", url = url, downloadUrl = url
            )
            val path = repository.downloadFile(fileItem) { progress ->
                _downloadProgress.value = progress
            }
            _localPath.value = path
            _isDownloading.value = false
        }
    }
}
