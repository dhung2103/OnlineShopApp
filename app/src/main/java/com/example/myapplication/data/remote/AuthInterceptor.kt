package com.example.onlineshopapp.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.onlineshopapp.utils.Constants
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds auth token to requests
 */
class AuthInterceptor(private val context: Context) : Interceptor {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME, Context.MODE_PRIVATE
    )

    private fun getToken(): String? {
        return sharedPrefs.getString(Constants.AUTH_TOKEN_KEY, null)
    }

    /**
     * Sets the Authorization header with the token if available
     */
    public fun setToken(token: String) {
        sharedPrefs.edit().putString(Constants.AUTH_TOKEN_KEY, token).apply()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for login and register
        if (originalRequest.url.encodedPath.contains("/auth/login") ||
            originalRequest.url.encodedPath.contains("/auth/register")) {
            return chain.proceed(originalRequest)
        }
          val requestBuilder = originalRequest.newBuilder()
        getToken()?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }

    companion object {
        fun loadTokenFromPrefs(context: Context): String? {
            val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(Constants.AUTH_TOKEN_KEY, null)
        }

        fun saveTokenToPrefs(context: Context, token: String) {
            val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(Constants.AUTH_TOKEN_KEY, token).apply()
        }

        fun clearTokenFromPrefs(context: Context) {
            val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(Constants.AUTH_TOKEN_KEY).apply()
        }
    }
}
