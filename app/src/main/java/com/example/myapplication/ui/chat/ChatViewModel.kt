package com.example.onlineshopapp.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshopapp.data.model.ChatMessage
import com.example.onlineshopapp.data.repository.ChatRepository
import com.example.onlineshopapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for chat functionality
 */
@HiltViewModel
class ChatViewModel @Inject constructor(private val chatRepository: ChatRepository) : ViewModel() {

    // LiveData for chat messages
    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages
    
    // Loading state
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    // Error message
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        // Load chat messages when ViewModel is created
        loadChatMessages()
        
        // Observe chat messages flow
        viewModelScope.launch {
            chatRepository.getChatMessagesAsFlow().collect { messages ->
                _chatMessages.postValue(messages)
            }
        }
    }
    
    /**
     * Load chat messages
     */
    fun loadChatMessages() {
        _loading.value = true
        
        viewModelScope.launch {
            when (val result = chatRepository.getChatMessages()) {
                is Resource.Success -> {
                    _chatMessages.postValue(result.data ?: emptyList())
                    _error.postValue(null)
                }
                is Resource.Error -> {
                    _error.postValue(result.message)
                }
                else -> {}
            }
            _loading.postValue(false)
        }
    }
    
    /**
     * Send a new message
     */
    fun sendMessage(message: String) {
        // Skip empty messages
        if (message.trim().isEmpty()) return
        
        viewModelScope.launch {
            when (val result = chatRepository.sendMessage(message)) {
                is Resource.Success -> {
                    // Message is automatically added to the list via Flow
                    _error.postValue(null)
                }
                is Resource.Error -> {
                    _error.postValue(result.message)
                }
                else -> {}
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}
