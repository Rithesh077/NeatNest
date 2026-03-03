package com.example.neatnest.ui.assethub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neatnest.data.local.NeatNestPreferences
import com.example.neatnest.data.model.ProcessedFile
import com.example.neatnest.data.repository.FileRepository
import com.example.neatnest.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// drives the digital asset hub screen with folder-card and file-list views
class AssetHubViewModel(
    private val fileRepository: FileRepository,
    private val prefs: NeatNestPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ProcessedFile>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ProcessedFile>>> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val categories: StateFlow<List<Pair<String, Int>>> = _categories.asStateFlow()

    val fileCount = fileRepository.getFileCount()

    init {
        loadCategories()
    }

    // loads distinct categories with counts for folder cards
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                fileRepository.getDistinctCategories().collect { cats ->
                    val withCounts = cats.map { category ->
                        val count = fileRepository.getCategoryCount(category).first()
                        category to count
                    }
                    _categories.value = withCounts
                }
            } catch (e: Exception) {
                _categories.value = emptyList()
            }
        }
    }

    // loads files for a specific category (when user taps a folder card)
    fun loadFilesByCategory(category: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                fileRepository.getFilesByCategory(category).collect { files ->
                    _uiState.value = UiState.Success(files)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load files")
            }
        }
    }

    // loads all files (for flat view fallback)
    fun loadAllFiles() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                fileRepository.getAllFiles().collect { files ->
                    _uiState.value = UiState.Success(files)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load files")
            }
        }
    }

    fun getRootUri(): String? = prefs.rootUri

    fun setRootUri(uri: String) {
        prefs.rootUri = uri
    }
}
