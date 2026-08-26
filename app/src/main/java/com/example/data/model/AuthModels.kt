package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AuthProvider {
    EMAIL,
    EMAIL_PASSWORD,
    GOOGLE,
    APPLE,
    SUPERHERO_IDENTITY
}

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val alias: String,
    val avatar: String = "🕷️",
    val avatarKey: String = "PETER",
    val heroRank: String = "Level 5 Web-Warrior",
    val provider: AuthProvider = AuthProvider.EMAIL,
    val authProvider: AuthProvider = AuthProvider.EMAIL,
    val dateJoined: Long = System.currentTimeMillis(),
    val isWebLockEnabled: Boolean = false,
    val webLockPin: String? = null,
    val sessionToken: String? = null
)

data class AuthSession(
    val isLoggedIn: Boolean,
    val currentUser: UserAccount?,
    val isVaultLocked: Boolean = false
)
