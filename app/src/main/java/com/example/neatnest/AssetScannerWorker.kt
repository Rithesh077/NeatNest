package com.example.neatnest

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class AssetScannerWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = applicationContext.getSharedPreferences("NeatNestPrefs", Context.MODE_PRIVATE)
        val rootUriString = prefs.getString("root_uri", null) ?: return@withContext Result.failure()
        val isCompleteScan = prefs.getBoolean("complete_scan_mode", false)
        val isMoveEnabled = prefs.getBoolean("move_files_enabled", false)
        
        val database = AppDatabase.getDatabase(applicationContext)
        val fileDao = database.processedFileDao()
        val folderDao = database.trackedFolderDao()
        
        val rootDir = DocumentFile.fromTreeUri(applicationContext, Uri.parse(rootUriString)) ?: return@withContext Result.failure()

        Log.d("AssetScannerWorker", "Starting Pass 1: Ingestion (Move/Copy to Root)")

        if (isCompleteScan) {
            // Foundational logic: Scan common media collections for "Complete" mode
            scanAndIngest(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileDao, rootDir, isMoveEnabled)
            scanAndIngest(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, fileDao, rootDir, isMoveEnabled)
        } else {
            // Scan only user-selected tracked folders
            val trackedFolders = folderDao.getAllTrackedFolders().first()
            for (folder in trackedFolders) {
                val sourceDir = DocumentFile.fromTreeUri(applicationContext, Uri.parse(folder.uri))
                sourceDir?.listFiles()?.forEach { file ->
                    if (file.isFile && !fileDao.isFileProcessed(file.uri.toString())) {
                        ingestFile(file, rootDir, fileDao, isMoveEnabled)
                    }
                }
            }
        }

        Log.d("AssetScannerWorker", "Starting Pass 2: Classification (Sorting inside Root)")
        classifyFilesInsideRoot(rootDir)

        Result.success()
    }

    private suspend fun scanAndIngest(collection: Uri, dao: ProcessedFileDao, rootDir: DocumentFile, isMove: Boolean) {
        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME)
        applicationContext.contentResolver.query(collection, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol)
                val uri = Uri.withAppendedPath(collection, id.toString())
                
                if (!dao.isFileProcessed(uri.toString())) {
                    val sourceFile = DocumentFile.fromSingleUri(applicationContext, uri)
                    sourceFile?.let { ingestFile(it, rootDir, dao, isMove) }
                }
            }
        }
    }

    private suspend fun ingestFile(sourceFile: DocumentFile, rootDir: DocumentFile, dao: ProcessedFileDao, isMove: Boolean) {
        val fileName = sourceFile.name ?: "unknown_${System.currentTimeMillis()}"
        
        // Check for duplicates in Root before moving
        if (rootDir.findFile(fileName) == null) {
            val success = FileMover.copyFileToDirectory(applicationContext, sourceFile.uri, rootDir, fileName)
            if (success) {
                if (isMove) {
                    try {
                        sourceFile.delete() 
                    } catch (e: Exception) {
                        Log.e("AssetScannerWorker", "Delete failed for ${sourceFile.name}, app will keep the copy.")
                    }
                }
                dao.insertProcessedFile(ProcessedFile(sourceFile.uri.toString(), fileName, rootDir.uri.toString(), ""))
            }
        }
    }

    private fun classifyFilesInsideRoot(rootDir: DocumentFile) {
        rootDir.listFiles().forEach { file ->
            if (file.isFile) {
                val fileName = file.name ?: return@forEach
                val extension = fileName.substringAfterLast('.', "").lowercase()
                if (extension.isNotEmpty()) {
                    var targetSubDir = rootDir.findFile(extension)
                    if (targetSubDir == null || !targetSubDir.isDirectory) {
                        targetSubDir = rootDir.createDirectory(extension)
                    }
                    
                    targetSubDir?.let { subDir ->
                        // Check if file already exists in extension folder to prevent duplicates
                        if (subDir.findFile(fileName) == null) {
                            FileMover.copyFileToDirectory(applicationContext, file.uri, subDir, fileName)
                            file.delete() // Cleanup from Root after sorting into subfolder
                        } else {
                            file.delete() // It's a duplicate, remove from Root
                        }
                    }
                }
            }
        }
    }
}
