package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuthProvider
import com.example.ui.components.AnimatedSpiderLogo
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

@Composable
fun AuthScreen(
    viewModel: MiracleViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Password strength calculation
    val passwordStrength = remember(password) {
        when {
            password.isEmpty() -> 0f
            password.length < 6 -> 0.25f
            password.length in 6..8 && !password.any { it.isDigit() } -> 0.5f
            password.length >= 8 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 1.0f
            else -> 0.75f
        }
    }

    val strengthLabel = remember(passwordStrength) {
        when {
            passwordStrength == 0f -> ""
            passwordStrength <= 0.25f -> "Weak"
            passwordStrength <= 0.5f -> "Fair"
            passwordStrength <= 0.75f -> "Strong"
            else -> "Superhero-Grade (Heroic)"
        }
    }

    val strengthColor = remember(passwordStrength) {
        when {
            passwordStrength <= 0.25f -> SpiderRedBright
            passwordStrength <= 0.5f -> SpiderGold
            passwordStrength <= 0.75f -> SpiderElectricBlue
            else -> SpiderGreenSuccess
        }
    }

    Box(modifier = modifier.fillMaxSize().background(SpiderNavy)) {
        SpiderWebBackground(alpha = 0.12f)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Emblem Logo
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedSpiderLogo(
                        size = 84.dp,
                        showGlowRing = true,
                        showWebAura = true,
                        isHero = true,
                        modifier = Modifier.testTag("auth_spider_hero_logo")
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "SPIDER-IDENTITY CORE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = SpiderTextPrimary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Secure Cryptographic Authentication & Neural Cloud Sync",
                        fontSize = 11.sp,
                        color = SpiderTextSecondary
                    )
                }
            }

            // Auth Mode Toggle Tabs
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(14.dp),
                    color = SpiderNavySurface,
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderNavyBorder, SpiderElectricBlue)))
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isRegisterMode) SpiderRed else Color.Transparent)
                                .clickable { isRegisterMode = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sign In",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isRegisterMode) Color.White else SpiderTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isRegisterMode) SpiderRed else Color.Transparent)
                                .clickable { isRegisterMode = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Register Hero",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRegisterMode) Color.White else SpiderTextSecondary
                            )
                        }
                    }
                }
            }

            // Main Credential Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SpiderNavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(SpiderRed, SpiderElectricBlue)))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Superhero Alias (for register mode)
                        AnimatedVisibility(visible = isRegisterMode) {
                            OutlinedTextField(
                                value = alias,
                                onValueChange = { alias = it },
                                label = { Text("Superhero Alias (e.g. Peter Parker)", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = SpiderElectricBlue) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SpiderElectricBlue,
                                    unfocusedBorderColor = SpiderNavyBorder,
                                    focusedTextColor = SpiderTextPrimary,
                                    unfocusedTextColor = SpiderTextPrimary
                                )
                            )
                        }

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            label = { Text("Spider-Network Email", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, null, tint = SpiderRedBright) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpiderRedBright,
                                unfocusedBorderColor = SpiderNavyBorder,
                                focusedTextColor = SpiderTextPrimary,
                                unfocusedTextColor = SpiderTextPrimary
                            )
                        )

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("Master Password", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = SpiderGold) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = SpiderTextMuted
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpiderGold,
                                unfocusedBorderColor = SpiderNavyBorder,
                                focusedTextColor = SpiderTextPrimary,
                                unfocusedTextColor = SpiderTextPrimary
                            )
                        )

                        // Password Strength Meter (if typing in register mode)
                        if (isRegisterMode && password.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Password Strength", fontSize = 10.sp, color = SpiderTextSecondary)
                                    Text(strengthLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = strengthColor)
                                }
                                LinearProgressIndicator(
                                    progress = { passwordStrength },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = strengthColor,
                                    trackColor = SpiderNavyDark
                                )
                            }
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                fontSize = 11.sp,
                                color = SpiderRedBright,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                if (email.isBlank() || !email.contains("@")) {
                                    errorMessage = "Please enter a valid email address."
                                    return@Button
                                }
                                if (password.length < 6) {
                                    errorMessage = "Password must be at least 6 characters."
                                    return@Button
                                }

                                if (isRegisterMode) {
                                    val heroAlias = if (alias.isNotBlank()) alias else email.substringBefore("@")
                                    viewModel.registerUser(
                                        email = email.trim(),
                                        password = password,
                                        alias = heroAlias,
                                        onSuccess = {
                                            Toast.makeText(context, "Welcome to the Spider-Network, $heroAlias!", Toast.LENGTH_SHORT).show()
                                            onAuthSuccess()
                                        },
                                        onError = { err -> errorMessage = err }
                                    )
                                } else {
                                    viewModel.loginUser(
                                        email = email.trim(),
                                        password = password,
                                        onSuccess = {
                                            Toast.makeText(context, "Identity verified successfully!", Toast.LENGTH_SHORT).show()
                                            onAuthSuccess()
                                        },
                                        onError = { err -> errorMessage = err }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiderRed)
                        ) {
                            Icon(Icons.Default.Security, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRegisterMode) "Create Spider-Vault Account" else "Authenticate & Enter Vault",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Social Logins (Google, Apple)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("OR FAST-CONNECT WITH FEDERATED IDENTITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SpiderTextMuted, letterSpacing = 1.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Google Sign-In
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, SpiderNavyBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.loginWithSocial(
                                        provider = AuthProvider.GOOGLE,
                                        email = "google.hero@spider.net",
                                        alias = "Google Web-Warrior",
                                        onSuccess = {
                                            Toast.makeText(context, "Google Sign-In authenticated!", Toast.LENGTH_SHORT).show()
                                            onAuthSuccess()
                                        }
                                    )
                                },
                            color = SpiderNavySurface
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌐", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Google", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                            }
                        }

                        // Apple Sign-In
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, SpiderNavyBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.loginWithSocial(
                                        provider = AuthProvider.APPLE,
                                        email = "apple.hero@spider.net",
                                        alias = "Apple ID Web-Agent",
                                        onSuccess = {
                                            Toast.makeText(context, "Apple ID authenticated!", Toast.LENGTH_SHORT).show()
                                            onAuthSuccess()
                                        }
                                    )
                                },
                            color = SpiderNavySurface
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🍎", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Apple ID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                            }
                        }
                    }
                }
            }

            // Quick Superhero Personas One-Tap Switch
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpiderNavyDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SpiderRed, SpiderElectricBlue)))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = SpiderElectricBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fast Persona Switch (Spider-Verse)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                        }

                        val personas = listOf(
                            Triple("peter.parker@spider.net", "Peter Parker (Spider-Man)", "SpiderMan123!"),
                            Triple("miles.morales@spider.net", "Miles Morales (Spider-Verse)", "Brooklyn2024!"),
                            Triple("gwen.stacy@spider.net", "Gwen Stacy (Ghost-Spider)", "GhostSpider99!")
                        )

                        personas.forEach { (pEmail, pAlias, pPass) ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, SpiderNavyBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.loginUser(
                                            email = pEmail,
                                            password = pPass,
                                            onSuccess = {
                                                Toast.makeText(context, "Switched to $pAlias", Toast.LENGTH_SHORT).show()
                                                onAuthSuccess()
                                            },
                                            onError = { err -> errorMessage = err }
                                        )
                                    },
                                color = SpiderNavyElevated
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(pAlias, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SpiderTextPrimary)
                                        Text(pEmail, fontSize = 10.sp, color = SpiderTextSecondary)
                                    }
                                    Text("Quick Login ⚡", fontSize = 10.sp, color = SpiderElectricBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
