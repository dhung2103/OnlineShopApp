package com.example.onlineshopapp.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshopapp.data.model.*
import com.example.onlineshopapp.data.repository.CartRepository
import com.example.onlineshopapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for shopping cart and checkout
 */
@HiltViewModel
class CartViewModel @Inject constructor(private val cartRepository: CartRepository) : ViewModel() {

    // LiveData for cart items
    val cartItems = cartRepository.getCartItems()
    
    // LiveData for cart count (number of items)
    val cartCount = cartRepository.getCartCount()
    
    // LiveData for cart total price
    val cartTotal = cartRepository.getCartTotal()
    
    // LiveData for checkout status
    private val _checkoutStatus = MutableLiveData<Resource<CheckoutResponse>>()
    val checkoutStatus: LiveData<Resource<CheckoutResponse>> = _checkoutStatus
    
    /**
     * Add product to cart
     */
    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                val result = cartRepository.addToCart(product, quantity)
                if (result is Resource.Error) {
                    android.util.Log.e("CartViewModel", "Error adding to cart: ${result.message}")
                } else {
                    android.util.Log.d("CartViewModel", "Successfully added product ${product.id} to cart")
                }
            } catch (e: Exception) {
                android.util.Log.e("CartViewModel", "Exception adding to cart", e)
            }
        }
    }
    
    /**
     * Update quantity of item in cart
     */
    fun updateCartItemQuantity(productId: Int, quantity: Int) {
        viewModelScope.launch {
            try {
                val result = cartRepository.updateCartItemQuantity(productId, quantity)
                if (result is Resource.Error) {
                    android.util.Log.e("CartViewModel", "Error updating cart: ${result.message}")
                } else {
                    android.util.Log.d("CartViewModel", "Successfully updated quantity for product $productId")
                }
            } catch (e: Exception) {
                android.util.Log.e("CartViewModel", "Exception updating cart quantity", e)
            }
        }
    }
    
    /**
     * Remove item from cart
     */
    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            try {
                val result = cartRepository.removeFromCart(productId)
                if (result is Resource.Error) {
                    android.util.Log.e("CartViewModel", "Error removing from cart: ${result.message}")
                } else {
                    android.util.Log.d("CartViewModel", "Successfully removed product $productId from cart")
                }
            } catch (e: Exception) {
                android.util.Log.e("CartViewModel", "Exception removing from cart", e)
            }
        }
    }
      /**
     * Clear entire cart
     */
    fun clearCart() {
        viewModelScope.launch {
            try {
                cartRepository.clearCart()
                android.util.Log.d("CartViewModel", "Cart cleared successfully")
            } catch (e: Exception) {
                android.util.Log.e("CartViewModel", "Error clearing cart", e)
            }
        }
    }
    
    /**
     * Process checkout
     */
    fun checkout(shippingAddress: String) {
        _checkoutStatus.value = Resource.Loading()
        
        viewModelScope.launch {
            try {
                val result = cartRepository.checkout(shippingAddress)
                _checkoutStatus.postValue(result)
                if (result is Resource.Error) {
                    android.util.Log.e("CartViewModel", "Checkout error: ${result.message}")
                } else {
                    android.util.Log.d("CartViewModel", "Checkout successful")
                }
            } catch (e: Exception) {
                android.util.Log.e("CartViewModel", "Exception during checkout", e)
                _checkoutStatus.postValue(Resource.Error("Error during checkout: ${e.message}"))
            }
        }
    }
    
    /**
     * Get current cart total as a double value
     */
    fun getCartTotal(): Double {
        // Get the current value from the live data if available,
        // otherwise default to 0.0
        return cartTotal.value?.toDouble() ?: 0.0
    }
    
    /**
     * Increase quantity by 1 for an item in cart
     */
    fun increaseQuantity(productId: Int) {
        viewModelScope.launch {
            try {
                val cartItem = cartRepository.getCartItemByProductId(productId)
                cartItem?.let {
                    val newQuantity = it.quantity + 1
                    val result = cartRepository.updateCartItemQuantity(productId, newQuantity)
                    if (result is Resource.Error) {
                        android.util.Log.e("CartViewModel", "Error increasing quantity: ${result.message}")
                    }
                } ?: run {
                    android.util.Log.e("CartViewModel", "Could not find product $productId in cart")
                }
            } catch (e: Exception) {
                android.util.Log.e("CartViewModel", "Exception increasing quantity", e)
            }
        }
    }
    
    /**
     * Decrease quantity by 1 for an item in cart
     */
    fun decreaseQuantity(productId: Int) {
        viewModelScope.launch {
            try {
                val cartItem = cartRepository.getCartItemByProductId(productId)
                cartItem?.let {
                    if (it.quantity > 1) {
                        val newQuantity = it.quantity - 1
                        val result = cartRepository.updateCartItemQuantity(productId, newQuantity)
                        if (result is Resource.Error) {
                            android.util.Log.e("CartViewModel", "Error decreasing quantity: ${result.message}")
                        }
                    } else {
                        android.util.Log.d("CartViewModel", "Quantity already at minimum (1)")
                    }
                } ?: run {
                    android.util.Log.e("CartViewModel", "Could not find product $productId in cart")
                }
            } catch (e: Exception) {
                android.util.Log.e("CartViewModel", "Exception decreasing quantity", e)
            }
        }
    }
}
