package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY dateAdded DESC")
    fun getAllMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE type = :type ORDER BY dateAdded DESC")
    fun getMediaByType(type: MediaType): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavorites(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE sharedFolderId = :folderId ORDER BY dateAdded DESC")
    fun getMediaByFolder(folderId: Long): Flow<List<MediaItem>>

    @Query("SELECT COUNT(*) FROM media_items WHERE sharedFolderId = :folderId")
    fun getMediaCountInFolder(folderId: Long): Flow<Int>

    @Query("UPDATE media_items SET sharedFolderId = :folderId WHERE id = :mediaId")
    suspend fun assignMediaToFolder(mediaId: Long, folderId: Long?)

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaById(id: Long): MediaItem?

    @Query("SELECT * FROM media_items WHERE title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchMedia(query: String): Flow<List<MediaItem>>

    @Query("SELECT SUM(sizeBytes) FROM media_items")
    fun getTotalSizeBytes(): Flow<Long?>

    @Query("SELECT SUM(originalSizeBytes - sizeBytes) FROM media_items WHERE originalSizeBytes > sizeBytes")
    fun getTotalSpaceSavedBytes(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM media_items")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(items: List<MediaItem>): List<Long>

    @Update
    suspend fun updateMedia(item: MediaItem)

    @Delete
    suspend fun deleteMedia(item: MediaItem)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    @Query("DELETE FROM media_items WHERE id IN (:ids)")
    suspend fun deleteMediaByIds(ids: List<Long>)
}
