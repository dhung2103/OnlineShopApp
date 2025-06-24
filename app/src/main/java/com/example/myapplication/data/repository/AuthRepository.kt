package com.example.onlineshopapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.onlineshopapp.data.local.UserDao
import com.example.onlineshopapp.data.model.AuthResponse
import com.example.onlineshopapp.data.model.LoginRequest
import com.example.onlineshopapp.data.model.RegisterRequest
import com.example.onlineshopapp.data.model.User
import com.example.onlineshopapp.data.remote.ApiService
import com.example.onlineshopapp.data.remote.AuthInterceptor
import com.example.onlineshopapp.data.remote.MockApiHelper
import com.example.onlineshopapp.utils.Constants
import com.example.onlineshopapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for authentication related operations
 */
class AuthRepository(
    private val userDao: UserDao,
    private val apiService: ApiService,
    private val context: Context,
    private val authInterceptor: AuthInterceptor
) {
    private val mockApiHelper = MockApiHelper(context)
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME, Context.MODE_PRIVATE
    )

    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = apiService.login(loginRequest)

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Save token and user data
                        saveAuthData(authResponse)

                        return@withContext Resource.Success(authResponse)
                    }
                }

                // Mock login for offline mode (only works with sample credentials)
                if (email == "johndoe@example.com" && password == "password") {
                    try {
                        val user = mockApiHelper.getUser()
                        val mockAuthResponse = AuthResponse(
                            token = "mock-token-${System.currentTimeMillis()}", user = user
                        )

                        // Save mock data
                        saveAuthData(mockAuthResponse)

                        return@withContext Resource.Success(mockAuthResponse)
                    } catch (e: Exception) {
                        // Continue with error handling
                    }
                }

                Resource.Error(
                    "Login failed: " + (response.errorBody()?.string() ?: "Unknown error")
                )
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> Resource.Error("Network error: ${e.message}")
                    is IOException -> {
                        // Try mock login for demo user
                        if (email == "johndoe@example.com" && password == "password") {
                            try {
                                val user = mockApiHelper.getUser()
                                val mockAuthResponse = AuthResponse(
                                    token = "mock-token-${System.currentTimeMillis()}", user = user
                                )

                                // Save mock data
                                saveAuthData(mockAuthResponse)

                                return@withContext Resource.Success(mockAuthResponse)
                            } catch (mockEx: Exception) {
                                Resource.Error("Offline login failed: ${mockEx.message}")
                            }
                        } else {
                            Resource.Error("Network error: Please check your connection")
                        }
                    }

                    else -> Resource.Error("Login error: ${e.message}")
                }
            }
        }
    }

    /**
     * Register a new user
     */
    suspend fun register(
        name: String, email: String, password: String, phone: String? = null
    ): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val registerRequest = RegisterRequest(name, email, password, phone)
                val response = apiService.register(registerRequest)

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Save token and user data
                        saveAuthData(authResponse)

                        return@withContext Resource.Success(authResponse)
                    }
                }

                Resource.Error(
                    "Registration failed: " + (response.errorBody()?.string() ?: "Unknown error")
                )
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> Resource.Error("Network error: ${e.message}")
                    is IOException -> Resource.Error("Network error: Please check your connection")
                    else -> Resource.Error("Registration error: ${e.message}")
                }
            }
        }
    }

    /**
     * Save authentication data (token and user)
     */
    private suspend fun saveAuthData(authResponse: AuthResponse) {
        // Save token and user ID to SharedPreferences
        sharedPreferences.edit().putString(Constants.AUTH_TOKEN_KEY, authResponse.token)
            .putString(Constants.USER_ID_KEY, authResponse.user.id)
            .putBoolean(Constants.IS_LOGGED_IN, true).apply()

        // Update auth interceptor token
        authInterceptor.setToken(authResponse.token)

        // Save user to database
        userDao.insertUser(authResponse.user)
    }

    /**
     * Get current logged-in user
     */
    suspend fun getCurrentUser(): User? {
        val userId = sharedPreferences.getString(Constants.USER_ID_KEY, null) ?: return null
        return userDao.getUserById(userId)
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(Constants.IS_LOGGED_IN, false)
    }

    /**
     * Logout user
     */
    fun logout() {
        // Clear stored data
        sharedPreferences.edit().remove(Constants.AUTH_TOKEN_KEY).remove(Constants.USER_ID_KEY)
            .putBoolean(Constants.IS_LOGGED_IN, false).apply()

        // Clear token from interceptor
        authInterceptor.setToken("")
    }
}
