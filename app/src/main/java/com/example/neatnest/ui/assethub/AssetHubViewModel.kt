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
import kotlinx.coroutines.launch

// drives the Digital Asset Hub screen
class AssetHubViewModel(
    private val fileRepository: FileRepository,
    private val prefs: NeatNestPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ProcessedFile>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ProcessedFile>>> = _uiState.asStateFlow()

    val fileCount = fileRepository.getFileCount()

    init {
        loadFiles()
    }

    private fun loadFiles() {
        viewModelScope.launch {
            try {
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

