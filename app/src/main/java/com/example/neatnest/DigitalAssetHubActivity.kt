package com.example.neatnest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.neatnest.ui.assethub.AssetHubViewModel
import com.example.neatnest.ui.common.UiState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DigitalAssetHubActivity : AppCompatActivity() {

    private val viewModel: AssetHubViewModel by viewModel()
    private lateinit var tvCurrentRoot: TextView
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageView
    private lateinit var assetAdapter: ProcessedFilesAdapter
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var rvFolders: RecyclerView
    private lateinit var rvAssetList: RecyclerView
    private lateinit var tvNoAssets: TextView
    private var rootUri: Uri? = null
    private var currentCategory: String? = null  // null = folders, non-null = file list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digital_asset_hub)

        tvCurrentRoot = findViewById(R.id.tvCurrentRoot)
        tvTitle = findViewById(R.id.tvTitle)
        btnBack = findViewById(R.id.btnBack)
        val btnViewRoot = findViewById<Button>(R.id.btnViewRoot)
        val btnReset = findViewById<Button>(R.id.btnReset)
        rvFolders = findViewById(R.id.rvFolders)
        rvAssetList = findViewById(R.id.rvAssetList)
        tvNoAssets = findViewById(R.id.tvNoAssets)

        // entrance animation
        val headerAnim = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in)
        tvTitle.startAnimation(headerAnim)

        // load root uri
        viewModel.getRootUri()?.let {
            rootUri = it.toUri()
            tvCurrentRoot.text = getString(R.string.root_path_display, rootUri?.path)
        }

        // folder cards
        folderAdapter = FolderAdapter { category ->
            showFilesForCategory(category)
        }
        rvFolders.layoutManager = LinearLayoutManager(this)
        rvFolders.adapter = folderAdapter

        // file list
        assetAdapter = ProcessedFilesAdapter { file ->
            showFileInfoDialog(file)
        }
        rvAssetList.layoutManager = LinearLayoutManager(this)
        rvAssetList.adapter = assetAdapter

        // observe categories
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { cats ->
                    if (currentCategory == null) {
                        folderAdapter.submitList(cats)
                        if (cats.isEmpty()) {
                            tvNoAssets.visibility = View.VISIBLE
                            rvFolders.visibility = View.GONE
                        } else {
                            tvNoAssets.visibility = View.GONE
                            rvFolders.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        // observe file list
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (currentCategory != null) {
                        when (state) {
                            is UiState.Loading -> {
                                rvAssetList.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                val files = state.data
                                assetAdapter.submitList(files)
                                if (files.isEmpty()) {
                                    tvNoAssets.visibility = View.VISIBLE
                                    rvAssetList.visibility = View.GONE
                                } else {
                                    tvNoAssets.visibility = View.GONE
                                    rvAssetList.visibility = View.VISIBLE
                                }
                            }
                            is UiState.Error -> {
                                tvNoAssets.visibility = View.VISIBLE
                                rvAssetList.visibility = View.GONE
                                Toast.makeText(this@DigitalAssetHubActivity, state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        // back button → folder view
        btnBack.setOnClickListener { showFolderView() }

        btnViewRoot.setOnClickListener { openRootFolder() }
        btnReset.setOnClickListener { showResyncConfirmationDialog() }
    }

    private fun showFilesForCategory(category: String) {
        currentCategory = category
        tvTitle.text = category
        btnBack.visibility = View.VISIBLE
        rvFolders.visibility = View.GONE
        rvAssetList.visibility = View.VISIBLE

        // animate transition
        rvAssetList.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right))

        viewModel.loadFilesByCategory(category)
    }

    private fun showFolderView() {
        currentCategory = null
        tvTitle.text = getString(R.string.digital_asset_hub_title)
        btnBack.visibility = View.GONE
        rvAssetList.visibility = View.GONE
        rvFolders.visibility = View.VISIBLE
        tvNoAssets.visibility = View.GONE

        // animate back
        rvFolders.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left))
    }

    private fun openRootFolder() {
        rootUri?.let {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(it, "vnd.android.document/directory")
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(this, getString(R.string.no_file_manager), Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, getString(R.string.root_not_set), Toast.LENGTH_SHORT).show()
    }

    private fun showFileInfoDialog(file: ProcessedFile) {
        val fmt = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        AlertDialog.Builder(this)
            .setTitle(file.fileName)
            .setMessage(
                getString(R.string.file_info_extension, file.extension.uppercase()) + "\n" +
                getString(R.string.file_info_original, file.originalUri) + "\n" +
                getString(R.string.file_info_target, file.targetPath) + "\n" +
                getString(R.string.file_info_processed, fmt.format(Date(file.timestamp))) + "\n" +
                "Engine: ${file.engineUsed}" + "\n" +
                "Category: ${file.category}"
            )
            .setPositiveButton(getString(R.string.dialog_ok), null)
            .show()
    }

    private fun showResyncConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Re-sync & Restore?")
            .setMessage("This will move all classified files back to their original locations, empty the root directory, and restart onboarding from scratch.\n\nThis cannot be undone.")
            .setPositiveButton("Yes, Re-sync") { _, _ ->
                initiateResync()
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun initiateResync() {
        val workManager = WorkManager.getInstance(this)
        val resetRequest = OneTimeWorkRequestBuilder<ResetWorker>().build()
        workManager.enqueue(resetRequest)

        workManager.getWorkInfoByIdLiveData(resetRequest.id).observe(this) { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> {
                    Toast.makeText(this, "Restoring files to original locations...", Toast.LENGTH_SHORT).show()
                }
                WorkInfo.State.SUCCEEDED -> {
                    Toast.makeText(this, "Re-sync complete. Starting fresh setup.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, OnboardingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                WorkInfo.State.FAILED -> {
                    Toast.makeText(this, getString(R.string.reset_failed), Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    @Deprecated("Use onBackPressedDispatcher instead")
    override fun onBackPressed() {
        if (currentCategory != null) {
            showFolderView()
        } else {
            super.onBackPressed()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
