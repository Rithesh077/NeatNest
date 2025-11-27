package com.example.neatnest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MessageCleanerActivity : AppCompatActivity() {

    private val extractedText = "Reminder: Math assignment due tomorrow at 11:59 PM."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_cleaner)

        val btnShare = findViewById<Button>(R.id.btnShareImportant)

        btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, extractedText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
    }
}
