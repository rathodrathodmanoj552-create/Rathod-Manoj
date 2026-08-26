package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SpiderElectricBlue
import com.example.ui.theme.SpiderNavyDark
import com.example.ui.theme.SpiderRed
import com.example.ui.theme.SpiderRedBright
import java.security.MessageDigest
import kotlin.math.abs

@Composable
fun SpiderQrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 200.dp,
    accentColor: Color = SpiderElectricBlue
) {
    // Generate deterministic 21x21 QR-like matrix from hash
    val matrix = remember(data) {
        generateQrMatrix(data, 21)
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(18.dp))
            .background(SpiderNavyDark)
            .border(2.dp, Brush.linearGradient(listOf(SpiderRed, accentColor)), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp - 28.dp)) {
            val canvasSize = size.width
            val gridSize = 21
            val cellSize = canvasSize / gridSize

            // Draw QR modules
            for (row in 0 until gridSize) {
                for (col in 0 until gridSize) {
                    if (matrix[row][col]) {
                        val isCornerMarker = (row < 7 && col < 7) ||
                                (row < 7 && col >= gridSize - 7) ||
                                (row >= gridSize - 7 && col < 7)

                        val cellColor = when {
                            isCornerMarker -> SpiderRedBright
                            (row + col) % 3 == 0 -> accentColor
                            else -> Color.White
                        }

                        drawRoundRect(
                            color = cellColor,
                            topLeft = Offset(col * cellSize + 0.5f, row * cellSize + 0.5f),
                            size = Size(cellSize - 1f, cellSize - 1f),
                            cornerRadius = CornerRadius(if (isCornerMarker) 2.5f else 1.5f)
                        )
                    }
                }
            }

            // Draw center Spider web badge marker
            val center = Offset(canvasSize / 2f, canvasSize / 2f)
            val badgeRadius = cellSize * 2.2f
            drawCircle(
                color = SpiderNavyDark,
                radius = badgeRadius,
                center = center
            )
            drawCircle(
                color = SpiderRed,
                radius = badgeRadius,
                center = center,
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = SpiderElectricBlue,
                radius = badgeRadius * 0.45f,
                center = center
            )
        }
    }
}

private fun generateQrMatrix(data: String, size: Int): Array<BooleanArray> {
    val matrix = Array(size) { BooleanArray(size) }

    // 1. Draw Position Detection Patterns (Top-Left, Top-Right, Bottom-Left)
    fun drawFinderPattern(startRow: Int, startCol: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                val isInner = r in 2..4 && c in 2..4
                matrix[startRow + r][startCol + c] = isOuter || isInner
            }
        }
    }

    drawFinderPattern(0, 0)
    drawFinderPattern(0, size - 7)
    drawFinderPattern(size - 7, 0)

    // 2. Timing patterns
    for (i in 7 until size - 7) {
        matrix[6][i] = i % 2 == 0
        matrix[i][6] = i % 2 == 0
    }

    // 3. Populate data cells from hash bytes
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(data.toByteArray(Charsets.UTF_8))
    var bitIndex = 0

    for (r in 0 until size) {
        for (c in 0 until size) {
            val inTopLeft = r < 8 && c < 8
            val inTopRight = r < 8 && c >= size - 8
            val inBottomLeft = r >= size - 8 && c < 8
            val inCenter = r in (size / 2 - 2)..(size / 2 + 2) && c in (size / 2 - 2)..(size / 2 + 2)
            val inTiming = (r == 6 && c >= 8 && c < size - 8) || (c == 6 && r >= 8 && r < size - 8)

            if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenter && !inTiming) {
                val byteVal = abs(hash[bitIndex % hash.size].toInt())
                val bitVal = (byteVal shr (bitIndex % 8)) and 1
                val isDark = bitVal == 1 || ((r * c + bitIndex) % 3 == 0)
                matrix[r][c] = isDark
                bitIndex++
            }
        }
    }

    return matrix
}
