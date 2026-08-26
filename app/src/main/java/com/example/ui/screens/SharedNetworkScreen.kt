package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MediaItem
import com.example.data.model.ShareLinkRecord
import com.example.data.model.SharedFolder
import com.example.data.model.SharedFolderMember
import com.example.ui.components.FullScreenMediaViewer
import com.example.ui.components.MediaItemCard
import com.example.ui.components.SpiderQrCodeView
import com.example.ui.components.SpiderShareDialog
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
import com.example.ui.theme.SpiderTextMuted
import com.example.ui.theme.SpiderTextPrimary
import com.example.ui.theme.SpiderTextSecondary
import com.example.ui.viewmodel.MiracleViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SharedNetworkScreen(
    viewModel: MiracleViewModel,
    onNavigateToMediaDetail: (MediaItem) -> Unit = {},
    onNavigateToAiStudio: (MediaItem) -> Unit = {},
    onNavigateToCompression: (MediaItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedFolders by viewModel.sharedFolders.collectAsState()
    val shareLinks by viewModel.shareLinks.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()

    var activeFolder by remember { mutableStateOf<SharedFolder?>(null) }
    var previewItem by remember { mutableStateOf<MediaItem?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var shareTargetItem by remember { mutableStateOf<Pair<String, Long>?>(null) }
    var qrCodeModalUrl by remember { mutableStateOf<String?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(SpiderNavy)) {
        SpiderWebBackground(alpha = 0.12f)

        if (activeFolder == null) {
            // Main Shared Network Dashboard
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SpiderElectricBlue.copy(alpha = 0.2f))
                                    .border(1.dp, SpiderElectricBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FolderShared, null, tint = SpiderElectricBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("SPIDER-NETWORK", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                Text("Collaborative Vaults & Shareable Links", fontSize = 11.sp, color = SpiderTextSecondary)
                            }
                        }

                        Button(
                            onClick = { showCreateFolderDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiderRed),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Vault", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Section 1: Collaborative Shared Folders
                item {
                    Text(
                        text = "COLLABORATIVE SHARED FOLDERS (${sharedFolders.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpiderTextPrimary,
                        letterSpacing = 1.sp
                    )
                }

                if (sharedFolders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SpiderNavySurface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🕸️ No shared folders yet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                Text("Create a collaborative vault to share mission media with the Web-Warriors.", fontSize = 11.sp, color = SpiderTextMuted)
                            }
                        }
                    }
                } else {
                    items(sharedFolders) { folder ->
                        SharedFolderCard(
                            folder = folder,
                            onClick = { activeFolder = folder },
                            onShareClick = {
                                shareTargetItem = Pair(folder.name, folder.id)
                            },
                            onDeleteClick = {
                                viewModel.deleteSharedFolder(folder.id)
                                Toast.makeText(context, "Deleted folder: ${folder.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // Section 2: Active Shareable Links
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ACTIVE SECURE SHARE LINKS (${shareLinks.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpiderTextPrimary,
                        letterSpacing = 1.sp
                    )
                }

                if (shareLinks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SpiderNavySurface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("No active share links", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                Text("Generate a shareable link with QR Code from any file or folder card.", fontSize = 11.sp, color = SpiderTextMuted)
                            }
                        }
                    }
                } else {
                    items(shareLinks) { link ->
                        ShareLinkItemCard(
                            link = link,
                            onCopyClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Spider Link", link.shareUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            onQrClick = { qrCodeModalUrl = link.shareUrl },
                            onDeleteClick = {
                                viewModel.deleteShareLink(link.id)
                                Toast.makeText(context, "Revoked share link", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        } else {
            // Inside Folder Detail View
            val folder = activeFolder!!
            val folderMedia by viewModel.getMediaByFolder(folder.id).collectAsState(initial = emptyList())
            val members by viewModel.getFolderMembers(folder.id).collectAsState(initial = emptyList())
            val activityLogs by viewModel.getFolderActivityLogs(folder.id).collectAsState(initial = emptyList())

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Back bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { activeFolder = null }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = SpiderTextPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(folder.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                Text("${folder.ownerAlias} • Invite: ${folder.inviteCode}", fontSize = 11.sp, color = SpiderElectricBlue)
                            }
                        }

                        Row {
                            IconButton(onClick = { showAddMemberDialog = true }) {
                                Icon(Icons.Default.PersonAdd, "Add Member", tint = SpiderGold)
                            }
                            IconButton(onClick = { shareTargetItem = Pair(folder.name, folder.id) }) {
                                Icon(Icons.Default.Share, "Share Folder", tint = SpiderRedBright)
                            }
                        }
                    }
                }

                // Member Avatars Row
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderRed, SpiderElectricBlue)))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Collaborators (${members.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                Text("Invite Code: ${folder.inviteCode}", fontSize = 10.sp, color = SpiderTextMuted)
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(members) { member ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SpiderNavyElevated,
                                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderNavyBorder, SpiderElectricBlue)))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(SpiderRed),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(member.avatarKey.take(2), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(member.alias, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                                Text(member.role, fontSize = 8.sp, color = SpiderGold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Media in folder
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SHARED MEDIA FILES (${folderMedia.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpiderTextPrimary,
                            letterSpacing = 1.sp
                        )

                        // Quick Add Existing Media to Folder
                        if (allMedia.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val unassigned = allMedia.firstOrNull { it.sharedFolderId != folder.id }
                                    if (unassigned != null) {
                                        viewModel.assignMediaToFolder(unassigned.id, folder.id)
                                        viewModel.logFolderActivity(folder.id, "Peter Parker", "Added ${unassigned.title} to folder")
                                        Toast.makeText(context, "Added ${unassigned.title} to shared folder", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "All current files are already in this folder!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SpiderElectricBlue),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync File", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }

                if (folderMedia.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SpiderNavySurface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Folder is Empty", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                Text("Tap 'Sync File' or import media from Vault Home.", fontSize = 11.sp, color = SpiderTextMuted)
                            }
                        }
                    }
                } else {
                    items(folderMedia) { item ->
                        MediaItemCard(
                            item = item,
                            onItemClick = { previewItem = item },
                            onFavoriteToggle = { viewModel.toggleFavorite(item) },
                            onCompressClick = { onNavigateToCompression(item) },
                            onEditAiClick = { onNavigateToAiStudio(item) },
                            onDeleteClick = {
                                viewModel.assignMediaToFolder(item.id, null)
                                Toast.makeText(context, "Removed from folder", Toast.LENGTH_SHORT).show()
                            },
                            onShareClick = { shareTargetItem = Pair(item.title, item.id) }
                        )
                    }
                }

                // Folder Activity Feed Log
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "LIVE ACTIVITY & REVISION LOG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpiderTextPrimary,
                        letterSpacing = 1.sp
                    )
                }

                if (activityLogs.isNotEmpty()) {
                    items(activityLogs) { log ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SpiderNavySurface,
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderNavyBorder, SpiderElectricBlue)))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.History, null, tint = SpiderElectricBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${log.actorName}: ${log.actionText}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SpiderTextPrimary
                                    )
                                    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                                    Text(dateStr, fontSize = 9.sp, color = SpiderTextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog: Create Folder
        if (showCreateFolderDialog) {
            var folderName by remember { mutableStateOf("") }
            var folderDesc by remember { mutableStateOf("") }
            var folderEmoji by remember { mutableStateOf("🕸️") }

            Dialog(onDismissRequest = { showCreateFolderDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Create Collaborative Shared Vault", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)

                        OutlinedTextField(
                            value = folderName,
                            onValueChange = { folderName = it },
                            label = { Text("Vault Name (e.g. NYC Night Patrol)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpiderRedBright,
                                unfocusedBorderColor = SpiderNavyBorder,
                                focusedTextColor = SpiderTextPrimary,
                                unfocusedTextColor = SpiderTextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = folderDesc,
                            onValueChange = { folderDesc = it },
                            label = { Text("Mission Description / Objective", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpiderRedBright,
                                unfocusedBorderColor = SpiderNavyBorder,
                                focusedTextColor = SpiderTextPrimary,
                                unfocusedTextColor = SpiderTextPrimary
                            )
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("🕸️", "🏙️", "⚡", "🔬", "🛡️", "📂").forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (folderEmoji == emoji) SpiderRed else SpiderNavyElevated)
                                        .clickable { folderEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 16.sp)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (folderName.isNotBlank()) {
                                    viewModel.createSharedFolder(
                                        name = folderName.trim(),
                                        description = folderDesc.trim(),
                                        iconEmoji = folderEmoji
                                    )
                                    showCreateFolderDialog = false
                                    Toast.makeText(context, "Shared Vault '$folderName' activated!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiderRed)
                        ) {
                            Text("Create Shared Vault", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Dialog: Add Member to Folder
        if (showAddMemberDialog && activeFolder != null) {
            var memberAlias by remember { mutableStateOf("") }
            var memberEmail by remember { mutableStateOf("") }
            var memberRole by remember { mutableStateOf("COLLABORATOR") }

            Dialog(onDismissRequest = { showAddMemberDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Invite Hero Collaborator", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)

                        OutlinedTextField(
                            value = memberAlias,
                            onValueChange = { memberAlias = it },
                            label = { Text("Hero Alias (e.g. Miles Morales)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpiderElectricBlue,
                                unfocusedBorderColor = SpiderNavyBorder,
                                focusedTextColor = SpiderTextPrimary,
                                unfocusedTextColor = SpiderTextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = memberEmail,
                            onValueChange = { memberEmail = it },
                            label = { Text("Spider-Network Email", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpiderElectricBlue,
                                unfocusedBorderColor = SpiderNavyBorder,
                                focusedTextColor = SpiderTextPrimary,
                                unfocusedTextColor = SpiderTextPrimary
                            )
                        )

                        Button(
                            onClick = {
                                if (memberAlias.isNotBlank()) {
                                    val member = SharedFolderMember(
                                        folderId = activeFolder!!.id,
                                        alias = memberAlias.trim(),
                                        email = if (memberEmail.isNotBlank()) memberEmail.trim() else "${memberAlias.lowercase().replace(" ", ".")}@spider.net",
                                        role = memberRole,
                                        avatarKey = memberAlias.take(2).uppercase()
                                    )
                                    viewModel.addFolderMember(member)
                                    viewModel.logFolderActivity(activeFolder!!.id, "Peter Parker", "Invited $memberAlias as $memberRole")
                                    showAddMemberDialog = false
                                    Toast.makeText(context, "Invited $memberAlias to folder", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiderElectricBlue)
                        ) {
                            Text("Send Mission Invite", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Dialog: QR Code Modal
        if (qrCodeModalUrl != null) {
            Dialog(onDismissRequest = { qrCodeModalUrl = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.88f).clip(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("SPIDER-NETWORK QR ACCESS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                        SpiderQrCodeView(data = qrCodeModalUrl!!, sizeDp = 200.dp)
                        Text(qrCodeModalUrl!!, fontSize = 10.sp, color = SpiderElectricBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Button(
                            onClick = { qrCodeModalUrl = null },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiderNavyElevated)
                        ) {
                            Text("Close", color = SpiderTextPrimary)
                        }
                    }
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
                    onNavigateToCompression(item)
                },
                onAiEdit = {
                    previewItem = null
                    onNavigateToAiStudio(item)
                },
                onDelete = {
                    previewItem = null
                    viewModel.assignMediaToFolder(item.id, null)
                },
                onCreateQuantumLink = {
                    previewItem = null
                    shareTargetItem = Pair(item.title, item.id)
                }
            )
        }

        // Dialog: Share Link Generator
        if (shareTargetItem != null) {
            val (title, id) = shareTargetItem!!
            SpiderShareDialog(
                targetTitle = title,
                targetType = "FILE",
                targetId = id,
                onDismiss = { shareTargetItem = null },
                onLinkGenerated = { record ->
                    viewModel.createShareLink(record)
                }
            )
        }
    }
}

@Composable
fun SharedFolderCard(
    folder: SharedFolder,
    onClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, SpiderNavyBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SpiderNavySurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpiderNavyDark)
                        .border(1.dp, SpiderElectricBlue, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(folder.iconEmoji, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(folder.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                    Text(folder.description, fontSize = 11.sp, color = SpiderTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Owner: ${folder.ownerAlias} • ${folder.memberCount} members", fontSize = 10.sp, color = SpiderGold)
                }
            }

            Row {
                IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, "Share", tint = SpiderElectricBlue, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = SpiderRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ShareLinkItemCard(
    link: ShareLinkRecord,
    onCopyClick: () -> Unit,
    onQrClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (link.isExpired) SpiderRed.copy(alpha = 0.5f) else SpiderNavyBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SpiderNavySurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, null, tint = SpiderElectricBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(link.targetTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary, maxLines = 1)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (link.isExpired) SpiderRed.copy(alpha = 0.2f) else SpiderGreenSuccess.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (link.isExpired) "EXPIRED" else link.permission.displayName,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (link.isExpired) SpiderRedBright else SpiderGreenSuccess,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = SpiderNavyDark
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(link.shareUrl, fontSize = 11.sp, color = SpiderElectricBlue, maxLines = 1, modifier = Modifier.weight(1f))
                    Row {
                        IconButton(onClick = onCopyClick, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.ContentCopy, "Copy", tint = SpiderTextPrimary, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = onQrClick, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.QrCode2, "QR", tint = SpiderGold, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Delete, "Revoke", tint = SpiderRed, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
