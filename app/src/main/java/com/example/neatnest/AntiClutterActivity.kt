package com.example.neatnest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AntiClutterActivity : AppCompatActivity() {

    private val PICK_IMAGE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anti_clutter)

        val btnPick = findViewById<Button>(R.id.btnPickImage)

        btnPick.setOnClickListener {
            val pickIntent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(pickIntent, PICK_IMAGE)
        }
    }
}
