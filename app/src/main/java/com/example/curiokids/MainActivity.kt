package com.example.curiokids

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    private lateinit var menuPanel: LinearLayout
    private lateinit var logoImageView: ImageView
    private lateinit var learningButton: Button
    private lateinit var trainerButton: Button
    private lateinit var backButton: Button
    private lateinit var clickPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        logoImageView = findViewById(R.id.logoImageView)
        menuPanel = findViewById(R.id.menuPanel)
        learningButton = findViewById(R.id.learningButton)
        trainerButton = findViewById(R.id.trainerButton)
        backButton = findViewById(R.id.backButton)

        clickPlayer = MediaPlayer.create(this, R.raw.click)

        logoImageView.setOnClickListener {
            playClick()
            // Navigate directly to the InteractiveLearningActivity when logo is clicked
            val intent = android.content.Intent(this, InteractiveLearningActivity::class.java)
            startActivity(intent)
        }

        /*learningButton.setOnClickListener {
            playClick()
            showFragment(InteractiveLearningFragment())
        }*/

        backButton.setOnClickListener {
            playClick()
            supportFragmentManager.popBackStack()
            menuPanel.visibility = View.GONE
        }
    }

    private fun playClick() {
        if (clickPlayer.isPlaying) clickPlayer.seekTo(0)
        clickPlayer.start()
    }

    private fun showFragment(fragment: Fragment) {
        menuPanel.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        clickPlayer.release()
    }
}

