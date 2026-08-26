package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.theme.SpiderBentoBorder
import com.example.ui.theme.SpiderBentoBorderNav
import com.example.ui.theme.SpiderBentoBorderRed
import com.example.ui.theme.SpiderBentoCard
import com.example.ui.theme.SpiderBentoDark
import com.example.ui.theme.SpiderBentoElevated
import com.example.ui.theme.SpiderBlueAccent
import com.example.ui.theme.SpiderElectricBlue
import com.example.ui.theme.SpiderGold
import com.example.ui.theme.SpiderGreenSuccess
import com.example.ui.theme.SpiderNavy
import com.example.ui.theme.SpiderNavyAccent
import com.example.ui.theme.SpiderNavyBorder
import com.example.ui.theme.SpiderNavyDark
import com.example.ui.theme.SpiderNavyElevated
import com.example.ui.theme.SpiderNavySurface
import com.example.ui.theme.SpiderRed
import com.example.ui.theme.SpiderRedBright
import com.example.ui.theme.SpiderRedDark
import com.example.ui.theme.SpiderRedGlow
import com.example.ui.theme.SpiderSlate100
import com.example.ui.theme.SpiderSlate300
import com.example.ui.theme.SpiderSlate400
import com.example.ui.theme.SpiderSlate500
import com.example.ui.theme.SpiderTextMuted
import com.example.ui.theme.SpiderTextPrimary
import com.example.ui.theme.SpiderTextSecondary
import com.example.util.CompressionEngine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpiderWebBackground(
    modifier: Modifier = Modifier,
    alpha: Float = 0.15f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "web_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "web_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val webColor = SpiderElectricBlue.copy(alpha = alpha)
        val redWebColor = SpiderRed.copy(alpha = alpha * 0.7f)

        // Web focal points
        val centerX = w * 0.9f
        val centerY = h * 0.12f

        // Radiating lines from top-right corner
        val rays = 10
        for (i in 0..rays) {
            val angle = (Math.PI * 0.5 * (i / rays.toDouble())).toFloat() + 0.3f
            val endX = centerX - (Math.cos(angle.toDouble()) * w * 1.5f * pulse).toFloat()
            val endY = centerY + (Math.sin(angle.toDouble()) * h * 1.2f * pulse).toFloat()
            drawLine(
                color = if (i % 2 == 0) webColor else redWebColor,
                start = Offset(centerX, centerY),
                end = Offset(endX, endY),
                strokeWidth = 1.5f
            )
        }

        // Concentric arcs
        for (r in 80..1200 step 120) {
            val radius = r * pulse
            drawCircle(
                color = webColor,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1f)
            )
        }

        // Bottom left secondary web node
        val bCenterX = w * 0.1f
        val bCenterY = h * 0.95f
        for (r in 60..500 step 100) {
            drawCircle(
                color = redWebColor,
                radius = r.toFloat(),
                center = Offset(bCenterX, bCenterY),
                style = Stroke(width = 1f)
            )
        }
    }
}

