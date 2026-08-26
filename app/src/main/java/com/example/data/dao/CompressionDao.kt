package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CompressionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CompressionDao {
    @Query("SELECT * FROM compression_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CompressionRecord>>

    @Query("SELECT SUM(originalSizeBytes - compressedSizeBytes) FROM compression_records WHERE originalSizeBytes > compressedSizeBytes")
    fun getTotalSpaceSavedBytes(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CompressionRecord): Long

    @Query("DELETE FROM compression_records")
    suspend fun clearHistory()
}
