package com.example.neatnest

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

class OnboardingActivity : AppCompatActivity() {

    private lateinit var switchStorage: SwitchCompat
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var switchBackground: SwitchCompat
    private lateinit var btnContinue: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvScanStatus: TextView

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (!granted) {
            switchStorage.isChecked = false
            Toast.makeText(this, "Storage permission is required for scanning.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        switchStorage = findViewById(R.id.switchStorage)
        switchNotifications = findViewById(R.id.switchNotifications)
        switchBackground = findViewById(R.id.switchBackground)
        btnContinue = findViewById(R.id.btnContinue)
        progressBar = findViewById(R.id.progressBar)
        tvScanStatus = findViewById(R.id.tvScanStatus)

        // Initialize switch states
        switchStorage.isChecked = isStoragePermissionGranted()
        switchNotifications.isChecked = isNotificationServiceEnabled()
        switchBackground.isChecked = isBatteryOptimizationIgnored()

        switchStorage.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isStoragePermissionGranted()) {
                requestStoragePermissions()
            }
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isNotificationServiceEnabled()) {
                try {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }

        switchBackground.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isBatteryOptimizationIgnored()) {
                requestIgnoreBatteryOptimizations()
            }
        }

        btnContinue.setOnClickListener {
            if (isStoragePermissionGranted() || isNotificationServiceEnabled()) {
                startInitialScan()
            } else {
                Toast.makeText(this, "Please grant at least one permission to proceed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        switchStorage.isChecked = isStoragePermissionGranted()
        switchNotifications.isChecked = isNotificationServiceEnabled()
        switchBackground.isChecked = isBatteryOptimizationIgnored()
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && TextUtils.equals(pkgName, cn.packageName)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isBatteryOptimizationIgnored(): Boolean {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(packageName)
        } else {
            true
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            )
        } else {
            storagePermissionLauncher.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        }
    }

    private fun startInitialScan() {
        btnContinue.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        tvScanStatus.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000) 
    }
}