@Composable
fun SpiderStorageMeter(
    totalSizeBytes: Long,
    totalSavedBytes: Long,
    totalCount: Int,
    photoCount: Int,
    videoCount: Int,
    docCount: Int,
    photoSizeBytes: Long = 0L,
    videoSizeBytes: Long = 0L,
    docSizeBytes: Long = 0L,
    archiveSizeBytes: Long = 0L,
    onCategoryClick: ((MediaType?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 128 GB total simulated device vault capacity
    val totalCapacityBytes = 128L * 1024 * 1024 * 1024
    val baselineSystemBytes = (84.2 * 1024 * 1024 * 1024).toLong()
    val usedBytes = baselineSystemBytes + totalSizeBytes
    val progress = (usedBytes.toFloat() / totalCapacityBytes.toFloat()).coerceIn(0.05f, 0.98f)
    val usedFormatted = if (totalSizeBytes > 0) {
        val totalGb = usedBytes.toDouble() / (1024 * 1024 * 1024)
        String.format(Locale.US, "%.1f", totalGb)
    } else {
        "84.2"
    }
    val usedPercent = (progress * 100).toInt()

    // Segment weights for the progress breakdown
    val safeVaultSize = totalSizeBytes.coerceAtLeast(1L).toFloat()
    val photoWeight = (photoSizeBytes.toFloat() / safeVaultSize).coerceIn(0.05f, 0.9f)
    val videoWeight = (videoSizeBytes.toFloat() / safeVaultSize).coerceIn(0.05f, 0.9f)
    val docWeight = (docSizeBytes.toFloat() / safeVaultSize).coerceIn(0.05f, 0.9f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spider_storage_meter"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(SpiderRed, SpiderNavyAccent),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Radial dot grid accent on top-right
            Canvas(modifier = Modifier.matchParentSize()) {
                val dotColor = Color.White.copy(alpha = 0.12f)
                val dotRadius = 1.5f
                val spacing = 12f
                val startX = size.width * 0.65f
                val startY = 0f
                var y = startY
                while (y < size.height * 0.7f) {
                    var x = startX
                    while (x < size.width) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                        x += spacing
                    }
                    y += spacing
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Label & Global Capacity Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SpiderGreenSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GLOBAL STORAGE CAPACITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 1.2.sp
                        )
                    }

                    if (totalSavedBytes > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, SpiderElectricBlue.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Saved",
                                    tint = SpiderElectricBlue,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "-${CompressionEngine.formatFileSize(totalSavedBytes)} Reclaimed",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SpiderElectricBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Big Numeric Stat + Vault Space summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = usedFormatted,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 38.sp
                        )
                        Text(
                            text = "GB",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                        )
                        Text(
                            text = " / 128 GB",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Vault Space: ${CompressionEngine.formatFileSize(totalSizeBytes)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$totalCount Encrypted Items",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Multi-Color Progress Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        if (totalSizeBytes > 0) {
                            if (photoSizeBytes > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(photoWeight)
                                        .height(10.dp)
                                        .background(SpiderGold)
                                )
                            }
                            if (videoSizeBytes > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(videoWeight)
                                        .height(10.dp)
                                        .background(SpiderRedBright)
                                )
                            }
                            if (docSizeBytes > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(docWeight)
                                        .height(10.dp)
                                        .background(SpiderElectricBlue)
                                )
                            }
                        }
                        // Fill remainder of used system space with clean white
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .background(Color.White.copy(alpha = 0.9f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tool Storage Breakdown Cards (Photos, Videos, Documents, Reclaimed)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Photos
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onCategoryClick?.invoke(MediaType.PHOTO) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SpiderGold))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Photos", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.85f))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (photoSizeBytes > 0) CompressionEngine.formatFileSize(photoSizeBytes) else "$photoCount items",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderGold
                            )
                        }
                    }

                    // 2. Videos
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onCategoryClick?.invoke(MediaType.VIDEO) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SpiderRedBright))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Videos", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.85f))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (videoSizeBytes > 0) CompressionEngine.formatFileSize(videoSizeBytes) else "$videoCount items",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderRedBright
                            )
                        }
                    }

                    // 3. Documents
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onCategoryClick?.invoke(MediaType.DOCUMENT) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SpiderElectricBlue))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Docs", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.85f))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (docSizeBytes > 0) CompressionEngine.formatFileSize(docSizeBytes) else "$docCount items",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderElectricBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bento Grid feature matrix matching the design specifications:
 * 1 Tall card (Galleries) + 2 Small cards (AI Edit, Zip-Web) + 1 Wide card (Spider-Vault)
 */
