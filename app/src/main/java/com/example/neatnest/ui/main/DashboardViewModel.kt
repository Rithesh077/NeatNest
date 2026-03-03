package com.example.neatnest.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neatnest.data.local.NeatNestPreferences
import com.example.neatnest.RecentActivityItem
import com.example.neatnest.data.repository.FileRepository
import com.example.neatnest.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// drives the main dashboard screen
class DashboardViewModel(
    private val fileRepository: FileRepository,
    private val notificationRepository: NotificationRepository,
    private val prefs: NeatNestPreferences
) : ViewModel() {

    // asset and signal counts
    val fileCount = fileRepository.getFileCount()
    val notificationCount = notificationRepository.getCount()

    // dedup key to activity item, preserves insertion order
    private val activityMap = LinkedHashMap<String, RecentActivityItem>()

    private val _activityList = MutableStateFlow<List<RecentActivityItem>>(emptyList())
    val activityList: StateFlow<List<RecentActivityItem>> = _activityList.asStateFlow()

    init {
        pushActivity(
            "App Launched",
            "NeatNest dashboard opened",
            RecentActivityItem.ActivityType.APP_LAUNCHED
        )

        // observe new files and push onto list
        viewModelScope.launch {
            fileRepository.getAllFiles().collect { files ->
                files.take(5).forEach { file ->
                    pushActivity(
                        "File Organized",
                        file.fileName,
                        RecentActivityItem.ActivityType.FILE_ORGANIZED
                    )
                }
            }
        }

        // observe new notifications and push onto list
        viewModelScope.launch {
            notificationRepository.getAllNotifications().collect { notifications ->
                notifications.take(5).forEach { notif ->
                    pushActivity(
                        "Notification Captured",
                        notif.title ?: notif.packageName,
                        RecentActivityItem.ActivityType.NOTIFICATION_CAPTURED
                    )
                }
            }
        }
    }

    fun pushActivity(title: String, description: String, type: RecentActivityItem.ActivityType) {
        val key = "${type.name}::$description"
        // update the entry if it exists (refreshes timestamp), or create new
        activityMap[key] = RecentActivityItem(title = title, description = description, type = type)
        _activityList.value = activityMap.values.toList().reversed() // newest first
    }

    fun isOnboardingCompleted(): Boolean = prefs.onboardingCompleted
}


