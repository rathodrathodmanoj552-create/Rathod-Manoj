package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AnimatedSpiderLogo
import com.example.ui.components.WebLockDialog
import com.example.ui.screens.AiSpiderStudioScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CompressionLabScreen
import com.example.ui.screens.SharedNetworkScreen
import com.example.ui.screens.SuitSettingsScreen
import com.example.ui.screens.VaultHomeScreen
import com.example.ui.theme.SpiderBentoBorderNav
import com.example.ui.theme.SpiderBentoCard
import com.example.ui.theme.SpiderBentoDark
import com.example.ui.theme.SpiderNavyAccent
import com.example.ui.theme.SpiderRed
import com.example.ui.theme.SpiderSlate300
import com.example.ui.theme.SpiderSlate500
import com.example.ui.viewmodel.MiracleViewModel

sealed class MiracleDestination(val index: Int, val title: String, val bentoTag: String, val icon: ImageVector) {
    object Vault : MiracleDestination(0, "Base", "BASE", Icons.Default.Folder)
    object Compress : MiracleDestination(1, "Zip-Web", "SQUEEZE", Icons.Default.Compress)
    object AiStudio : MiracleDestination(2, "AI Studio", "AI LAB", Icons.Default.AutoAwesome)
    object Network : MiracleDestination(3, "Network", "NETWORK", Icons.Default.Hub)
    object Settings : MiracleDestination(4, "Suit Tech", "TECH", Icons.Default.Settings)
}

@Composable
fun MiracleApp(
    viewModel: MiracleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var currentTab by remember { mutableIntStateOf(0) }
    var showAuthScreen by remember { mutableStateOf(false) }
    var isWebLockUnlocked by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.clearStatusMessage()
        }
    }

    // Biometric / Web-Lock Check
    if (currentUser?.isWebLockEnabled == true && !isWebLockUnlocked) {
        WebLockDialog(
            isSettingUpPin = false,
            correctPin = currentUser?.webLockPin ?: "1234",
            onPinEntered = {
                isWebLockUnlocked = true
            },
            onDismiss = {
                // Keep locked or allow fallback
            }
        )
    }

    if (showAuthScreen) {
        AuthScreen(
            viewModel = viewModel,
            onAuthSuccess = { showAuthScreen = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = SpiderBentoDark,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                MiracleTopBar(
                    avatarText = currentUser?.avatar ?: currentUser?.alias?.take(2)?.uppercase() ?: "PP",
                    onSuitBadgeClick = { currentTab = 4 }
                )
            },
            bottomBar = {
                MiracleBottomNavBar(
                    selectedTabIndex = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> VaultHomeScreen(
                            viewModel = viewModel,
                            onNavigateToCompress = { item ->
                                viewModel.selectItemForCompression(item)
                                currentTab = 1
                            },
                            onNavigateToAiEdit = { item ->
                                viewModel.loadItemForAiStudio(item)
                                currentTab = 2
                            },
                            onNavigateToTab = { targetTab ->
                                currentTab = targetTab
                            }
                        )
                        1 -> CompressionLabScreen(viewModel = viewModel)
                        2 -> AiSpiderStudioScreen(viewModel = viewModel)
                        3 -> SharedNetworkScreen(
                            viewModel = viewModel,
                            onNavigateToCompression = { item ->
                                viewModel.selectItemForCompression(item)
                                currentTab = 1
                            },
                            onNavigateToAiStudio = { item ->
                                viewModel.loadItemForAiStudio(item)
                                currentTab = 2
                            }
                        )
                        4 -> SuitSettingsScreen(
                            viewModel = viewModel,
                            onNavigateToAuth = { showAuthScreen = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiracleTopBar(
    avatarText: String,
    onSuitBadgeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = SpiderBentoDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bento Brand Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Animated Spider Logo
                AnimatedSpiderLogo(
                    size = 40.dp,
                    showGlowRing = true,
                    showWebAura = true,
                    modifier = Modifier.testTag("app_brand_spider_logo")
                )

                Column {
                    Text(
                        text = "MIRACLE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "THE SPIDER-VAULT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpiderRed,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Right Avatar / Suit Status Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, SpiderNavyAccent, CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(SpiderBentoCard)
                    .clickable { onSuitBadgeClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpiderSlate300
                )
            }
        }
    }
}

@Composable
fun MiracleBottomNavBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        MiracleDestination.Vault,
        MiracleDestination.Compress,
        MiracleDestination.AiStudio,
        MiracleDestination.Network,
        MiracleDestination.Settings
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = SpiderBentoCard,
        border = BorderStroke(1.dp, SpiderBentoBorderNav)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { dest ->
                val isSelected = selectedTabIndex == dest.index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(dest.index) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("nav_tab_${dest.title.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier.size(26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.title,
                            tint = if (isSelected) SpiderRed else SpiderSlate500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dest.bentoTag,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) SpiderRed else SpiderSlate500,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}
