package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SharePermission(val displayName: String, val description: String) {
    VIEW_ONLY("View Only", "Recipients can view and download media"),
    CAN_EDIT("Collaborator", "Recipients can upload, edit, and organize files")
}

enum class ShareExpiry(val displayName: String, val millis: Long) {
    ONE_HOUR("1 Hour", 60 * 60 * 1000L),
    TWENTY_FOUR_HOURS("24 Hours", 24 * 60 * 60 * 1000L),
    SEVEN_DAYS("7 Days", 7 * 24 * 60 * 60 * 1000L),
    NEVER("Never Expires", 0L)
}

@Entity(tableName = "share_links")
data class ShareLinkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetType: String, // "FILE" or "FOLDER"
    val targetId: Long,
    val targetTitle: String,
    val shareToken: String,
    val shareUrl: String,
    val permission: SharePermission = SharePermission.VIEW_ONLY,
    val isPasswordProtected: Boolean = false,
    val passwordHash: String? = null,
    val expiryTimestamp: Long = 0L, // 0 = never
    val createdTimestamp: Long = System.currentTimeMillis(),
    val accessCount: Int = 0,
    val maxAccessCount: Int = 0 // 0 = unlimited
) {
    val isExpired: Boolean
        get() = expiryTimestamp > 0L && System.currentTimeMillis() > expiryTimestamp
}

@Entity(tableName = "shared_folders")
data class SharedFolder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconEmoji: String = "🕸️",
    val ownerAlias: String = "Peter Parker",
    val ownerEmail: String = "peter.parker@spider.net",
    val dateCreated: Long = System.currentTimeMillis(),
    val inviteCode: String,
    val memberCount: Int = 1,
    val isLocked: Boolean = false,
    val webPin: String? = null
)

@Entity(tableName = "shared_folder_members")
data class SharedFolderMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val folderId: Long,
    val alias: String,
    val email: String,
    val role: String = "COLLABORATOR", // "OWNER", "COLLABORATOR", "VIEWER"
    val avatarKey: String = "PP",
    val joinedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "folder_activity_logs")
data class FolderActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val folderId: Long,
    val actorName: String,
    val actionText: String,
    val timestamp: Long = System.currentTimeMillis()
)
