package com.example.neatnest.data.repository

import com.example.neatnest.data.local.NeatNestPreferences
import com.example.neatnest.data.local.ProcessedFileDao
import com.example.neatnest.data.local.TrackedFolderDao
import com.example.neatnest.data.model.ProcessedFile
import com.example.neatnest.data.model.TrackedFolder
import kotlinx.coroutines.flow.Flow

// single source of truth for file and folder operations
class FileRepository(
    private val fileDao: ProcessedFileDao,
    private val folderDao: TrackedFolderDao,
    val prefs: NeatNestPreferences
) {
    fun getAllFiles(): Flow<List<ProcessedFile>> = fileDao.getAllProcessedFiles()
    fun getFileCount(): Flow<Int> = fileDao.getProcessedFilesCount()
    suspend fun isProcessed(uri: String) = fileDao.isFileProcessed(uri)
    suspend fun insertFile(file: ProcessedFile) = fileDao.insertProcessedFile(file)

    // category (folder-card) queries
    fun getDistinctCategories(): Flow<List<String>> = fileDao.getDistinctCategories()
    fun getCategoryCount(category: String): Flow<Int> = fileDao.getCategoryCount(category)
    fun getFilesByCategory(category: String): Flow<List<ProcessedFile>> = fileDao.getFilesByCategory(category)

    fun getTrackedFolders(): Flow<List<TrackedFolder>> = folderDao.getAllTrackedFolders()
    suspend fun addFolder(folder: TrackedFolder) = folderDao.insertFolder(folder)
    suspend fun removeFolder(folder: TrackedFolder) = folderDao.removeFolder(folder)
}

