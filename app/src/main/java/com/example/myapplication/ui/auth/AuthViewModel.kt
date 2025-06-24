package com.example.onlineshopapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshopapp.data.model.AuthResponse
import com.example.onlineshopapp.data.model.User
import com.example.onlineshopapp.data.repository.AuthRepository
import com.example.onlineshopapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication screens
 */
@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    // LiveData for login status
    private val _loginStatus = MutableLiveData<Resource<AuthResponse>>()
    val loginStatus: LiveData<Resource<AuthResponse>> = _loginStatus

    // LiveData for registration status
    private val _registerStatus = MutableLiveData<Resource<AuthResponse>>()
    val registerStatus: LiveData<Resource<AuthResponse>> = _registerStatus

    // LiveData for current user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    // Track login state
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    init {
        // Check login status at initialization
        _isLoggedIn.value = authRepository.isLoggedIn()

        // Load current user if logged in
        if (_isLoggedIn.value == true) {
            loadCurrentUser()
        }
    }

    /**
     * Login with email and password
     */
    fun login(email: String, password: String) {
        _loginStatus.value = Resource.Loading()

        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _loginStatus.postValue(result)

            if (result is Resource.Success) {
                _isLoggedIn.postValue(true)
                loadCurrentUser()
            }
        }
    }

    /**
     * Register a new user
     */
    fun register(name: String, email: String, password: String, phone: String? = null) {
        _registerStatus.value = Resource.Loading()

        viewModelScope.launch {
            val result = authRepository.register(name, email, password, phone)
            _registerStatus.postValue(result)

            if (result is Resource.Success) {
                _isLoggedIn.postValue(true)
                loadCurrentUser()
            }
        }
    }

    /**
     * Load current user data
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.postValue(authRepository.getCurrentUser())
        }
    }

    /**
     * Logout the current user
     */
    fun logout() {
        authRepository.logout()
        _isLoggedIn.postValue(false)
        _currentUser.postValue(null)
    }
}
