package com.example.onlineshopapp.data.model

import java.util.Date

/**
 * Order and checkout models
 */
data class Order(
    val id: String,
    val userId: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val date: Date,
    val status: OrderStatus,
    val shippingAddress: String
)

enum class OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

data class CheckoutRequest(
    val items: List<CartItem>,
    val totalAmount: Double,
    val shippingAddress: String
)

data class CheckoutResponse(
    val success: Boolean,
    val message: String,
    val order: Order? = null
)
