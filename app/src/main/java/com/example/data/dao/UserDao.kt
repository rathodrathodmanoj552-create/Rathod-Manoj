package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserAccount?

    @Query("SELECT * FROM users WHERE sessionToken = :token LIMIT 1")
    suspend fun getUserBySessionToken(token: String): UserAccount?

    @Query("SELECT * FROM users ORDER BY dateJoined DESC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long

    @Update
    suspend fun updateUser(user: UserAccount)

    @Delete
    suspend fun deleteUser(user: UserAccount)

    @Query("UPDATE users SET sessionToken = :token WHERE id = :userId")
    suspend fun updateSessionToken(userId: Long, token: String?)

    @Query("UPDATE users SET isWebLockEnabled = :enabled, webLockPin = :pin WHERE id = :userId")
    suspend fun updateWebLock(userId: Long, enabled: Boolean, pin: String?)
}
