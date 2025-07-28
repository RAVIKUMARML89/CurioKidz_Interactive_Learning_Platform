package com.example.curiokids

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class InteractiveLearningActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interactive_learning)

        // Enable the back button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, InteractiveLearningFragment())
                .commit()
        }
    }

    // Handle back button press in the action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Override onBackPressed to ensure it finishes this activity and goes back to MainActivity
    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}
