package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object StorageManager {

    private const val VAULT_DIR = "miracle_vault"

    fun getVaultDir(context: Context): File {
        val dir = File(context.filesDir, VAULT_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun importUri(context: Context, uri: Uri): MediaItem? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            var fileName = "file_${System.currentTimeMillis()}"
            var fileSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            val mimeType = contentResolver.getType(uri) ?: getMimeTypeFromExtension(fileName)
            val mediaType = determineMediaType(mimeType, fileName)

            val vaultDir = getVaultDir(context)
            val targetFile = File(vaultDir, "${System.currentTimeMillis()}_$fileName")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (fileSize == 0L) {
                fileSize = targetFile.length()
            }

            var width = 0
            var height = 0
            if (mediaType == MediaType.PHOTO) {
                val bounds = ImageProcessingEngine.getImageDimensions(targetFile.absolutePath)
                width = bounds.first
                height = bounds.second
            }

            MediaItem(
                title = fileName,
                filePath = targetFile.absolutePath,
                type = mediaType,
                sizeBytes = targetFile.length(),
                originalSizeBytes = targetFile.length(),
                mimeType = mimeType,
                dateAdded = System.currentTimeMillis(),
                tags = defaultTagsForType(mediaType),
                width = width,
                height = height
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveBitmapToVault(
        context: Context,
        bitmap: Bitmap,
        baseName: String,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 90
    ): MediaItem = withContext(Dispatchers.IO) {
        val ext = when (format) {
            Bitmap.CompressFormat.PNG -> "png"
            Bitmap.CompressFormat.WEBP, Bitmap.CompressFormat.WEBP_LOSSY, Bitmap.CompressFormat.WEBP_LOSSLESS -> "webp"
            else -> "jpg"
        }
        val fileName = "${baseName}_${System.currentTimeMillis()}.$ext"
        val targetFile = File(getVaultDir(context), fileName)

        FileOutputStream(targetFile).use { out ->
            bitmap.compress(format, quality, out)
        }

        MediaItem(
            title = fileName,
            filePath = targetFile.absolutePath,
            type = MediaType.PHOTO,
            sizeBytes = targetFile.length(),
            originalSizeBytes = targetFile.length(),
            mimeType = "image/$ext",
            dateAdded = System.currentTimeMillis(),
            tags = "Edited, Spider-AI, Photo",
            width = bitmap.width,
            height = bitmap.height
        )
    }

    suspend fun createInitialSeedData(context: Context): List<MediaItem> = withContext(Dispatchers.IO) {
        val seedItems = mutableListOf<MediaItem>()
        val vaultDir = getVaultDir(context)

        // 1. Generate Spider-Man Hero Wallpaper (Photo)
        val heroFile = File(vaultDir, "spiderman_hero_wallpaper.png")
        if (!heroFile.exists()) {
            val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Background Spider Dark Navy to Red Gradient
            paint.color = AndroidColor.parseColor("#090D16")
            canvas.drawRect(0f, 0f, 1080f, 1920f, paint)

            // Web grid
            paint.color = AndroidColor.parseColor("#152036")
            paint.strokeWidth = 3f
            for (i in 0..1080 step 60) {
                canvas.drawLine(i.toFloat(), 0f, 1080f - i.toFloat(), 1920f, paint)
                canvas.drawLine(0f, i.toFloat() * 1.8f, 1080f, 1920f - i.toFloat() * 1.8f, paint)
            }

            // Radial Web concentric rings
            paint.style = Paint.Style.STROKE
            paint.color = AndroidColor.parseColor("#00E5FF")
            paint.alpha = 60
            for (r in 100..900 step 100) {
                canvas.drawCircle(540f, 960f, r.toFloat(), paint)
            }

            // Giant Glowing Spider Emblem
            paint.style = Paint.Style.FILL
            paint.color = AndroidColor.parseColor("#E23636")
            paint.alpha = 255
            canvas.drawCircle(540f, 960f, 180f, paint)

            paint.color = AndroidColor.parseColor("#FFD700")
            canvas.drawCircle(540f, 960f, 60f, paint)

            paint.color = AndroidColor.WHITE
            paint.textSize = 54f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("MIRACLE VAULT", 540f, 1300f, paint)
            paint.textSize = 34f
            paint.color = AndroidColor.parseColor("#00E5FF")
            canvas.drawText("SPIDER-HERO SECURE STORAGE", 540f, 1360f, paint)

            FileOutputStream(heroFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            seedItems.add(
                MediaItem(
                    title = "spiderman_hero_wallpaper.png",
                    filePath = heroFile.absolutePath,
                    type = MediaType.PHOTO,
                    sizeBytes = heroFile.length(),
                    originalSizeBytes = heroFile.length(),
                    mimeType = "image/png",
                    isFavorite = true,
                    tags = "Spider-Man, Wallpaper, Superhero, High-Res",
                    aiAnalysis = "High resolution Spider-Man heroic neon badge featuring cyber web nodes and Stark tech emblem.",
                    width = 1080,
                    height = 1920
                )
            )
        }

        // 2. Generate Spider-Verse Comic Art Sample (Photo)
        val comicArtFile = File(vaultDir, "spider_verse_neon_city.jpg")
        if (!comicArtFile.exists()) {
            val bitmap = Bitmap.createBitmap(1200, 800, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Neon cityscape backdrop
            paint.color = AndroidColor.parseColor("#120826")
            canvas.drawRect(0f, 0f, 1200f, 800f, paint)

            paint.color = AndroidColor.parseColor("#FF1744")
            for (i in 0..1200 step 120) {
                canvas.drawRect(i.toFloat(), (400 + (i % 200)).toFloat(), (i + 80).toFloat(), 800f, paint)
            }

            paint.color = AndroidColor.parseColor("#00E5FF")
            paint.strokeWidth = 5f
            canvas.drawLine(0f, 100f, 1200f, 700f, paint)
            canvas.drawLine(0f, 700f, 1200f, 100f, paint)

            paint.color = AndroidColor.WHITE
            paint.textSize = 48f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("NEW YORK 2099 // SPIDER-VERSE", 600f, 250f, paint)

            FileOutputStream(comicArtFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }

            seedItems.add(
                MediaItem(
                    title = "spider_verse_neon_city.jpg",
                    filePath = comicArtFile.absolutePath,
                    type = MediaType.PHOTO,
                    sizeBytes = comicArtFile.length(),
                    originalSizeBytes = comicArtFile.length(),
                    mimeType = "image/jpeg",
                    isFavorite = true,
                    tags = "Spider-Verse, City, Neon, Comic",
                    aiAnalysis = "Vibrant multi-verse cyberpunk skyline with red skyscraper silhouettes and electric web trails.",
                    width = 1200,
                    height = 800
                )
            )
        }

        // 3. Generate Secret Superhero Dossier (Document)
        val docFile = File(vaultDir, "stark_industries_suit_blueprints.txt")
        if (!docFile.exists()) {
            docFile.writeText(
                """
                ====================================================
                PROJECT MIRACLE: SPIDER-SUIT V5.0 TECHNICAL BLUEPRINT
                CLASSIFIED // STARK INDUSTRIES // PARKER LABS
                ====================================================
                
                1. WEB-SHOOTER COMPRESSION SPECIFICATIONS
                   - Fluid Formula: Synthetic fluid with 120x compression density
                   - Tensile Strength: 300 megapascals per fiber
                   - Rapid Decompression Chamber: Millisecond response
                
                2. SPIDER-SENSE QUANTUM RADAR
                   - Multimodal Visual Analysis Engine
                   - Threat Detection & Object Tagging
                   - High-Speed Color & Contrast Equalizer
                
                3. SECURE MEDIA VAULT MATRIX
                   - Advanced Lossless/Lossy Neural Compression
                   - Encrypted Storage Core
                   - AI Super-Resolution & Style Synthesis
                
                Status: OPERATIONAL
                Encrypted with Miracle Protocol.
                """.trimIndent()
            )

            seedItems.add(
                MediaItem(
                    title = "stark_industries_suit_blueprints.txt",
                    filePath = docFile.absolutePath,
                    type = MediaType.DOCUMENT,
                    sizeBytes = docFile.length(),
                    originalSizeBytes = docFile.length(),
                    mimeType = "text/plain",
                    tags = "Blueprint, Classified, Stark, Text",
                    aiAnalysis = "Classified technical dossier for Spider-Man suit upgrades and storage compression matrices."
                )
            )
        }

        // 4. Generate Web-Slinger Field Notes (Document)
        val missionNotes = File(vaultDir, "daily_bugle_investigation.md")
        if (!missionNotes.exists()) {
            missionNotes.writeText(
                """
                # Daily Bugle Photo Archive Notes
                *Author: Peter Parker*
                
                - Captured 4K shots of the bridge incident.
                - Use Miracle AI Image Editor to adjust contrast and bring out web details in dark shadows.
                - Compress photos using Miracle Web-Squeezer before uploading to the Bugle FTP server.
                """.trimIndent()
            )

            seedItems.add(
                MediaItem(
                    title = "daily_bugle_investigation.md",
                    filePath = missionNotes.absolutePath,
                    type = MediaType.DOCUMENT,
                    sizeBytes = missionNotes.length(),
                    originalSizeBytes = missionNotes.length(),
                    mimeType = "text/markdown",
                    tags = "Bugle, Notes, Peter, Markdown"
                )
            )
        }

        seedItems
    }

    fun deletePhysicalFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun determineMediaType(mimeType: String, fileName: String): MediaType {
        val lower = fileName.lowercase()
        return when {
            mimeType.startsWith("image/") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".bmp") -> MediaType.PHOTO
            mimeType.startsWith("video/") || lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi") || lower.endsWith(".mov") || lower.endsWith(".webm") -> MediaType.VIDEO
            mimeType.startsWith("text/") || mimeType.contains("pdf") || mimeType.contains("document") || mimeType.contains("word") || lower.endsWith(".pdf") || lower.endsWith(".txt") || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".md") || lower.endsWith(".json") -> MediaType.DOCUMENT
            mimeType.contains("zip") || mimeType.contains("tar") || mimeType.contains("rar") || mimeType.contains("gzip") || lower.endsWith(".zip") || lower.endsWith(".gz") || lower.endsWith(".tar") || lower.endsWith(".7z") -> MediaType.ARCHIVE
            else -> MediaType.OTHER
        }
    }

    private fun getMimeTypeFromExtension(fileName: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(fileName)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }

    private fun defaultTagsForType(type: MediaType): String {
        return when (type) {
            MediaType.PHOTO -> "Photo, Image, Vault"
            MediaType.VIDEO -> "Video, Media, Vault"
            MediaType.DOCUMENT -> "Document, Text, Vault"
            MediaType.ARCHIVE -> "Archive, Zip, Compressed"
            MediaType.OTHER -> "File, Storage"
        }
    }
}
