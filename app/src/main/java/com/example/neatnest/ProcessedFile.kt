package com.example.neatnest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_files")
data class ProcessedFile(
    @PrimaryKey val originalUri: String,
    val fileName: String,
    val targetPath: String,
    val extension: String,
    val timestamp: Long = System.currentTimeMillis()
)
