package com.example.neatnest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // animate logo fade-in on load
        val logo = findViewById<ImageView>(R.id.ivLogo)
        val fadeScaleIn = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in)
        logo.startAnimation(fadeScaleIn)

        // loading delay of 2 seconds before moving to the main app flow
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }, 2000)
    }
}

