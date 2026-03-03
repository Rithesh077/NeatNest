package com.example.neatnest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
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
import com.example.neatnest.ui.common.UiState
import com.example.neatnest.ui.signalcleaner.SignalCleanerViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignalNoiseCleanerActivity : AppCompatActivity() {

    private val viewModel: SignalCleanerViewModel by viewModel()
    private lateinit var notificationsAdapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signal_noise_cleaner)

        val btnAccess = findViewById<Button>(R.id.btnShareImportant)
        val btnClearNoise = findViewById<Button>(R.id.btnClearNoise)
        val tvNotifStatus = findViewById<TextView>(R.id.tvNotifStatus)
        val tvNoNotifications = findViewById<TextView>(R.id.tvNoNotifications)
        val rvNotifications = findViewById<RecyclerView>(R.id.rvNotifications)

        // analytics views
        val cardAnalytics = findViewById<MaterialCardView>(R.id.cardAnalytics)
        val tvTotalCount = findViewById<TextView>(R.id.tvTotalCount)
        val tvHighCount = findViewById<TextView>(R.id.tvHighCount)
        val tvNormalCount = findViewById<TextView>(R.id.tvNormalCount)
        val tvLowCount = findViewById<TextView>(R.id.tvLowCount)
        val tvTopApps = findViewById<TextView>(R.id.tvTopApps)

        // entrance animations
        cardAnalytics.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in))

        // recyclerview for notifications
        notificationsAdapter = NotificationsAdapter()
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = notificationsAdapter

        // update status text
        updateNotifStatus(tvNotifStatus)

        // observe notification list
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            tvNoNotifications.visibility = View.VISIBLE
                            rvNotifications.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            val notifications = state.data
                            notificationsAdapter.submitList(notifications)
                            if (notifications.isEmpty()) {
                                tvNoNotifications.visibility = View.VISIBLE
                                rvNotifications.visibility = View.GONE
                            } else {
                                tvNoNotifications.visibility = View.GONE
                                rvNotifications.visibility = View.VISIBLE
                            }
                        }
                        is UiState.Error -> {
                            tvNoNotifications.visibility = View.VISIBLE
                            rvNotifications.visibility = View.GONE
                            Toast.makeText(this@SignalNoiseCleanerActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // observe analytics
        viewModel.notificationCount.asLiveData().observe(this) { count ->
            tvTotalCount.text = count.toString()
        }
        viewModel.highCount.asLiveData().observe(this) { count ->
            tvHighCount.text = count.toString()
        }
        viewModel.normalCount.asLiveData().observe(this) { count ->
            tvNormalCount.text = count.toString()
        }
        viewModel.lowCount.asLiveData().observe(this) { count ->
            tvLowCount.text = count.toString()
        }
        viewModel.topPackages.asLiveData().observe(this) { packages ->
            if (packages.isNotEmpty()) {
                tvTopApps.visibility = View.VISIBLE
                val topAppsText = packages.joinToString(" • ") { pkg ->
                    val appName = pkg.packageName.substringAfterLast('.')
                    "$appName (${pkg.count})"
                }
                tvTopApps.text = "Top apps: $topAppsText"
            } else {
                tvTopApps.visibility = View.GONE
            }
        }

        btnAccess.setOnClickListener { handleNotificationAccess() }
        btnClearNoise.setOnClickListener { showClearNoiseDialog() }
    }

    override fun onResume() {
        super.onResume()
        val tvNotifStatus = findViewById<TextView>(R.id.tvNotifStatus)
        updateNotifStatus(tvNotifStatus)
    }

    private fun handleNotificationAccess() {
        if (!PermissionManager.isNotificationServiceEnabled(this)) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.notif_access_dialog_title))
                .setMessage(getString(R.string.notif_access_dialog_message))
                .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .show()
        } else {
            Toast.makeText(this, getString(R.string.notif_access_granted), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showClearNoiseDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_noise_dialog_title))
            .setMessage(getString(R.string.clear_noise_dialog_message))
            .setPositiveButton(getString(R.string.clear_noise_confirm)) { _, _ ->
                viewModel.clearAllNotifications()
                Toast.makeText(this, getString(R.string.noise_cleared), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun updateNotifStatus(tvNotifStatus: TextView) {
        if (PermissionManager.isNotificationServiceEnabled(this)) {
            tvNotifStatus.text = getString(R.string.notif_status_active)
            tvNotifStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            tvNotifStatus.text = getString(R.string.notif_status_inactive)
            tvNotifStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
