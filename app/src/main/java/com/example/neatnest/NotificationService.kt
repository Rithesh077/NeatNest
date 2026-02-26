package com.example.neatnest

import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

// captures notifications and classifies by importance
class NotificationService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        val title = sbn.notification?.extras?.getString("android.title")
        val packageName = sbn.packageName ?: "unknown"
        val priority = classifyPriority(sbn)

        Log.d("NotificationService", "captured: $title from $packageName ($priority)")

        val database = AppDatabase.getDatabase(applicationContext)
        val notification = ProcessedNotification(
            title = title,
            packageName = packageName,
            priority = priority
        )
        serviceScope.launch {
            try {
                database.processedNotificationDao().insertNotification(notification)
            } catch (e: Exception) {
                Log.e("NotificationService", "save failed", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    @Suppress("DEPRECATION")
    private fun classifyPriority(sbn: StatusBarNotification): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ranking = Ranking()
            val success = currentRanking.getRanking(sbn.key, ranking)
            if (success) {
                return when (ranking.importance) {
                    NotificationManager.IMPORTANCE_HIGH -> "Most Important"
                    NotificationManager.IMPORTANCE_DEFAULT -> "Normal"
                    NotificationManager.IMPORTANCE_LOW -> "Low"
                    NotificationManager.IMPORTANCE_MIN -> "Least Important"
                    NotificationManager.IMPORTANCE_NONE -> "Blocked"
                    else -> "Normal"
                }
            }
        }
        return when (sbn.notification.priority) {
            Notification.PRIORITY_HIGH, Notification.PRIORITY_MAX -> "Most Important"
            Notification.PRIORITY_DEFAULT -> "Normal"
            Notification.PRIORITY_LOW -> "Low"
            Notification.PRIORITY_MIN -> "Least Important"
            else -> "Normal"
        }
    }
}
