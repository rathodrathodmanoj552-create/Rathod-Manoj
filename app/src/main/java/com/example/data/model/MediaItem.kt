package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MediaType {
    PHOTO,
    VIDEO,
    DOCUMENT,
    ARCHIVE,
    OTHER
}

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val type: MediaType,
    val sizeBytes: Long,
    val originalSizeBytes: Long = sizeBytes,
    val mimeType: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val tags: String = "",
    val aiAnalysis: String? = null,
    val isCompressed: Boolean = false,
    val compressionRatio: Float = 1.0f,
    val width: Int = 0,
    val height: Int = 0,
    val durationMs: Long = 0,
    val sharedFolderId: Long? = null
) {
    val spaceSavedBytes: Long
        get() = if (originalSizeBytes > sizeBytes) originalSizeBytes - sizeBytes else 0L

    val spaceSavedPercent: Int
        get() = if (originalSizeBytes > 0 && originalSizeBytes > sizeBytes) {
            (((originalSizeBytes - sizeBytes).toDouble() / originalSizeBytes) * 100).toInt()
        } else 0
}
