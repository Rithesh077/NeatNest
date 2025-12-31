package com.example.neatnest

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile

class DigitalAssetHubActivity : AppCompatActivity() {

    private lateinit var tvCurrentRoot: TextView
    private var rootUri: Uri? = null

    private val dirPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            rootUri = it
            // Persist permissions across reboots
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            
            // Save the root URI for the background worker to use
            val sharedPrefs = getSharedPreferences("NeatNestPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putString("root_uri", it.toString()).apply()
            
            tvCurrentRoot.text = "Root: ${it.path}"
            Toast.makeText(this, "Root Directory Set", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digital_asset_hub)

        tvCurrentRoot = findViewById(R.id.tvCurrentRoot)
        val btnSetup = findViewById<Button>(R.id.btnSetupStorage)

        // Load existing root URI if available
        val sharedPrefs = getSharedPreferences("NeatNestPrefs", Context.MODE_PRIVATE)
        val savedUriString = sharedPrefs.getString("root_uri", null)
        if (savedUriString != null) {
            rootUri = Uri.parse(savedUriString)
            tvCurrentRoot.text = "Root: ${rootUri?.path}"
        }

        btnSetup.setOnClickListener {
            dirPickerLauncher.launch(null)
        }
    }

    /**
     * Foundational logic to organize a file based on its extension.
     */
    private fun organizeFile(fileUri: Uri, fileName: String) {
        val root = rootUri?.let { DocumentFile.fromTreeUri(this, it) } ?: return
        
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension.isEmpty()) return

        var targetDir = root.findFile(extension)
        if (targetDir == null || !targetDir.isDirectory) {
            targetDir = root.createDirectory(extension)
        }

        targetDir?.let {
            // Actual file movement will be handled by a centralized utility
            // that uses contentResolver.openInputStream and targetDir.createFile
            Toast.makeText(this, "Organizing $fileName into /${extension}/", Toast.LENGTH_SHORT).show()
        }
    }
}
