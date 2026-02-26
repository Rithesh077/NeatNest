package com.example.neatnest.ui.signalcleaner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neatnest.data.model.ProcessedNotification
import com.example.neatnest.data.repository.NotificationRepository
import com.example.neatnest.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// drives the Signal Noise Cleaner screen
class SignalCleanerViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ProcessedNotification>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ProcessedNotification>>> = _uiState.asStateFlow()

    val notificationCount = notificationRepository.getCount()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            try {
                notificationRepository.getAllNotifications().collect { notifications ->
                    _uiState.value = UiState.Success(notifications)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load notifications")
            }
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteAll()
        }
    }
}

