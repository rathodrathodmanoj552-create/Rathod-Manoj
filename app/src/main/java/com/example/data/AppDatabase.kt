package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CompressionDao
import com.example.data.dao.MediaDao
import com.example.data.dao.ShareDao
import com.example.data.dao.UserDao
import com.example.data.model.CompressionRecord
import com.example.data.model.FolderActivityLog
import com.example.data.model.MediaItem
import com.example.data.model.ShareLinkRecord
import com.example.data.model.SharedFolder
import com.example.data.model.SharedFolderMember
import com.example.data.model.UserAccount

@Database(
    entities = [
        MediaItem::class,
        CompressionRecord::class,
        ShareLinkRecord::class,
        SharedFolder::class,
        SharedFolderMember::class,
        FolderActivityLog::class,
        UserAccount::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun compressionDao(): CompressionDao
    abstract fun shareDao(): ShareDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "miracle_vault.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
