package com.example.neatnest

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessedFileDao {
    @Query("SELECT * FROM processed_files")
    fun getAllProcessedFiles(): Flow<List<ProcessedFile>>

    @Query("SELECT COUNT(*) FROM processed_files")
    fun getProcessedFilesCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM processed_files WHERE originalUri = :uri)")
    suspend fun isFileProcessed(uri: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcessedFile(file: ProcessedFile)
}

@Dao
interface TrackedFolderDao {
    @Query("SELECT * FROM tracked_folders")
    fun getAllTrackedFolders(): Flow<List<TrackedFolder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: TrackedFolder)

    @Delete
    suspend fun removeFolder(folder: TrackedFolder)
}

@Dao
interface ProcessedNotificationDao {
    @Query("SELECT * FROM processed_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<ProcessedNotification>>

    @Query("SELECT COUNT(*) FROM processed_notifications")
    fun getNotificationCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: ProcessedNotification)
}
