package com.example.neatnest

import android.content.Context
import com.example.neatnest.ml.FileClassificationEngine

// classifies files into categories using keyword matching or ML engines
object DigitalAssetHub {

    enum class AssetType {
        STUDY_MATERIAL,
        WORK_DOCUMENT,
        MEDIA,
        DIGITAL_CLUTTER,
        UNCATEGORIZED
    }

    // legacy keyword-only classification
    fun classifyByName(fileName: String): AssetType = when {
        STUDY_KEYWORDS.any { fileName.contains(it, ignoreCase = true) } -> AssetType.STUDY_MATERIAL
        fileName.endsWith(".pdf", ignoreCase = true) -> AssetType.STUDY_MATERIAL
        CLUTTER_KEYWORDS.any { fileName.contains(it, ignoreCase = true) } -> AssetType.DIGITAL_CLUTTER
        else -> AssetType.UNCATEGORIZED
    }

    // ml-powered classification using selected engine
    fun classifyWithML(
        context: Context,
        fileName: String,
        extension: String = "",
        mimeType: String? = null
    ): AssetType {
        val engine = FileClassificationEngine.create(context)
        return try {
            val result = engine.classify(fileName, extension, mimeType)
            mapCategory(result.category)
        } finally {
            engine.close()
        }
    }

    // maps ml categories to asset types
    private fun mapCategory(category: FileClassificationEngine.FileCategory): AssetType {
        return when (category) {
            FileClassificationEngine.FileCategory.STUDY_MATERIAL -> AssetType.STUDY_MATERIAL
            FileClassificationEngine.FileCategory.WORK_DOCUMENT -> AssetType.WORK_DOCUMENT
            FileClassificationEngine.FileCategory.MEDIA -> AssetType.MEDIA
            FileClassificationEngine.FileCategory.DIGITAL_CLUTTER -> AssetType.DIGITAL_CLUTTER
            FileClassificationEngine.FileCategory.UNCATEGORIZED -> AssetType.UNCATEGORIZED
        }
    }

    private val STUDY_KEYWORDS = listOf("lecture", "assignment", "exam", "notes", "quiz", "textbook")
    private val CLUTTER_KEYWORDS = listOf("meme", "whatsapp", "temp", "junk", "screenshot")
}
