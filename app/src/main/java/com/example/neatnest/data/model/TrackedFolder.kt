package com.example.neatnest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// folder selected by user for scanning
@Entity(tableName = "tracked_folders")
data class TrackedFolder(
    @PrimaryKey val uri: String,
    val folderName: String,
    val dateAdded: Long = System.currentTimeMillis()
)

