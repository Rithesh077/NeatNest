package com.example.neatnest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStudy = findViewById<Button>(R.id.btnStudyCollector)
        val btnMsg = findViewById<Button>(R.id.btnMessageCleaner)
        val btnClutter = findViewById<Button>(R.id.btnAntiClutter)

        btnStudy.setOnClickListener {
            startActivity(Intent(this, StudyCollectorActivity::class.java))
        }

        btnMsg.setOnClickListener {
            startActivity(Intent(this, MessageCleanerActivity::class.java))
        }

        btnClutter.setOnClickListener {
            startActivity(Intent(this, AntiClutterActivity::class.java))
        }
    }
}
