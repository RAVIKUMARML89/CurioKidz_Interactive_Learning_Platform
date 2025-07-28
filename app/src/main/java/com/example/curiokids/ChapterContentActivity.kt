package com.example.curiokids

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.curiokids.data.ChapterItem
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ChapterContentActivity : AppCompatActivity(), TextToSpeech.OnInitListener, ChapterContentFragment.VideoPlaybackListener {

    private lateinit var tts: TextToSpeech
    private lateinit var contentText: TextView
    private lateinit var backButton: Button
    private lateinit var unitTitle: TextView
    private lateinit var chapterTitle: TextView
    private lateinit var videoView: android.widget.VideoView
    private lateinit var pausePlayButton: Button
    private lateinit var speedSpinner: Spinner
    private lateinit var videoControlPanel: LinearLayout

    private var currentItem: ChapterItem? = null
    private var languageCode: String = "en"
    private val videoQueue = LinkedList<String>()
    private var currentSpeed = 1.0f
    private val speedOptions = arrayOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)
    private val speedLabels = arrayOf("0.5x", "1.0x", "1.25x", "1.5x", "2.0x")
    private var currentSpeedIndex = 1 // Start with 1.0x speed

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter_content)

        // Get the ChapterItem from intent using the updated API
        currentItem = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("chapterItem", ChapterItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("chapterItem")
        }
        languageCode = intent.getStringExtra("language") ?: "en"

        tts = TextToSpeech(this, this)

        // Initialize views
        unitTitle = findViewById(R.id.unitTitle)
        chapterTitle = findViewById(R.id.chapterTitle)
        contentText = findViewById(R.id.contentText)
        backButton = findViewById(R.id.backButton)
        videoView = findViewById(R.id.textBookVideo)
        pausePlayButton = findViewById(R.id.pausePlayButton)
        speedSpinner = findViewById(R.id.speedSpinner)
        videoControlPanel = findViewById(R.id.videoControlPanel)

        // Setup video completion listener to play next video in queue
        videoView.setOnCompletionListener {
            playNextVideoInQueue()
        }

        // Setup video control buttons
        setupVideoControls()

        // Add click listener to stop video when user taps on the video screen
        videoView.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.stopPlayback()
                videoView.visibility = View.GONE
                videoControlPanel.visibility = View.GONE
                videoQueue.clear() // Clear any remaining videos in queue
                // Toast.makeText(this, "Video stopped", Toast.LENGTH_SHORT).show()
            }
        }

        currentItem?.let { item ->
            unitTitle.text = "📘 ${item.unit} - ${item.unit_name}"
            chapterTitle.text = "📖 ${item.chapter} - ${item.chapter_name}"
            contentText.text = item.content

            val playButton: Button = findViewById(R.id.playTextBookVideo)
            val playAiButton: Button = findViewById(R.id.playAiContent)

            // Regular video play button
            playButton.setOnClickListener {
                playRegularVideo(item.file_path)
            }

            // AI content play button
            playAiButton.setOnClickListener {
                playAiVideo(item.ai_file_path)
            }

            // Set a proper onClickListener for the back button
            backButton.setOnClickListener {
                onBackPressed() // This will properly handle the back navigation
            }
        } ?: null
       // Toast.makeText(this, "No content found", Toast.LENGTH_SHORT).show()
    }

    private fun setupVideoControls() {
        pausePlayButton.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                pausePlayButton.text = "▶️"
            } else {
                videoView.start()
                pausePlayButton.text = "⏸️"
            }
        }

        // Setup speed spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, speedLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        speedSpinner.adapter = adapter
        speedSpinner.setSelection(currentSpeedIndex)

        speedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentSpeedIndex = position
                currentSpeed = speedOptions[currentSpeedIndex]
                applyPlaybackSpeed()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun applyPlaybackSpeed() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (videoView.isPlaying) {
                    // For videos that are currently playing, we need to access the MediaPlayer directly
                    val mediaPlayer = getVideoViewMediaPlayer()
                    mediaPlayer?.let { mp ->
                        mp.playbackParams = mp.playbackParams.setSpeed(currentSpeed)
                    }
                }
                // Also set for future playback
                videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(currentSpeed)
                }
            } else {
                Toast.makeText(this, "Speed control requires Android 6.0+", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("SPEED_CONTROL", "Error applying playback speed: ${e.message}")
            Toast.makeText(this, "Speed control not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getVideoViewMediaPlayer(): android.media.MediaPlayer? {
        return try {
            val mediaPlayerField = android.widget.VideoView::class.java.getDeclaredField("mMediaPlayer")
            mediaPlayerField.isAccessible = true
            mediaPlayerField.get(videoView) as? android.media.MediaPlayer
        } catch (e: Exception) {
            android.util.Log.e("SPEED_CONTROL", "Could not access MediaPlayer: ${e.message}")
            null
        }
    }

    private fun showVideoControls() {
        videoControlPanel.visibility = View.VISIBLE
        pausePlayButton.text = "⏸️" // Show pause button when video is playing
        speedSpinner.setSelection(currentSpeedIndex)
    }

    private fun hideVideoControls() {
        videoControlPanel.visibility = View.GONE
    }

    private fun playNextVideoInQueue() {
        if (videoQueue.isEmpty()) {
            // No more videos to play
            videoView.visibility = View.GONE
            return
        }

        // Use ?: to provide a default value in case poll() returns null
        val nextVideo = videoQueue.poll() ?: return
        // Toast.makeText(this, "Playing: $nextVideo", Toast.LENGTH_SHORT).show()
        playAssetVideo(nextVideo)
    }

    private fun playAssetVideo(assetPath: String) {
        try {
            // Make the VideoView visible
            videoView.visibility = View.VISIBLE

            // Show available assets for debugging
            val TAG = "CURIOKIDS_VIDEO"
            android.util.Log.d(TAG, "Attempting to play: $assetPath")
            // Toast.makeText(this, "Attempting to play: $assetPath", Toast.LENGTH_LONG).show()

            // Add "videos/" prefix if it's not already there and not a raw resource path
            val fullAssetPath = when {
                assetPath.startsWith("db/videos/") -> assetPath
                assetPath.startsWith("raw/") -> assetPath
                else -> "videos/$assetPath"
            }

            android.util.Log.d(TAG, "Full asset path: $fullAssetPath")

            if (fullAssetPath.startsWith("raw/")) {
                // Handle raw resource path
                val videoName = fullAssetPath.substringAfter("raw/").substringBeforeLast(".")
                android.util.Log.d(TAG, "Video resource name: $videoName")
                val resourceId = resources.getIdentifier(videoName, "raw", packageName)

                if (resourceId != 0) {
                    val videoUri = Uri.parse("android.resource://$packageName/$resourceId")
                    videoView.setVideoURI(videoUri)
                    videoView.start()
                    showVideoControls()
                } else {
                  //  Toast.makeText(this, "Video resource not found: $videoName", Toast.LENGTH_SHORT).show()
                    playNextVideoInQueue()
                }
            } else {
                // Check if the asset exists before trying to open it
                var assetExists = false
                try {
                    val assetFd = assets.openFd(fullAssetPath)
                    assetFd.close()
                    assetExists = true
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Asset not found: $fullAssetPath, error: ${e.message}")
                   // Toast.makeText(this, "Video not found: $fullAssetPath", Toast.LENGTH_LONG).show()
                    playNextVideoInQueue()
                }

                if (assetExists) {
                    // Create a temporary file to store the video
                    val tempFile = File.createTempFile("video", ".mp4", cacheDir)
                    val outputStream = FileOutputStream(tempFile)

                    // Copy the asset to the temporary file
                    assets.open(fullAssetPath).use { input ->
                        input.copyTo(outputStream)
                    }
                    outputStream.close()

                    android.util.Log.d(TAG, "Video copied to: ${tempFile.absolutePath}")
                   // Toast.makeText(this, "Video loaded successfully!", Toast.LENGTH_SHORT).show()

                    // Set up and play the video
                    videoView.setVideoPath(tempFile.absolutePath)
                    videoView.requestFocus()
                    videoView.start()
                    showVideoControls()

                    // Add error listeners
                    videoView.setOnErrorListener { mp, what, extra ->
                        android.util.Log.e(TAG, "Video error: what=$what, extra=$extra")
                       // Toast.makeText(this, "Error playing video: $what", Toast.LENGTH_LONG).show()
                        playNextVideoInQueue()
                        true
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CURIOKIDS_VIDEO_ERROR", "Failed to play video: ${e.message}", e)
           // Toast.makeText(this, "Failed to play video: ${e.message}", Toast.LENGTH_LONG).show()
            videoView.visibility = View.GONE
            playNextVideoInQueue()
        }
    }

    // Implementation of VideoPlaybackListener interface methods
    override fun playRegularVideo(filePath: String) {
        if (filePath.isNotEmpty()) {
            videoQueue.clear()
            videoQueue.add(filePath)
            playNextVideoInQueue()
        } else {
          //  Toast.makeText(this, "No video available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun playAiVideo(filePath: String) {
        if (filePath.isNotEmpty()) {
            videoQueue.clear()
            videoQueue.add(filePath)
            playNextVideoInQueue()
        } else {
            // Toast.makeText(this, "No AI content available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale(languageCode)
        } else {
           //  Toast.makeText(this, "Text-to-Speech init failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        videoView.stopPlayback()
        tts.shutdown()
        super.onDestroy()
    }
}