@Composable
fun BentoFeatureGrid(
    photoCount: Int,
    totalCount: Int,
    onGalleriesClick: () -> Unit,
    onAiEditClick: () -> Unit,
    onZipWebClick: () -> Unit,
    onVaultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Row: Tall Left Card (Galleries) + Stacked Right Cards (AI Edit & Zip-Web)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Tall Card: Galleries
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onGalleriesClick() }
                    .testTag("bento_galleries_card"),
                shape = RoundedCornerShape(24.dp),
                color = SpiderBentoCard,
                border = BorderStroke(1.dp, SpiderBentoBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SpiderNavyAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Galleries",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Galleries",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpiderSlate100
                        )
                        Text(
                            text = "$photoCount Photos",
                            fontSize = 10.sp,
                            color = SpiderSlate400
                        )
                    }
                }
            }

            // Right Stacked Column (AI Edit + Zip-Web)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // AI Edit Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onAiEditClick() }
                        .testTag("bento_ai_edit_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = SpiderBentoCard,
                    border = BorderStroke(1.dp, SpiderBentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SpiderRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Edit",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "AI Edit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderSlate100
                            )
                            Text(
                                text = "ENHANCE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderRedBright,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Zip-Web Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onZipWebClick() }
                        .testTag("bento_zip_web_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = SpiderBentoCard,
                    border = BorderStroke(1.dp, SpiderBentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SpiderBentoElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = "Zip-Web",
                                tint = SpiderSlate300,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Zip-Web",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderSlate100
                            )
                            Text(
                                text = "COMPRESS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderSlate400,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Bottom Wide Span Card: Spider-Vault
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable { onVaultClick() }
                .testTag("bento_spider_vault_card"),
            shape = RoundedCornerShape(24.dp),
            color = SpiderBentoCard,
            border = BorderStroke(1.dp, SpiderBentoBorderRed)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Diagonal stripes corner pattern
                Canvas(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(90.dp)
                        .height(64.dp)
                ) {
                    val stripeColor = SpiderRed.copy(alpha = 0.08f)
                    var x = 0f
                    while (x < size.width + size.height) {
                        drawLine(
                            color = stripeColor,
                            start = Offset(x, 0f),
                            end = Offset(x - size.height, size.height),
                            strokeWidth = 2f
                        )
                        x += 10f
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(SpiderRed, SpiderBentoCard),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = "Vault",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Spider-Vault",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderSlate100
                            )
                            Text(
                                text = "End-to-end encrypted storage",
                                fontSize = 11.sp,
                                color = SpiderSlate400
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = SpiderRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SpiderBentoElevated)
            .border(1.dp, SpiderBentoBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = SpiderTextMuted)
    }
}

@Composable
fun SpiderSensePulseBanner(
    isScanning: Boolean,
    title: String = "Spider-Sense Active",
    subtitle: String = "Monitoring media integrity & compression potential",
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = SpiderBentoCard,
        border = BorderStroke(1.dp, SpiderBentoBorderRed)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(if (isScanning) pulseScale else 1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpiderRed.copy(alpha = 0.2f))
                    .border(1.dp, if (isScanning) SpiderRedBright else SpiderRed, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Spider-Sense",
                    tint = if (isScanning) SpiderRedBright else SpiderRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpiderSlate100
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SpiderRed)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (isScanning) "SCANNING" else "AI READY",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = SpiderSlate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MediaItemCard(
    item: MediaItem,
    isSelected: Boolean = false,
    onItemClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onCompressClick: () -> Unit,
    onEditAiClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val file = File(item.filePath)
    val exists = file.exists()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) SpiderRed else SpiderBentoBorder,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onItemClick() }
            .testTag("media_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = SpiderBentoCard)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail / Header preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(SpiderBentoElevated),
                contentAlignment = Alignment.Center
            ) {
                if (item.type == MediaType.PHOTO && exists) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(file)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Type Icon Graphic
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val (icon, tint) = when (item.type) {
                            MediaType.PHOTO -> Pair(Icons.Default.Image, SpiderElectricBlue)
                            MediaType.VIDEO -> Pair(Icons.Default.Movie, SpiderRedBright)
                            MediaType.DOCUMENT -> Pair(Icons.Default.Description, SpiderGold)
                            MediaType.ARCHIVE -> Pair(Icons.Default.FolderZip, SpiderGreenSuccess)
                            MediaType.OTHER -> Pair(Icons.Default.Description, SpiderSlate400)
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SpiderNavyAccent.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = item.type.name,
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.type.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpiderSlate400
                        )
                    }
                }

                // Top Floating Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Space Saved or Compressed Badge
                    if (item.isCompressed || item.spaceSavedBytes > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SpiderGreenSuccess)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "-${item.spaceSavedPercent}%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = CompressionEngine.formatFileSize(item.sizeBytes),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Favorite Button
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) SpiderRedBright else Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Body info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpiderSlate100,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.dateAdded))
                    Text(
                        text = dateFormatted,
                        fontSize = 10.sp,
                        color = SpiderSlate400
                    )

                    if (item.width > 0 && item.height > 0) {
                        Text(
                            text = "${item.width}x${item.height}",
                            fontSize = 9.sp,
                            color = SpiderSlate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Bar with Bento pill buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpiderBentoElevated)
                        .border(1.dp, SpiderBentoBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.type == MediaType.PHOTO) {
                        IconButton(onClick = onEditAiClick, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Edit",
                                tint = SpiderRedBright,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    IconButton(onClick = onCompressClick, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Compress,
                            contentDescription = "Compress",
                            tint = SpiderElectricBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(onClick = onShareClick, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share/Export",
                            tint = SpiderSlate300,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = SpiderRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated Spider Logo with dynamic leg articulation, glowing spider-sense optics,
 * cyber-web energy rings, and interactive tap haptics/shockwaves.
 */
@Composable
fun AnimatedSpiderLogo(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    showGlowRing: Boolean = true,
    showWebAura: Boolean = true,
    isHero: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val tapScale = remember { Animatable(1f) }
    val shockwaveProgress = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "spider_logo_infinite")

    // Subtle breathing pulse for core
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    // Dynamic leg articulation waves
    val legWave1 by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leg_wave_1"
    )

    val legWave2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leg_wave_2"
    )

    // Eye spider-sense optic flare
    val eyeGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eye_glow"
    )

    // Slow orbital rotation for tech aura
    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_rot"
    )

    val handleTap: () -> Unit = {
        coroutineScope.launch {
            tapScale.animateTo(1.22f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            tapScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
        }
        coroutineScope.launch {
            shockwaveProgress.snapTo(0f)
            shockwaveProgress.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
        }
        onClick?.invoke()
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(tapScale.value)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = handleTap
            )
            .testTag("animated_spider_logo"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // 1. Shockwave Ripple on tap
            if (shockwaveProgress.value > 0f && shockwaveProgress.value < 1f) {
                val swRadius = (w * 0.7f) * shockwaveProgress.value
                val swAlpha = (1f - shockwaveProgress.value) * 0.8f
                drawCircle(
                    color = SpiderElectricBlue.copy(alpha = swAlpha),
                    radius = swRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.5f)
                )
                drawCircle(
                    color = SpiderRedBright.copy(alpha = swAlpha * 0.6f),
                    radius = swRadius * 0.75f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5f)
                )
            }

            // 2. Cyber Web Aura / Orbital Ring
            if (showWebAura) {
                rotate(auraRotation, pivot = Offset(cx, cy)) {
                    // Segmented outer arc
                    val ringRadius = w * 0.46f
                    drawCircle(
                        color = SpiderRed.copy(alpha = 0.25f),
                        radius = ringRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.2f)
                    )

                    // 4 glowing tech orbital ticks
                    val tickLen = w * 0.08f
                    drawLine(
                        color = SpiderElectricBlue,
                        start = Offset(cx, cy - ringRadius - tickLen / 2),
                        end = Offset(cx, cy - ringRadius + tickLen / 2),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = SpiderElectricBlue,
                        start = Offset(cx, cy + ringRadius - tickLen / 2),
                        end = Offset(cx, cy + ringRadius + tickLen / 2),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = SpiderRedBright,
                        start = Offset(cx - ringRadius - tickLen / 2, cy),
                        end = Offset(cx - ringRadius + tickLen / 2, cy),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = SpiderRedBright,
                        start = Offset(cx + ringRadius - tickLen / 2, cy),
                        end = Offset(cx + ringRadius + tickLen / 2, cy),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Glow Ring & Background Shield
            if (showGlowRing) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SpiderRed.copy(alpha = 0.45f * breathScale),
                            SpiderNavyAccent.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = w * 0.48f
                    ),
                    radius = w * 0.48f,
                    center = Offset(cx, cy)
                )
            }

            // Scale calculations for Spider Anatomy
            val s = (w / 100f) * breathScale
            val sx = cx
            val sy = cy

            // Leg Stroke Width
            val legStrokeWidth = (w * 0.045f).coerceAtLeast(1.8f)

            // Dynamic leg offsets
            val lOff1 = legWave1 * (4f * s)
            val lOff2 = legWave2 * (3.5f * s)
            val lOff3 = legWave1 * (-3.5f * s)
            val lOff4 = legWave2 * (-4f * s)

            // 4. Draw Spider Legs (Pair 1: Upper)
            val legColorTop = Color.White
            val legColorMid = SpiderRedBright
            val legColorBottom = SpiderRed

            // Leg 1 Left & Right (Upper Reaching)
            val pathLeg1L = Path().apply {
                moveTo(sx - 4 * s, sy - 8 * s)
                lineTo(sx - 20 * s, sy - (24 * s + lOff1))
                lineTo(sx - 34 * s, sy - (12 * s + lOff1))
            }
            val pathLeg1R = Path().apply {
                moveTo(sx + 4 * s, sy - 8 * s)
                lineTo(sx + 20 * s, sy - (24 * s + lOff1))
                lineTo(sx + 34 * s, sy - (12 * s + lOff1))
            }
            drawPath(pathLeg1L, color = legColorTop, style = Stroke(width = legStrokeWidth, cap = StrokeCap.Round))
            drawPath(pathLeg1R, color = legColorTop, style = Stroke(width = legStrokeWidth, cap = StrokeCap.Round))

            // Leg 2 Left & Right (Upper Mid Spreading)
            val pathLeg2L = Path().apply {
                moveTo(sx - 6 * s, sy - 2 * s)
                lineTo(sx - 26 * s, sy - (6 * s + lOff2))
                lineTo(sx - 38 * s, sy + (6 * s + lOff2))
            }
            val pathLeg2R = Path().apply {
                moveTo(sx + 6 * s, sy - 2 * s)
                lineTo(sx + 26 * s, sy - (6 * s + lOff2))
                lineTo(sx + 38 * s, sy + (6 * s + lOff2))
            }
            drawPath(pathLeg2L, color = legColorTop, style = Stroke(width = legStrokeWidth, cap = StrokeCap.Round))
            drawPath(pathLeg2R, color = legColorTop, style = Stroke(width = legStrokeWidth, cap = StrokeCap.Round))

            // Leg 3 Left & Right (Lower Mid)
            val pathLeg3L = Path().apply {
                moveTo(sx - 6 * s, sy + 6 * s)
                lineTo(sx - 24 * s, sy + (14 * s + lOff3))
                lineTo(sx - 32 * s, sy + (28 * s + lOff3))
            }
            val pathLeg3R = Path().apply {
                moveTo(sx + 6 * s, sy + 6 * s)
                lineTo(sx + 24 * s, sy + (14 * s + lOff3))
                lineTo(sx + 32 * s, sy + (28 * s + lOff3))
            }
            drawPath(pathLeg3L, color = legColorMid, style = Stroke(width = legStrokeWidth * 0.95f, cap = StrokeCap.Round))
            drawPath(pathLeg3R, color = legColorMid, style = Stroke(width = legStrokeWidth * 0.95f, cap = StrokeCap.Round))

            // Leg 4 Left & Right (Bottom Trailing)
            val pathLeg4L = Path().apply {
                moveTo(sx - 4 * s, sy + 14 * s)
                lineTo(sx - 16 * s, sy + (26 * s + lOff4))
                lineTo(sx - 20 * s, sy + (38 * s + lOff4))
            }
            val pathLeg4R = Path().apply {
                moveTo(sx + 4 * s, sy + 14 * s)
                lineTo(sx + 16 * s, sy + (26 * s + lOff4))
                lineTo(sx + 20 * s, sy + (38 * s + lOff4))
            }
            drawPath(pathLeg4L, color = legColorBottom, style = Stroke(width = legStrokeWidth * 0.9f, cap = StrokeCap.Round))
            drawPath(pathLeg4R, color = legColorBottom, style = Stroke(width = legStrokeWidth * 0.9f, cap = StrokeCap.Round))

            // 5. Spider Upper Head & Thorax (Filled Red Body)
            val thoraxPath = Path().apply {
                moveTo(sx, sy - 14 * s)
                lineTo(sx + 8 * s, sy - 6 * s)
                lineTo(sx + 7 * s, sy + 2 * s)
                lineTo(sx, sy + 4 * s)
                lineTo(sx - 7 * s, sy + 2 * s)
                lineTo(sx - 8 * s, sy - 6 * s)
                close()
            }
            drawPath(
                thoraxPath,
                brush = Brush.verticalGradient(
                    colors = listOf(SpiderRedBright, SpiderRedDark),
                    startY = sy - 14 * s,
                    endY = sy + 4 * s
                )
            )

            // 6. Spider Lower Abdomen (Elongated Arachnid)
            val abdomenPath = Path().apply {
                moveTo(sx, sy + 4 * s)
                lineTo(sx + 9 * s, sy + 14 * s)
                lineTo(sx + 7 * s, sy + 26 * s)
                lineTo(sx, sy + 34 * s)
                lineTo(sx - 7 * s, sy + 26 * s)
                lineTo(sx - 9 * s, sy + 14 * s)
                close()
            }
            drawPath(
                abdomenPath,
                brush = Brush.verticalGradient(
                    colors = listOf(SpiderRed, Color(0xFF8B0000)),
                    startY = sy + 4 * s,
                    endY = sy + 34 * s
                )
            )

            // 7. Core Insignia Jewel (Electric Blue Center Node)
            val coreNodePath = Path().apply {
                moveTo(sx, sy - 2 * s)
                lineTo(sx + 4 * s, sy + 4 * s)
                lineTo(sx, sy + 10 * s)
                lineTo(sx - 4 * s, sy + 4 * s)
                close()
            }
            drawPath(
                coreNodePath,
                color = SpiderElectricBlue
            )

            // 8. Glowing Spider Eyes (Spider-Sense Optics)
            val eyeRadiusX = 2.2f * s
            val eyeRadiusY = 3.2f * s
            val eyeAlpha = eyeGlow.coerceIn(0.4f, 1.0f)
            val eyeColor = SpiderElectricBlue.copy(alpha = eyeAlpha)

            // Left Eye
            drawOval(
                color = eyeColor,
                topLeft = Offset(sx - 5.5f * s - eyeRadiusX, sy - 8f * s - eyeRadiusY),
                size = Size(eyeRadiusX * 2, eyeRadiusY * 2)
            )
            // Right Eye
            drawOval(
                color = eyeColor,
                topLeft = Offset(sx + 5.5f * s - eyeRadiusX, sy - 8f * s - eyeRadiusY),
                size = Size(eyeRadiusX * 2, eyeRadiusY * 2)
            )

            // Inner white optic center
            drawCircle(
                color = Color.White.copy(alpha = eyeAlpha),
                radius = 1.2f * s,
                center = Offset(sx - 5.2f * s, sy - 7.8f * s)
            )
            drawCircle(
                color = Color.White.copy(alpha = eyeAlpha),
                radius = 1.2f * s,
                center = Offset(sx + 5.2f * s, sy - 7.8f * s)
            )
        }
    }
}
