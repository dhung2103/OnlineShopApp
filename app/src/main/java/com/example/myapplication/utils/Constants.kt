package com.example.onlineshopapp.utils

/**
 * Contains application-wide constant values
 */
object Constants {
    // API Base URL
    const val BASE_URL = "https://68653f0b5b5d8d0339806c3f.mockapi.io/api/"
    
    // Shared Preferences
    const val PREFS_NAME = "online_shop_prefs"
    const val AUTH_TOKEN_KEY = "auth_token"
    const val USER_ID_KEY = "user_id"
    const val IS_LOGGED_IN = "is_logged_in"
    
    // Local data filenames
    const val PRODUCTS_JSON_FILE = "products.json"
    const val USER_JSON_FILE = "user.json"
    const val CHAT_MESSAGES_FILE = "chat_messages.json"
    const val STORE_LOCATION_FILE = "store_location.json"
    
    // UI Constants
    const val SPLASH_DELAY = 2000L // 2 seconds
    const val CART_NOTIFICATION_ID = 100
}
