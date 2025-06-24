package com.example.onlineshopapp.data.model

/**
 * Store location model
 */
data class StoreLocation(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String,
    val openingHours: String
)
