package com.example.onlineshopapp.ui.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshopapp.data.model.Product
import com.example.onlineshopapp.data.repository.ProductRepository
import com.example.onlineshopapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for product-related screens
 */
@HiltViewModel
class ProductViewModel @Inject constructor(private val productRepository: ProductRepository) : ViewModel() {

    // LiveData for product list
    private val _products = MutableLiveData<Resource<List<Product>>>()
    val products: LiveData<Resource<List<Product>>> = _products
    
    // LiveData for product details
    private val _productDetails = MutableLiveData<Resource<Product>>()
    val productDetails: LiveData<Resource<Product>> = _productDetails
    
    // Local cached products
    val localProducts = productRepository.getLocalProducts()
    
    init {
        // Load products when ViewModel is created
        loadProducts()
    }
    
    /**
     * Load all products
     */
    fun loadProducts() {
        viewModelScope.launch {
            _products.value = Resource.Loading()
            _products.value = Resource.Success(emptyList()) // Clear the list first
            
            // Collect from repository
            productRepository.getProducts().observeForever { resource ->
                _products.value = resource
            }
        }
    }
    
    /**
     * Load a single product by ID
     */
    fun loadProductById(productId: Int) {
        viewModelScope.launch {
            _productDetails.value = Resource.Loading()
            
            val result = productRepository.getProductById(productId)
            _productDetails.postValue(result)
        }
    }
    
    /**
     * Search products by name or category
     */
    fun searchProducts(query: String) {
        viewModelScope.launch {
            val currentProducts = _products.value?.data ?: emptyList()
            if (query.isEmpty()) {
                // If query is empty, restore full list
                _products.postValue(Resource.Success(currentProducts))
                return@launch
            }
            
            // Filter products by name or category
            val filteredProducts = currentProducts.filter {
                it.name.contains(query, ignoreCase = true) || 
                it.category.contains(query, ignoreCase = true)
            }
            
            _products.postValue(Resource.Success(filteredProducts))
        }
    }
}
