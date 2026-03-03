package com.example.neatnest.ml

import com.example.neatnest.ml.FileClassificationEngine.ClassificationResult
import com.example.neatnest.ml.FileClassificationEngine.FileCategory
import kotlin.math.ln

// pure kotlin naive bayes classifier using pre-trained word frequency priors
class NaiveBayesClassifier : FileClassificationEngine {

    // category prior probabilities
    private val categoryPriors = mapOf(
        FileCategory.STUDY_MATERIAL to 0.25,
        FileCategory.WORK_DOCUMENT to 0.20,
        FileCategory.MEDIA to 0.30,
        FileCategory.DIGITAL_CLUTTER to 0.15,
        FileCategory.UNCATEGORIZED to 0.10
    )

    // study material indicators
    private val studyWords = mapOf(
        "lecture" to 0.92, "assignment" to 0.90, "exam" to 0.88, "quiz" to 0.87,
        "notes" to 0.85, "textbook" to 0.91, "syllabus" to 0.93, "homework" to 0.89,
        "chapter" to 0.80, "module" to 0.75, "coursework" to 0.88, "tutorial" to 0.78,
        "study" to 0.82, "review" to 0.60, "midterm" to 0.90, "final" to 0.55,
        "semester" to 0.85, "lab" to 0.70, "research" to 0.72, "thesis" to 0.91,
        "paper" to 0.65, "journal" to 0.70, "dissertation" to 0.93, "academic" to 0.88,
        "class" to 0.60, "unit" to 0.55, "test" to 0.65, "grade" to 0.72,
        "slide" to 0.68, "ppt" to 0.70, "lesson" to 0.80, "worksheet" to 0.85,
        "handout" to 0.82, "formula" to 0.75, "equation" to 0.78, "solution" to 0.65
    )

    // work document indicators
    private val workWords = mapOf(
        "report" to 0.82, "invoice" to 0.91, "budget" to 0.88, "proposal" to 0.85,
        "contract" to 0.90, "memo" to 0.80, "agenda" to 0.82, "minutes" to 0.78,
        "receipt" to 0.87, "statement" to 0.80, "spreadsheet" to 0.85, "quarterly" to 0.88,
        "annual" to 0.75, "fiscal" to 0.82, "audit" to 0.84, "compliance" to 0.86,
        "payroll" to 0.90, "tax" to 0.82, "expense" to 0.85, "revenue" to 0.80,
        "sales" to 0.70, "marketing" to 0.68, "strategy" to 0.72, "plan" to 0.55,
        "presentation" to 0.75, "brief" to 0.65, "analysis" to 0.72, "summary" to 0.60,
        "project" to 0.60, "deadline" to 0.70, "timeline" to 0.72, "milestone" to 0.75,
        "deliverable" to 0.80, "specification" to 0.78, "requirement" to 0.75
    )

    // media indicators
    private val mediaWords = mapOf(
        "photo" to 0.90, "video" to 0.92, "music" to 0.88, "song" to 0.87,
        "movie" to 0.90, "clip" to 0.78, "recording" to 0.82, "podcast" to 0.85,
        "album" to 0.80, "playlist" to 0.82, "camera" to 0.85, "selfie" to 0.88,
        "portrait" to 0.75, "landscape" to 0.60, "wallpaper" to 0.70, "ringtone" to 0.82,
        "img" to 0.85, "vid" to 0.82, "pic" to 0.80, "dcim" to 0.90,
        "dsc" to 0.82, "mov" to 0.80, "raw" to 0.55, "hdr" to 0.70,
        "panorama" to 0.78, "timelapse" to 0.80, "slow" to 0.45, "burst" to 0.65,
        "thumbnail" to 0.65, "cover" to 0.55, "artwork" to 0.60, "audio" to 0.82
    )

    // clutter indicators
    private val clutterWords = mapOf(
        "meme" to 0.92, "whatsapp" to 0.85, "temp" to 0.80, "junk" to 0.88,
        "screenshot" to 0.78, "cache" to 0.85, "tmp" to 0.82, "download" to 0.55,
        "untitled" to 0.72, "copy" to 0.60, "duplicate" to 0.75, "backup" to 0.50,
        "trash" to 0.85, "spam" to 0.88, "forward" to 0.65, "fwd" to 0.68,
        "received" to 0.55, "sent" to 0.50, "status" to 0.70, "sticker" to 0.80,
        "gif" to 0.60, "reaction" to 0.72, "saved" to 0.45, "random" to 0.65,
        "misc" to 0.60, "old" to 0.50, "delete" to 0.70, "remove" to 0.65,
        "snap" to 0.72, "insta" to 0.70, "tiktok" to 0.75, "reel" to 0.68
    )

