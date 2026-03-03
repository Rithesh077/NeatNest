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

    // folder-card queries
    @Query("SELECT DISTINCT category FROM processed_files WHERE category != '' ORDER BY category")
    fun getDistinctCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM processed_files WHERE category = :category")
    fun getCategoryCount(category: String): Flow<Int>

    @Query("SELECT * FROM processed_files WHERE category = :category ORDER BY timestamp DESC")
    fun getFilesByCategory(category: String): Flow<List<ProcessedFile>>

    // for re-sync: get all files as a snapshot
    @Query("SELECT * FROM processed_files")
    suspend fun getAllProcessedFilesSnapshot(): List<ProcessedFile>

    @Query("DELETE FROM processed_files")
    suspend fun deleteAll()
}


