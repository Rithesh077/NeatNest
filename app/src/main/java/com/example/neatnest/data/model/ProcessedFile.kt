package com.example.neatnest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// file that has been scanned and organized
@Entity(tableName = "processed_files")
data class ProcessedFile(
    @PrimaryKey val originalUri: String,
    val fileName: String,
    val targetPath: String,
    val extension: String,
    val timestamp: Long = System.currentTimeMillis(),
    val engineUsed: String = "legacy",
    val category: String = ""
)

