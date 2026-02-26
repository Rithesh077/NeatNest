package com.example.neatnest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.card.MaterialCardView

// utility hub screen with rescan and placeholders for upcoming tools
class UtilityHubActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utility_hub)

        val btnBack = findViewById<android.widget.ImageView>(R.id.btnBack)
        val cardRescanFiles = findViewById<MaterialCardView>(R.id.cardRescanFiles)
        val cardVideoEditor = findViewById<MaterialCardView>(R.id.cardVideoEditor)
        val cardFileEditor = findViewById<MaterialCardView>(R.id.cardFileEditor)
        val cardDataExtractor = findViewById<MaterialCardView>(R.id.cardDataExtractor)
        val cardPriceTracker = findViewById<MaterialCardView>(R.id.cardPriceTracker)

        btnBack.setOnClickListener { finish() }

        // rescan is the only active tool right now
        cardRescanFiles.setOnClickListener {
            showResyncDialog()
        }

        // placeholders for upcoming features
        cardVideoEditor.setOnClickListener {
            Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        }
        cardFileEditor.setOnClickListener {
            Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        }
        cardDataExtractor.setOnClickListener {
            Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        }
        cardPriceTracker.setOnClickListener {
            Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showResyncDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.resync_dialog_title))
            .setMessage(getString(R.string.resync_dialog_message))
            .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                performResync()
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun performResync() {
        val workManager = WorkManager.getInstance(this)
        val scanRequest = OneTimeWorkRequestBuilder<AssetScannerWorker>().build()
        workManager.enqueue(scanRequest)

        workManager.getWorkInfoByIdLiveData(scanRequest.id).observe(this) { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING ->
                    Toast.makeText(this, getString(R.string.resync_running), Toast.LENGTH_SHORT).show()
                WorkInfo.State.SUCCEEDED ->
                    Toast.makeText(this, getString(R.string.resync_success), Toast.LENGTH_SHORT).show()
                WorkInfo.State.FAILED ->
                    Toast.makeText(this, getString(R.string.resync_failed), Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }
}

