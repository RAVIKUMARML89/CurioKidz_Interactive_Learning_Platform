package com.example.curiokids

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.curiokids.data.ChapterItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class InteractiveLearningFragment : Fragment() {
    private lateinit var container: LinearLayout
    private var fullData: List<ChapterItem> = emptyList()
    private var filteredData: List<ChapterItem> = emptyList()

    private var viewState: ViewState = ViewState.TYPE
    private var selectedType: String? = null
    private var selectedUnit: String? = null
    private var selectedChapter: String? = null

    enum class ViewState {
        TYPE, UNIT, CHAPTER, CONTENT
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_interactive_learning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            container = view.findViewById(R.id.buttonContainer)
            loadLocalJsonData()
        } catch (e: Exception) {
            Log.e("InteractiveLearning", "Error in onViewCreated", e)
            // Toast.makeText(context, "Failed to initialize: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLocalJsonData() {
        try {
            val inputStream = requireContext().assets.open("data.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<ChapterItem>>() {}.type
            fullData = Gson().fromJson(json, listType)
            renderTypeSelection()
        } catch (e: IOException) {
            Log.e("InteractiveLearning", "Failed to load data.json", e)
           // Toast.makeText(context, "Failed to load content: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            // Show some dummy data so the app doesn't crash
            showDummyData()
        } catch (e: Exception) {
            Log.e("InteractiveLearning", "Error parsing JSON", e)
           // Toast.makeText(context, "Error processing content: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            showDummyData()
        }
    }

    private fun showDummyData() {
        // Provide some fallback data so the app doesn't crash
        container.removeAllViews()
        addButton("Sample Content (Data unavailable)") {
           // Toast.makeText(context, "Sample content selected", Toast.LENGTH_SHORT).show()
        }
        addButton("⬅ Back") {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun renderTypeSelection() {
        viewState = ViewState.TYPE
        container.removeAllViews()
        val types = fullData.map { it.type }.distinct()
        if (types.isEmpty()) {
            addButton("No content types found") {}
        } else {
            types.forEach { type ->
                addButton(type) {
                    selectedType = type
                    filteredData = fullData.filter { it.type == type }
                    renderUnitSelection()
                }
            }
        }
        addButton("⬅ Back to Home") {
            requireActivity().finish() // This will properly return to the MainActivity
        }
    }

    private fun renderUnitSelection() {
        viewState = ViewState.UNIT
        container.removeAllViews()

        val units = filteredData.map { it.unit_name }.distinct()
        if (units.isEmpty()) {
            addButton("No units found") {}
        } else {
            units.forEach { unitName ->
                val label = filteredData.find { it.unit_name == unitName }
                addButton("${label?.unit}: $unitName") {
                    selectedUnit = unitName
                    renderChapterSelection()
                }
            }
        }
        addButton("⬅ Back to Type") { renderTypeSelection() }
    }

    private fun renderChapterSelection() {
        viewState = ViewState.CHAPTER
        container.removeAllViews()

        val chapters = filteredData
            .filter { it.unit_name == selectedUnit }
            .map { it.chapter_name }
            .distinct()

        if (chapters.isEmpty()) {
            addButton("No chapters found") {}
        } else {
            chapters.forEach { chapterName ->
                val chapter = filteredData.find { it.unit_name == selectedUnit && it.chapter_name == chapterName }
                addButton("${chapter?.chapter}: $chapterName") {
                    selectedChapter = chapterName
                    val selectedItem = filteredData.find {
                        it.unit_name == selectedUnit && it.chapter_name == selectedChapter
                    }
                    selectedItem?.let {
                        val intent = android.content.Intent(requireContext(), ChapterContentActivity::class.java)
                        intent.putExtra("chapterItem", it)
                        intent.putExtra("language", "en")  // Default language
                        startActivity(intent)
                    }
                }
            }
        }
        addButton("⬅ Back to Unit") { renderUnitSelection() }
    }

    private fun addButton(text: String, onClick: () -> Unit) {
        val button = Button(requireContext()).apply {
            this.text = text
            setPadding(24, 16, 24, 16)
            setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
            setTextColor(Color.BLACK)
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
            setOnClickListener {
                MediaPlayer.create(context, R.raw.click).start()
                onClick()
            }
        }
        container.addView(button)
    }
}

