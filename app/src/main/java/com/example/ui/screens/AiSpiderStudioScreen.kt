package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaType
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
import com.example.ui.viewmodel.AiStudioEditMode
import com.example.ui.viewmodel.MiracleViewModel
import com.example.util.ArtisticStyle
import com.example.util.SpiderFilterType
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSpiderStudioScreen(
    viewModel: MiracleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allMedia by viewModel.allMedia.collectAsState()
    val aiState by viewModel.aiStudioState.collectAsState()

    val photoItems = allMedia.filter { it.type == MediaType.PHOTO }
    var promptInput by remember { mutableStateOf("") }
    var showFineTuning by remember { mutableStateOf(false) }

    // Object Eraser Mask State
    var brushRadius by remember { mutableStateOf(35f) }
    val maskPaths = remember { mutableStateListOf<Path>() }
    var currentDrawingPath by remember { mutableStateOf<Path?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Upscale settings
    var selectedUpscaleFactor by remember { mutableStateOf(2.0f) }
    var enhanceDetails by remember { mutableStateOf(true) }
    var noiseReduction by remember { mutableStateOf(true) }

    // If no item loaded, load the first photo
    if (aiState.targetItem == null && photoItems.isNotEmpty()) {
        viewModel.loadItemForAiStudio(photoItems.first())
    }

    Box(modifier = modifier.fillMaxSize().background(SpiderNavy)) {
        SpiderWebBackground(alpha = 0.1f)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Photo Selector Carousel
            item {
                Text(
                    text = "Select Photo to Enhance",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpiderTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (photoItems.isEmpty()) {
                    Text("No photos found in Vault. Import photos from Vault tab.", color = SpiderTextMuted, fontSize = 12.sp)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(photoItems) { item ->
                            val isSelected = aiState.targetItem?.id == item.id
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) SpiderElectricBlue else SpiderNavyBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        maskPaths.clear()
                                        viewModel.loadItemForAiStudio(item)
                                    }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(File(item.filePath)).build(),
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // AI Tool Mode Selector Tabs
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SpiderNavySurface,
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderNavyBorder, SpiderElectricBlue)))
                ) {
                    LazyRow(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(AiStudioEditMode.values()) { mode ->
                            val isSelected = aiState.activeEditMode == mode
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setAiEditMode(mode) },
                                color = if (isSelected) SpiderRed else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(mode.emoji, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = mode.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else SpiderTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Main Interactive Canvas Preview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .testTag("ai_preview_canvas"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderRed, SpiderElectricBlue)))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { canvasSize = it.size },
                        contentAlignment = Alignment.Center
                    ) {
                        val preview = aiState.previewBitmap
                        val source = aiState.sourceBitmap

                        if (preview != null) {
                            if (aiState.showSplitComparison && source != null) {
                                // Split View (Before / After)
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(aiState.splitComparisonRatio)
                                            .fillMaxSize()
                                    ) {
                                        Image(
                                            bitmap = source.asImageBitmap(),
                                            contentDescription = "Original",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                                            shape = RoundedCornerShape(4.dp),
                                            color = SpiderNavyDark.copy(alpha = 0.8f)
                                        ) {
                                            Text("ORIGINAL", fontSize = 9.sp, color = SpiderTextSecondary, modifier = Modifier.padding(4.dp))
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .fillMaxSize()
                                            .background(SpiderElectricBlue)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f - aiState.splitComparisonRatio)
                                            .fillMaxSize()
                                    ) {
                                        Image(
                                            bitmap = preview.asImageBitmap(),
                                            contentDescription = "AI Enhanced",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                                            shape = RoundedCornerShape(4.dp),
                                            color = SpiderRed.copy(alpha = 0.8f)
                                        ) {
                                            Text("AI ENHANCED", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(4.dp))
                                        }
                                    }
                                }
                            } else {
                                Image(
                                    bitmap = preview.asImageBitmap(),
                                    contentDescription = "AI Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, null, tint = SpiderTextMuted, modifier = Modifier.size(48.dp))
                                Text("Select a superhero photo to launch AI Studio", color = SpiderTextSecondary, fontSize = 12.sp)
                            }
                        }

                        // Drawing Overlay for Object Eraser / Inpainting
                        if (aiState.activeEditMode == AiStudioEditMode.OBJECT_REMOVER && preview != null) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                val path = Path().apply { moveTo(offset.x, offset.y) }
                                                currentDrawingPath = path
                                                maskPaths.add(path)
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                currentDrawingPath?.lineTo(change.position.x, change.position.y)
                                            },
                                            onDragEnd = {
                                                currentDrawingPath = null
                                            }
                                        )
                                    }
                            ) {
                                maskPaths.forEach { path ->
                                    drawPath(
                                        path = path,
                                        color = SpiderRedBright.copy(alpha = 0.55f),
                                        style = Stroke(
                                            width = brushRadius,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                        }

                        // Top Controls Overlay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SpiderNavyDark.copy(alpha = 0.85f)
                            ) {
                                Text(
                                    text = aiState.activeEditMode.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SpiderElectricBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Before / After comparison toggle
                                IconButton(
                                    onClick = { viewModel.setSplitComparison(!aiState.showSplitComparison) },
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(if (aiState.showSplitComparison) SpiderElectricBlue else SpiderNavyDark.copy(alpha = 0.8f))
                                ) {
                                    Icon(Icons.Default.Compare, "Compare", tint = if (aiState.showSplitComparison) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.rotateImage() },
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(SpiderNavyDark.copy(alpha = 0.8f))
                                ) {
                                    Icon(Icons.Default.RotateRight, "Rotate", tint = Color.White, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.flipImage() },
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(SpiderNavyDark.copy(alpha = 0.8f))
                                ) {
                                    Icon(Icons.Default.Flip, "Flip", tint = Color.White, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = {
                                        maskPaths.clear()
                                        viewModel.resetAiStudioEdits()
                                    },
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(SpiderNavyDark.copy(alpha = 0.8f))
                                ) {
                                    Icon(Icons.Default.RestartAlt, "Reset", tint = SpiderRedBright, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Processing Spinners
                        if (aiState.isUpscaling || aiState.isInpainting || aiState.isAiAnalyzing) {
                            val statusLabel = when {
                                aiState.isUpscaling -> "AI Super-Resolution Upscaling (${selectedUpscaleFactor.toInt()}x)..."
                                aiState.isInpainting -> "AI Erasing Selected Objects & Blending Background..."
                                else -> "Spider-Sense Scanning Image with Gemini AI..."
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(SpiderNavyDark.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = SpiderRedBright, strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(statusLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderElectricBlue)
                                }
                            }
                        }
                    }
                }
            }

            // Split Comparison Slider (when split mode is on)
            if (aiState.showSplitComparison) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SpiderNavySurface).padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Before / After Comparison Split", fontSize = 11.sp, color = SpiderTextSecondary)
                            Text("${(aiState.splitComparisonRatio * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SpiderElectricBlue)
                        }
                        Slider(
                            value = aiState.splitComparisonRatio,
                            onValueChange = { viewModel.setSplitComparison(true, it) },
                            colors = SliderDefaults.colors(thumbColor = SpiderElectricBlue, activeTrackColor = SpiderElectricBlue)
                        )
                    }
                }
            }

            // ==========================================
            // MODE SPECIFIC CONTROLS
            // ==========================================
            when (aiState.activeEditMode) {
                // 1. AI ARTISTIC STYLE TRANSFER
                AiStudioEditMode.ARTISTIC_STYLE -> {
                    item {
                        Text(
                            text = "AI ARTISTIC MULTIVERSE STYLES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpiderTextPrimary,
                            letterSpacing = 1.sp
                        )
                    }

                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ArtisticStyle.values()) { style ->
                                val isSelected = aiState.selectedArtStyle == style
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) SpiderElectricBlue else SpiderNavyBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.applyArtisticStyle(style) },
                                    color = if (isSelected) SpiderNavyElevated else SpiderNavySurface
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(style.iconEmoji, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = style.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) SpiderElectricBlue else SpiderTextPrimary
                                            )
                                        }
                                        Text(
                                            text = style.subtitle,
                                            fontSize = 9.sp,
                                            color = SpiderTextMuted,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Style Intensity Slider
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SpiderNavySurface)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Style Transfer Intensity", fontSize = 11.sp, color = SpiderTextSecondary)
                                Text("${(aiState.styleIntensity * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SpiderElectricBlue)
                            }
                            Slider(
                                value = aiState.styleIntensity,
                                onValueChange = { viewModel.applyArtisticStyle(aiState.selectedArtStyle, it) },
                                valueRange = 0.1f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = SpiderElectricBlue, activeTrackColor = SpiderElectricBlue)
                            )
                        }
                    }
                }

                // 2. AI OBJECT REMOVAL / INPAINTING
                AiStudioEditMode.OBJECT_REMOVER -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(SpiderRed, SpiderElectricBlue)))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Brush, null, tint = SpiderRedBright, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("AI Magic Eraser / Inpainting", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                    }
                                    if (maskPaths.isNotEmpty()) {
                                        TextButton(onClick = { maskPaths.clear() }) {
                                            Text("Clear Mask", color = SpiderRedBright, fontSize = 11.sp)
                                        }
                                    }
                                }

                                Text(
                                    "Draw with your finger on the photo above to highlight photobombers, wires, or unwanted objects. AI will erase and reconstruct the background.",
                                    fontSize = 11.sp,
                                    color = SpiderTextSecondary,
                                    lineHeight = 15.sp
                                )

                                // Brush Size Slider
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Brush Size:", fontSize = 11.sp, color = SpiderTextSecondary)
                                    Text("${brushRadius.toInt()} px", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SpiderGold)
                                }
                                Slider(
                                    value = brushRadius,
                                    onValueChange = { brushRadius = it },
                                    valueRange = 15f..80f,
                                    colors = SliderDefaults.colors(thumbColor = SpiderGold, activeTrackColor = SpiderGold)
                                )

                                Button(
                                    onClick = {
                                        val preview = aiState.previewBitmap ?: return@Button
                                        if (maskPaths.isEmpty()) {
                                            Toast.makeText(context, "Please brush over the object to erase first!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        // Render mask paths to a mask Bitmap matching preview dimensions
                                        val maskBmp = Bitmap.createBitmap(preview.width, preview.height, Bitmap.Config.ARGB_8888)
                                        val maskCanvas = AndroidCanvas(maskBmp)
                                        val maskPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                                            color = android.graphics.Color.RED
                                            style = AndroidPaint.Style.STROKE
                                            strokeCap = AndroidPaint.Cap.ROUND
                                            strokeJoin = AndroidPaint.Join.ROUND
                                            strokeWidth = brushRadius * (preview.width.toFloat() / (canvasSize.width.coerceAtLeast(1)))
                                        }

                                        val scaleX = preview.width.toFloat() / (canvasSize.width.coerceAtLeast(1))
                                        val scaleY = preview.height.toFloat() / (canvasSize.height.coerceAtLeast(1))

                                        maskPaths.forEach { composePath ->
                                            val androidPath = composePath.asAndroidPath()
                                            val scaledPath = AndroidPath(androidPath)
                                            val matrix = android.graphics.Matrix()
                                            matrix.setScale(scaleX, scaleY)
                                            scaledPath.transform(matrix)
                                            maskCanvas.drawPath(scaledPath, maskPaint)
                                        }

                                        viewModel.executeObjectRemoval(maskBmp)
                                        maskPaths.clear()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SpiderRed),
                                    enabled = !aiState.isInpainting && maskPaths.isNotEmpty()
                                ) {
                                    Icon(Icons.Default.CleaningServices, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Erase Highlighted Object 🧹", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 3. AI SUPER-RESOLUTION UPSCALE
                AiStudioEditMode.SUPER_RESOLUTION -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(SpiderElectricBlue, SpiderGold)))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HighQuality, null, tint = SpiderElectricBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("AI Super-Resolution Upscaler", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                }

                                Text(
                                    "Enhance image resolution up to 4K fidelity with AI neural edge sharpening and noise suppression.",
                                    fontSize = 11.sp,
                                    color = SpiderTextSecondary,
                                    lineHeight = 15.sp
                                )

                                // Scale factor selector
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(2.0f to "2x Super HD", 4.0f to "4x Ultra 4K").forEach { (scale, title) ->
                                        val isSelected = selectedUpscaleFactor == scale
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) SpiderElectricBlue else SpiderNavyBorder,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { selectedUpscaleFactor = scale },
                                            color = if (isSelected) SpiderNavyElevated else SpiderNavyDark
                                        ) {
                                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) SpiderElectricBlue else SpiderTextPrimary
                                                )
                                            }
                                        }
                                    }
                                }

                                // Detail & Noise Toggles
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Unsharp Detail Enhancement", fontSize = 11.sp, color = SpiderTextPrimary)
                                    Switch(
                                        checked = enhanceDetails,
                                        onCheckedChange = { enhanceDetails = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = SpiderElectricBlue, checkedTrackColor = SpiderNavyBorder)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("High-Frequency Noise Filter", fontSize = 11.sp, color = SpiderTextPrimary)
                                    Switch(
                                        checked = noiseReduction,
                                        onCheckedChange = { noiseReduction = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = SpiderElectricBlue, checkedTrackColor = SpiderNavyBorder)
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.executeSuperResolution(
                                            scaleFactor = selectedUpscaleFactor,
                                            enhanceDetails = enhanceDetails,
                                            noiseReduction = noiseReduction
                                        )
                                        Toast.makeText(context, "Upscaled to ${selectedUpscaleFactor.toInt()}x Super-Resolution!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SpiderElectricBlue),
                                    enabled = !aiState.isUpscaling
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Execute Super-Resolution Upscale ⚡", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 4. SPIDER-SENSE VISION (Gemini AI multimodal assistant)
                AiStudioEditMode.SPIDER_VISION -> {
                    item {
                        val analysis = aiState.aiAnalysisResult
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("spider_sense_analysis_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SpiderNavyElevated),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderRedBright, SpiderGold)))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, null, tint = SpiderGold, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Spider-Sense AI Vision Inspector", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                    }
                                    Button(
                                        onClick = { viewModel.triggerSpiderSenseAnalysis() },
                                        colors = ButtonDefaults.buttonColors(containerColor = SpiderRed),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Scan AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (analysis != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SpiderRedDark.copy(alpha = 0.35f))
                                            .border(1.dp, SpiderRed.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = analysis.comicCaption,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SpiderGold
                                        )
                                    }

                                    Text(
                                        text = analysis.description,
                                        fontSize = 11.sp,
                                        color = SpiderTextPrimary,
                                        lineHeight = 16.sp
                                    )

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(analysis.suggestedTags) { tag ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(SpiderNavyDark)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("#$tag", fontSize = 10.sp, color = SpiderElectricBlue)
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Tap 'Scan AI' to let Gemini Vision inspect your photo, suggest comic style enhancements, and generate tags.",
                                        fontSize = 11.sp,
                                        color = SpiderTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. AI PROMPT & PUBLIC WISH STUDIO
                AiStudioEditMode.AI_PROMPT_STUDIO -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("ai_prompt_studio_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderRedBright, SpiderElectricBlue)))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = SpiderElectricBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Prompt AI: Edit for Public's Wish", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                }

                                Text(
                                    text = "Give natural language instructions to AI on how to edit this photo or video according to the public's wishes and trending styles.",
                                    fontSize = 11.sp,
                                    color = SpiderTextSecondary,
                                    lineHeight = 15.sp
                                )

                                // Trending Public Wish Suggestion Chips
                                Text("Popular Public Wishes & Trends:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SpiderGold)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val promptSuggestions = listOf(
                                        "Make it viral & vibrant for Instagram",
                                        "Comic book superhero action pop",
                                        "Dramatic cyberpunk dark mood",
                                        "Retro 90s vintage film aesthetic",
                                        "Clean high-contrast black and white",
                                        "Brighten colors & sharpen details"
                                    )
                                    items(promptSuggestions) { suggestion ->
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    promptInput = suggestion
                                                    viewModel.applyAiPromptEdit(suggestion)
                                                },
                                            color = SpiderNavyElevated,
                                            border = BorderStroke(1.dp, SpiderNavyBorder)
                                        ) {
                                            Text(
                                                text = suggestion,
                                                fontSize = 10.sp,
                                                color = SpiderTextPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                // Prompt Input Field
                                OutlinedTextField(
                                    value = promptInput,
                                    onValueChange = { promptInput = it },
                                    label = { Text("Enter your custom edit prompt...") },
                                    placeholder = { Text("e.g. Add high contrast comic pop colors with sharp edges") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SpiderNavyDark,
                                        unfocusedContainerColor = SpiderNavyDark,
                                        focusedBorderColor = SpiderElectricBlue,
                                        unfocusedBorderColor = SpiderNavyBorder,
                                        focusedTextColor = SpiderTextPrimary,
                                        unfocusedTextColor = SpiderTextPrimary,
                                        focusedPlaceholderColor = SpiderTextMuted,
                                        unfocusedPlaceholderColor = SpiderTextMuted
                                    )
                                )

                                // Execute Prompt Button
                                Button(
                                    onClick = {
                                        if (promptInput.isNotBlank()) {
                                            viewModel.applyAiPromptEdit(promptInput)
                                        } else {
                                            Toast.makeText(context, "Please enter an edit prompt first", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SpiderRed),
                                    enabled = promptInput.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("APPLY PUBLIC WISH AI EDIT ✨", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                // AI Response / Commentary Box
                                aiState.aiAnalysisResult?.let { analysis ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SpiderNavyDark)
                                            .border(1.dp, SpiderElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AutoAwesome, null, tint = SpiderElectricBlue, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("AI Director Commentary:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SpiderElectricBlue)
                                            }
                                            Text(analysis.comicCaption, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SpiderGold)
                                            Text(analysis.description, fontSize = 10.sp, color = SpiderTextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fine-Tuning Light & Color Sliders Accordion
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, null, tint = SpiderElectricBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Precision Light & Color Sliders", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                    }
                    TextButton(onClick = { showFineTuning = !showFineTuning }) {
                        Text(if (showFineTuning) "Hide" else "Show Sliders", color = SpiderElectricBlue, fontSize = 12.sp)
                    }
                }

                AnimatedVisibility(visible = showFineTuning) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SpiderNavySurface)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Brightness
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Brightness:", fontSize = 11.sp, color = SpiderTextSecondary)
                            Text("${aiState.brightness.toInt()}", fontSize = 11.sp, color = SpiderTextPrimary)
                        }
                        Slider(
                            value = aiState.brightness,
                            onValueChange = { viewModel.updateAdjustments(brightness = it) },
                            valueRange = -60f..60f,
                            colors = SliderDefaults.colors(thumbColor = SpiderElectricBlue, activeTrackColor = SpiderElectricBlue)
                        )

                        // Contrast
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Contrast:", fontSize = 11.sp, color = SpiderTextSecondary)
                            Text(String.format("%.1fx", aiState.contrast), fontSize = 11.sp, color = SpiderTextPrimary)
                        }
                        Slider(
                            value = aiState.contrast,
                            onValueChange = { viewModel.updateAdjustments(contrast = it) },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(thumbColor = SpiderRedBright, activeTrackColor = SpiderRed)
                        )

                        // Saturation
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Saturation:", fontSize = 11.sp, color = SpiderTextSecondary)
                            Text(String.format("%.1fx", aiState.saturation), fontSize = 11.sp, color = SpiderTextPrimary)
                        }
                        Slider(
                            value = aiState.saturation,
                            onValueChange = { viewModel.updateAdjustments(saturation = it) },
                            valueRange = 0.0f..2.5f,
                            colors = SliderDefaults.colors(thumbColor = SpiderGold, activeTrackColor = SpiderGold)
                        )
                    }
                }
            }

            // Save to Vault Action Button
            item {
                Button(
                    onClick = {
                        val baseName = aiState.targetItem?.title ?: "spider_edit"
                        viewModel.saveAiStudioResult(baseName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_ai_edit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SpiderRed),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !aiState.isSaving && aiState.previewBitmap != null
                ) {
                    if (aiState.isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving to Miracle Vault...", color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SAVE EDITED MASTER TO VAULT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}
