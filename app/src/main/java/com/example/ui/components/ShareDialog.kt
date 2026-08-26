package com.example.ui.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MediaItem
import com.example.data.model.ShareExpiry
import com.example.data.model.ShareLinkRecord
import com.example.data.model.SharePermission
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
import java.util.UUID

@Composable
fun ShareDialog(
    item: MediaItem,
    onDismiss: () -> Unit,
    onDirectShareFile: () -> Unit = {},
    onGenerateLink: (ShareLinkRecord) -> Unit
) {
    SpiderShareDialog(
        targetTitle = item.title,
        targetType = "FILE",
        targetId = item.id,
        onDismiss = onDismiss,
        onDirectShareFile = onDirectShareFile,
        onLinkGenerated = onGenerateLink
    )
}

@Composable
fun SpiderShareDialog(
    targetTitle: String,
    targetType: String, // "FILE" or "FOLDER"
    targetId: Long,
    onDismiss: () -> Unit,
    onDirectShareFile: (() -> Unit)? = null,
    onLinkGenerated: (ShareLinkRecord) -> Unit
) {
    val context = LocalContext.current
    var selectedPermission by remember { mutableStateOf(SharePermission.VIEW_ONLY) }
    var selectedExpiry by remember { mutableStateOf(ShareExpiry.TWENTY_FOUR_HOURS) }
    var enablePassword by remember { mutableStateOf(false) }
    var passwordPin by remember { mutableStateOf("") }
    var showQrCode by remember { mutableStateOf(false) }
    var generatedRecord by remember { mutableStateOf<ShareLinkRecord?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, Brush.linearGradient(listOf(SpiderRed, SpiderElectricBlue)), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SpiderNavySurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SpiderRed.copy(alpha = 0.2f))
                                .border(1.dp, SpiderRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, "Share", tint = SpiderRedBright, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("SPIDER-NETWORK SHARE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                            Text(targetTitle, fontSize = 11.sp, color = SpiderTextSecondary, maxLines = 1)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, "Close", tint = SpiderTextMuted)
                    }
                }

                if (generatedRecord == null) {
                    if (onDirectShareFile != null) {
                        Button(
                            onClick = {
                                onDismiss()
                                onDirectShareFile()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiderRed)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share File to External Apps (WhatsApp, Drive...)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(SpiderNavyBorder))
                            Text("  OR GENERATE WEB-LINK  ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SpiderTextMuted)
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(SpiderNavyBorder))
                        }
                    }

                    // 1. Permission Selector
                    Text("Link Access Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SharePermission.values().forEach { perm ->
                            val isSelected = selectedPermission == perm
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) SpiderElectricBlue else SpiderNavyBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedPermission = perm },
                                color = if (isSelected) SpiderNavyElevated else SpiderNavyDark
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = perm.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SpiderElectricBlue else SpiderTextPrimary
                                    )
                                    Text(
                                        text = perm.description,
                                        fontSize = 9.sp,
                                        color = SpiderTextSecondary,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // 2. Expiration Selector
                    Text("Link Expiration", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShareExpiry.values().forEach { expiry ->
                            val isSelected = selectedExpiry == expiry
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) SpiderGold else SpiderNavyBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedExpiry = expiry },
                                color = if (isSelected) SpiderGold.copy(alpha = 0.15f) else SpiderNavyDark
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = expiry.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SpiderGold else SpiderTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // 3. Password Protection Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = SpiderRedBright, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Web-PIN Security", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                Text("Require 4-digit PIN to access", fontSize = 10.sp, color = SpiderTextSecondary)
                            }
                        }
                        Switch(
                            checked = enablePassword,
                            onCheckedChange = { enablePassword = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpiderRedBright,
                                checkedTrackColor = SpiderRed.copy(alpha = 0.4f),
                                uncheckedThumbColor = SpiderTextMuted,
                                uncheckedTrackColor = SpiderNavyBorder
                            )
                        )
                    }

                    if (enablePassword) {
                        OutlinedTextField(
                            value = passwordPin,
                            onValueChange = { if (it.length <= 8) passwordPin = it },
                            label = { Text("Set Web-PIN / Password", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpiderRedBright,
                                unfocusedBorderColor = SpiderNavyBorder,
                                focusedTextColor = SpiderTextPrimary,
                                unfocusedTextColor = SpiderTextPrimary
                            )
                        )
                    }

                    // Generate Button
                    Button(
                        onClick = {
                            val token = UUID.randomUUID().toString().take(12)
                            val shareUrl = "https://miracle.vault/share/$token"
                            val expiryTs = if (selectedExpiry.millis > 0) System.currentTimeMillis() + selectedExpiry.millis else 0L
                            val record = ShareLinkRecord(
                                targetType = targetType,
                                targetId = targetId,
                                targetTitle = targetTitle,
                                shareToken = token,
                                shareUrl = shareUrl,
                                permission = selectedPermission,
                                isPasswordProtected = enablePassword,
                                passwordHash = if (enablePassword) passwordPin else null,
                                expiryTimestamp = expiryTs
                            )
                            generatedRecord = record
                            onLinkGenerated(record)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpiderRed)
                    ) {
                        Icon(Icons.Default.Security, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Secure Spider-Link", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Generated Link Preview
                    val record = generatedRecord!!
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SpiderGreenSuccess.copy(alpha = 0.15f),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderGreenSuccess, SpiderElectricBlue)))
                        ) {
                            Text(
                                "⚡ Secure Quantum Link Generated!",
                                color = SpiderGreenSuccess,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Share URL Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SpiderNavyDark,
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderElectricBlue, SpiderRed)))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = record.shareUrl,
                                    fontSize = 12.sp,
                                    color = SpiderElectricBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Spider Share Link", record.shareUrl)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, "Copy", tint = SpiderTextPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Toggle QR Code Button
                        Button(
                            onClick = { showQrCode = !showQrCode },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiderNavyElevated)
                        ) {
                            Icon(Icons.Default.QrCode2, null, tint = SpiderGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (showQrCode) "Hide QR Code" else "Show Spider QR Code", fontSize = 12.sp, color = SpiderTextPrimary)
                        }

                        if (showQrCode) {
                            SpiderQrCodeView(data = record.shareUrl, sizeDp = 180.dp)
                        }

                        // Native Share Action
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "🕸️ Miracle Spider-Vault Secure Share:\n${record.targetTitle}\nLink: ${record.shareUrl}")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share via Spider-Network")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SpiderElectricBlue)
                            ) {
                                Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Link", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SpiderNavyElevated)
                            ) {
                                Text("Done", color = SpiderTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
