package com.example.onlineshopapp.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a product in the store
 */
@Parcelize
@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val inStock: Boolean = true,
    val rating: Float = 0f,
    val reviewCount: Int = 0
) : Parcelable