    // extension → category fallback
    private val extensionPriors = mapOf(
        // study
        "pdf" to FileCategory.STUDY_MATERIAL,
        "doc" to FileCategory.WORK_DOCUMENT,
        "docx" to FileCategory.WORK_DOCUMENT,
        "pptx" to FileCategory.STUDY_MATERIAL,
        "ppt" to FileCategory.STUDY_MATERIAL,
        // work
        "xlsx" to FileCategory.WORK_DOCUMENT,
        "xls" to FileCategory.WORK_DOCUMENT,
        "csv" to FileCategory.WORK_DOCUMENT,
        // media
        "jpg" to FileCategory.MEDIA,
        "jpeg" to FileCategory.MEDIA,
        "png" to FileCategory.MEDIA,
        "mp4" to FileCategory.MEDIA,
        "mp3" to FileCategory.MEDIA,
        "wav" to FileCategory.MEDIA,
        "avi" to FileCategory.MEDIA,
        "mkv" to FileCategory.MEDIA,
        "flac" to FileCategory.MEDIA,
        "webm" to FileCategory.MEDIA,
        "webp" to FileCategory.MEDIA,
        "heic" to FileCategory.MEDIA,
        "heif" to FileCategory.MEDIA,
        "aac" to FileCategory.MEDIA,
        "ogg" to FileCategory.MEDIA,
        "mov" to FileCategory.MEDIA,
        "3gp" to FileCategory.MEDIA,
        // clutter
        "tmp" to FileCategory.DIGITAL_CLUTTER
    )

    override fun classify(fileName: String, extension: String, mimeType: String?): ClassificationResult {
        val nameLower = fileName.lowercase()
        val ext = extension.lowercase().ifEmpty { nameLower.substringAfterLast('.', "") }

        // tokenize: split on non-alphanumeric, filter short tokens
        val tokens = nameLower
            .replace(Regex("[^a-z0-9]"), " ")
            .split(" ")
            .filter { it.length >= 2 }

        // compute log-likelihood for each category
        val scores = mutableMapOf<FileCategory, Double>()

        for ((category, prior) in categoryPriors) {
            var logProb = ln(prior)

            for (token in tokens) {
                val weight = when (category) {
                    FileCategory.STUDY_MATERIAL -> studyWords[token]
                    FileCategory.WORK_DOCUMENT -> workWords[token]
                    FileCategory.MEDIA -> mediaWords[token]
                    FileCategory.DIGITAL_CLUTTER -> clutterWords[token]
                    FileCategory.UNCATEGORIZED -> null
                }

                if (weight != null) {
                    logProb += ln(weight)
                } else {
                    // laplace smoothing: small penalty for unknown tokens
                    logProb += ln(0.05)
                }
            }

            scores[category] = logProb
        }

        // extension boost: if extension maps to a category, boost its score
        val extCategory = extensionPriors[ext]
        if (extCategory != null && scores.containsKey(extCategory)) {
            scores[extCategory] = scores[extCategory]!! + ln(2.0)
        }

        // mime type boost
        if (mimeType != null) {
            val mimeCategory = when {
                mimeType.startsWith("image/") -> FileCategory.MEDIA
                mimeType.startsWith("video/") -> FileCategory.MEDIA
                mimeType.startsWith("audio/") -> FileCategory.MEDIA
                mimeType.contains("pdf") -> FileCategory.STUDY_MATERIAL
                mimeType.contains("spreadsheet") || mimeType.contains("excel") -> FileCategory.WORK_DOCUMENT
                mimeType.contains("presentation") || mimeType.contains("powerpoint") -> FileCategory.STUDY_MATERIAL
                mimeType.contains("document") || mimeType.contains("word") -> FileCategory.WORK_DOCUMENT
                else -> null
            }
            if (mimeCategory != null && scores.containsKey(mimeCategory)) {
                scores[mimeCategory] = scores[mimeCategory]!! + ln(1.5)
            }
        }

        // convert log-probabilities to normalized confidence
        val maxScore = scores.values.max()
        val expScores = scores.mapValues { Math.exp(it.value - maxScore) }
        val sumExp = expScores.values.sum()
        val probabilities = expScores.mapValues { (it.value / sumExp).toFloat() }

        val bestCategory = probabilities.maxByOrNull { it.value }?.key ?: FileCategory.UNCATEGORIZED
        val confidence = probabilities[bestCategory] ?: 0f

        // if confidence is below threshold, fall back to uncategorized
        val finalCategory = if (confidence < FileClassificationEngine.CONFIDENCE_THRESHOLD) {
            FileCategory.UNCATEGORIZED
        } else {
            bestCategory
        }

        return ClassificationResult(finalCategory, confidence, engineName())
    }

    override fun engineName(): String = "NaiveBayes"
}
