package com.example.onlineshopapp.data.repository

import android.content.Context
import com.example.onlineshopapp.data.model.ChatMessage
import com.example.onlineshopapp.data.model.ChatMessageRequest
import com.example.onlineshopapp.data.remote.ApiService
import com.example.onlineshopapp.data.remote.MockApiHelper
import com.example.onlineshopapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Date

/**
 * Repository for chat-related operations
 */
class ChatRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    private val mockApiHelper = MockApiHelper(context)
    private val chatMessages = mutableListOf<ChatMessage>()
    
    /**
     * Get all chat messages
     */
    suspend fun getChatMessages(): Resource<List<ChatMessage>> {
        return withContext(Dispatchers.IO) {
            try {
                // Try to fetch from API
                val response = apiService.getChatMessages()
                
                if (response.isSuccessful) {
                    response.body()?.let { messages ->
                        // Update local cache
                        chatMessages.clear()
                        chatMessages.addAll(messages)
                        return@withContext Resource.Success(messages)
                    }
                }
                
                // If API fails and local cache is empty, try to get from mock data
                if (chatMessages.isEmpty()) {
                    val mockMessages = mockApiHelper.getChatMessages()
                    chatMessages.addAll(mockMessages)
                    return@withContext Resource.Success(mockMessages)
                }
                
                Resource.Success(chatMessages)
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> Resource.Error("Network error: ${e.message}")
                    is IOException -> {
                        // Try to get from local cache
                        if (chatMessages.isNotEmpty()) {
                            return@withContext Resource.Success(chatMessages)
                        }
                        
                        // Try to get from mock data
                        try {
                            val mockMessages = mockApiHelper.getChatMessages()
                            chatMessages.addAll(mockMessages)
                            Resource.Success(mockMessages)
                        } catch (mockEx: Exception) {
                            Resource.Error("Unable to load chat messages: ${mockEx.message}")
                        }
                    }
                    else -> Resource.Error("Error fetching chat messages: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Send a new chat message
     */
    suspend fun sendMessage(message: String): Resource<ChatMessage> {
        return withContext(Dispatchers.IO) {
            try {
                val messageRequest = ChatMessageRequest(message)
                
                // Try to send via API
                val response = apiService.sendChatMessage(messageRequest)
                
                if (response.isSuccessful) {
                    response.body()?.let { chatMessage ->
                        // Add to local cache
                        chatMessages.add(chatMessage)
                        return@withContext Resource.Success(chatMessage)
                    }
                }
                
                // Offline mode - create a mock message
                val mockMessage = ChatMessage(
                    id = "msg-${System.currentTimeMillis()}",
                    userId = "user123",
                    userName = "John Doe",
                    isFromUser = true,
                    message = message,
                    timestamp = Date()
                )
                
                // Add to local cache
                chatMessages.add(mockMessage)
                
                // Create a mock response from store (simulate conversation)
                if (message.isNotEmpty()) {
                    val storeResponse = createMockStoreResponse(message)
                    chatMessages.add(storeResponse)
                }
                
                Resource.Success(mockMessage)
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> Resource.Error("Network error: ${e.message}")
                    is IOException -> {
                        // Offline mode - create a mock message
                        val mockMessage = ChatMessage(
                            id = "msg-${System.currentTimeMillis()}",
                            userId = "user123",
                            userName = "John Doe",
                            isFromUser = true,
                            message = message,
                            timestamp = Date()
                        )
                        
                        // Add to local cache
                        chatMessages.add(mockMessage)
                        
                        // Create a mock response from store (simulate conversation)
                        if (message.isNotEmpty()) {
                            val storeResponse = createMockStoreResponse(message)
                            chatMessages.add(storeResponse)
                        }
                        
                        Resource.Success(mockMessage)
                    }
                    else -> Resource.Error("Error sending message: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Create a mock response from store based on user message
     */
    private fun createMockStoreResponse(userMessage: String): ChatMessage {
        // Simulate a delay
        Thread.sleep(500)
        
        val responseMessage = when {
            userMessage.contains("hello", ignoreCase = true) || 
            userMessage.contains("hi", ignoreCase = true) -> 
                "Hello! How can I help you today?"
                
            userMessage.contains("order", ignoreCase = true) -> 
                "I'd be happy to help with your order. Could you provide your order number?"
                
            userMessage.contains("delivery", ignoreCase = true) || 
            userMessage.contains("shipping", ignoreCase = true) -> 
                "Our standard delivery time is 3-5 business days. Express shipping is available for an additional fee."
                
            userMessage.contains("price", ignoreCase = true) || 
            userMessage.contains("cost", ignoreCase = true) ->
                "Our prices are competitive and include all taxes. Is there a specific product you're interested in?"
                
            userMessage.contains("thank", ignoreCase = true) -> 
                "You're welcome! Is there anything else I can help you with?"
                
            else -> "Thank you for your message. A customer representative will get back to you shortly."
        }
        
        return ChatMessage(
            id = "response-${System.currentTimeMillis()}",
            userId = "store",
            userName = "Store Support",
            isFromUser = false,
            message = responseMessage,
            timestamp = Date()
        )
    }
    
    /**
     * Get real-time chat messages as Flow
     */
    fun getChatMessagesAsFlow(): Flow<List<ChatMessage>> = flow {
        // Initial load
        try {
            val result = getChatMessages()
            if (result is Resource.Success) {
                emit(result.data ?: emptyList())
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
}
