package com.example.neatnest

import android.net.Uri

/**
 * Core logic engine for the Digital Asset Hub.
 * Handles the foundational classification and processing for Study Materials and Anti-Clutter.
 */
class DigitalAssetHub {

    enum class AssetType {
        STUDY_MATERIAL,
        DIGITAL_CLUTTER,
        UNCATEGORIZED
    }

    data class Asset(
        val uri: Uri,
        val type: AssetType,
        val metadata: Map<String, String> = emptyMap()
    )

    /**
     * Foundational pipeline for classifying incoming files.
     * This will eventually be replaced by TinyML integration.
     */
    fun classifyAsset(uri: Uri, fileName: String): AssetType {
        return when {
            isStudyRelated(fileName) -> AssetType.STUDY_MATERIAL
            isClutterRelated(fileName) -> AssetType.DIGITAL_CLUTTER
            else -> AssetType.UNCATEGORIZED
        }
    }

    private fun isStudyRelated(fileName: String): Boolean {
        val studyKeywords = listOf("lecture", "assignment", "exam", "notes", "quiz", "textbook")
        return studyKeywords.any { fileName.contains(it, ignoreCase = true) } || fileName.endsWith(".pdf", ignoreCase = true)
    }

    private fun isClutterRelated(fileName: String): Boolean {
        val clutterKeywords = listOf("meme", "whatsapp", "temp", "junk", "screenshot")
        return clutterKeywords.any { fileName.contains(it, ignoreCase = true) }
    }

    /**
     * Foundational logic for "Actioning" an asset based on its classification.
     */
    fun processAsset(asset: Asset) {
        when (asset.type) {
            AssetType.STUDY_MATERIAL -> {
                // Logic for "Auto-organize" (e.g., Tagging, OCR extraction)
            }
            AssetType.DIGITAL_CLUTTER -> {
                // Logic for "Flagging" (e.g., preparing for cleaning suggestions)
            }
            AssetType.UNCATEGORIZED -> {
                // Default handling
            }
        }
    }
}
