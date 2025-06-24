package com.example.onlineshopapp.data.model

import java.util.Date

/**
 * Chat message model
 */
data class ChatMessage(
    val id: String,
    val userId: String,
    val userName: String,
    val isFromUser: Boolean,
    val message: String,
    val timestamp: Date
)

data class ChatMessageRequest(
    val message: String
)
