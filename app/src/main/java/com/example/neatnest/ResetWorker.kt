package com.example.neatnest

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// restores classified files to original locations, empties root, resets onboarding
class ResetWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("ResetWorker", "starting re-sync")

        // step 1: verify permissions
        if (!PermissionManager.canWriteToRoot(applicationContext)) {
            Log.e("ResetWorker", "cannot write to root, aborting")
            return@withContext Result.failure()
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val prefs = applicationContext.getSharedPreferences("NeatNestPrefs", Context.MODE_PRIVATE)
        val resolver = applicationContext.contentResolver

        // step 2: restore files to original locations using DB records
        try {
            val allFiles = database.processedFileDao().getAllProcessedFilesSnapshot()
            Log.d("ResetWorker", "restoring ${allFiles.size} files to original locations")

            allFiles.forEach { file ->
                try {
                    val targetUri = file.targetPath.toUri()
                    val sourceDoc = DocumentFile.fromSingleUri(applicationContext, targetUri)

                    if (sourceDoc?.exists() != true) {
                        Log.w("ResetWorker", "classified file not found, skipping: ${file.fileName}")
                        return@forEach
                    }

                    val originalUri = file.originalUri.toUri()

                    // try to restore to original location
                    // for SAF tree URIs, we need to find/create the parent directory
                    val originalParent = try {
                        val parentUri = originalUri.buildUpon()
                            .path(originalUri.path?.substringBeforeLast('/'))
                            .build()
                        DocumentFile.fromTreeUri(applicationContext, parentUri)
                    } catch (e: Exception) {
                        null
                    }

                    if (originalParent?.exists() == true) {
                        // restore to original location
                        val restoredFile = FileMover.copyFileToDirectory(
                            applicationContext, sourceDoc.uri, originalParent, file.fileName, null
                        )
                        if (restoredFile != null) {
                            sourceDoc.delete()
                            Log.d("ResetWorker", "restored to original: ${file.fileName}")
                        } else {
                            Log.e("ResetWorker", "copy back failed: ${file.fileName}")
                        }
                    } else {
                        // original directory doesn't exist or not accessible
                        // create it via the root directory if possible
                        Log.w("ResetWorker", "original dir not accessible for: ${file.fileName}, restoring via root")
                        val rootUriString = prefs.getString("root_uri", null)
                        val rootDir = rootUriString?.let {
                            DocumentFile.fromTreeUri(applicationContext, it.toUri())
                        }
                        if (rootDir != null) {
                            var restoredDir = rootDir.findFile("Restored")
                            if (restoredDir == null || !restoredDir.isDirectory) {
                                restoredDir = rootDir.createDirectory("Restored")
                            }
                            restoredDir?.let { dir ->
                                val restoredFile = FileMover.copyFileToDirectory(
                                    applicationContext, sourceDoc.uri, dir, file.fileName, null
                                )
                                if (restoredFile != null) {
                                    sourceDoc.delete()
                                    Log.d("ResetWorker", "restored to Restored folder: ${file.fileName}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ResetWorker", "restore failed: ${file.fileName}", e)
                }
            }

            // step 3: empty root directory (delete all subdirectories and files, keep root itself)
            val rootUriString = prefs.getString("root_uri", null)
            val rootDir = rootUriString?.let { DocumentFile.fromTreeUri(applicationContext, it.toUri()) }
            if (rootDir != null && rootDir.exists()) {
                deleteDirectoryContents(rootDir)
                Log.d("ResetWorker", "root directory emptied")
            }
        } catch (e: Exception) {
            Log.e("ResetWorker", "critical error during restoration", e)
            return@withContext Result.failure()
        }

        // step 4: clear all app state and reset onboarding
        Log.d("ResetWorker", "wiping app state")
        WorkManager.getInstance(applicationContext).cancelAllWorkByTag("periodic_scan")
        database.clearAllTables()
        prefs.edit { clear() }

        Log.d("ResetWorker", "re-sync complete — onboarding reset")
        return@withContext Result.success()
    }

    private fun deleteDirectoryContents(directory: DocumentFile?) {
        directory?.listFiles()?.forEach { file ->
            if (file.isDirectory) deleteDirectoryContents(file)
            try { file.delete() }
            catch (e: Exception) { Log.e("ResetWorker", "delete failed: ${file.name}", e) }
        }
    }
}
