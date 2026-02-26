package com.example.neatnest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// captured notification classified by priority
@Entity(tableName = "processed_notifications")
data class ProcessedNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String?,
    val packageName: String,
    val priority: String,
    val timestamp: Long = System.currentTimeMillis()
)

