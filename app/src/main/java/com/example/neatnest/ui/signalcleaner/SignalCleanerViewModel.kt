package com.example.neatnest.ui.signalcleaner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neatnest.data.local.PackageCount
import com.example.neatnest.data.model.ProcessedNotification
import com.example.neatnest.data.repository.NotificationRepository
import com.example.neatnest.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// drives the signal noise cleaner screen with notification list + analytics
class SignalCleanerViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ProcessedNotification>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ProcessedNotification>>> = _uiState.asStateFlow()

    val notificationCount = notificationRepository.getCount()

    // analytics state
    private val _highCount = MutableStateFlow(0)
    val highCount: StateFlow<Int> = _highCount.asStateFlow()

    private val _normalCount = MutableStateFlow(0)
    val normalCount: StateFlow<Int> = _normalCount.asStateFlow()

    private val _lowCount = MutableStateFlow(0)
    val lowCount: StateFlow<Int> = _lowCount.asStateFlow()

    private val _topPackages = MutableStateFlow<List<PackageCount>>(emptyList())
    val topPackages: StateFlow<List<PackageCount>> = _topPackages.asStateFlow()

    init {
        loadNotifications()
        loadAnalytics()
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

    private fun loadAnalytics() {
        viewModelScope.launch {
            notificationRepository.getCountByPriority("HIGH").collect { _highCount.value = it }
        }
        viewModelScope.launch {
            notificationRepository.getCountByPriority("NORMAL").collect { _normalCount.value = it }
        }
        viewModelScope.launch {
            notificationRepository.getCountByPriority("LOW").collect { _lowCount.value = it }
        }
        viewModelScope.launch {
            notificationRepository.getTopPackages().collect { _topPackages.value = it }
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteAll()
        }
    }
}
