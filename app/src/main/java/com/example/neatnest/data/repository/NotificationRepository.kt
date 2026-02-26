package com.example.neatnest.data.repository

import com.example.neatnest.data.local.ProcessedNotificationDao
import com.example.neatnest.data.model.ProcessedNotification
import kotlinx.coroutines.flow.Flow

// single source of truth for notification data
class NotificationRepository(private val dao: ProcessedNotificationDao) {
    fun getAllNotifications(): Flow<List<ProcessedNotification>> = dao.getAllNotifications()
    fun getCount(): Flow<Int> = dao.getNotificationCount()
    suspend fun insert(notification: ProcessedNotification) = dao.insertNotification(notification)
    suspend fun deleteAll() = dao.deleteAllNotifications()
}

