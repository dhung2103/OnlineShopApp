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
            cartRepository.addToCart(product, quantity)
        }
    }
    
    /**
     * Update quantity of item in cart
     */
    fun updateCartItemQuantity(productId: Int, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateCartItemQuantity(productId, quantity)
        }
    }
    
    /**
     * Remove item from cart
     */
    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            cartRepository.removeFromCart(productId)
        }
    }
      /**
     * Clear entire cart
     */
    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }
    
    /**
     * Process checkout
     */
    fun checkout(shippingAddress: String) {
        _checkoutStatus.value = Resource.Loading()
        
        viewModelScope.launch {
            val result = cartRepository.checkout(shippingAddress)
            _checkoutStatus.postValue(result)
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
            val cartItem = cartRepository.getCartItemByProductId(productId)
            cartItem?.let {
                val newQuantity = it.quantity + 1
                cartRepository.updateCartItemQuantity(productId, newQuantity)
            }
        }
    }
    
    /**
     * Decrease quantity by 1 for an item in cart
     */
    fun decreaseQuantity(productId: Int) {
        viewModelScope.launch {
            val cartItem = cartRepository.getCartItemByProductId(productId)
            cartItem?.let {
                if (it.quantity > 1) {
                    val newQuantity = it.quantity - 1
                    cartRepository.updateCartItemQuantity(productId, newQuantity)
                }
            }
        }
    }
}
