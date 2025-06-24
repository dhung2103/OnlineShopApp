package com.example.onlineshopapp.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.onlineshopapp.data.local.ProductDao
import com.example.onlineshopapp.data.model.Product
import com.example.onlineshopapp.data.remote.ApiService
import com.example.onlineshopapp.data.remote.MockApiHelper
import com.example.onlineshopapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository class for Product-related operations
 */
class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: ApiService,
    private val context: Context
) {
    private val mockApiHelper = MockApiHelper(context)
    
    /**
     * Get all products, first from API then fallback to local database
     */
    fun getProducts() = liveData(Dispatchers.IO) {
        emit(Resource.Loading<List<Product>>())
        try {
            // Try to fetch from API
            val response = apiService.getProducts()
            if (response.isSuccessful) {
                response.body()?.let { products ->
                    // Cache the products in database
                    productDao.insertProducts(products)
                    emit(Resource.Success(products))
                }
            } else {
                // If API fails, try to get from database
                val localProducts = productDao.getAllProducts().value
                if (localProducts != null && localProducts.isNotEmpty()) {
                    emit(Resource.Success(localProducts))
                } else {
                    // If database is empty, try to get from mock data
                    val mockProducts = mockApiHelper.getProducts()
                    productDao.insertProducts(mockProducts)
                    emit(Resource.Success(mockProducts))
                }
            }
        } catch (e: Exception) {
            // Handle network errors or other exceptions
            when (e) {
                is HttpException -> emit(Resource.Error("Network error: ${e.message}"))
                is IOException -> {
                    // Network error, try to get from database
                    val localProducts = productDao.getAllProducts().value
                    if (localProducts != null && localProducts.isNotEmpty()) {
                        emit(Resource.Success(localProducts))
                    } else {
                        // If database is empty, try to get from mock data
                        try {
                            val mockProducts = mockApiHelper.getProducts()
                            productDao.insertProducts(mockProducts)
                            emit(Resource.Success(mockProducts))
                        } catch (mockEx: Exception) {
                            emit(Resource.Error("Unable to load products: ${mockEx.message}"))
                        }
                    }
                }
                else -> emit(Resource.Error("Error fetching products: ${e.message}"))
            }
        }
    }
    
    /**
     * Get a single product by ID
     */
    suspend fun getProductById(id: Int): Resource<Product> {
        return withContext(Dispatchers.IO) {
            try {
                // Try to fetch from API
                val response = apiService.getProductById(id)
                if (response.isSuccessful) {
                    response.body()?.let {
                        productDao.insertProduct(it)
                        return@withContext Resource.Success(it)
                    }
                }
                
                // Try to get from local database
                val localProduct = productDao.getProductById(id)
                if (localProduct != null) {
                    return@withContext Resource.Success(localProduct)
                }
                
                // Try to get from mock data
                val mockProduct = mockApiHelper.getProductById(id)
                if (mockProduct != null) {
                    productDao.insertProduct(mockProduct)
                    return@withContext Resource.Success(mockProduct)
                }
                
                Resource.Error("Product not found")
            } catch (e: Exception) {
                // Handle errors
                when (e) {
                    is HttpException -> Resource.Error("Network error: ${e.message}")
                    is IOException -> {
                        // Try local database
                        val localProduct = productDao.getProductById(id)
                        if (localProduct != null) {
                            Resource.Success(localProduct)
                        } else {
                            // Try mock data
                            val mockProduct = mockApiHelper.getProductById(id)
                            if (mockProduct != null) {
                                productDao.insertProduct(mockProduct)
                                Resource.Success(mockProduct)
                            } else {
                                Resource.Error("Unable to load product: ${e.message}")
                            }
                        }
                    }
                    else -> Resource.Error("Error fetching product: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Get all products from local database
     */
    fun getLocalProducts(): LiveData<List<Product>> {
        return productDao.getAllProducts()
    }
}
