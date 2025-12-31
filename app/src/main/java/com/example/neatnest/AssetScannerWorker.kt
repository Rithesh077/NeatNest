package com.example.neatnest

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetScannerWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("AssetScannerWorker", "Starting background scan...")

        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.processedFileDao()

        val sharedPrefs = applicationContext.getSharedPreferences("NeatNestPrefs", Context.MODE_PRIVATE)
        val rootUriString = sharedPrefs.getString("root_uri", null) ?: return@withContext Result.failure()
        val rootUri = Uri.parse(rootUriString)
        val rootDir = DocumentFile.fromTreeUri(applicationContext, rootUri) ?: return@withContext Result.failure()

        // Scan MediaStore for Images
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val cursor = applicationContext.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (it.moveToNext()) {
                val fileName = it.getString(nameColumn)
                val fileId = it.getLong(idColumn)
                val fileUriString = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileId.toString()).toString()

                // Check Database instead of FileSystem
                if (dao.isFileProcessed(fileUriString)) {
                    Log.d("AssetScannerWorker", "Skipping $fileName, already recorded in database.")
                    continue
                }

                val extension = fileName.substringAfterLast('.', "").lowercase()
                if (extension.isNotEmpty()) {
                    var targetSubDir = rootDir.findFile(extension)
                    if (targetSubDir == null || !targetSubDir.isDirectory) {
                        targetSubDir = rootDir.createDirectory(extension)
                    }

                    if (targetSubDir != null) {
                        Log.d("AssetScannerWorker", "Copying $fileName to /${extension}/")
                        val success = FileMover.copyFileToDirectory(applicationContext, Uri.parse(fileUriString), targetSubDir, fileName)
                        
                        if (success) {
                            // Persist to Database
                            val processedFile = ProcessedFile(
                                originalUri = fileUriString,
                                fileName = fileName,
                                targetPath = "${targetSubDir.uri}/${fileName}",
                                extension = extension
                            )
                            dao.insertProcessedFile(processedFile)
                        }
                    }
                }
            }
        }

        Result.success()
    }
}
