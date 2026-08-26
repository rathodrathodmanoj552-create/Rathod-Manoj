package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.components.BentoFeatureGrid
import com.example.ui.components.FullScreenMediaViewer
import com.example.ui.components.MediaItemCard
import com.example.ui.components.ShareDialog
import com.example.ui.components.SpiderSensePulseBanner
import com.example.ui.components.SpiderStorageMeter
import com.example.ui.components.SpiderWebBackground
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
import com.example.ui.viewmodel.MiracleViewModel
import com.example.util.CompressionEngine
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(
    viewModel: MiracleViewModel,
    onNavigateToCompress: (MediaItem) -> Unit,
    onNavigateToAiEdit: (MediaItem) -> Unit,
    onNavigateToTab: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()
    val filteredMedia by viewModel.filteredMedia.collectAsState()
    val totalSizeBytes by viewModel.totalSizeBytes.collectAsState()
    val totalSavedBytes by viewModel.totalSavedBytes.collectAsState()
    val totalCount by viewModel.totalMediaCount.collectAsState()

    var showFabMenu by remember { mutableStateOf(false) }
    var previewItem by remember { mutableStateOf<MediaItem?>(null) }
    var itemToDelete by remember { mutableStateOf<MediaItem?>(null) }
    var itemToShare by remember { mutableStateOf<MediaItem?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importUris(uris)
        }
    }

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importUris(uris)
        }
    }

    val photoCount = allMedia.count { it.type == MediaType.PHOTO }
    val videoCount = allMedia.count { it.type == MediaType.VIDEO }
    val docCount = allMedia.count { it.type == MediaType.DOCUMENT || it.type == MediaType.ARCHIVE }

    val photoSizeBytes = allMedia.filter { it.type == MediaType.PHOTO }.sumOf { it.sizeBytes }
    val videoSizeBytes = allMedia.filter { it.type == MediaType.VIDEO }.sumOf { it.sizeBytes }
    val docSizeBytes = allMedia.filter { it.type == MediaType.DOCUMENT || it.type == MediaType.ARCHIVE }.sumOf { it.sizeBytes }

    Box(modifier = modifier.fillMaxSize().background(SpiderBentoDark)) {
        SpiderWebBackground(alpha = 0.08f)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Bento Storage Card with Global Capacity & Tool Breakdown
            item {
                SpiderStorageMeter(
                    totalSizeBytes = totalSizeBytes,
                    totalSavedBytes = totalSavedBytes,
                    totalCount = totalCount,
                    photoCount = photoCount,
                    videoCount = videoCount,
                    docCount = docCount,
                    photoSizeBytes = photoSizeBytes,
                    videoSizeBytes = videoSizeBytes,
                    docSizeBytes = docSizeBytes,
                    onCategoryClick = { category ->
                        viewModel.setCategory(category)
                    }
                )
            }

            // 2. Bento Feature Grid Matrix (Galleries, AI Edit, Zip-Web, Spider-Vault)
            item {
                BentoFeatureGrid(
                    photoCount = photoCount,
                    totalCount = totalCount,
                    onGalleriesClick = {
                        viewModel.setCategory(MediaType.PHOTO)
                    },
                    onAiEditClick = {
                        val firstPhoto = allMedia.firstOrNull { it.type == MediaType.PHOTO }
                        if (firstPhoto != null) {
                            onNavigateToAiEdit(firstPhoto)
                        } else {
                            onNavigateToTab(2) // Jump to AI Studio tab
                        }
                    },
                    onZipWebClick = {
                        val firstItem = allMedia.firstOrNull()
                        if (firstItem != null) {
                            onNavigateToCompress(firstItem)
                        } else {
                            onNavigateToTab(1) // Jump to Compress tab
                        }
                    },
                    onVaultClick = {
                        viewModel.setCategory(null)
                    }
                )
            }

            // 3. Spider-Sense Interactive AI Banner
            item {
                SpiderSensePulseBanner(
                    isScanning = uiState.isSpiderSenseScanning,
                    title = "Spider-Sense Vault Scanner",
                    subtitle = "Neural indexing & media encryption enabled",
                    onClick = {
                        val firstPhoto = allMedia.firstOrNull { it.type == MediaType.PHOTO }
                        if (firstPhoto != null) {
                            onNavigateToAiEdit(firstPhoto)
                        }
                    }
                )
            }

            // 4. Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    placeholder = { Text("Search photos, docs, tags...", color = SpiderSlate500, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SpiderRed)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = SpiderSlate400)
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SpiderBentoCard,
                        unfocusedContainerColor = SpiderBentoCard,
                        focusedBorderColor = SpiderRed,
                        unfocusedBorderColor = SpiderBentoBorder,
                        focusedTextColor = SpiderSlate100,
                        unfocusedTextColor = SpiderSlate100
                    ),
                    singleLine = true
                )
            }

            // 5. Category Filter Chips & View Mode Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedCategory == null && !uiState.isFavoritesOnly,
                                onClick = {
                                    viewModel.setCategory(null)
                                    if (uiState.isFavoritesOnly) viewModel.toggleFavoritesFilter()
                                },
                                label = { Text("All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(14.dp),
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = uiState.selectedCategory == null && !uiState.isFavoritesOnly, borderColor = SpiderBentoBorder, selectedBorderColor = SpiderRed),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpiderRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = SpiderBentoCard,
                                    labelColor = SpiderSlate300
                                )
                            )
                        }

                        item {
                            FilterChip(
                                selected = uiState.selectedCategory == MediaType.PHOTO,
                                onClick = { viewModel.setCategory(MediaType.PHOTO) },
                                label = { Text("Photos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Image, null, modifier = Modifier.size(14.dp)) },
                                shape = RoundedCornerShape(14.dp),
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = uiState.selectedCategory == MediaType.PHOTO, borderColor = SpiderBentoBorder, selectedBorderColor = SpiderElectricBlue),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpiderNavyAccent,
                                    selectedLabelColor = Color.White,
                                    containerColor = SpiderBentoCard,
                                    labelColor = SpiderSlate300
                                )
                            )
                        }

                        item {
                            FilterChip(
                                selected = uiState.selectedCategory == MediaType.VIDEO,
                                onClick = { viewModel.setCategory(MediaType.VIDEO) },
                                label = { Text("Videos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Movie, null, modifier = Modifier.size(14.dp)) },
                                shape = RoundedCornerShape(14.dp),
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = uiState.selectedCategory == MediaType.VIDEO, borderColor = SpiderBentoBorder, selectedBorderColor = SpiderRedBright),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpiderRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = SpiderBentoCard,
                                    labelColor = SpiderSlate300
                                )
                            )
                        }

                        item {
                            FilterChip(
                                selected = uiState.selectedCategory == MediaType.DOCUMENT,
                                onClick = { viewModel.setCategory(MediaType.DOCUMENT) },
                                label = { Text("Docs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Description, null, modifier = Modifier.size(14.dp)) },
                                shape = RoundedCornerShape(14.dp),
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = uiState.selectedCategory == MediaType.DOCUMENT, borderColor = SpiderBentoBorder, selectedBorderColor = SpiderGold),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpiderBentoElevated,
                                    selectedLabelColor = SpiderGold,
                                    containerColor = SpiderBentoCard,
                                    labelColor = SpiderSlate300
                                )
                            )
                        }

                        item {
                            FilterChip(
                                selected = uiState.selectedCategory == MediaType.ARCHIVE,
                                onClick = { viewModel.setCategory(MediaType.ARCHIVE) },
                                label = { Text("Archives", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.FolderZip, null, modifier = Modifier.size(14.dp)) },
                                shape = RoundedCornerShape(14.dp),
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = uiState.selectedCategory == MediaType.ARCHIVE, borderColor = SpiderBentoBorder, selectedBorderColor = SpiderGreenSuccess),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpiderBentoElevated,
                                    selectedLabelColor = SpiderGreenSuccess,
                                    containerColor = SpiderBentoCard,
                                    labelColor = SpiderSlate300
                                )
                            )
                        }

                        item {
                            FilterChip(
                                selected = uiState.isFavoritesOnly,
                                onClick = { viewModel.toggleFavoritesFilter() },
                                label = { Text("Starred", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Favorite, null, modifier = Modifier.size(14.dp)) },
                                shape = RoundedCornerShape(14.dp),
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = uiState.isFavoritesOnly, borderColor = SpiderBentoBorder, selectedBorderColor = SpiderRedBright),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpiderRedBright,
                                    selectedLabelColor = Color.White,
                                    containerColor = SpiderBentoCard,
                                    labelColor = SpiderSlate300
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleViewMode() },
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SpiderBentoCard)
                            .border(1.dp, SpiderBentoBorder, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = if (uiState.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle View",
                            tint = SpiderRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 6. Media Items Listing
            if (filteredMedia.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SpiderBentoCard)
                            .border(1.dp, SpiderBentoBorder, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SpiderBentoElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Empty",
                                    tint = SpiderRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Vault Empty in this Section",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiderSlate100
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the Web-Shooter button below to import photos, videos, or documents to Miracle Vault.",
                                fontSize = 11.sp,
                                color = SpiderSlate400,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                if (uiState.isGridView) {
                    val chunked = filteredMedia.chunked(2)
                    items(chunked) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (item in rowItems) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MediaItemCard(
                                        item = item,
                                        onItemClick = { previewItem = item },
                                        onFavoriteToggle = { viewModel.toggleFavorite(item) },
                                        onCompressClick = { onNavigateToCompress(item) },
                                        onEditAiClick = { onNavigateToAiEdit(item) },
                                        onDeleteClick = { itemToDelete = item },
                                        onShareClick = { itemToShare = item }
                                    )
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(filteredMedia, key = { it.id }) { item ->
                        MediaItemCard(
                            item = item,
                            onItemClick = { previewItem = item },
                            onFavoriteToggle = { viewModel.toggleFavorite(item) },
                            onCompressClick = { onNavigateToCompress(item) },
                            onEditAiClick = { onNavigateToAiEdit(item) },
                            onDeleteClick = { itemToDelete = item },
                            onShareClick = { itemToShare = item }
                        )
                    }
                }
            }
        }

        // Floating Web-Shooter FAB Menu
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedVisibility(
                    visible = showFabMenu,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Import Photos
                        FloatingActionButton(
                            onClick = {
                                showFabMenu = false
                                photoPickerLauncher.launch("image/*")
                            },
                            containerColor = SpiderElectricBlue,
                            contentColor = SpiderNavyDark,
                            shape = CircleShape,
                            modifier = Modifier.size(46.dp).testTag("import_photos_fab")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Import Photos")
                        }

                        // Import Videos
                        FloatingActionButton(
                            onClick = {
                                showFabMenu = false
                                photoPickerLauncher.launch("video/*")
                            },
                            containerColor = SpiderRedBright,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(46.dp).testTag("import_videos_fab")
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = "Import Videos")
                        }

                        // Import Documents
                        FloatingActionButton(
                            onClick = {
                                showFabMenu = false
                                docPickerLauncher.launch(arrayOf("*/*"))
                            },
                            containerColor = SpiderGold,
                            contentColor = SpiderNavyDark,
                            shape = CircleShape,
                            modifier = Modifier.size(46.dp).testTag("import_docs_fab")
                        ) {
                            Icon(Icons.Default.Description, contentDescription = "Import Docs")
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = SpiderRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .border(2.dp, SpiderElectricBlue, RoundedCornerShape(18.dp))
                        .testTag("main_web_fab")
                ) {
                    Icon(
                        imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Web-Shooter Add",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Full-Screen Immersive Media Viewer
        previewItem?.let { item ->
            FullScreenMediaViewer(
                item = item,
                onDismiss = { previewItem = null },
                onDirectShare = {
                    viewModel.shareMedia(item)
                },
                onOpenExternally = {
                    viewModel.openMediaExternally(item)
                },
                onToggleFavorite = {
                    viewModel.toggleFavorite(item)
                },
                onCompress = {
                    previewItem = null
                    onNavigateToCompress(item)
                },
                onAiEdit = {
                    previewItem = null
                    onNavigateToAiEdit(item)
                },
                onDelete = {
                    previewItem = null
                    itemToDelete = item
                },
                onCreateQuantumLink = {
                    previewItem = null
                    itemToShare = item
                }
            )
        }

        // Share & Link Dialog
        itemToShare?.let { item ->
            ShareDialog(
                item = item,
                onDismiss = { itemToShare = null },
                onDirectShareFile = {
                    viewModel.shareMedia(item)
                },
                onGenerateLink = { link ->
                    viewModel.createShareLink(link)
                }
            )
        }

        // Delete Confirmation Dialog
        itemToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                containerColor = SpiderNavySurface,
                titleContentColor = SpiderRedBright,
                textContentColor = SpiderTextPrimary,
                title = { Text("Delete Media Item?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete '${item.title}' from Miracle Vault? This file will be permanently removed.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteMedia(item)
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpiderRed)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("Cancel", color = SpiderTextSecondary)
                    }
                }
            )
        }
    }
}
