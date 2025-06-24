package com.example.onlineshopapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.onlineshopapp.data.model.User

/**
 * Data Access Object for users
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}
