package com.example.neatnest

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.neatnest.ui.analyser.DeviceAnalyserViewModel
import com.example.neatnest.ui.analyser.NetworkFragment
import com.example.neatnest.ui.analyser.OverviewFragment
import com.example.neatnest.ui.analyser.PerformanceFragment
import com.example.neatnest.ui.analyser.StorageFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

// host activity for Device Analyser with ViewPager2 + TabLayout
class DeviceAnalyserActivity : AppCompatActivity() {

    private val viewModel: DeviceAnalyserViewModel by viewModel()
    private val batteryReceiver = BatteryReceiver()
    private var refreshTimer: Timer? = null

    private val tabTitles = arrayOf("Overview", "Storage", "Performance", "Network")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_analyser)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val fabReport = findViewById<FloatingActionButton>(R.id.fabReport)

        // viewpager adapter
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 4
            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> OverviewFragment()
                1 -> StorageFragment()
                2 -> PerformanceFragment()
                3 -> NetworkFragment()
                else -> OverviewFragment()
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // battery receiver for real-time updates
        batteryReceiver.onBatteryChanged = { level, temp, charging ->
            viewModel.updateBattery(level, temp, charging)
        }

        // schedule periodic stats worker
        val statsRequest = PeriodicWorkRequestBuilder<DeviceStatsWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "device_stats",
            ExistingPeriodicWorkPolicy.KEEP,
            statsRequest
        )

        // FAB → generate report
        fabReport.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    viewModel.takeSnapshot()
                    val reportGen = ReportGenerator(this@DeviceAnalyserActivity)
                    val uri = reportGen.generateReport(viewModel.currentStats.value)
                    if (uri != null) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share Device Report"))
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@DeviceAnalyserActivity, "Report failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        // real-time refresh every 3 seconds
        refreshTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() { viewModel.refresh() }
            }, 0, 3000)
        }
    }

    override fun onPause() {
        super.onPause()
        try { unregisterReceiver(batteryReceiver) } catch (_: Exception) {}
        refreshTimer?.cancel()
        refreshTimer = null
    }

    // toolbar options menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> { viewModel.refresh(); true }
            R.id.action_share_report -> {
                findViewById<FloatingActionButton>(R.id.fabReport).performClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
