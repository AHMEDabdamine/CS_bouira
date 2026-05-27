package com.example.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.api.RetrofitClient
import com.example.data.local.AppDatabase
import com.example.data.repository.CsbouiraRepository
import com.example.ui.screens.bookmarks.BookmarksViewModel
import com.example.ui.screens.files.FilesViewModel
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.modules.ModulesViewModel
import com.example.ui.screens.search.SearchViewModel
import com.example.ui.screens.viewer.ViewerViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    private val repository: CsbouiraRepository by lazy {
        val db = AppDatabase.getDatabase(context.applicationContext)
        CsbouiraRepository(RetrofitClient.api, db.csbouiraDao(), context.applicationContext)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository) as T
            modelClass.isAssignableFrom(ModulesViewModel::class.java) -> ModulesViewModel(repository) as T
            modelClass.isAssignableFrom(FilesViewModel::class.java) -> FilesViewModel(repository) as T
            modelClass.isAssignableFrom(BookmarksViewModel::class.java) -> BookmarksViewModel(repository) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(repository) as T
            modelClass.isAssignableFrom(ViewerViewModel::class.java) -> ViewerViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
