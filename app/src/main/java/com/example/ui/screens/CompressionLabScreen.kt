package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.components.FullScreenMediaViewer
import com.example.ui.components.SpiderWebBackground
import com.example.ui.theme.SpiderElectricBlue
import com.example.ui.theme.SpiderGold
import com.example.ui.theme.SpiderGreenSuccess
import com.example.ui.theme.SpiderNavy
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
import com.example.ui.viewmodel.MiracleViewModel
import com.example.util.CompressionEngine
import com.example.util.ImageCompressFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressionLabScreen(
    viewModel: MiracleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val compressionState by viewModel.compressionState.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()
    val history by viewModel.compressionHistory.collectAsState()
    val totalSavedBytes by viewModel.totalSavedBytes.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Media Squeezer", "Zip Archiver", "Savings History")

    var squeezerCategoryFilter by remember { mutableStateOf<MediaType?>(null) }
    val selectedZipItems = remember { mutableStateListOf<MediaItem>() }
    var zipArchiveName by remember { mutableStateOf("miracle_bundle") }

    // Full screen preview state
    var fullScreenPreviewItem by remember { mutableStateOf<MediaItem?>(null) }

    val selectableSqueezerItems = remember(allMedia, squeezerCategoryFilter) {
        if (squeezerCategoryFilter == null) allMedia else allMedia.filter { it.type == squeezerCategoryFilter }
    }
    val currentTarget = compressionState.targetItem ?: selectableSqueezerItems.firstOrNull() ?: allMedia.firstOrNull()

    // Full-Screen Media Viewer Dialog
    fullScreenPreviewItem?.let { item ->
        FullScreenMediaViewer(
            item = item,
            onDismiss = { fullScreenPreviewItem = null },
            onDirectShare = { viewModel.shareMedia(item) },
            onOpenExternally = { viewModel.openMediaExternally(item) },
            onToggleFavorite = { viewModel.toggleFavorite(item) },
            onCompress = {
                viewModel.selectItemForCompression(item)
                fullScreenPreviewItem = null
            },
            onAiEdit = {
                viewModel.loadItemForAiStudio(item)
                fullScreenPreviewItem = null
            },
            onDelete = {
                viewModel.deleteMedia(item)
                fullScreenPreviewItem = null
            },
            onCreateQuantumLink = {
                viewModel.shareMedia(item)
            }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(SpiderNavy)) {
        SpiderWebBackground(alpha = 0.1f)

        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SpiderNavySurface,
                contentColor = SpiderElectricBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = SpiderRedBright,
                        height = 3.dp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color.White else SpiderTextSecondary
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // TAB 1: Media Squeezer
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Select Media to Squeeze",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SpiderTextPrimary
                                )

                                // Category Filter Chips for Squeezer
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(
                                        null to "All",
                                        MediaType.PHOTO to "Photos",
                                        MediaType.DOCUMENT to "Docs",
                                        MediaType.VIDEO to "Videos"
                                    ).forEach { (type, label) ->
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { squeezerCategoryFilter = type },
                                            color = if (squeezerCategoryFilter == type) SpiderRed else SpiderNavyElevated,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (squeezerCategoryFilter == type) Color.White else SpiderTextSecondary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Media Picker Carousel
                        item {
                            if (selectableSqueezerItems.isEmpty()) {
                                Text("No items found in this category. Import items from the Home screen.", color = SpiderTextSecondary, fontSize = 12.sp)
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(selectableSqueezerItems) { item ->
                                        val isSelected = (currentTarget?.id == item.id)
                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) SpiderElectricBlue else SpiderNavyBorder,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { viewModel.selectItemForCompression(item) }
                                        ) {
                                            when (item.type) {
                                                MediaType.PHOTO -> {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(context).data(File(item.filePath)).build(),
                                                        contentDescription = item.title,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                                MediaType.VIDEO -> {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize().background(SpiderNavyDark),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Movie, null, tint = SpiderRedBright, modifier = Modifier.size(28.dp))
                                                    }
                                                }
                                                MediaType.DOCUMENT -> {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize().background(SpiderNavyDark),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Description, null, tint = SpiderElectricBlue, modifier = Modifier.size(28.dp))
                                                    }
                                                }
                                                MediaType.ARCHIVE -> {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize().background(SpiderNavyDark),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.FolderZip, null, tint = SpiderGold, modifier = Modifier.size(28.dp))
                                                    }
                                                }
                                                else -> {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize().background(SpiderNavyDark),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Description, null, tint = SpiderElectricBlue, modifier = Modifier.size(28.dp))
                                                    }
                                                }
                                            }

                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(SpiderElectricBlue.copy(alpha = 0.25f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Squeezer Settings Card
                        if (currentTarget != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("compression_settings_card"),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderRed, SpiderElectricBlue)))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = currentTarget.title,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SpiderTextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${currentTarget.type.name} • ${CompressionEngine.formatFileSize(currentTarget.sizeBytes)}",
                                                    fontSize = 11.sp,
                                                    color = SpiderElectricBlue
                                                )
                                            }

                                            IconButton(
                                                onClick = { fullScreenPreviewItem = currentTarget }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Fullscreen,
                                                    contentDescription = "Preview Full Screen",
                                                    tint = SpiderElectricBlue,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        if (currentTarget.type == MediaType.PHOTO) {
                                            // Format Chips
                                            Column {
                                                Text("Target Compression Format:", fontSize = 12.sp, color = SpiderTextSecondary)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    ImageCompressFormat.values().forEach { fmt ->
                                                        FilterChip(
                                                            selected = compressionState.targetFormat == fmt,
                                                            onClick = { viewModel.updateCompressionSettings(format = fmt) },
                                                            label = { Text(fmt.name.replace("_", " "), fontSize = 11.sp) },
                                                            colors = FilterChipDefaults.filterChipColors(
                                                                selectedContainerColor = SpiderRed,
                                                                selectedLabelColor = Color.White,
                                                                containerColor = SpiderNavyElevated,
                                                                labelColor = SpiderTextSecondary
                                                            )
                                                        )
                                                    }
                                                }
                                            }

                                            // Quality Slider
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Quality Factor:", fontSize = 12.sp, color = SpiderTextSecondary)
                                                    Text("${compressionState.quality}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderRedBright)
                                                }
                                                Slider(
                                                    value = compressionState.quality.toFloat(),
                                                    onValueChange = { viewModel.updateCompressionSettings(quality = it.toInt()) },
                                                    valueRange = 10f..95f,
                                                    steps = 16,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = SpiderRedBright,
                                                        activeTrackColor = SpiderRed,
                                                        inactiveTrackColor = SpiderNavyElevated
                                                    )
                                                )
                                            }

                                            // Resolution Scale
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Resolution Scale:", fontSize = 12.sp, color = SpiderTextSecondary)
                                                    Text("${(compressionState.scaleFactor * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderGold)
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { scale ->
                                                        OutlinedButton(
                                                            onClick = { viewModel.updateCompressionSettings(scaleFactor = scale) },
                                                            modifier = Modifier.weight(1f),
                                                            colors = ButtonDefaults.outlinedButtonColors(
                                                                containerColor = if (compressionState.scaleFactor == scale) SpiderGold else SpiderNavyElevated,
                                                                contentColor = if (compressionState.scaleFactor == scale) SpiderNavyDark else SpiderTextPrimary
                                                            ),
                                                            contentPadding = PaddingValues(vertical = 4.dp)
                                                        ) {
                                                            Text("${(scale * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            // Document / Video compression description
                                            Text(
                                                text = "Non-image files are compressed into an optimized high-ratio archive envelope to free up vault storage.",
                                                fontSize = 11.sp,
                                                color = SpiderTextSecondary
                                            )
                                        }

                                        // Action Button
                                        Button(
                                            onClick = {
                                                if (compressionState.targetItem == null) {
                                                    viewModel.selectItemForCompression(currentTarget)
                                                }
                                                viewModel.executeCompression()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("compress_action_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = SpiderRed),
                                            enabled = !compressionState.isCompressing,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            if (compressionState.isCompressing) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Compressing Media File...", color = Color.White)
                                            } else {
                                                Icon(Icons.Default.Compress, null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("SQUEEZE & COMPRESS MEDIA", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Last Compression Result Card with Instant Full-Screen Viewer Button!
                        compressionState.lastResult?.let { result ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("compression_result_card"),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderGreenSuccess, SpiderElectricBlue)))
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AutoAwesome, null, tint = SpiderGreenSuccess, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Compression Complete!", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SpiderGreenSuccess)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = SpiderGreenSuccess.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "-${result.savedPercentage}% Saved",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SpiderGreenSuccess,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Original", fontSize = 11.sp, color = SpiderTextMuted)
                                                Text(CompressionEngine.formatFileSize(result.originalSizeBytes), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SpiderTextPrimary)
                                            }
                                            Column {
                                                Text("Compressed", fontSize = 11.sp, color = SpiderTextMuted)
                                                Text(CompressionEngine.formatFileSize(result.compressedSizeBytes), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SpiderElectricBlue)
                                            }
                                            Column {
                                                Text("Speed", fontSize = 11.sp, color = SpiderTextMuted)
                                                Text("${result.durationMs}ms", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SpiderGold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Prominent Full-Screen View Button
                                        Button(
                                            onClick = {
                                                val targetToView = result.newMediaItem ?: currentTarget
                                                if (targetToView != null) {
                                                    fullScreenPreviewItem = targetToView
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .testTag("view_compressed_fullscreen_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = SpiderNavyElevated),
                                            border = BorderStroke(1.dp, SpiderElectricBlue),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Fullscreen, null, tint = SpiderElectricBlue, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "View Compressed File in Full Screen",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SpiderElectricBlue
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 2: Zip Archiver
                        item {
                            Text(
                                text = "Create Compressed ZIP Archive",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderTextPrimary
                            )
                            Text(
                                text = "Select files from your vault to bundle and compress into a single .zip file.",
                                fontSize = 12.sp,
                                color = SpiderTextSecondary
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = zipArchiveName,
                                onValueChange = { zipArchiveName = it },
                                label = { Text("Archive Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SpiderNavySurface,
                                    unfocusedContainerColor = SpiderNavySurface,
                                    focusedBorderColor = SpiderElectricBlue,
                                    unfocusedBorderColor = SpiderNavyBorder,
                                    focusedTextColor = SpiderTextPrimary,
                                    unfocusedTextColor = SpiderTextPrimary
                                )
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Select Files (${selectedZipItems.size} selected)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SpiderElectricBlue
                                )
                                Button(
                                    onClick = {
                                        viewModel.compressSelectedItemsToZip(selectedZipItems.toList(), zipArchiveName)
                                        selectedZipItems.clear()
                                    },
                                    enabled = selectedZipItems.isNotEmpty() && !compressionState.isCompressing,
                                    colors = ButtonDefaults.buttonColors(containerColor = SpiderRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.FolderZip, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pack ZIP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Media selection items
                        items(allMedia) { item ->
                            val isChecked = selectedZipItems.any { it.id == item.id }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (isChecked) SpiderElectricBlue else SpiderNavyBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (isChecked) {
                                            selectedZipItems.removeAll { it.id == item.id }
                                        } else {
                                            selectedZipItems.add(item)
                                        }
                                    },
                                color = if (isChecked) SpiderNavyElevated else SpiderNavySurface
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isChecked) SpiderElectricBlue else SpiderNavyDark)
                                            .border(1.dp, SpiderElectricBlue, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isChecked) {
                                            Icon(Icons.Default.Check, null, tint = SpiderNavyDark, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SpiderTextPrimary, maxLines = 1)
                                        Text("${item.type.name} • ${CompressionEngine.formatFileSize(item.sizeBytes)}", fontSize = 11.sp, color = SpiderTextMuted)
                                    }
                                    IconButton(onClick = { fullScreenPreviewItem = item }) {
                                        Icon(Icons.Default.Fullscreen, "View Full Screen", tint = SpiderElectricBlue, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 3: Savings History
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderGreenSuccess, SpiderElectricBlue)))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Speed, null, tint = SpiderGreenSuccess, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("WEB OF SAVINGS METRIC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SpiderTextMuted)
                                        Text(
                                            text = CompressionEngine.formatFileSize(totalSavedBytes),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SpiderGreenSuccess
                                        )
                                        Text("Total phone storage reclaimed", fontSize = 11.sp, color = SpiderTextSecondary)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Compression Log",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderTextPrimary
                            )
                        }

                        if (history.isEmpty()) {
                            item {
                                Text("No compression history recorded yet.", color = SpiderTextMuted, fontSize = 12.sp)
                            }
                        } else {
                            items(history) { record ->
                                val matchingMedia = allMedia.firstOrNull { it.title.contains(record.fileName) || record.fileName.contains(it.title) }
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                                    color = SpiderNavySurface,
                                    border = BorderStroke(1.dp, SpiderNavyBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(record.fileName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SpiderTextPrimary, maxLines = 1)
                                            Text(
                                                "${CompressionEngine.formatFileSize(record.originalSizeBytes)} ➔ ${CompressionEngine.formatFileSize(record.compressedSizeBytes)} (${record.format})",
                                                fontSize = 11.sp,
                                                color = SpiderElectricBlue
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = SpiderGreenSuccess.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "-${record.savedPercentage}%",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SpiderGreenSuccess,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            if (matchingMedia != null) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(onClick = { fullScreenPreviewItem = matchingMedia }) {
                                                    Icon(Icons.Default.Fullscreen, "View Full Screen", tint = SpiderElectricBlue, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
