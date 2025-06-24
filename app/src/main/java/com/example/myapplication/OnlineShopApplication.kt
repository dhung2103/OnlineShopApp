package com.example.onlineshopapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.example.onlineshopapp.data.local.AppDatabase
import com.example.onlineshopapp.data.remote.ApiService
import com.example.onlineshopapp.utils.Constants
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for initializing app-wide components
 */
@HiltAndroidApp
class OnlineShopApplication : Application() {

    // Database and API service instances for non-DI code
    companion object {
        lateinit var database: AppDatabase
            private set
            
//        lateinit var apiService: ApiService
//            private set
            
        const val CART_NOTIFICATION_CHANNEL_ID = "cart_notification_channel"
    }
      override fun onCreate() {
        super.onCreate()
        
        // Initialize Room Database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "online_shop_database"
        ).fallbackToDestructiveMigration().build()
        
        // Initialize API service
//        apiService = ApiService.create(Constants.BASE_URL)
        
        // Create notification channels
        createNotificationChannels()
        
        // Note: Most of this initialization is also handled by Hilt dependency injection.
        // These properties are maintained for backward compatibility with non-Hilt code.
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val cartChannel = NotificationChannel(
                CART_NOTIFICATION_CHANNEL_ID,
                "Cart Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for products in your cart"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(cartChannel)
        }
    }
}
