package com.example.onlineshopapp.data.remote

import android.content.Context
import com.example.onlineshopapp.data.model.*
import com.example.onlineshopapp.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Mock API implementation that uses local JSON files
 * for offline data when the real API is unavailable
 */
class MockApiHelper(private val context: Context) {

    private val gson = Gson()
    
    /**
     * Load products from local JSON file
     */
    suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open(Constants.PRODUCTS_JSON_FILE).bufferedReader().use { it.readText() }
            val productType = object : TypeToken<List<Product>>() {}.type
            gson.fromJson(jsonString, productType)
        } catch (e: Exception) {
            // Fallback to hardcoded sample data if file is missing
            getSampleProducts()
        }
    }
    
    /**
     * Get a single product by ID
     */
    suspend fun getProductById(id: Int): Product? = withContext(Dispatchers.IO) {
        getProducts().find { it.id == id }
    }
    
    /**
     * Load user data from local JSON
     */
    suspend fun getUser(): User = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open(Constants.USER_JSON_FILE).bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, User::class.java)
        } catch (e: Exception) {
            // Fallback sample user
            getSampleUser()
        }
    }
    
    /**
     * Load chat messages from local JSON
     */
    suspend fun getChatMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open(Constants.CHAT_MESSAGES_FILE).bufferedReader().use { it.readText() }
            val messageType = object : TypeToken<List<ChatMessage>>() {}.type
            gson.fromJson(jsonString, messageType)
        } catch (e: Exception) {
            // Fallback sample messages
            getSampleChatMessages()
        }
    }
    
    /**
     * Get store location from local JSON
     */
    suspend fun getStoreLocation(): StoreLocation = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open(Constants.STORE_LOCATION_FILE).bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, StoreLocation::class.java)
        } catch (e: Exception) {
            // Fallback sample location
            getSampleStoreLocation()
        }
    }
    
    /**
     * Generate sample products as fallback
     */
    private fun getSampleProducts(): List<Product> {
        return listOf(
            Product(
                id = 1,
                name = "Smartphone XYZ",
                description = "A powerful smartphone with latest features",
                price = 699.99,
                imageUrl = "https://via.placeholder.com/400",
                category = "Electronics",
                rating = 4.5f,
                reviewCount = 120
            ),
            Product(
                id = 2,
                name = "Laptop Pro",
                description = "Ultra-thin laptop with high performance",
                price = 1299.99,
                imageUrl = "https://via.placeholder.com/400",
                category = "Electronics",
                rating = 4.8f,
                reviewCount = 85
            ),
            Product(
                id = 3,
                name = "Wireless Headphones",
                description = "Premium sound quality headphones",
                price = 199.99,
                imageUrl = "https://via.placeholder.com/400",
                category = "Audio",
                rating = 4.3f,
                reviewCount = 210
            ),
            Product(
                id = 4,
                name = "Smart Watch",
                description = "Track your fitness with this smart watch",
                price = 249.99,
                imageUrl = "https://via.placeholder.com/400",
                category = "Wearables",
                rating = 4.2f,
                reviewCount = 65
            ),
            Product(
                id = 5,
                name = "Bluetooth Speaker",
                description = "Portable speaker with deep bass",
                price = 89.99,
                imageUrl = "https://via.placeholder.com/400",
                category = "Audio",
                rating = 4.0f,
                reviewCount = 178
            ),
            Product(
                id = 6,
                name = "Tablet Air",
                description = "Lightweight tablet perfect for entertainment",
                price = 399.99,
                imageUrl = "https://via.placeholder.com/400",
                category = "Electronics",
                rating = 4.6f,
                reviewCount = 92
            ),
            Product(
                id = 7,
                name = "Camera 4K",
                description = "Professional camera with 4K video recording",
                price = 899.99,
                imageUrl = "https://via.placeholder.com/400",
                category = "Photography",
                rating = 4.7f,
                reviewCount = 45
            ),
            Product(
                id = 8,
                name = "Gaming Console",
                description = "Next generation gaming experience",
                price = 499.99,
                imageUrl = "https://via.placeholder.com/400",
                category = "Gaming",
                rating = 4.9f,
                reviewCount = 320
            )
        )
    }
    
    /**
     * Generate a sample user as fallback
     */
    private fun getSampleUser(): User {
        return User(
            id = "user123",
            name = "John Doe",
            email = "johndoe@example.com",
            phone = "0123456789",
            avatarUrl = "https://via.placeholder.com/150",
            address = "123 Main St, City, Country"
        )
    }
    
    /**
     * Generate sample chat messages as fallback
     */
    private fun getSampleChatMessages(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "msg1",
                userId = "user123",
                userName = "John Doe",
                isFromUser = true,
                message = "Hi, I have a question about my recent order",
                timestamp = Date(System.currentTimeMillis() - 86400000) // Yesterday
            ),
            ChatMessage(
                id = "msg2",
                userId = "store",
                userName = "Store Support",
                isFromUser = false,
                message = "Hello John, how can I help you today?",
                timestamp = Date(System.currentTimeMillis() - 86000000)
            )
        )
    }
    
    /**
     * Generate a sample store location as fallback
     */
    private fun getSampleStoreLocation(): StoreLocation {
        return StoreLocation(
            id = "store1",
            name = "Main Store",
            address = "456 Shop Street, Shopping District, City",
            latitude = 21.027763,  // Example coordinates (Hanoi)
            longitude = 105.834160,
            phoneNumber = "0987654321",
            openingHours = "Mon-Sat: 9:00 - 20:00, Sun: 10:00 - 18:00"
        )
    }
}
