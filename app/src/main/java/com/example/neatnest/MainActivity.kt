package com.example.neatnest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val TAG = "Lifecycle-Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        Toast.makeText(this, "MainActivity: onCreate", Toast.LENGTH_SHORT).show()
        setContentView(R.layout.activity_main)

        val btnStudy = findViewById<Button>(R.id.btnStudyCollector)
        val btnMsg = findViewById<Button>(R.id.btnMessageCleaner)
        val btnClutter = findViewById<Button>(R.id.btnAntiClutter)

        btnStudy.setOnClickListener {
            startActivity(Intent(this, StudyCollectorActivity::class.java))
        }

        btnMsg.setOnClickListener {
            startActivity(Intent(this, MessageCleanerActivity::class.java))
        }

        btnClutter.setOnClickListener {
            startActivity(Intent(this, AntiClutterActivity::class.java))
        }
    }

    override fun onStart() { super.onStart(); Log.d(TAG,"onStart"); Toast.makeText(this,"onStart",Toast.LENGTH_SHORT).show() }
    override fun onResume() { super.onResume(); Log.d(TAG,"onResume"); Toast.makeText(this,"onResume",Toast.LENGTH_SHORT).show() }
    override fun onPause() { super.onPause(); Log.d(TAG,"onPause"); }
    override fun onStop() { super.onStop(); Log.d(TAG,"onStop"); }
    override fun onRestart() { super.onRestart(); Log.d(TAG,"onRestart"); }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG,"onDestroy"); }
}
