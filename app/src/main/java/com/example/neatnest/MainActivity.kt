package com.example.neatnest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.neatnest.ui.main.DashboardViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModel()
    private lateinit var recentActivityAdapter: RecentActivityAdapter
    private lateinit var tvNoRecentFiles: TextView
    private lateinit var rvRecentFiles: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvAssetCount = findViewById<TextView>(R.id.tvAssetCount)
        val tvSignalCount = findViewById<TextView>(R.id.tvSignalCount)
        tvNoRecentFiles = findViewById(R.id.tvNoRecentFiles)
        rvRecentFiles = findViewById(R.id.rvRecentFiles)
        val cardAssetHub = findViewById<MaterialCardView>(R.id.cardAssetHub)
        val cardSignalCleaner = findViewById<MaterialCardView>(R.id.cardSignalCleaner)
        val cardResync = findViewById<MaterialCardView>(R.id.cardResync)
        val cardUtilityHub = findViewById<MaterialCardView>(R.id.cardUtilityHub)
        val cardDevMode = findViewById<MaterialCardView>(R.id.cardDevMode)
        val fabResync = findViewById<FloatingActionButton>(R.id.fabResync)

        val workManager = WorkManager.getInstance(this)

        // recyclerview backed by viewmodel activity list
        recentActivityAdapter = RecentActivityAdapter { item ->
            showActivityDetailDialog(item)
        }
        rvRecentFiles.layoutManager = LinearLayoutManager(this)
        rvRecentFiles.adapter = recentActivityAdapter

        // observe asset count via viewmodel
        viewModel.fileCount.asLiveData().observe(this) { count ->
            tvAssetCount.text = count.toString()
        }

        // observe signal count via viewmodel
        viewModel.notificationCount.asLiveData().observe(this) { count ->
            tvSignalCount.text = count.toString()
        }

        // observe recent activity list from viewmodel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityList.collect { activities ->
                    recentActivityAdapter.submitList(activities)
                    updateRecentList(activities.isEmpty())
                }
            }
        }

        // digital organizer needs onboarding (folder setup) first
        cardAssetHub.setOnClickListener {
            if (viewModel.isOnboardingCompleted()) {
                startActivity(Intent(this, DigitalAssetHubActivity::class.java))
            } else {
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
        }
        cardSignalCleaner.setOnClickListener {
            startActivity(Intent(this, SignalNoiseCleanerActivity::class.java))
        }
        cardResync.setOnClickListener {
            showResyncDialog(workManager)
        }

        // utility hub navigates to the utility hub screen
        cardUtilityHub.setOnClickListener {
            startActivity(Intent(this, UtilityHubActivity::class.java))
        }

        // dev mode launches the menu and lifecycle demo
        cardDevMode.setOnClickListener {
            startActivity(Intent(this, FileMoverActivity::class.java))
        }

        fabResync.setOnClickListener {
            showResyncDialog(workManager)
        }
    }

    private fun updateRecentList(isEmpty: Boolean) {
        if (isEmpty) {
            tvNoRecentFiles.visibility = View.VISIBLE
            rvRecentFiles.visibility = View.GONE
        } else {
            tvNoRecentFiles.visibility = View.GONE
            rvRecentFiles.visibility = View.VISIBLE
        }
    }

    // detail dialog for tapped activity
    private fun showActivityDetailDialog(item: RecentActivityItem) {
        val fmt = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
        AlertDialog.Builder(this)
            .setTitle(item.title)
            .setMessage(
                "${getString(R.string.activity_detail_desc)} ${item.description}\n" +
                "${getString(R.string.activity_detail_type)} ${item.type.name}\n" +
                "${getString(R.string.activity_detail_time)} ${fmt.format(Date(item.timestamp))}"
            )
            .setPositiveButton(getString(R.string.dialog_ok), null)
            .show()
    }

    // confirmation dialog before re-sync
    private fun showResyncDialog(workManager: WorkManager) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.resync_dialog_title))
            .setMessage(getString(R.string.resync_dialog_message))
            .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                performResync(workManager)
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun performResync(workManager: WorkManager) {
        viewModel.pushActivity(
            getString(R.string.activity_sync_started),
            getString(R.string.activity_sync_started_desc),
            RecentActivityItem.ActivityType.SYNC_COMPLETED
        )

        val scanRequest = OneTimeWorkRequestBuilder<AssetScannerWorker>().build()
        workManager.enqueue(scanRequest)

        workManager.getWorkInfoByIdLiveData(scanRequest.id).observe(this) { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING ->
                    Toast.makeText(this, getString(R.string.resync_running), Toast.LENGTH_SHORT).show()
                WorkInfo.State.SUCCEEDED -> {
                    Toast.makeText(this, getString(R.string.resync_success), Toast.LENGTH_SHORT).show()
                    viewModel.pushActivity(
                        getString(R.string.activity_sync_complete),
                        getString(R.string.activity_sync_complete_desc),
                        RecentActivityItem.ActivityType.SYNC_COMPLETED
                    )
                }
                WorkInfo.State.FAILED ->
                    Toast.makeText(this, getString(R.string.resync_failed), Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }
}
