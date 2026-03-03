package com.example.neatnest.ml

import android.content.Context
import android.util.Log
import com.example.neatnest.ml.FileClassificationEngine.ClassificationResult
import com.example.neatnest.ml.FileClassificationEngine.FileCategory
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

// tflite classifier — loads model from assets, falls back to naive bayes on failure
class TFLiteClassifier(private val context: Context) : FileClassificationEngine {

    private var interpreter: Interpreter? = null
    private val maxSeqLength = 64
    private val vocabSize = 128

    // model output index → FileCategory
    private val indexToCategory = arrayOf(
        FileCategory.STUDY_MATERIAL,  // index 0
        FileCategory.WORK_DOCUMENT,   // index 1
        FileCategory.MEDIA,           // index 2
        FileCategory.DIGITAL_CLUTTER, // index 3
        FileCategory.UNCATEGORIZED    // index 4
    )

    init {
        try {
            val model = loadModelFile("file_classifier.tflite")
            val options = Interpreter.Options().apply {
                setNumThreads(2)
            }
            interpreter = Interpreter(model, options)
            Log.d("TFLiteClassifier", "model loaded successfully")
        } catch (e: Exception) {
            Log.e("TFLiteClassifier", "failed to load tflite model, falling back to naive bayes", e)
        }
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    // convert filename to fixed-length float buffer of normalized char codes
    private fun tokenize(fileName: String): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(maxSeqLength * 4) // 4 bytes per float
        buffer.order(ByteOrder.nativeOrder())

        val chars = fileName.lowercase().toCharArray()
        for (i in 0 until maxSeqLength) {
            val value = if (i < chars.size) {
                val code = chars[i].code
                if (code < vocabSize) code.toFloat() / vocabSize.toFloat() else 0f
            } else {
                0f // padding
            }
            buffer.putFloat(value)
        }
        buffer.rewind()
        return buffer
    }

    override fun classify(fileName: String, extension: String, mimeType: String?): ClassificationResult {
        val interp = interpreter
        if (interp == null) {
            // fallback to naive bayes if model failed to load
            Log.w("TFLiteClassifier", "interpreter not available, using naive bayes fallback")
            return NaiveBayesClassifier().classify(fileName, extension, mimeType)
        }

        return try {
            val input = tokenize(fileName)
            val output = Array(1) { FloatArray(indexToCategory.size) }

            interp.run(input, output)

            val probabilities = output[0]
            var maxIdx = 0
            var maxProb = probabilities[0]
            for (i in 1 until probabilities.size) {
                if (probabilities[i] > maxProb) {
                    maxProb = probabilities[i]
                    maxIdx = i
                }
            }

            val category = if (maxProb >= FileClassificationEngine.CONFIDENCE_THRESHOLD) {
                indexToCategory[maxIdx]
            } else {
                FileCategory.UNCATEGORIZED
            }

            ClassificationResult(category, maxProb, engineName())
        } catch (e: Exception) {
            Log.e("TFLiteClassifier", "inference failed, falling back", e)
            NaiveBayesClassifier().classify(fileName, extension, mimeType)
        }
    }

    override fun engineName(): String = "TFLite"

    override fun close() {
        interpreter?.close()
        interpreter = null
    }
}
