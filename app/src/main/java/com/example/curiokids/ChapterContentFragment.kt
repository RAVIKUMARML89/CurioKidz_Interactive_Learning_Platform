package com.example.curiokids

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.curiokids.data.ChapterItem

class ChapterContentFragment : Fragment() {
    private lateinit var contentText: TextView
    private lateinit var backButton: Button
    private lateinit var unitTitle: TextView
    private lateinit var chapterTitle: TextView
    private var videoPlaybackListener: VideoPlaybackListener? = null

    // Interface for delegating video playback to the activity
    interface VideoPlaybackListener {
        fun playRegularVideo(filePath: String)
        fun playAiVideo(filePath: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Check if the activity implements our interface
        if (context is VideoPlaybackListener) {
            videoPlaybackListener = context
        } else {
// Toast.makeText(context, "Host activity must implement VideoPlaybackListener", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_chapter_content, container, false)
        unitTitle = view.findViewById(R.id.unitTitle)
        chapterTitle = view.findViewById(R.id.chapterTitle)
        contentText = view.findViewById(R.id.contentText)
        backButton = view.findViewById(R.id.backButton)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fix to correctly retrieve ChapterItem from arguments
        val items = arguments?.getParcelableArrayList<ChapterItem>("chapterContent")
        val item = items?.firstOrNull()

        // Toast.makeText(context, "Item found: ${item != null}, has path: ${item?.file_path != null}", Toast.LENGTH_LONG)
          //  .show()

        val playVideo = view.findViewById<Button>(R.id.playTextBookVideo)
        val playAiContent = view.findViewById<Button>(R.id.playAiContent)

        // Set up content text and titles
        item?.let { chapterItem ->
            unitTitle.text = "📘 ${chapterItem.unit} - ${chapterItem.unit_name}"
            chapterTitle.text = "📖 ${chapterItem.chapter} - ${chapterItem.chapter_name}"
            contentText.text = chapterItem.content

            // Set up buttons to delegate video playback to the activity
            playVideo.setOnClickListener {
                if (chapterItem.file_path.isNotEmpty()) {
                    videoPlaybackListener?.playRegularVideo(chapterItem.file_path)
                } else {
                   // Toast.makeText(context, "No video available", Toast.LENGTH_SHORT).show()
                }
            }

            playAiContent.setOnClickListener {
                if (chapterItem.ai_file_path.isNotEmpty()) {
                    videoPlaybackListener?.playAiVideo(chapterItem.ai_file_path)
                } else {
                  //  Toast.makeText(context, "No AI content available", Toast.LENGTH_SHORT).show()
                }
            }

            backButton.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }
}

