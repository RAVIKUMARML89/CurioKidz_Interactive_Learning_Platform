package com.example.curiokids

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var menuPanel: LinearLayout
    private lateinit var audio: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        menuPanel = findViewById(R.id.menuPanel)
        val learningButton = findViewById<Button>(R.id.learningButton)
        val trainerButton = findViewById<Button>(R.id.trainerButton)
        val backButton = findViewById<Button>(R.id.backButton)

        audio = MediaPlayer.create(this, R.raw.click)

        // Show the logo initially and display menu after a short delay
        logoImageView.alpha = 0f
        logoImageView.animate().alpha(1f).setDuration(1000).start()

        logoImageView.postDelayed({
            menuPanel.visibility = LinearLayout.VISIBLE
        }, 1500)

        learningButton.setOnClickListener {
            audio.start()
            try {
                val intent = Intent()
                intent.setClassName(packageName, "com.example.curiokids.InteractiveLearningActivity")
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback
                val intent = Intent(this, InteractiveLearningActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        trainerButton.setOnClickListener {
            audio.start()
            // Add your intent or logic here
        }
        backButton.setOnClickListener {
            audio.start()
            // Add your intent or logic here
        }
    }
}

