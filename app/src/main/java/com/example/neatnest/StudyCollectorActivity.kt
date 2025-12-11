package com.example.neatnest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

class StudyCollectorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_collector)

        val btnPdf = findViewById<Button>(R.id.btnOpenPdf)

        btnPdf.setOnClickListener {
            val samplePdf =
                ("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pd36" +
                        "f33").toUri()

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(samplePdf, "application/pdf")
            }

            startActivity(intent)
        }
    }
}
