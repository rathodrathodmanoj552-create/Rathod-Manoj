package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.theme.SpiderElectricBlue
import com.example.ui.theme.SpiderGold
import com.example.ui.theme.SpiderGreenSuccess
import com.example.ui.theme.SpiderNavyAccent
import com.example.ui.theme.SpiderNavyBorder
import com.example.ui.theme.SpiderNavyDark
import com.example.ui.theme.SpiderNavyElevated
import com.example.ui.theme.SpiderNavySurface
import com.example.ui.theme.SpiderRed
import com.example.ui.theme.SpiderRedBright
import com.example.ui.theme.SpiderRedDark
import com.example.ui.theme.SpiderTextMuted
import com.example.ui.theme.SpiderTextPrimary
import com.example.ui.theme.SpiderTextSecondary
import com.example.util.CompressionEngine
import java.io.File

@Composable
fun SpiderMediaViewerDialog(
    item: MediaItem,
    onDismiss: () -> Unit,
    onDirectShare: () -> Unit,
    onOpenExternally: () -> Unit,
    onCompress: () -> Unit,
    onAiEdit: () -> Unit,
    onCreateQuantumLink: () -> Unit
) {
    val context = LocalContext.current
    val file = remember(item.filePath) { File(item.filePath) }
    val exists = remember(item.filePath) { file.exists() }

    // Interactive image zoom/pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Text file reading for preview
    val textExcerpt = remember(item) {
        if (item.type == MediaType.DOCUMENT && exists && file.length() < 150_000) {
            try {
                file.readText().take(2000)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(SpiderRed, SpiderElectricBlue)),
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = SpiderNavySurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val headerIcon = when (item.type) {
                            MediaType.PHOTO -> Icons.Default.Image
                            MediaType.VIDEO -> Icons.Default.Movie
                            MediaType.DOCUMENT -> Icons.Default.Description
                            MediaType.ARCHIVE -> Icons.Default.FolderZip
                            MediaType.OTHER -> Icons.Default.Description
                        }
                        val headerTint = when (item.type) {
                            MediaType.PHOTO -> SpiderElectricBlue
                            MediaType.VIDEO -> SpiderRedBright
                            MediaType.DOCUMENT -> SpiderGold
                            MediaType.ARCHIVE -> SpiderGreenSuccess
                            MediaType.OTHER -> SpiderTextSecondary
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(headerTint.copy(alpha = 0.2f))
                                .border(1.dp, headerTint, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(headerIcon, contentDescription = null, tint = headerTint, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.type.name} • ${CompressionEngine.formatFileSize(item.sizeBytes)}",
                                fontSize = 11.sp,
                                color = SpiderTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SpiderNavyElevated)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SpiderTextPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                // Media Display Canvas
                when (item.type) {
                    MediaType.PHOTO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SpiderNavyDark)
                                .border(1.dp, SpiderNavyBorder, RoundedCornerShape(16.dp))
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 4f)
                                        if (scale > 1f) {
                                            offsetX += pan.x
                                            offsetY += pan.y
                                        } else {
                                            offsetX = 0f
                                            offsetY = 0f
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (exists) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(file)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offsetX,
                                            translationY = offsetY
                                        ),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = "Preview generated internally",
                                    color = SpiderTextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            // Zoom reset helper badge
                            if (scale > 1.1f) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .clickable {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Reset Zoom (${String.format("%.1fx", scale)})", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    MediaType.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(SpiderNavyDark, SpiderNavyAccent.copy(alpha = 0.5f))
                                    )
                                )
                                .border(1.dp, SpiderRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(SpiderRed)
                                        .border(2.dp, Color.White, CircleShape)
                                        .clickable { onOpenExternally() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Text(
                                    text = "Tap to Play Video with Player",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SpiderTextPrimary
                                )
                                Text(
                                    text = item.mimeType.ifBlank { "video/mp4" },
                                    fontSize = 11.sp,
                                    color = SpiderElectricBlue
                                )
                            }
                        }
                    }

                    MediaType.DOCUMENT -> {
                        if (textExcerpt != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 140.dp, max = 220.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SpiderNavyDark)
                                    .border(1.dp, SpiderNavyBorder, RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "DOCUMENT PREVIEW",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SpiderGold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = textExcerpt,
                                        fontSize = 11.sp,
                                        color = SpiderTextPrimary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SpiderNavyDark)
                                    .border(1.dp, SpiderNavyBorder, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Description, null, tint = SpiderGold, modifier = Modifier.size(44.dp))
                                    Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                    Text("Open with PDF / Document Viewer", fontSize = 11.sp, color = SpiderTextSecondary)
                                }
                            }
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SpiderNavyDark)
                                .border(1.dp, SpiderNavyBorder, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FolderZip, null, tint = SpiderGreenSuccess, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                Text(item.mimeType, fontSize = 11.sp, color = SpiderTextSecondary)
                            }
                        }
                    }
                }

                // File Metrics Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpiderNavyElevated)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current Size:", fontSize = 12.sp, color = SpiderTextSecondary)
                        Text(CompressionEngine.formatFileSize(item.sizeBytes), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                    }

                    if (item.isCompressed || item.spaceSavedBytes > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Original Size:", fontSize = 12.sp, color = SpiderTextSecondary)
                            Text(CompressionEngine.formatFileSize(item.originalSizeBytes), fontSize = 12.sp, color = SpiderTextMuted)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Reclaimed Space:", fontSize = 12.sp, color = SpiderGreenSuccess)
                            Text("-${CompressionEngine.formatFileSize(item.spaceSavedBytes)} (${item.spaceSavedPercent}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderGreenSuccess)
                        }
                    }

                    if (item.width > 0 && item.height > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Resolution:", fontSize = 12.sp, color = SpiderTextSecondary)
                            Text("${item.width} x ${item.height} px", fontSize = 12.sp, color = SpiderElectricBlue)
                        }
                    }
                }

                // AI Lore / Analysis Box
                if (!item.aiAnalysis.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SpiderRedDark.copy(alpha = 0.3f))
                            .border(1.dp, SpiderRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, tint = SpiderRedBright, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Spider-Sense Vision Analysis", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderRedBright)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.aiAnalysis, fontSize = 11.sp, color = SpiderTextPrimary, lineHeight = 15.sp)
                        }
                    }
                }

                // Primary Direct Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Direct Share to Apps & Open Externally
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDirectShare,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiderRed)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share to Apps", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onOpenExternally,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiderNavyElevated)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Open", tint = SpiderElectricBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in App", color = SpiderTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Row 2: AI Studio / Compress / Quantum Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.type == MediaType.PHOTO) {
                            Button(
                                onClick = onAiEdit,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SpiderElectricBlue)
                            ) {
                                Icon(Icons.Default.AutoAwesome, null, tint = SpiderNavyDark, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Studio", color = SpiderNavyDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = onCompress,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiderNavyElevated)
                        ) {
                            Icon(Icons.Default.Compress, null, tint = SpiderElectricBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compress", color = SpiderTextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onCreateQuantumLink,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SpiderTextPrimary),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderRed, SpiderElectricBlue)))
                        ) {
                            Icon(Icons.Default.Link, null, tint = SpiderGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Web-Link", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
