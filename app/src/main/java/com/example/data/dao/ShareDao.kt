package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FolderActivityLog
import com.example.data.model.ShareLinkRecord
import com.example.data.model.SharedFolder
import com.example.data.model.SharedFolderMember
import kotlinx.coroutines.flow.Flow

@Dao
interface ShareDao {

    // --- Share Links ---
    @Query("SELECT * FROM share_links ORDER BY createdTimestamp DESC")
    fun getAllShareLinks(): Flow<List<ShareLinkRecord>>

    @Query("SELECT * FROM share_links WHERE shareToken = :token LIMIT 1")
    suspend fun getShareLinkByToken(token: String): ShareLinkRecord?

    @Query("SELECT * FROM share_links WHERE targetType = :targetType AND targetId = :targetId ORDER BY createdTimestamp DESC")
    fun getShareLinksForTarget(targetType: String, targetId: Long): Flow<List<ShareLinkRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShareLink(link: ShareLinkRecord): Long

    @Update
    suspend fun updateShareLink(link: ShareLinkRecord)

    @Query("UPDATE share_links SET accessCount = accessCount + 1 WHERE id = :linkId")
    suspend fun incrementAccessCount(linkId: Long)

    @Delete
    suspend fun deleteShareLink(link: ShareLinkRecord)

    @Query("DELETE FROM share_links WHERE id = :linkId")
    suspend fun deleteShareLinkById(linkId: Long)

    // --- Shared Folders ---
    @Query("SELECT * FROM shared_folders ORDER BY dateCreated DESC")
    fun getAllSharedFolders(): Flow<List<SharedFolder>>

    @Query("SELECT * FROM shared_folders")
    suspend fun getSharedFoldersSync(): List<SharedFolder>

    @Query("SELECT * FROM shared_folders WHERE id = :id LIMIT 1")
    suspend fun getSharedFolderById(id: Long): SharedFolder?

    @Query("SELECT * FROM shared_folders WHERE inviteCode = :code LIMIT 1")
    suspend fun getSharedFolderByInviteCode(code: String): SharedFolder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedFolder(folder: SharedFolder): Long

    @Update
    suspend fun updateSharedFolder(folder: SharedFolder)

    @Delete
    suspend fun deleteSharedFolder(folder: SharedFolder)

    @Query("DELETE FROM shared_folders WHERE id = :folderId")
    suspend fun deleteSharedFolderById(folderId: Long)

    // --- Shared Folder Members ---
    @Query("SELECT * FROM shared_folder_members WHERE folderId = :folderId ORDER BY joinedDate ASC")
    fun getMembersForFolder(folderId: Long): Flow<List<SharedFolderMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: SharedFolderMember): Long

    @Delete
    suspend fun deleteMember(member: SharedFolderMember)

    @Query("DELETE FROM shared_folder_members WHERE id = :memberId")
    suspend fun deleteMemberById(memberId: Long)

    // --- Activity Logs ---
    @Query("SELECT * FROM folder_activity_logs WHERE folderId = :folderId ORDER BY timestamp DESC LIMIT 50")
    fun getActivityLogsForFolder(folderId: Long): Flow<List<FolderActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: FolderActivityLog): Long
}
