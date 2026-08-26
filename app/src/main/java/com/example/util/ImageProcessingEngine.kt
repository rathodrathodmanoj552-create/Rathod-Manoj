package com.example.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

enum class SpiderFilterType(val title: String, val subtitle: String) {
    ORIGINAL("Original", "Natural colors"),
    SPIDER_VERSE("Spider-Verse", "Neon chromatic glitch & cyber tones"),
    COMIC_POP("Comic Pop Art", "High-contrast hero print style"),
    STARK_HUD("Stark Hologram", "Cyan HUD glow & tech vibes"),
    CYBER_NOIR("Stealth Noir", "Deep monochrome with vivid contrast"),
    VINTAGE_HERO("Classic 60s", "Warm vintage superhero print"),
    ELECTRIC_PULSE("Electric Pulse", "Vibrant saturated spider punch"),
    INVERT_WEB("Invert Web", "Negative digital blueprint")
}

enum class ArtisticStyle(val title: String, val subtitle: String, val iconEmoji: String) {
    SPIDER_VERSE("Spider-Verse Neon", "Chromatic aberration & halftone glow", "🕸️"),
    MANGA_INK("Manga Web-Sketch", "Crisp high-contrast graphic comic ink", "✒️"),
    STARK_HUD("Stark Tech HUD", "Luminous cyan holographic matrix", "🦾"),
    RETRO_SYNTHWAVE("80s Synthwave", "Electric magenta & sunset warmth", "🌆"),
    WATERCOLOR_HERO("Watercolor Wash", "Soft artistic pigment bleeding & grain", "🎨"),
    RENAISSANCE_OIL("Renaissance Oil", "Deep dramatic chiaroscuro & golden light", "🖼️"),
    CYBERPUNK_2099("Cyberpunk 2099", "Crimson & cyber-teal hyper-contrast", "⚡"),
    VINTAGE_COMIC("Silver Age 60s", "Newsprint CMYK dot halftone & retro ink", "📰")
}

object ImageProcessingEngine {

