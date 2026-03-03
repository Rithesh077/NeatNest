package com.example.neatnest

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

// utility hub with placeholder tools
class UtilityHubActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utility_hub)

        val btnBack = findViewById<android.widget.ImageView>(R.id.btnBack)
        val cardVideoEditor = findViewById<MaterialCardView>(R.id.cardVideoEditor)
        val cardFileEditor = findViewById<MaterialCardView>(R.id.cardFileEditor)
        val cardDataExtractor = findViewById<MaterialCardView>(R.id.cardDataExtractor)
        val cardPriceTracker = findViewById<MaterialCardView>(R.id.cardPriceTracker)

        // staggered card entrance
        val cards = listOf(cardVideoEditor, cardFileEditor, cardDataExtractor, cardPriceTracker)
        val anim = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in)
        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            card.postDelayed({
                card.alpha = 1f
                card.startAnimation(anim)
            }, (index * 80).toLong())
        }

        btnBack.setOnClickListener { finish() }

        cardVideoEditor.setOnClickListener {
            Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        }
        cardFileEditor.setOnClickListener {
            Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        }
        cardDataExtractor.setOnClickListener {
            Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        }
        cardPriceTracker.setOnClickListener {
            Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
