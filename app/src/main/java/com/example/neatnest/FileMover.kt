package com.example.neatnest

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.InputStream
import java.io.OutputStream

/**
 * Utility class to handle foundational file movement (Copy logic).
 */
object FileMover {

    /**
     * Copies a file from the source URI to a target directory in the Scoped Storage tree.
     */
    fun copyFileToDirectory(context: Context, sourceUri: Uri, targetDir: DocumentFile, fileName: String): Boolean {
        return try {
            val contentResolver = context.contentResolver
            
            // 1. Create the target file in the subdirectory
            val targetFile = targetDir.createFile("*/*", fileName) ?: return false

            // 2. Open streams
            val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
            val outputStream: OutputStream? = contentResolver.openOutputStream(targetFile.uri)

            if (inputStream != null && outputStream != null) {
                // 3. Copy the bits
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
