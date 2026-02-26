package com.example.neatnest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.neatnest.data.model.TrackedFolder
import com.example.neatnest.ui.onboarding.OnboardingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class OnboardingActivity : AppCompatActivity() {

    private val viewModel: OnboardingViewModel by viewModel()

    private lateinit var switchNotifications: SwitchCompat
    private lateinit var switchBackground: SwitchCompat
    private lateinit var switchMoveFiles: SwitchCompat
    private lateinit var rgScanMode: RadioGroup
    private lateinit var btnPickSource: Button
    private lateinit var btnSelectRoot: Button
    private lateinit var btnStart: Button
    private lateinit var rvTrackedFolders: RecyclerView
    private lateinit var tvNoFoldersSelected: TextView
    private lateinit var tvRootPath: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    private lateinit var foldersAdapter: TrackedFoldersAdapter
    private var hasNavigated = false

    // source folder picker
    private val sourceFolderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            // request both read and write so move-mode can delete source files
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            saveSourceFolder(it)
        }
    }

    private val rootFolderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            viewModel.setRootUri(it.toString())
            tvRootPath.text = it.path
        }
    }

    // storage permission request
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startPipeline()
        } else {
            Toast.makeText(this, getString(R.string.storage_permission_required), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // if onboarding was already done, go straight to asset hub
        if (viewModel.isOnboardingCompleted()) {
            navigateToAssetHub()
            return
        }

        setContentView(R.layout.activity_onboarding)
        initViews()
        setupRecyclerView()
        setupListeners()
        observeTrackedFolders()
    }

    private fun initViews() {
        switchNotifications = findViewById(R.id.switchNotifications)
        switchBackground = findViewById(R.id.switchBackground)
        switchMoveFiles = findViewById(R.id.switchMoveFiles)
        rgScanMode = findViewById(R.id.rgScanMode)
        btnPickSource = findViewById(R.id.btnPickSourceFolders)
        btnSelectRoot = findViewById(R.id.btnSelectRoot)
        btnStart = findViewById(R.id.btnStart)
        rvTrackedFolders = findViewById(R.id.rvTrackedFolders)
        tvNoFoldersSelected = findViewById(R.id.tvNoFoldersSelected)
        tvRootPath = findViewById(R.id.tvRootPath)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
    }

    private fun setupRecyclerView() {
        foldersAdapter = TrackedFoldersAdapter { folder ->
            showRemoveFolderDialog(folder)
        }
        rvTrackedFolders.layoutManager = LinearLayoutManager(this)
        rvTrackedFolders.adapter = foldersAdapter
    }

    private fun setupListeners() {
        rgScanMode.setOnCheckedChangeListener { _, checkedId ->
            val isPickMode = checkedId == R.id.rbPickFolders
            btnPickSource.visibility = if (isPickMode) View.VISIBLE else View.GONE
            rvTrackedFolders.visibility = if (isPickMode) View.VISIBLE else View.GONE
            tvNoFoldersSelected.visibility = if (isPickMode) View.VISIBLE else View.GONE

            if (checkedId == R.id.rbCompleteScan) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, "package:$packageName".toUri())
                    startActivity(intent)
                }
                btnStart.isEnabled = true
            }
        }

        btnPickSource.setOnClickListener { sourceFolderPicker.launch(null) }
        btnSelectRoot.setOnClickListener { rootFolderPicker.launch(null) }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !PermissionManager.isNotificationServiceEnabled(this)) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        switchBackground.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !PermissionManager.isIgnoringBatteryOptimizations(this)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:$packageName".toUri())
                startActivity(intent)
            }
        }

        btnStart.setOnClickListener { validateAndStart() }
    }

    private fun showRemoveFolderDialog(folder: TrackedFolder) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.remove_folder_title))
            .setMessage(getString(R.string.remove_folder_message))
            .setPositiveButton(getString(R.string.remove_confirm)) { _, _ ->
                removeFolder(folder)
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun removeFolder(folder: TrackedFolder) {
        lifecycleScope.launch {
            contentResolver.releasePersistableUriPermission(folder.uri.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.removeFolder(folder)
        }
    }

    private fun saveSourceFolder(uri: Uri) {
        viewModel.addFolder(TrackedFolder(uri.toString(), uri.path ?: "Folder"))
    }

    private fun observeTrackedFolders() {
        viewModel.trackedFolders.asLiveData().observe(this) { folders ->
            foldersAdapter.submitList(folders)
            val hasFolders = folders.isNotEmpty()
            tvNoFoldersSelected.visibility = if (hasFolders) View.GONE else View.VISIBLE

            if (rgScanMode.checkedRadioButtonId == R.id.rbPickFolders) {
                btnStart.isEnabled = hasFolders
            }
        }
    }

    private fun validateAndStart() {
        if (viewModel.getRootUri() == null) {
            Toast.makeText(this, getString(R.string.select_root_required), Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.setMoveFilesEnabled(switchMoveFiles.isChecked)
        viewModel.setCompleteScanMode(rgScanMode.checkedRadioButtonId == R.id.rbCompleteScan)
        viewModel.completeOnboarding()

        // request permissions before starting pipeline
        requestStoragePermissions()
    }

    private fun requestStoragePermissions() {
        val isCompleteScan = rgScanMode.checkedRadioButtonId == R.id.rbCompleteScan

        // pick-folders mode uses SAF document tree URIs which already grant access
        // — no MediaStore permissions needed, go straight to the pipeline
        if (!isCompleteScan) {
            startPipeline()
            return
        }

        // complete scan mode needs MediaStore permissions to query all device media
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val needed = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            ).filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()

            if (needed.isNotEmpty()) {
                storagePermissionLauncher.launch(needed)
            } else {
                startPipeline()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                storagePermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                startPipeline()
            }
        }
    }

    private fun startPipeline() {
        btnStart.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = getString(R.string.scan_in_progress)

        val workManager = WorkManager.getInstance(this)
        val scanRequest = OneTimeWorkRequestBuilder<AssetScannerWorker>().build()
        workManager.enqueue(scanRequest)

        if (switchBackground.isChecked) {
            val periodicScan = PeriodicWorkRequestBuilder<AssetScannerWorker>(4, TimeUnit.HOURS)
                .addTag("periodic_scan")
                .build()
            workManager.enqueue(periodicScan)
        }

        // safety timeout so the user is never stuck on this screen
        lifecycleScope.launch {
            delay(30_000)
            if (!hasNavigated) {
                tvStatus.text = getString(R.string.scan_timeout)
                Toast.makeText(this@OnboardingActivity, getString(R.string.scan_timeout), Toast.LENGTH_SHORT).show()
                navigateToAssetHub()
            }
        }

        workManager.getWorkInfoByIdLiveData(scanRequest.id).observe(this) { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.SUCCEEDED -> {
                    tvStatus.text = getString(R.string.scan_complete)
                    navigateToAssetHub()
                }
                WorkInfo.State.FAILED -> {
                    tvStatus.text = getString(R.string.scan_failed_continuing)
                    Toast.makeText(this, getString(R.string.scan_failed_continuing), Toast.LENGTH_LONG).show()
                    navigateToAssetHub()
                }
                else -> {}
            }
        }
    }

    private fun navigateToAssetHub() {
        if (!isFinishing && !hasNavigated) {
            hasNavigated = true
            startActivity(Intent(this, DigitalAssetHubActivity::class.java))
            finish()
        }
    }
}
