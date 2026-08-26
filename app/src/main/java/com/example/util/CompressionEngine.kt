package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.data.model.CompressionRecord
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

enum class ImageCompressFormat(val displayName: String, val extension: String, val bitmapFormat: Bitmap.CompressFormat) {
    WEBP_LOSSY("WEBP (Maximum Savings)", "webp", Bitmap.CompressFormat.WEBP),
    JPEG("JPEG (Balanced Quality)", "jpg", Bitmap.CompressFormat.JPEG),
    PNG("PNG (Crisp Lossless)", "png", Bitmap.CompressFormat.PNG)
}

data class CompressionResult(
    val success: Boolean,
    val outputFile: File?,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val savedBytes: Long,
    val savedPercentage: Int,
    val durationMs: Long,
    val newMediaItem: MediaItem? = null,
    val error: String? = null
)

object CompressionEngine {

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val df = DecimalFormat("#,##0.#")
        return "${df.format(bytes / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
    }

    suspend fun compressImage(
        context: Context,
        inputFile: File,
        targetFormat: ImageCompressFormat = ImageCompressFormat.WEBP_LOSSY,
        quality: Int = 80,
        scaleFactor: Float = 1.0f
    ): CompressionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val originalSize = inputFile.length()
            val originalBitmap = BitmapFactory.decodeFile(inputFile.absolutePath)
                ?: return@withContext CompressionResult(false, null, originalSize, 0, 0, 0, 0, error = "Failed to decode image file")

            val targetWidth = (originalBitmap.width * scaleFactor).toInt().coerceAtLeast(1)
            val targetHeight = (originalBitmap.height * scaleFactor).toInt().coerceAtLeast(1)

            val scaledBitmap = if (scaleFactor < 0.99f || scaleFactor > 1.01f) {
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            } else {
                originalBitmap
            }

            val baseName = inputFile.nameWithoutExtension
            val outputFileName = "${baseName}_compressed_${System.currentTimeMillis()}.${targetFormat.extension}"
            val outputFile = File(StorageManager.getVaultDir(context), outputFileName)

            FileOutputStream(outputFile).use { out ->
                scaledBitmap.compress(targetFormat.bitmapFormat, quality.coerceIn(1, 100), out)
            }

            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()

            val compressedSize = outputFile.length()
            val savedBytes = (originalSize - compressedSize).coerceAtLeast(0)
            val savedPercent = if (originalSize > 0) (((originalSize - compressedSize).toDouble() / originalSize) * 100).toInt().coerceAtLeast(0) else 0
            val duration = System.currentTimeMillis() - startTime

            val mediaItem = MediaItem(
                title = outputFileName,
                filePath = outputFile.absolutePath,
                type = MediaType.PHOTO,
                sizeBytes = compressedSize,
                originalSizeBytes = originalSize,
                mimeType = "image/${targetFormat.extension}",
                dateAdded = System.currentTimeMillis(),
                isCompressed = true,
                compressionRatio = if (originalSize > 0) compressedSize.toFloat() / originalSize.toFloat() else 1f,
                tags = "Compressed, ${targetFormat.displayName}, Web-Squeezed",
                width = targetWidth,
                height = targetHeight
            )

            CompressionResult(
                success = true,
                outputFile = outputFile,
                originalSizeBytes = originalSize,
                compressedSizeBytes = compressedSize,
                savedBytes = savedBytes,
                savedPercentage = savedPercent,
                durationMs = duration,
                newMediaItem = mediaItem
            )
        } catch (e: Exception) {
            e.printStackTrace()
            CompressionResult(false, null, inputFile.length(), 0, 0, 0, System.currentTimeMillis() - startTime, error = e.localizedMessage)
        }
    }

    suspend fun compressFilesToZip(
        context: Context,
        inputFiles: List<File>,
        archiveName: String
    ): CompressionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val totalOriginalSize = inputFiles.sumOf { it.length() }
            val cleanName = if (archiveName.endsWith(".zip")) archiveName else "$archiveName.zip"
            val outputFile = File(StorageManager.getVaultDir(context), "${System.currentTimeMillis()}_$cleanName")

            ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
                val buffer = ByteArray(8192)
                for (file in inputFiles) {
                    if (file.exists() && file.isFile) {
                        val entry = ZipEntry(file.name)
                        zos.putNextEntry(entry)
                        FileInputStream(file).use { fis ->
                            var count: Int
                            while (fis.read(buffer).also { count = it } > 0) {
                                zos.write(buffer, 0, count)
                            }
                        }
                        zos.closeEntry()
                    }
                }
            }

            val compressedSize = outputFile.length()
            val savedBytes = (totalOriginalSize - compressedSize).coerceAtLeast(0)
            val savedPercent = if (totalOriginalSize > 0) (((totalOriginalSize - compressedSize).toDouble() / totalOriginalSize) * 100).toInt().coerceAtLeast(0) else 0

            val mediaItem = MediaItem(
                title = cleanName,
                filePath = outputFile.absolutePath,
                type = MediaType.ARCHIVE,
                sizeBytes = compressedSize,
                originalSizeBytes = totalOriginalSize,
                mimeType = "application/zip",
                dateAdded = System.currentTimeMillis(),
                isCompressed = true,
                tags = "Archive, Zip, Compressed, Miracle-Vault"
            )

            CompressionResult(
                success = true,
                outputFile = outputFile,
                originalSizeBytes = totalOriginalSize,
                compressedSizeBytes = compressedSize,
                savedBytes = savedBytes,
                savedPercentage = savedPercent,
                durationMs = System.currentTimeMillis() - startTime,
                newMediaItem = mediaItem
            )
        } catch (e: Exception) {
            e.printStackTrace()
            CompressionResult(false, null, 0, 0, 0, 0, 0, error = e.localizedMessage)
        }
    }

    suspend fun extractZipArchive(
        context: Context,
        zipFile: File
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val extractedItems = mutableListOf<MediaItem>()
        try {
            val destDir = StorageManager.getVaultDir(context)
            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                val buffer = ByteArray(8192)
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val outFile = File(destDir, "extracted_${System.currentTimeMillis()}_${entry.name}")
                        FileOutputStream(outFile).use { fos ->
                            var count: Int
                            while (zis.read(buffer).also { count = it } > 0) {
                                fos.write(buffer, 0, count)
                            }
                        }
                        extractedItems.add(
                            MediaItem(
                                title = entry.name,
                                filePath = outFile.absolutePath,
                                type = MediaType.DOCUMENT,
                                sizeBytes = outFile.length(),
                                originalSizeBytes = outFile.length(),
                                mimeType = "application/octet-stream",
                                tags = "Extracted, Zip-Unpacked"
                            )
                        )
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        extractedItems
    }
}
