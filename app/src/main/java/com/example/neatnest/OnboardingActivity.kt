package com.example.neatnest

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class OnboardingActivity : AppCompatActivity() {

    private lateinit var switchNotifications: SwitchCompat
    private lateinit var switchBackground: SwitchCompat
    private lateinit var switchMoveFiles: SwitchCompat
    private lateinit var rgScanMode: RadioGroup
    private lateinit var btnPickSource: Button
    private lateinit var btnSelectRoot: Button
    private lateinit var btnStart: Button
    private lateinit var tvPickedFolders: TextView
    private lateinit var tvRootPath: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    private val sourceFolderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            saveSourceFolder(it)
        }
    }

    private val rootFolderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            getSharedPreferences("NeatNestPrefs", MODE_PRIVATE).edit().putString("root_uri", it.toString()).apply()
            tvRootPath.text = it.path
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("NeatNestPrefs", MODE_PRIVATE)
        if (prefs.getBoolean("onboarding_completed", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_onboarding)

        initViews()
        setupListeners()
        updatePickedFoldersList()
    }

    private fun initViews() {
        switchNotifications = findViewById(R.id.switchNotifications)
        switchBackground = findViewById(R.id.switchBackground)
        switchMoveFiles = findViewById(R.id.switchMoveFiles)
        rgScanMode = findViewById(R.id.rgScanMode)
        btnPickSource = findViewById(R.id.btnPickSourceFolders)
        btnSelectRoot = findViewById(R.id.btnSelectRoot)
        btnStart = findViewById(R.id.btnStart)
        tvPickedFolders = findViewById(R.id.tvPickedFolders)
        tvRootPath = findViewById(R.id.tvRootPath)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
    }

    private fun setupListeners() {
        rgScanMode.setOnCheckedChangeListener { _, checkedId ->
            val isPickMode = checkedId == R.id.rbPickFolders
            btnPickSource.visibility = if (isPickMode) View.VISIBLE else View.GONE
            tvPickedFolders.visibility = if (isPickMode) View.VISIBLE else View.GONE
            
            if (checkedId == R.id.rbCompleteScan) checkAllFilesAccess()
        }

        btnPickSource.setOnClickListener { sourceFolderPicker.launch(null) }
        btnSelectRoot.setOnClickListener { rootFolderPicker.launch(null) }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isNotificationServiceEnabled()) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        switchBackground.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isBatteryOptimizationIgnored()) requestIgnoreBatteryOptimizations()
        }

        btnStart.setOnClickListener { validateAndStart() }
    }

    private fun checkAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun saveSourceFolder(uri: Uri) {
        val database = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            database.trackedFolderDao().insertFolder(TrackedFolder(uri.toString(), uri.path ?: "Folder"))
        }
    }

    private fun updatePickedFoldersList() {
        AppDatabase.getDatabase(this).trackedFolderDao().getAllTrackedFolders().asLiveData().observe(this) { folders ->
            tvPickedFolders.text = if (folders.isEmpty()) "No source folders selected" 
                                   else folders.joinToString("\n") { it.folderName }
        }
    }

    private fun validateAndStart() {
        val prefs = getSharedPreferences("NeatNestPrefs", MODE_PRIVATE)
        val rootUri = prefs.getString("root_uri", null)
        
        if (rootUri == null) {
            Toast.makeText(this, "Please select an Organization Destination", Toast.LENGTH_SHORT).show()
            return
        }

        prefs.edit().apply {
            putBoolean("onboarding_completed", true)
            putBoolean("move_files_enabled", switchMoveFiles.isChecked)
            putBoolean("complete_scan_mode", rgScanMode.checkedRadioButtonId == R.id.rbCompleteScan)
            apply()
        }

        startPipeline()
    }

    private fun startPipeline() {
        btnStart.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        tvStatus.visibility = View.VISIBLE

        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(OneTimeWorkRequestBuilder<AssetScannerWorker>().build())

        if (switchBackground.isChecked) {
            val periodicScan = PeriodicWorkRequestBuilder<AssetScannerWorker>(4, TimeUnit.HOURS).build()
            workManager.enqueue(periodicScan)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return !TextUtils.isEmpty(flat) && flat.contains(packageName)
    }

    private fun isBatteryOptimizationIgnored(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun requestIgnoreBatteryOptimizations() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
}
