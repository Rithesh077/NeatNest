package com.example.neatnest.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neatnest.data.local.NeatNestPreferences
import com.example.neatnest.data.model.TrackedFolder
import com.example.neatnest.data.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// drives the onboarding screen, folder selection and initial setup
class OnboardingViewModel(
    private val fileRepository: FileRepository,
    private val prefs: NeatNestPreferences
) : ViewModel() {

    val trackedFolders: Flow<List<TrackedFolder>> = fileRepository.getTrackedFolders()

    fun addFolder(folder: TrackedFolder) {
        viewModelScope.launch {
            fileRepository.addFolder(folder)
        }
    }

    fun removeFolder(folder: TrackedFolder) {
        viewModelScope.launch {
            fileRepository.removeFolder(folder)
        }
    }

    fun getRootUri(): String? = prefs.rootUri

    fun setRootUri(uri: String) {
        prefs.rootUri = uri
    }

    fun setMoveFilesEnabled(enabled: Boolean) {
        prefs.moveFilesEnabled = enabled
    }

    fun setCompleteScanMode(enabled: Boolean) {
        prefs.completeScanMode = enabled
    }

    fun completeOnboarding() {
        prefs.onboardingCompleted = true
    }

    fun isOnboardingCompleted(): Boolean = prefs.onboardingCompleted
}

