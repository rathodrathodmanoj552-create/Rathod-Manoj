package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compression_records")
data class CompressionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val format: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val spaceSavedBytes: Long
        get() = if (originalSizeBytes > compressedSizeBytes) originalSizeBytes - compressedSizeBytes else 0L

    val savedPercentage: Int
        get() = if (originalSizeBytes > 0 && originalSizeBytes > compressedSizeBytes) {
            (((originalSizeBytes - compressedSizeBytes).toDouble() / originalSizeBytes) * 100).toInt()
        } else 0
}
