package com.example.neatnest

// recent activity entry displayed in dashboard stack
data class RecentActivityItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val type: ActivityType,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class ActivityType {
        FILE_SCANNED,
        FILE_ORGANIZED,
        NOTIFICATION_CAPTURED,
        SYNC_COMPLETED,
        RESET_PERFORMED,
        APP_LAUNCHED
    }
}
