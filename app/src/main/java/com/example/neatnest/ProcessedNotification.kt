package com.example.neatnest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_notifications")
data class ProcessedNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String?,
    val packageName: String,
    val priority: String, // "Least Important" to "Most Important"
    val timestamp: Long = System.currentTimeMillis()
)
