package com.example.neatnest

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile

// copies a file to target directory using content resolver
object FileMover {

    fun copyFileToDirectory(context: Context, sourceUri: Uri, targetDir: DocumentFile, fileName: String, mimeType: String?): DocumentFile? {
        return try {
            val resolver = context.contentResolver
            val finalMime = mimeType
                ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substringAfterLast('.'))
                ?: "*/*"

            val targetFile = targetDir.createFile(finalMime, fileName) ?: return null

            val input = resolver.openInputStream(sourceUri)
            val output = resolver.openOutputStream(targetFile.uri)

            if (input != null && output != null) {
                input.use { i -> output.use { o -> i.copyTo(o) } }
                targetFile
            } else {
                targetFile.delete()
                null
            }
        } catch (e: Exception) {
            Log.e("FileMover", "copy failed: $fileName", e)
            null
        }
    }
}
