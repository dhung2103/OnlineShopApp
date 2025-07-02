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
@Database(entities = [Product::class, User::class, CartItem::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun userDao(): UserDao
    
    companion object {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Migration from version 1 to 2
                // No schema changes, just force recreation for stability
            }
        }
        
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Migration from version 2 to 3
                // Remove foreign key constraint from cart_items table
                database.execSQL("CREATE TABLE IF NOT EXISTS cart_items_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "productId INTEGER NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "productName TEXT NOT NULL, " +
                        "productPrice REAL NOT NULL, " +
                        "productImage TEXT NOT NULL)")
                        
                database.execSQL("INSERT INTO cart_items_new SELECT * FROM cart_items")
                database.execSQL("DROP TABLE cart_items")
                database.execSQL("ALTER TABLE cart_items_new RENAME TO cart_items")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cart_items_productId ON cart_items(productId)")
            }
        }
    }
}
