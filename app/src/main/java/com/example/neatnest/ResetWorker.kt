package com.example.neatnest

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

// reverses the organization pipeline and restores files
class ResetWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("ResetWorker", "starting reset")

        // step 1: verify permissions
        if (!PermissionManager.canWriteToRoot(applicationContext)) {
            Log.e("ResetWorker", "cannot write to root, aborting")
            return@withContext Result.failure()
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val prefs = applicationContext.getSharedPreferences("NeatNestPrefs", Context.MODE_PRIVATE)

        // step 2: restore files to downloads
        try {
            val rootUriString = prefs.getString("root_uri", null)
            val rootDir = rootUriString?.let { DocumentFile.fromTreeUri(applicationContext, it.toUri()) }

            // scan the actual root directory tree for real files instead of relying on
            // potentially stale DB paths. this ensures we find files even after reclassification.
            if (rootDir != null && rootDir.exists()) {
                restoreAllFilesFromDirectory(rootDir)
            } else {
                // fallback: try DB paths in case root dir itself is inaccessible
                Log.w("ResetWorker", "root dir not accessible, trying DB paths as fallback")
                val allFiles = database.processedFileDao().getAllProcessedFiles().first()
                allFiles.forEach { file ->
                    try {
                        val sourceUri = file.targetPath.toUri()
                        val sourceFile = DocumentFile.fromSingleUri(applicationContext, sourceUri)
                        if (sourceFile?.exists() == true) {
                            restoreFileToDownloads(sourceFile, file.fileName)
                        }
                    } catch (e: Exception) {
                        Log.e("ResetWorker", "restore failed: ${file.fileName}", e)
                    }
                }
            }

            // delete root directory contents after restoration
            if (rootDir != null && rootDir.exists()) {
                deleteDirectoryContents(rootDir)
                // don't delete root itself since the user selected it as their root
            }
        } catch (e: Exception) {
            Log.e("ResetWorker", "critical error during restoration", e)
            return@withContext Result.failure()
        }

        // step 3: clear app state
        Log.d("ResetWorker", "wiping app state")
        WorkManager.getInstance(applicationContext).cancelAllWorkByTag("periodic_scan")
        database.clearAllTables()
        prefs.edit { clear() }

        Log.d("ResetWorker", "reset complete")
        return@withContext Result.success()
    }

    // recursively walks the root directory tree and restores every file it finds
    private fun restoreAllFilesFromDirectory(directory: DocumentFile) {
        directory.listFiles().forEach { file ->
            if (file.isDirectory) {
                restoreAllFilesFromDirectory(file)
            } else if (file.isFile) {
                val fileName = file.name ?: "restored_${System.currentTimeMillis()}"
                try {
                    restoreFileToDownloads(file, fileName)
                } catch (e: Exception) {
                    Log.e("ResetWorker", "restore failed: $fileName", e)
                }
            }
        }
    }

    private fun deleteDirectoryContents(directory: DocumentFile?) {
        directory?.listFiles()?.forEach { file ->
            if (file.isDirectory) deleteDirectoryContents(file)
            try { file.delete() }
            catch (e: Exception) { Log.e("ResetWorker", "delete failed: ${file.name}", e) }
        }
    }

    @Suppress("DEPRECATION")
    private fun restoreFileToDownloads(sourceFile: DocumentFile, originalFileName: String) {
        val resolver = applicationContext.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, originalFileName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/NeatNest_Restored")
            }
        }

        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val targetUri = resolver.insert(collectionUri, contentValues)
        if (targetUri != null) {
            try {
                val inputStream = resolver.openInputStream(sourceFile.uri)
                val outputStream = resolver.openOutputStream(targetUri)
                if (inputStream != null && outputStream != null) {
                    inputStream.use { inp -> outputStream.use { out -> inp.copyTo(out) } }
                    // only delete source after confirmed successful copy
                    sourceFile.delete()
                    Log.d("ResetWorker", "restored: $originalFileName")
                } else {
                    Log.e("ResetWorker", "stream null for: $originalFileName")
                    resolver.delete(targetUri, null, null)
                }
            } catch (e: Exception) {
                Log.e("ResetWorker", "copy failed: ${sourceFile.name}", e)
                resolver.delete(targetUri, null, null)
            }
        }
    }
}
