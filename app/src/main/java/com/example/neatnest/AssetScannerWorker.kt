package com.example.neatnest

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

// scans source folders and organizes files into root directory
class AssetScannerWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // root write permission is always required
        if (!PermissionManager.canWriteToRoot(applicationContext)) {
            Log.e("AssetScannerWorker", "cannot write to root, aborting")
            return@withContext Result.failure()
        }

        val prefs = applicationContext.getSharedPreferences("NeatNestPrefs", Context.MODE_PRIVATE)
        val rootUriString = prefs.getString("root_uri", null) ?: return@withContext Result.failure()
        val isCompleteScan = prefs.getBoolean("complete_scan_mode", false)
        val isMoveEnabled = prefs.getBoolean("move_files_enabled", false)

        val database = AppDatabase.getDatabase(applicationContext)
        val fileDao = database.processedFileDao()
        val folderDao = database.trackedFolderDao()
        val rootDir = DocumentFile.fromTreeUri(applicationContext, rootUriString.toUri())
            ?: return@withContext Result.failure()

        // pass 1: ingest files into root
        Log.d("AssetScannerWorker", "pass 1: ingestion")
        if (isCompleteScan && PermissionManager.hasAllFilesAccess() && PermissionManager.hasStorageAccess(applicationContext)) {
            scanAndIngest(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileDao, rootDir, isMoveEnabled)
            scanAndIngest(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, fileDao, rootDir, isMoveEnabled)
            scanAndIngest(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, fileDao, rootDir, isMoveEnabled)
        } else {
            val trackedFolders = folderDao.getAllTrackedFolders().first()
            for (folder in trackedFolders) {
                val sourceDir = DocumentFile.fromTreeUri(applicationContext, folder.uri.toUri())
                sourceDir?.listFiles()?.forEach { file ->
                    if (file.isFile && !fileDao.isFileProcessed(file.uri.toString())) {
                        ingestFile(file, rootDir, fileDao, isMoveEnabled)
                    }
                }
            }
        }

        // pass 2: classify files into subdirectories by type and extension
        Log.d("AssetScannerWorker", "pass 2: classification")
        classifyFilesInsideRoot(rootDir, fileDao)

        return@withContext Result.success()
    }

    private suspend fun scanAndIngest(collection: Uri, dao: ProcessedFileDao, rootDir: DocumentFile, isMove: Boolean) {
        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.MIME_TYPE)
        applicationContext.contentResolver.query(collection, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val mimeType = cursor.getString(mimeTypeCol)
                val uri = Uri.withAppendedPath(collection, id.toString())
                if (!dao.isFileProcessed(uri.toString())) {
                    val sourceFile = DocumentFile.fromSingleUri(applicationContext, uri)
                    sourceFile?.let { ingestFile(it, rootDir, dao, isMove, mimeType) }
                }
            }
        }
    }

    private suspend fun ingestFile(sourceFile: DocumentFile, rootDir: DocumentFile, dao: ProcessedFileDao, isMove: Boolean, mimeType: String? = null) {
        val fileName = sourceFile.name ?: "unknown_${System.currentTimeMillis()}"
        if (rootDir.findFile(fileName) == null) {
            val targetFile = FileMover.copyFileToDirectory(applicationContext, sourceFile.uri, rootDir, fileName, mimeType ?: sourceFile.type)
            if (targetFile != null) {
                if (isMove) {
                    try { sourceFile.delete() }
                    catch (e: Exception) { Log.e("AssetScannerWorker", "source delete failed (move mode): ${sourceFile.name}", e) }
                }
                val ext = fileName.substringAfterLast('.', "")
                dao.insertProcessedFile(ProcessedFile(sourceFile.uri.toString(), fileName, targetFile.uri.toString(), ext))
            }
        }
    }

    // classifies files inside root into subdirectories using digitalassethub for smart
    // categorization, then by extension. updates db paths after each successful move.
    private suspend fun classifyFilesInsideRoot(rootDir: DocumentFile, dao: ProcessedFileDao) {
        // get the engine name for tracking
        val engine = com.example.neatnest.ml.FileClassificationEngine.create(applicationContext)
        val engineName = engine.engineName()
        engine.close()

        rootDir.listFiles().forEach { file ->
            if (file.isFile) {
                val fileName = file.name ?: return@forEach
                val mimeType = file.type
                val ext = fileName.substringAfterLast('.', "").lowercase()
                if (ext.isEmpty()) return@forEach

                // use ml-powered classification engine (naive bayes or tflite)
                val assetType = DigitalAssetHub.classifyWithML(applicationContext, fileName, ext, mimeType)
                val categoryDir = when (assetType) {
                    DigitalAssetHub.AssetType.STUDY_MATERIAL -> "Study Material"
                    DigitalAssetHub.AssetType.WORK_DOCUMENT -> "Work Documents"
                    DigitalAssetHub.AssetType.MEDIA -> "Media"
                    DigitalAssetHub.AssetType.DIGITAL_CLUTTER -> "Clutter"
                    DigitalAssetHub.AssetType.UNCATEGORIZED -> ext
                }

                // find or create the target subdirectory
                var subDir = rootDir.findFile(categoryDir)
                if (subDir == null || !subDir.isDirectory) {
                    subDir = rootDir.createDirectory(categoryDir)
                }

                subDir?.let { dir ->
                    if (dir.findFile(fileName) == null) {
                        val copiedFile = FileMover.copyFileToDirectory(applicationContext, file.uri, dir, fileName, mimeType)
                        if (copiedFile != null) {
                            val rootCopyPath = file.uri.toString()
                            val newPath = copiedFile.uri.toString()
                            try {
                                // look up the existing record to get the real source URI
                                val existingRecord = dao.getFileByTargetPath(rootCopyPath)
                                val realOriginalUri = existingRecord?.originalUri ?: rootCopyPath

                                // update with real originalUri, new path, engine, and category
                                dao.insertProcessedFile(
                                    ProcessedFile(
                                        originalUri = realOriginalUri,
                                        fileName = fileName,
                                        targetPath = newPath,
                                        extension = ext,
                                        engineUsed = engineName,
                                        category = categoryDir
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("AssetScannerWorker", "db update failed: $fileName", e)
                            }
                            // only delete the original after confirmed successful copy and db update
                            file.delete()
                        } else {
                            Log.e("AssetScannerWorker", "copy to subdirectory failed, keeping original: $fileName")
                        }
                    } else {
                        // file already exists in subdirectory, safe to remove the root copy
                        file.delete()
                    }
                }
            }
        }
    }
}
