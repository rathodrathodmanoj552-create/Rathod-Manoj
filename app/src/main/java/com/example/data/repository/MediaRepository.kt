package com.example.data.repository

import com.example.data.dao.CompressionDao
import com.example.data.dao.MediaDao
import com.example.data.dao.ShareDao
import com.example.data.dao.UserDao
import com.example.data.model.CompressionRecord
import com.example.data.model.FolderActivityLog
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.ShareLinkRecord
import com.example.data.model.SharedFolder
import com.example.data.model.SharedFolderMember
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

class MediaRepository(
    private val mediaDao: MediaDao,
    private val compressionDao: CompressionDao,
    private val shareDao: ShareDao,
    private val userDao: UserDao
) {
    val allMedia: Flow<List<MediaItem>> = mediaDao.getAllMedia()
    val favorites: Flow<List<MediaItem>> = mediaDao.getFavorites()
    val totalSizeBytes: Flow<Long?> = mediaDao.getTotalSizeBytes()
    val totalSpaceSavedBytes: Flow<Long?> = mediaDao.getTotalSpaceSavedBytes()
    val totalMediaCount: Flow<Int> = mediaDao.getTotalCount()
    val compressionHistory: Flow<List<CompressionRecord>> = compressionDao.getAllRecords()
    val totalCompressionSavedBytes: Flow<Long?> = compressionDao.getTotalSpaceSavedBytes()

    // Share & Collaboration Flows
    val allShareLinks: Flow<List<ShareLinkRecord>> = shareDao.getAllShareLinks()
    val allSharedFolders: Flow<List<SharedFolder>> = shareDao.getAllSharedFolders()
    val allUsers: Flow<List<UserAccount>> = userDao.getAllUsers()

    fun getMediaByType(type: MediaType): Flow<List<MediaItem>> = mediaDao.getMediaByType(type)

    fun searchMedia(query: String): Flow<List<MediaItem>> = mediaDao.searchMedia(query)

    fun getMediaByFolder(folderId: Long): Flow<List<MediaItem>> = mediaDao.getMediaByFolder(folderId)

    fun getMembersForFolder(folderId: Long): Flow<List<SharedFolderMember>> = shareDao.getMembersForFolder(folderId)

    fun getActivityLogsForFolder(folderId: Long): Flow<List<FolderActivityLog>> = shareDao.getActivityLogsForFolder(folderId)

    fun getShareLinksForTarget(targetType: String, targetId: Long): Flow<List<ShareLinkRecord>> =
        shareDao.getShareLinksForTarget(targetType, targetId)

    suspend fun getMediaById(id: Long): MediaItem? = mediaDao.getMediaById(id)

    suspend fun insertMedia(item: MediaItem): Long = mediaDao.insertMedia(item)

    suspend fun insertMediaList(items: List<MediaItem>): List<Long> = mediaDao.insertMediaList(items)

    suspend fun updateMedia(item: MediaItem) = mediaDao.updateMedia(item)

    suspend fun deleteMedia(item: MediaItem) = mediaDao.deleteMedia(item)

    suspend fun deleteMediaById(id: Long) = mediaDao.deleteMediaById(id)

    suspend fun deleteMediaByIds(ids: List<Long>) = mediaDao.deleteMediaByIds(ids)

    suspend fun assignMediaToFolder(mediaId: Long, folderId: Long?) = mediaDao.assignMediaToFolder(mediaId, folderId)

    suspend fun recordCompression(record: CompressionRecord): Long = compressionDao.insertRecord(record)

    suspend fun clearCompressionHistory() = compressionDao.clearHistory()

    // Share Links
    suspend fun createShareLink(link: ShareLinkRecord): Long = shareDao.insertShareLink(link)

    suspend fun getShareLinkByToken(token: String): ShareLinkRecord? = shareDao.getShareLinkByToken(token)

    suspend fun deleteShareLink(linkId: Long) = shareDao.deleteShareLinkById(linkId)

    suspend fun incrementShareAccess(linkId: Long) = shareDao.incrementAccessCount(linkId)

    // Shared Folders
    suspend fun createSharedFolder(folder: SharedFolder): Long = shareDao.insertSharedFolder(folder)

    suspend fun getSharedFolderById(id: Long): SharedFolder? = shareDao.getSharedFolderById(id)

    suspend fun deleteSharedFolder(folderId: Long) = shareDao.deleteSharedFolderById(folderId)

    suspend fun addFolderMember(member: SharedFolderMember): Long = shareDao.insertMember(member)

    suspend fun removeFolderMember(memberId: Long) = shareDao.deleteMemberById(memberId)

    suspend fun logFolderActivity(log: FolderActivityLog): Long = shareDao.insertActivityLog(log)

    // User Operations
    suspend fun getUserByEmail(email: String): UserAccount? = userDao.getUserByEmail(email)

    suspend fun getUserById(id: Long): UserAccount? = userDao.getUserById(id)

    suspend fun getUserBySessionToken(token: String): UserAccount? = userDao.getUserBySessionToken(token)

    suspend fun insertUser(user: UserAccount): Long = userDao.insertUser(user)

    suspend fun updateUser(user: UserAccount) = userDao.updateUser(user)

    suspend fun updateSessionToken(userId: Long, token: String?) = userDao.updateSessionToken(userId, token)

    suspend fun updateWebLock(userId: Long, enabled: Boolean, pin: String?) = userDao.updateWebLock(userId, enabled, pin)
}

