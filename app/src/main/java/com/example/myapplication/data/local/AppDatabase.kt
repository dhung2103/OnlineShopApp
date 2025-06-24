package com.example.onlineshopapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.onlineshopapp.data.model.CartItem
import com.example.onlineshopapp.data.model.Product
import com.example.onlineshopapp.data.model.User

/**
 * Main database for the application
 */
@Database(entities = [Product::class, User::class, CartItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun userDao(): UserDao
}
