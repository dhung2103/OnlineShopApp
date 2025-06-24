package com.example.onlineshopapp.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.onlineshopapp.MainActivity
import com.example.onlineshopapp.OnlineShopApplication
import com.example.onlineshopapp.R

/**
 * Helper class for creating and displaying notifications
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Show a notification for items in the cart
     * @param itemCount Number of items in the cart
     * @param totalPrice Total price of items in the cart
     */
    fun showCartNotification(itemCount: Int, totalPrice: Double) {
        // Create intent to open the cart when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "cart")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, OnlineShopApplication.CART_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cart_notification)
            .setContentTitle(context.getString(R.string.items_in_cart))
            .setContentText(context.getString(R.string.cart_notification_text, itemCount, totalPrice))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Show the notification
        notificationManager.notify(Constants.CART_NOTIFICATION_ID, notification)
    }
    
    /**
     * Clear all notifications
     */
    fun clearNotifications() {
        notificationManager.cancelAll()
    }
}
