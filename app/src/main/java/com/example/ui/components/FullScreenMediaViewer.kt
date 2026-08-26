package com.example.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.theme.SpiderBentoBorder
import com.example.ui.theme.SpiderBentoCard
import com.example.ui.theme.SpiderBentoDark
import com.example.ui.theme.SpiderBentoElevated
import com.example.ui.theme.SpiderElectricBlue
import com.example.ui.theme.SpiderGold
import com.example.ui.theme.SpiderGreenSuccess
import com.example.ui.theme.SpiderRed
import com.example.ui.theme.SpiderRedBright
import com.example.ui.theme.SpiderSlate100
import com.example.ui.theme.SpiderSlate300
import com.example.ui.theme.SpiderSlate400
import com.example.ui.theme.SpiderSlate500
import com.example.util.CompressionEngine
import java.io.File

@Composable
fun FullScreenMediaViewer(
    item: MediaItem,
    onDismiss: () -> Unit,
    onDirectShare: () -> Unit,
    onOpenExternally: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCompress: () -> Unit,
    onAiEdit: () -> Unit,
    onDelete: () -> Unit,
    onCreateQuantumLink: () -> Unit
) {
    val context = LocalContext.current
    val file = remember(item.filePath) { File(item.filePath) }
    val exists = remember(item.filePath) { file.exists() }

    var showControls by remember { mutableStateOf(true) }
    var showInfoSheet by remember { mutableStateOf(false) }

    // Interactive zoom & pan for photos
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Text content for document reader
    val documentContent = remember(item) {
        if (item.type == MediaType.DOCUMENT && exists) {
            try {
                if (file.length() < 300_000) {
                    file.readText()
                } else {
                    file.bufferedReader().useLines { lines ->
                        lines.take(500).joinToString("\n") + "\n\n... [Large file truncated. Tap 'Open in App' to view full document]"
                    }
                }
            } catch (e: Exception) {
                "Unable to preview text directly. Tap 'Open in App' below to view with your device's Document / PDF reader."
            }
        } else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler {
            if (showInfoSheet) {
                showInfoSheet = false
            } else {
                onDismiss()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07090E))
        ) {
            // Main Media Content Layer
            when (item.type) {
                MediaType.PHOTO -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { showControls = !showControls },
                                    onDoubleTap = {
                                        if (scale > 1f) {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        } else {
                                            scale = 2.5f
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.8f, 5f)
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = SpiderRed, modifier = Modifier.size(48.dp))
                                Text("Photo file not found on storage", color = SpiderSlate400, fontSize = 14.sp)
                            }
                        }

                        // Floating zoom indicator & reset
                        if (scale != 1f) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 100.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .border(1.dp, SpiderBentoBorder, RoundedCornerShape(20.dp))
                                    .clickable {
                                        scale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = SpiderElectricBlue, modifier = Modifier.size(14.dp))
                                    Text("Zoom: ${String.format("%.1fx", scale)} (Tap to reset)", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                MediaType.VIDEO -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { showControls = !showControls })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (exists) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        val mediaController = MediaController(ctx)
                                        mediaController.setAnchorView(this)
                                        setMediaController(mediaController)
                                        setVideoURI(Uri.fromFile(file))
                                        setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            start()
                                        }
                                        setOnErrorListener { _, _, _ ->
                                            false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp)
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = SpiderRed, modifier = Modifier.size(56.dp))
                                Text("Video file not found", color = SpiderSlate400, fontSize = 14.sp)
                            }
                        }
                    }
                }

                MediaType.DOCUMENT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 80.dp, bottom = 90.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0F1420),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SpiderBentoBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = SpiderGold, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("FULL DOCUMENT READER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SpiderGold, letterSpacing = 1.sp)
                                    }
                                    Button(
                                        onClick = onOpenExternally,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SpiderBentoElevated),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = SpiderElectricBlue, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open PDF / Doc App", fontSize = 11.sp, color = SpiderSlate100)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF080B12))
                                        .padding(14.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = documentContent ?: "Loading document...",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = SpiderSlate300,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, tint = SpiderGreenSuccess, modifier = Modifier.size(64.dp))
                            Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SpiderSlate100)
                            Text(CompressionEngine.formatFileSize(item.sizeBytes), fontSize = 13.sp, color = SpiderSlate400)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onOpenExternally,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SpiderRed)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Archive / File")
                            }
                        }
                    }
                }
            }

            // Top Header Bar Overlay
            AnimatedVisibility(
                visible = showControls,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.82f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SpiderBentoElevated)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SpiderSlate100, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SpiderSlate100,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.type.name} • ${CompressionEngine.formatFileSize(item.sizeBytes)}",
                                    fontSize = 11.sp,
                                    color = SpiderSlate400
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Direct Share to External Apps
                            IconButton(
                                onClick = onDirectShare,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SpiderRed)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share to apps", tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            // Favorite Toggle
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SpiderBentoElevated)
                            ) {
                                Icon(
                                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (item.isFavorite) SpiderRedBright else SpiderSlate300,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Info Sheet Toggle
                            IconButton(
                                onClick = { showInfoSheet = !showInfoSheet },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SpiderBentoElevated)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = SpiderElectricBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Bottom Action Bar Overlay
            AnimatedVisibility(
                visible = showControls && !showInfoSheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.85f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick Action Buttons
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
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share to Apps", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = onOpenExternally,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SpiderBentoElevated)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = SpiderElectricBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open in App", color = SpiderSlate100, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (item.type == MediaType.PHOTO) {
                                Button(
                                    onClick = onAiEdit,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SpiderElectricBlue)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Studio", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = onCompress,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SpiderBentoCard)
                            ) {
                                Icon(Icons.Default.Compress, null, tint = SpiderElectricBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Compress", color = SpiderSlate100, fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = onCreateQuantumLink,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SpiderSlate100)
                            ) {
                                Icon(Icons.Default.Link, null, tint = SpiderGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Web-Link", fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SpiderRed.copy(alpha = 0.15f))
                                    .border(1.dp, SpiderRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SpiderRedBright, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Slide-up Info Details Panel
            AnimatedVisibility(
                visible = showInfoSheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    color = Color(0xFF101522),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SpiderBentoBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("FILE METRICS & DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderElectricBlue, letterSpacing = 1.sp)
                            IconButton(onClick = { showInfoSheet = false }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close Info", tint = SpiderSlate400, modifier = Modifier.size(18.dp))
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SpiderBentoElevated)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Filename:", fontSize = 12.sp, color = SpiderSlate400)
                                Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderSlate100, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Storage Size:", fontSize = 12.sp, color = SpiderSlate400)
                                Text(CompressionEngine.formatFileSize(item.sizeBytes), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderSlate100)
                            }
                            if (item.isCompressed || item.spaceSavedBytes > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Reclaimed Space:", fontSize = 12.sp, color = SpiderGreenSuccess)
                                    Text("-${CompressionEngine.formatFileSize(item.spaceSavedBytes)} (${item.spaceSavedPercent}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderGreenSuccess)
                                }
                            }
                            if (item.width > 0 && item.height > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Resolution:", fontSize = 12.sp, color = SpiderSlate400)
                                    Text("${item.width} x ${item.height} px", fontSize = 12.sp, color = SpiderElectricBlue)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("MIME Type:", fontSize = 12.sp, color = SpiderSlate400)
                                Text(item.mimeType.ifBlank { "Unknown" }, fontSize = 12.sp, color = SpiderSlate300)
                            }
                        }

                        if (!item.aiAnalysis.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SpiderRed.copy(alpha = 0.12f))
                                    .border(1.dp, SpiderRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, null, tint = SpiderRedBright, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("AI Vision Analysis", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderRedBright)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.aiAnalysis, fontSize = 11.sp, color = SpiderSlate100, lineHeight = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
