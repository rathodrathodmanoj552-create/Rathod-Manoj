package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.dao.UserDao
import com.example.data.model.AuthProvider
import com.example.data.model.UserAccount
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

object AuthManager {

    private const val PREFS_NAME = "miracle_auth_prefs"
    private const val KEY_SESSION_TOKEN = "session_token"
    private const val KEY_CURRENT_USER_ID = "current_user_id"
    private const val KEY_IS_VAULT_LOCKED = "is_vault_locked"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun hashPassword(password: String, salt: String): String {
        val input = "$password:$salt:miracle_spider_secret"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    fun generateSessionToken(): String {
        return "SPIDER_SESS_${UUID.randomUUID()}_${System.currentTimeMillis()}"
    }

    suspend fun seedDefaultUsersIfEmpty(userDao: UserDao) = withContext(Dispatchers.IO) {
        val defaultUser = userDao.getUserByEmail("peter.parker@spider.net")
        if (defaultUser == null) {
            val salt1 = generateSalt()
            val user1 = UserAccount(
                email = "peter.parker@spider.net",
                passwordHash = hashPassword("SpiderMan123!", salt1),
                salt = salt1,
                alias = "Peter Parker (Spider-Man)",
                avatar = "🕷️",
                avatarKey = "PETER",
                heroRank = "Senior Avenger & Web-Warrior Lead",
                provider = AuthProvider.EMAIL,
                authProvider = AuthProvider.EMAIL,
                isWebLockEnabled = true,
                webLockPin = "1962",
                sessionToken = generateSessionToken()
            )
            userDao.insertUser(user1)

            val salt2 = generateSalt()
            userDao.insertUser(
                UserAccount(
                    email = "miles.morales@spider.net",
                    passwordHash = hashPassword("Brooklyn2024!", salt2),
                    salt = salt2,
                    alias = "Miles Morales (Spider-Verse)",
                    avatar = "⚡",
                    avatarKey = "MILES",
                    heroRank = "Bio-Electric Stealth Specialist",
                    provider = AuthProvider.EMAIL,
                    authProvider = AuthProvider.EMAIL,
                    isWebLockEnabled = false
                )
            )

            val salt3 = generateSalt()
            userDao.insertUser(
                UserAccount(
                    email = "gwen.stacy@spider.net",
                    passwordHash = hashPassword("GhostSpider99!", salt3),
                    salt = salt3,
                    alias = "Gwen Stacy (Ghost-Spider)",
                    avatar = "🕸️",
                    avatarKey = "GWEN",
                    heroRank = "Multiverse Dimensional Scout",
                    provider = AuthProvider.EMAIL,
                    authProvider = AuthProvider.EMAIL,
                    isWebLockEnabled = false
                )
            )
        }
    }

    suspend fun restoreSession(userDao: UserDao, context: Context): UserAccount? = withContext(Dispatchers.IO) {
        val token = getSavedSessionToken(context)
        if (!token.isNullOrBlank()) {
            val user = userDao.getUserBySessionToken(token)
            if (user != null) return@withContext user
        }
        val defaultUser = userDao.getUserByEmail("peter.parker@spider.net")
        if (defaultUser != null) {
            val newToken = defaultUser.sessionToken ?: generateSessionToken()
            userDao.updateSessionToken(defaultUser.id, newToken)
            saveSession(context, newToken)
            return@withContext defaultUser.copy(sessionToken = newToken)
        }
        null
    }

    suspend fun authenticateUser(
        repository: MediaRepository,
        email: String,
        password: String
    ): UserAccount? = withContext(Dispatchers.IO) {
        val user = repository.getUserByEmail(email.trim().lowercase()) ?: return@withContext null
        val computedHash = hashPassword(password, user.salt)
        if (computedHash == user.passwordHash) {
            val newToken = generateSessionToken()
            val updated = user.copy(sessionToken = newToken)
            repository.updateUser(updated)
            updated
        } else {
            null
        }
    }

    suspend fun registerUser(
        repository: MediaRepository,
        email: String,
        password: String,
        alias: String,
        avatar: String,
        provider: AuthProvider
    ): UserAccount = withContext(Dispatchers.IO) {
        val salt = generateSalt()
        val passwordHash = hashPassword(password, salt)
        val token = generateSessionToken()
        val newUser = UserAccount(
            email = email.trim().lowercase(),
            passwordHash = passwordHash,
            salt = salt,
            alias = alias.trim(),
            avatar = avatar,
            avatarKey = "CUSTOM",
            heroRank = "Recruit Web-Warrior",
            provider = provider,
            authProvider = provider,
            isWebLockEnabled = false,
            sessionToken = token
        )
        val id = repository.insertUser(newUser)
        newUser.copy(id = id)
    }

    suspend fun authenticateWithSocial(
        repository: MediaRepository,
        provider: AuthProvider,
        email: String,
        alias: String,
        avatar: String
    ): UserAccount = withContext(Dispatchers.IO) {
        val existing = repository.getUserByEmail(email.trim().lowercase())
        val token = generateSessionToken()
        if (existing != null) {
            val updated = existing.copy(sessionToken = token)
            repository.updateUser(updated)
            updated
        } else {
            val salt = generateSalt()
            val user = UserAccount(
                email = email.trim().lowercase(),
                passwordHash = hashPassword(UUID.randomUUID().toString(), salt),
                salt = salt,
                alias = alias,
                avatar = avatar,
                avatarKey = "SOCIAL",
                heroRank = "Verified Hero Operative",
                provider = provider,
                authProvider = provider,
                isWebLockEnabled = false,
                sessionToken = token
            )
            val id = repository.insertUser(user)
            user.copy(id = id)
        }
    }

    fun getSavedSessionToken(context: Context): String? {
        return getPrefs(context).getString(KEY_SESSION_TOKEN, null)
    }

    fun saveSession(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_SESSION_TOKEN, token).apply()
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit().remove(KEY_SESSION_TOKEN).remove(KEY_CURRENT_USER_ID).apply()
    }

    fun isVaultLocked(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_VAULT_LOCKED, false)
    }

    fun setVaultLocked(context: Context, locked: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_VAULT_LOCKED, locked).apply()
    }
}
