package com.example.onlineshopapp.data.model

/**
 * Authentication request/response models
 */
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null
)

data class AuthResponse(
    val token: String,
    val user: User
)
