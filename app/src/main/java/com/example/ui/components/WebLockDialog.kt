package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SpiderElectricBlue
import com.example.ui.theme.SpiderNavyBorder
import com.example.ui.theme.SpiderNavyDark
import com.example.ui.theme.SpiderNavyElevated
import com.example.ui.theme.SpiderNavySurface
import com.example.ui.theme.SpiderRed
import com.example.ui.theme.SpiderRedBright
import com.example.ui.theme.SpiderTextMuted
import com.example.ui.theme.SpiderTextPrimary
import com.example.ui.theme.SpiderTextSecondary

@Composable
fun WebLockDialog(
    isSettingUpPin: Boolean = false,
    correctPin: String = "1962",
    onPinEntered: (String) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Brush.linearGradient(listOf(SpiderRed, SpiderElectricBlue)), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SpiderNavySurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isError) SpiderRed.copy(alpha = 0.25f) else SpiderElectricBlue.copy(alpha = 0.2f))
                        .border(1.5.dp, if (isError) SpiderRedBright else SpiderElectricBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Security else Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = if (isError) SpiderRedBright else SpiderElectricBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isSettingUpPin) "SET 4-DIGIT WEB-LOCK PIN" else "SPIDER-SENSE WEB-LOCK",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpiderTextPrimary
                    )
                    Text(
                        text = if (isError) "Incorrect PIN. Spider-Sense alert!" else if (isSettingUpPin) "Choose a memorable PIN for your vault" else "Enter 4-digit PIN to authenticate",
                        fontSize = 11.sp,
                        color = if (isError) SpiderRedBright else SpiderTextSecondary
                    )
                }

                // 4-Dot Display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    for (i in 0..3) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) (if (isError) SpiderRedBright else SpiderElectricBlue)
                                    else SpiderNavyDark
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isFilled) Color.White else SpiderNavyBorder,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Number Pad (1-9, 0, Backspace)
                val keypad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    keypad.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(SpiderNavyElevated)
                                        .border(1.dp, SpiderNavyBorder, CircleShape)
                                        .clickable {
                                            when (key) {
                                                "C" -> {
                                                    enteredPin = ""
                                                    isError = false
                                                }
                                                "DEL" -> {
                                                    if (enteredPin.isNotEmpty()) {
                                                        enteredPin = enteredPin.dropLast(1)
                                                        isError = false
                                                    }
                                                }
                                                else -> {
                                                    if (enteredPin.length < 4) {
                                                        val newPin = enteredPin + key
                                                        enteredPin = newPin
                                                        isError = false
                                                        if (newPin.length == 4) {
                                                            if (isSettingUpPin) {
                                                                onPinEntered(newPin)
                                                            } else {
                                                                if (newPin == correctPin) {
                                                                    onPinEntered(newPin)
                                                                } else {
                                                                    isError = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "DEL") {
                                        Icon(Icons.Default.Backspace, "Delete", tint = SpiderTextPrimary, modifier = Modifier.size(20.dp))
                                    } else {
                                        Text(
                                            text = key,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (key == "C") SpiderRedBright else SpiderTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Close Button
                Text(
                    text = "Dismiss",
                    fontSize = 12.sp,
                    color = SpiderTextMuted,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun WebLockKeypadDialog(
    title: String = "SPIDER-SENSE WEB-LOCK",
    subtitle: String = "Enter 4-digit PIN to authenticate",
    correctPin: String? = "1962",
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    WebLockDialog(
        isSettingUpPin = false,
        correctPin = correctPin ?: "1962",
        onPinEntered = { onSuccess() },
        onDismiss = onDismiss
    )
}
