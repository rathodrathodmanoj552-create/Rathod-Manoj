package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiVisionService
import com.example.ui.components.AnimatedSpiderLogo
import com.example.ui.components.SpiderWebBackground
import com.example.ui.components.WebLockDialog
import com.example.ui.theme.SpiderBlueAccent
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
import com.example.ui.theme.SpiderTextMuted
import com.example.ui.theme.SpiderTextPrimary
import com.example.ui.theme.SpiderTextSecondary
import com.example.ui.viewmodel.MiracleViewModel
import com.example.ui.viewmodel.SpiderSuitTheme
import com.example.util.CompressionEngine

@Composable
fun SuitSettingsScreen(
    viewModel: MiracleViewModel,
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val totalSizeBytes by viewModel.totalSizeBytes.collectAsState()
    val totalSavedBytes by viewModel.totalSavedBytes.collectAsState()
    val totalCount by viewModel.totalMediaCount.collectAsState()
    val isApiKeyActive = GeminiVisionService.isApiKeyConfigured()

    var showPinSetupDialog by remember { mutableStateOf(false) }

    if (showPinSetupDialog) {
        WebLockDialog(
            isSettingUpPin = true,
            onPinEntered = { newPin ->
                viewModel.toggleWebLock(true, newPin)
                showPinSetupDialog = false
            },
            onDismiss = { showPinSetupDialog = false }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(SpiderNavy)) {
        SpiderWebBackground(alpha = 0.1f)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedSpiderLogo(
                        size = 40.dp,
                        showGlowRing = true,
                        showWebAura = true,
                        modifier = Modifier.testTag("settings_spider_logo")
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("MIRACLE PROTOCOLS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                        Text("Spider-Vault Security, Auth & Suit Settings", fontSize = 11.sp, color = SpiderTextSecondary)
                    }
                }
            }

            // USER AUTHENTICATION PROFILE CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderElectricBlue, SpiderGold)))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(SpiderRed)
                                        .border(2.dp, SpiderElectricBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser?.avatar ?: "🕸️",
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentUser?.alias ?: "Hero Operative",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SpiderTextPrimary
                                    )
                                    Text(
                                        text = currentUser?.email ?: "guest@spider.net",
                                        fontSize = 11.sp,
                                        color = SpiderTextSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SpiderNavyElevated
                            ) {
                                Text(
                                    text = currentUser?.provider?.name ?: "LOCAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SpiderElectricBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onNavigateToAuth,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Switch Account", fontSize = 11.sp, color = SpiderElectricBlue)
                            }

                            Button(
                                onClick = {
                                    viewModel.logout()
                                    onNavigateToAuth()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SpiderNavyDark)
                            ) {
                                Icon(Icons.Default.Logout, null, tint = SpiderRedBright, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sign Out", fontSize = 11.sp, color = SpiderRedBright)
                            }
                        }
                    }
                }
            }

            // SPIDER WEB-LOCK PIN SECURITY
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderRed, SpiderElectricBlue)))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (currentUser?.isWebLockEnabled == true) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (currentUser?.isWebLockEnabled == true) SpiderGold else SpiderTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Spider Web-Lock Security PIN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                    Text("4-digit biometric PIN protection for Vault", fontSize = 11.sp, color = SpiderTextSecondary)
                                }
                            }

                            Switch(
                                checked = currentUser?.isWebLockEnabled == true,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        showPinSetupDialog = true
                                    } else {
                                        viewModel.toggleWebLock(false, null)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SpiderGold,
                                    checkedTrackColor = SpiderNavyBorder
                                )
                            )
                        }
                    }
                }
            }

            // Spider Suit Theme Picker
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderRed, SpiderElectricBlue)))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, null, tint = SpiderElectricBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Spider-Man Suit Theme", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                        }

                        SpiderSuitTheme.values().forEach { suit ->
                            val isSelected = uiState.currentSuitTheme == suit
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) SpiderElectricBlue else SpiderNavyBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setSuitTheme(suit) },
                                color = if (isSelected) SpiderNavyElevated else SpiderNavyDark
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(suit.displayName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) SpiderElectricBlue else SpiderTextPrimary)
                                        Text(suit.description, fontSize = 11.sp, color = SpiderTextSecondary)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, null, tint = SpiderElectricBlue, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Storage Diagnostics Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderGreenSuccess, SpiderElectricBlue)))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, null, tint = SpiderGreenSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vault Storage Telemetry", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                        }

                        DiagnosticRow("Secured Files in Vault", "$totalCount items")
                        DiagnosticRow("Total Active Space Used", CompressionEngine.formatFileSize(totalSizeBytes))
                        DiagnosticRow("Compression Reclaimed Space", "-${CompressionEngine.formatFileSize(totalSavedBytes)}")
                        DiagnosticRow("Neural Optimizer Status", "Fully Operational")
                    }
                }
            }

            // Gemini AI & Spider-Sense Service Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderGold, SpiderRed)))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, null, tint = SpiderGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Spider-Sense Gemini AI Core", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gemini API Key Connection", fontSize = 12.sp, color = SpiderTextSecondary)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isApiKeyActive) SpiderGreenSuccess.copy(alpha = 0.2f) else SpiderGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (isApiKeyActive) "Connected (Cloud AI)" else "Active (Local Hybrid)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isApiKeyActive) SpiderGreenSuccess else SpiderGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "Miracle AI operates in dual mode: with a Gemini API key configured in AI Studio Secrets, you get full multimodal cloud neural vision. Offline or without a key, the app seamlessly runs high-speed local Spider-Sense vision routines.",
                            fontSize = 11.sp,
                            color = SpiderTextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // App About
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpiderNavyElevated)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("MIRACLE MEDIA VAULT v1.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                        Text("Built for superhero-grade storage, intelligent media compression & AI image creativity.", fontSize = 11.sp, color = SpiderTextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp, color = SpiderTextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
    }
}