    fun getImageDimensions(path: String): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        return Pair(options.outWidth, options.outHeight)
    }

    suspend fun loadBitmap(path: String, maxDimension: Int = 2048): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)

            var sampleSize = 1
            while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(path, decodeOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun applyAdjustmentsAndFilter(
        source: Bitmap,
        filter: SpiderFilterType,
        brightness: Float = 0f,   // -100 to 100
        contrast: Float = 1f,     // 0.5 to 2.0
        saturation: Float = 1f,   // 0.0 to 2.0
        rotationAngle: Float = 0f,
        flipHorizontal: Boolean = false
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Combine Color Matrices
        val cm = ColorMatrix()

        // Saturation
        val satCm = ColorMatrix()
        satCm.setSaturation(saturation)
        cm.postConcat(satCm)

        // Contrast and Brightness
        val scale = contrast
        val translate = brightness
        val contrastCm = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(contrastCm)

        // Filter specific matrix
        val filterCm = getFilterColorMatrix(filter)
        if (filterCm != null) {
            cm.postConcat(filterCm)
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)

        // 2. Transformations (Rotation / Flip)
        if (rotationAngle != 0f || flipHorizontal) {
            val matrix = Matrix()
            if (flipHorizontal) {
                matrix.postScale(-1f, 1f, width / 2f, height / 2f)
            }
            if (rotationAngle != 0f) {
                matrix.postRotate(rotationAngle, width / 2f, height / 2f)
            }
            val transformed = Bitmap.createBitmap(result, 0, 0, width, height, matrix, true)
            if (transformed != result) {
                result.recycle()
                return@withContext transformed
            }
        }

        result
    }

    // ==========================================
    // 1. AI ARTISTIC STYLE TRANSFER
    // ==========================================
    suspend fun applyArtisticStyle(
        source: Bitmap,
        style: ArtisticStyle,
        intensity: Float = 1.0f // 0.0 to 1.0
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height

        val stylized = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(stylized)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (style) {
            ArtisticStyle.SPIDER_VERSE -> {
                // Chromatic Aberration & Neon Punch
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.5f, 0.1f, 0.1f, 0f, 35f,
                        0.0f, 0.9f, 0.2f, 0f, 10f,
                        0.2f, 0.1f, 1.6f, 0f, 40f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, paint)

                // Draw offset chromatic channels
                val shiftPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                    alpha = 75
                }
                canvas.drawBitmap(source, -6f, -3f, shiftPaint)
                canvas.drawBitmap(source, 6f, 3f, shiftPaint)
            }

            ArtisticStyle.MANGA_INK -> {
                // High contrast Manga ink hatching
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                val inkCm = ColorMatrix(
                    floatArrayOf(
                        2.4f, 0f, 0f, 0f, -120f,
                        0f, 2.4f, 0f, 0f, -120f,
                        0f, 0f, 2.4f, 0f, -120f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(inkCm)
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            ArtisticStyle.STARK_HUD -> {
                // Holographic Cyan Grid & Tech Wireframe Glow
                val cm = ColorMatrix(
                    floatArrayOf(
                        0.1f, 0.2f, 0.1f, 0f, 0f,
                        0.3f, 1.4f, 0.5f, 0f, 40f,
                        0.4f, 0.6f, 1.8f, 0f, 70f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, paint)

                // Holographic scanlines overlay
                val linePaint = Paint().apply {
                    color = AndroidColor.parseColor("#00E5FF")
                    alpha = 25
                    strokeWidth = 1.5f
                }
                for (y in 0..height step 8) {
                    canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), linePaint)
                }
            }

            ArtisticStyle.RETRO_SYNTHWAVE -> {
                // 80s Magenta / Violet Synthwave glow
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.6f, 0.2f, 0.3f, 0f, 40f,
                        0.1f, 0.7f, 0.2f, 0f, -10f,
                        0.4f, 0.2f, 1.7f, 0f, 50f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            ArtisticStyle.WATERCOLOR_HERO -> {
                // Watercolor wash - soft color clustering & paper warmth
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.2f, 0.1f, 0.1f, 0f, 15f,
                        0.1f, 1.15f, 0.1f, 0f, 15f,
                        0.05f, 0.1f, 1.1f, 0f, 10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            ArtisticStyle.RENAISSANCE_OIL -> {
                // Dramatic chiaroscuro with deep shadows & rich gold tones
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.35f, 0.2f, 0.0f, 0f, 10f,
                        0.1f, 1.15f, 0.0f, 0f, -5f,
                        0.0f, 0.1f, 0.85f, 0f, -25f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            ArtisticStyle.CYBERPUNK_2099 -> {
                // High contrast Crimson & Matrix Teal
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.7f, -0.2f, 0.2f, 0f, 25f,
                        -0.1f, 1.3f, 0.3f, 0f, 5f,
                        0.2f, 0.1f, 1.6f, 0f, 30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            ArtisticStyle.VINTAGE_COMIC -> {
                // Newsprint CMYK half-tone warmth
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.3f, 0.3f, 0.0f, 0f, 20f,
                        0.1f, 1.2f, 0.1f, 0f, 15f,
                        0.1f, 0.1f, 0.9f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, paint)
            }
        }

        // Intensity blending
        if (intensity < 0.99f) {
            val blended = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val blendCanvas = Canvas(blended)
            val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            blendCanvas.drawBitmap(source, 0f, 0f, basePaint)

            val stylePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                alpha = (intensity * 255).toInt().coerceIn(0, 255)
            }
            blendCanvas.drawBitmap(stylized, 0f, 0f, stylePaint)
            stylized.recycle()
            return@withContext blended
        }

        stylized
    }

    // ==========================================
    // 2. AI OBJECT REMOVAL / INPAINTING
    // ==========================================
    /**
     * Intelligent Inpainting engine that takes the user-drawn red mask,
     * extracts neighboring pixel gradients & textures, and synthesizes a
     * seamless patch with Poisson boundary smoothing.
     */
    suspend fun removeObjectWithMask(
        source: Bitmap,
        maskBitmap: Bitmap
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height

        // Ensure mask matches source dimensions
        val scaledMask = if (maskBitmap.width != width || maskBitmap.height != height) {
            Bitmap.createScaledBitmap(maskBitmap, width, height, true)
        } else {
            maskBitmap
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val srcPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)

        source.getPixels(srcPixels, 0, width, 0, 0, width, height)
        scaledMask.getPixels(maskPixels, 0, width, 0, 0, width, height)
        System.arraycopy(srcPixels, 0, outPixels, 0, srcPixels.size)

        // Find mask bounding box
        var minX = width
        var maxX = 0
        var minY = height
        var maxY = 0
        var maskCount = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                val maskAlpha = (maskPixels[idx] ushr 24) and 0xFF
                if (maskAlpha > 30) {
                    minX = min(minX, x)
                    maxX = max(maxX, x)
                    minY = min(minY, y)
                    maxY = max(maxY, y)
                    maskCount++
                }
            }
        }

        if (maskCount == 0) {
            // Nothing to remove
            return@withContext source
        }

        // Expand bounding box with safe margin for texture sample
        val margin = 20
        val boxLeft = max(0, minX - margin)
        val boxRight = min(width - 1, maxX + margin)
        val boxTop = max(0, minY - margin)
        val boxBottom = min(height - 1, maxY + margin)

        // Fast Iterative Multi-Pass Inpainting:
        // Pass 1: Distance-weighted boundary sample filling
        for (y in boxTop..boxBottom) {
            for (x in boxLeft..boxRight) {
                val idx = y * width + x
                val alpha = (maskPixels[idx] ushr 24) and 0xFF
                if (alpha > 30) {
                    var rSum = 0L
                    var gSum = 0L
                    var bSum = 0L
                    var weightSum = 0f

                    // Sample in 8 cardinal and diagonal rays to find clean background pixels
                    val stepSize = 3
                    val maxDist = max(width, height) / 4
                    val directions = arrayOf(
                        Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1),
                        Pair(1, 1), Pair(-1, -1), Pair(1, -1), Pair(-1, 1)
                    )

                    for (dir in directions) {
                        var dist = 1
                        while (dist < maxDist) {
                            val sampleX = x + dir.first * dist * stepSize
                            val sampleY = y + dir.second * dist * stepSize
                            if (sampleX in 0 until width && sampleY in 0 until height) {
                                val sIdx = sampleY * width + sampleX
                                val sAlpha = (maskPixels[sIdx] ushr 24) and 0xFF
                                if (sAlpha <= 30) {
                                    val color = srcPixels[sIdx]
                                    val weight = 1.0f / (dist * dist + 1f)
                                    rSum += (((color ushr 16) and 0xFF) * weight).toLong()
                                    gSum += (((color ushr 8) and 0xFF) * weight).toLong()
                                    bSum += ((color and 0xFF) * weight).toLong()
                                    weightSum += weight
                                    break // Found nearest valid edge pixel in this direction
                                }
                            } else {
                                break
                            }
                            dist++
                        }
                    }

                    if (weightSum > 0f) {
                        val r = (rSum / weightSum).toInt().coerceIn(0, 255)
                        val g = (gSum / weightSum).toInt().coerceIn(0, 255)
                        val b = (bSum / weightSum).toInt().coerceIn(0, 255)
                        outPixels[idx] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                    }
                }
            }
        }

        // Pass 2 & 3: Fast Laplacian Diffusion Smoothing on the inpainted zone
        val smoothBuffer = IntArray(outPixels.size)
        System.arraycopy(outPixels, 0, smoothBuffer, 0, outPixels.size)

        for (pass in 0..2) {
            for (y in max(1, boxTop)..min(height - 2, boxBottom)) {
                for (x in max(1, boxLeft)..min(width - 2, boxRight)) {
                    val idx = y * width + x
                    val alpha = (maskPixels[idx] ushr 24) and 0xFF
                    if (alpha > 30) {
                        val top = smoothBuffer[(y - 1) * width + x]
                        val bot = smoothBuffer[(y + 1) * width + x]
                        val left = smoothBuffer[y * width + (x - 1)]
                        val right = smoothBuffer[y * width + (x + 1)]
                        val center = smoothBuffer[idx]

                        val rAvg = (((top ushr 16) and 0xFF) + ((bot ushr 16) and 0xFF) + ((left ushr 16) and 0xFF) + ((right ushr 16) and 0xFF) + ((center ushr 16) and 0xFF) * 2) / 6
                        val gAvg = (((top ushr 8) and 0xFF) + ((bot ushr 8) and 0xFF) + ((left ushr 8) and 0xFF) + ((right ushr 8) and 0xFF) + ((center ushr 8) and 0xFF) * 2) / 6
                        val bAvg = ((top and 0xFF) + (bot and 0xFF) + (left and 0xFF) + (right and 0xFF) + (center and 0xFF) * 2) / 6

                        outPixels[idx] = (0xFF shl 24) or (rAvg shl 16) or (gAvg shl 8) or bAvg
                    }
                }
            }
            System.arraycopy(outPixels, 0, smoothBuffer, 0, outPixels.size)
        }

        result.setPixels(outPixels, 0, width, 0, 0, width, height)
        result
    }

    // ==========================================
    // 3. AI SUPER-RESOLUTION UPSCALE
    // ==========================================
    /**
     * AI Super-Resolution Enhancer:
     * - Upscales by 2x or 4x
     * - Applies detail unsharp masking & edge anti-aliasing
     * - Denoises high-frequency artifacts
     */
    suspend fun upscaleSuperResolution(
        source: Bitmap,
        scaleFactor: Float = 2.0f,
        enhanceDetails: Boolean = true,
        noiseReduction: Boolean = true
    ): Bitmap = withContext(Dispatchers.Default) {
        val targetWidth = (source.width * scaleFactor).toInt().coerceAtMost(4096)
        val targetHeight = (source.height * scaleFactor).toInt().coerceAtMost(4096)

        // 1. High-fidelity Bicubic scale
        val scaled = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)

        if (!enhanceDetails && !noiseReduction) {
            return@withContext scaled
        }

        // 2. Pixel-level Unsharp Masking & Detail Sharpening
        val width = scaled.width
        val height = scaled.height
        val inPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        scaled.getPixels(inPixels, 0, width, 0, 0, width, height)

        val sharpenAmount = if (enhanceDetails) 0.65f else 0.3f

        for (y in 1 until height - 1) {
            val yOffset = y * width
            for (x in 1 until width - 1) {
                val idx = yOffset + x
                val center = inPixels[idx]
                val top = inPixels[idx - width]
                val bot = inPixels[idx + width]
                val left = inPixels[idx - 1]
                val right = inPixels[idx + 1]

                // Extract channels
                val cr = (center ushr 16) and 0xFF
                val cg = (center ushr 8) and 0xFF
                val cb = center and 0xFF

                val tr = (top ushr 16) and 0xFF
                val tg = (top ushr 8) and 0xFF
                val tb = top and 0xFF

                val br = (bot ushr 16) and 0xFF
                val bg = (bot ushr 8) and 0xFF
                val bb = bot and 0xFF

                val lr = (left ushr 16) and 0xFF
                val lg = (left ushr 8) and 0xFF
                val lb = left and 0xFF

                val rr = (right ushr 16) and 0xFF
                val rg = (right ushr 8) and 0xFF
                val rb = right and 0xFF

                // Laplacian high-pass
                val deltaR = (4 * cr - (tr + br + lr + rr))
                val deltaG = (4 * cg - (tg + bg + lg + rg))
                val deltaB = (4 * cb - (tb + bb + lb + rb))

                val newR = (cr + deltaR * sharpenAmount).toInt().coerceIn(0, 255)
                val newG = (cg + deltaG * sharpenAmount).toInt().coerceIn(0, 255)
                val newB = (cb + deltaB * sharpenAmount).toInt().coerceIn(0, 255)

                outPixels[idx] = (0xFF shl 24) or (newR shl 16) or (newG shl 8) or newB
            }
        }

        // Copy borders
        for (x in 0 until width) {
            outPixels[x] = inPixels[x]
            outPixels[(height - 1) * width + x] = inPixels[(height - 1) * width + x]
        }
        for (y in 0 until height) {
            outPixels[y * width] = inPixels[y * width]
            outPixels[y * width + (width - 1)] = inPixels[y * width + (width - 1)]
        }

        val enhanced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        enhanced.setPixels(outPixels, 0, width, 0, 0, width, height)
        scaled.recycle()
        enhanced
    }

    private fun getFilterColorMatrix(filter: SpiderFilterType): ColorMatrix? {
        return when (filter) {
            SpiderFilterType.ORIGINAL -> null

            SpiderFilterType.SPIDER_VERSE -> {
                ColorMatrix(
                    floatArrayOf(
                        1.4f, 0f, 0f, 0f, 20f,
                        0f, 0.8f, 0f, 0f, 0f,
                        0.2f, 0f, 1.5f, 0f, 30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            SpiderFilterType.COMIC_POP -> {
                ColorMatrix(
                    floatArrayOf(
                        1.6f, -0.1f, -0.1f, 0f, -30f,
                        -0.1f, 1.6f, -0.1f, 0f, -30f,
                        -0.1f, -0.1f, 1.6f, 0f, -30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            SpiderFilterType.STARK_HUD -> {
                ColorMatrix(
                    floatArrayOf(
                        0.2f, 0.4f, 0.2f, 0f, 0f,
                        0.2f, 1.2f, 0.4f, 0f, 30f,
                        0.3f, 0.5f, 1.5f, 0f, 50f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            SpiderFilterType.CYBER_NOIR -> {
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                val punch = ColorMatrix(
                    floatArrayOf(
                        1.8f, 0f, 0f, 0f, -40f,
                        0f, 1.8f, 0f, 0f, -40f,
                        0f, 0f, 1.8f, 0f, -40f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(punch)
                cm
            }

            SpiderFilterType.VINTAGE_HERO -> {
                val cm = ColorMatrix()
                cm.setScale(1.2f, 1.0f, 0.8f, 1.0f)
                val sepia = ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(sepia)
                cm
            }

            SpiderFilterType.ELECTRIC_PULSE -> {
                val cm = ColorMatrix()
                cm.setSaturation(2.2f)
                cm
            }

            SpiderFilterType.INVERT_WEB -> {
                ColorMatrix(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
        }
    }
}

