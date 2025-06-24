package com.example.onlineshopapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user account
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val address: String? = null
)
