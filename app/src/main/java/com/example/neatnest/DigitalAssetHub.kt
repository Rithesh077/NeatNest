package com.example.neatnest

// classifies files by name into study material, clutter, or uncategorized
object DigitalAssetHub {

    enum class AssetType { STUDY_MATERIAL, DIGITAL_CLUTTER, UNCATEGORIZED }

    fun classifyByName(fileName: String): AssetType = when {
        STUDY_KEYWORDS.any { fileName.contains(it, ignoreCase = true) } -> AssetType.STUDY_MATERIAL
        fileName.endsWith(".pdf", ignoreCase = true) -> AssetType.STUDY_MATERIAL
        CLUTTER_KEYWORDS.any { fileName.contains(it, ignoreCase = true) } -> AssetType.DIGITAL_CLUTTER
        else -> AssetType.UNCATEGORIZED
    }

    private val STUDY_KEYWORDS = listOf("lecture", "assignment", "exam", "notes", "quiz", "textbook")
    private val CLUTTER_KEYWORDS = listOf("meme", "whatsapp", "temp", "junk", "screenshot")
}
