package com.example.curiokids.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChapterItem(
    val type: String,
    val unit: String,
    val unit_name: String,
    val chapter: String,
    val chapter_name: String,
    val content: String,
    val file_path: String,
    val ai_file_path: String
): Parcelable

