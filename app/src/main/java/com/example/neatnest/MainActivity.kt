package com.example.neatnest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnDigitalHub = findViewById<Button>(R.id.btnDigitalAssetHub)
        val btnSignalNoise = findViewById<Button>(R.id.btnSignalNoiseCleaner)
        val tvAssetCount = findViewById<TextView>(R.id.tvAssetCount)
        val tvSignalCount = findViewById<TextView>(R.id.tvSignalCount)

        val database = AppDatabase.getDatabase(this)
        
        // Observe Asset Stats
        database.processedFileDao().getProcessedFilesCount().asLiveData().observe(this) { count ->
            tvAssetCount.text = "Assets Organized: $count"
        }

        // Observe Signal Stats
        database.processedNotificationDao().getNotificationCount().asLiveData().observe(this) { count ->
            tvSignalCount.text = "Signals Cleaned: $count"
        }

        btnDigitalHub.setOnClickListener {
            startActivity(Intent(this, DigitalAssetHubActivity::class.java))
        }

        btnSignalNoise.setOnClickListener {
            startActivity(Intent(this, SignalNoiseCleanerActivity::class.java))
        }
    }
}
