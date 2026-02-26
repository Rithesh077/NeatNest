package com.example.neatnest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.neatnest.data.model.ProcessedFile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessedFileDao {
    @Query("SELECT * FROM processed_files ORDER BY timestamp DESC")
    fun getAllProcessedFiles(): Flow<List<ProcessedFile>>

    @Query("SELECT COUNT(*) FROM processed_files")
    fun getProcessedFilesCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM processed_files WHERE originalUri = :uri)")
    suspend fun isFileProcessed(uri: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcessedFile(file: ProcessedFile)

    @Query("UPDATE processed_files SET targetPath = :newPath WHERE targetPath = :oldPath")
    suspend fun updateTargetPath(oldPath: String, newPath: String)

    @Query("SELECT * FROM processed_files WHERE targetPath = :path LIMIT 1")
    suspend fun getFileByTargetPath(path: String): ProcessedFile?
}

