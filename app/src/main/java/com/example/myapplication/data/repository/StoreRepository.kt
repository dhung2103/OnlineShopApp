package com.example.onlineshopapp.data.repository

import android.content.Context
import com.example.onlineshopapp.data.model.StoreLocation
import com.example.onlineshopapp.data.remote.ApiService
import com.example.onlineshopapp.data.remote.MockApiHelper
import com.example.onlineshopapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for store location data
 */
class StoreRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    private val mockApiHelper = MockApiHelper(context)
    
    /**
     * Get store locations information
     */
    suspend fun getStoreLocations(): Resource<List<StoreLocation>> {
        return withContext(Dispatchers.IO) {
            try {
                // Try to fetch from API
                val response = apiService.getStoreLocations()
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        return@withContext Resource.Success(it)
                    }
                }
                
                // Try to get from mock data
                val mockLocation = mockApiHelper.getStoreLocation()
                return@withContext Resource.Success(listOf(mockLocation))
                
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> Resource.Error("Network error: ${e.message}")
                    is IOException -> {
                        // Try to get from mock data
                        try {
                            val mockLocation = mockApiHelper.getStoreLocation()
                            Resource.Success(listOf(mockLocation))
                        } catch (mockEx: Exception) {
                            Resource.Error("Unable to load store locations: ${mockEx.message}")
                        }
                    }
                    else -> Resource.Error("Error fetching store locations: ${e.message}")
                }
            }
        }
    }
}
