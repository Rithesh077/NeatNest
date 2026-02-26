package com.example.neatnest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.neatnest.data.model.ProcessedNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessedNotificationDao {
    @Query("SELECT * FROM processed_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<ProcessedNotification>>

    @Query("SELECT COUNT(*) FROM processed_notifications")
    fun getNotificationCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: ProcessedNotification)

    @Query("DELETE FROM processed_notifications")
    suspend fun deleteAllNotifications()
}

