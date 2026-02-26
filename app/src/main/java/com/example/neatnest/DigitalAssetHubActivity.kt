package com.example.neatnest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DigitalAssetHubActivity : AppCompatActivity() {

    private val viewModel: AssetHubViewModel by viewModel()
    private lateinit var tvCurrentRoot: TextView
    private lateinit var assetAdapter: ProcessedFilesAdapter
    private var rootUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digital_asset_hub)

        tvCurrentRoot = findViewById(R.id.tvCurrentRoot)
        val btnViewRoot = findViewById<Button>(R.id.btnViewRoot)
        val btnReset = findViewById<Button>(R.id.btnReset)
        val rvAssetList = findViewById<RecyclerView>(R.id.rvAssetList)
        val tvNoAssets = findViewById<TextView>(R.id.tvNoAssets)
        val fabViewRoot = findViewById<FloatingActionButton>(R.id.fabViewRoot)

        // load root uri from viewmodel
        viewModel.getRootUri()?.let {
            rootUri = it.toUri()
            tvCurrentRoot.text = getString(R.string.root_path_display, rootUri?.path)
        }

        // recyclerview for processed files
        assetAdapter = ProcessedFilesAdapter { file ->
            showFileInfoDialog(file)
        }
        rvAssetList.layoutManager = LinearLayoutManager(this)
        rvAssetList.adapter = assetAdapter

        // observe ui state from viewmodel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            tvNoAssets.visibility = View.VISIBLE
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

        btnViewRoot.setOnClickListener { openRootFolder() }
        fabViewRoot.setOnClickListener { openRootFolder() }
        btnReset.setOnClickListener { showResetConfirmationDialog() }
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
                getString(R.string.file_info_processed, fmt.format(Date(file.timestamp)))
            )
            .setPositiveButton(getString(R.string.dialog_ok), null)
            .show()
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reset_dialog_title))
            .setMessage(getString(R.string.reset_dialog_message))
            .setPositiveButton(getString(R.string.reset_confirm)) { _, _ ->
                initiateReset()
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun initiateReset() {
        val workManager = WorkManager.getInstance(this)
        val resetRequest = OneTimeWorkRequestBuilder<ResetWorker>().build()
        workManager.enqueue(resetRequest)

        workManager.getWorkInfoByIdLiveData(resetRequest.id).observe(this) { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> {
                    Toast.makeText(this, getString(R.string.resetting), Toast.LENGTH_SHORT).show()
                }
                WorkInfo.State.SUCCEEDED -> {
                    Toast.makeText(this, getString(R.string.reset_complete), Toast.LENGTH_LONG).show()
                    val intent = Intent(this, OnboardingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                WorkInfo.State.FAILED -> {
                    Toast.makeText(this, getString(R.string.reset_failed), Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}
