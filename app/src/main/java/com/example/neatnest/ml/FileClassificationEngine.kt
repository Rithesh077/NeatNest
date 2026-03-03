package com.example.neatnest.ml

import android.content.Context

// contract for file classification engines
interface FileClassificationEngine {

    enum class FileCategory {
        STUDY_MATERIAL,   // lectures, assignments, notes, textbooks
        WORK_DOCUMENT,    // reports, spreadsheets, presentations
        MEDIA,            // photos, videos, music, recordings
        DIGITAL_CLUTTER,  // memes, temp files, screenshots, whatsapp junk
        UNCATEGORIZED     // fallback when confidence is too low
    }

    data class ClassificationResult(
        val category: FileCategory,
        val confidence: Float,   // 0.0 to 1.0
        val engineName: String   // "NaiveBayes" or "TFLite"
    )

    // classify a file and return category + confidence
    fun classify(fileName: String, extension: String = "", mimeType: String? = null): ClassificationResult

    // human-readable engine name
    fun engineName(): String

    // release resources (tflite interpreter, etc.)
    fun close() {}

    companion object {
        const val CONFIDENCE_THRESHOLD = 0.45f

        // factory to get the selected engine
        fun create(context: Context): FileClassificationEngine {
            val prefs = context.getSharedPreferences("NeatNestPrefs", Context.MODE_PRIVATE)
            val model = prefs.getString("classification_model", "naive_bayes") ?: "naive_bayes"
            return when (model) {
                "tflite" -> TFLiteClassifier(context)
                else -> NaiveBayesClassifier()
            }
        }
    }
}
