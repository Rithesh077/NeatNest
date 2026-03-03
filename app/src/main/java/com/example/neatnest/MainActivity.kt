package com.example.neatnest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

// main hub — simple launcher for the 4 core features
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cardAssetHub = findViewById<MaterialCardView>(R.id.cardAssetHub)
        val cardSignalCleaner = findViewById<MaterialCardView>(R.id.cardSignalCleaner)
        val cardDevMode = findViewById<MaterialCardView>(R.id.cardDevMode)
        val cardUtilityHub = findViewById<MaterialCardView>(R.id.cardUtilityHub)

        // staggered card entrance animations
        animateCardsEntrance(cardAssetHub, cardSignalCleaner, cardDevMode, cardUtilityHub)

        // logo fade-in
        val ivLogo = findViewById<View>(R.id.ivLogo)
        ivLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in))

        // navigation handlers
        cardAssetHub.setOnClickListener {
            // check if onboarding is done; if not, go to onboarding first
            val prefs = getSharedPreferences("NeatNestPrefs", MODE_PRIVATE)
            val onboarded = prefs.getBoolean("onboarding_completed", false)
            if (onboarded) {
                startActivity(Intent(this, DigitalAssetHubActivity::class.java))
            } else {
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        cardSignalCleaner.setOnClickListener {
            startActivity(Intent(this, SignalNoiseCleanerActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        cardDevMode.setOnClickListener {
            startActivity(Intent(this, FileMoverActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        cardUtilityHub.setOnClickListener {
            startActivity(Intent(this, UtilityHubActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    // staggered fade-scale entrance for dashboard cards
    private fun animateCardsEntrance(vararg cards: View) {
        val anim = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in)
        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            card.postDelayed({
                card.alpha = 1f
                card.startAnimation(anim)
            }, (index * 100).toLong())
        }
    }
}
