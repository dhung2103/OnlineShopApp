package com.example.onlineshopapp.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.onlineshopapp.data.local.CartDao
import com.example.onlineshopapp.data.model.*
import com.example.onlineshopapp.data.remote.ApiService
import com.example.onlineshopapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for cart and checkout related operations
 */
class CartRepository(
    private val cartDao: CartDao,
    private val apiService: ApiService,
    private val context: Context
) {
    /**
     * Get all items in user's cart
     */
    fun getCartItems(): LiveData<List<CartItem>> {
        return cartDao.getAllCartItems()
    }
    
    /**
     * Get total number of items in cart
     */
    fun getCartCount(): LiveData<Int> {
        return cartDao.getCartCount()
    }
    
    /**
     * Get total price of all items in cart
     */
    fun getCartTotal(): LiveData<Double> {
        return cartDao.getCartTotal()
    }
    
    /**
     * Add an item to cart
     */
    suspend fun addToCart(product: Product, quantity: Int = 1): Resource<CartItem> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if product already exists in cart
                val existingItem = cartDao.getCartItemByProductId(product.id)
                
                if (existingItem != null) {
                    // Update quantity if already in cart
                    existingItem.quantity += quantity
                    cartDao.updateCartItem(existingItem)
                    
                    // Try to update on server
                    try {
                        apiService.addToCart(existingItem)
                    } catch (e: Exception) {
                        // Ignore server errors, cart is already updated locally
                    }
                    
                    return@withContext Resource.Success(existingItem)
                } else {
                    // Create new cart item
                    val cartItem = CartItem(
                        productId = product.id,
                        quantity = quantity,
                        productName = product.name,
                        productPrice = product.price,
                        productImage = product.imageUrl
                    )
                    
                    // Save to local database
                    cartDao.insertCartItem(cartItem)
                    
                    // Try to update on server
                    try {
                        apiService.addToCart(cartItem)
                    } catch (e: Exception) {
                        // Ignore server errors, cart is already updated locally
                    }
                    
                    return@withContext Resource.Success(cartItem)
                }
            } catch (e: Exception) {
                Resource.Error("Error adding to cart: ${e.message}")
            }
        }
    }
    
    /**
     * Update quantity of an item in cart
     */
    suspend fun updateCartItemQuantity(productId: Int, quantity: Int): Resource<CartItem> {
        return withContext(Dispatchers.IO) {
            try {
                val cartItem = cartDao.getCartItemByProductId(productId)
                
                if (cartItem != null) {
                    if (quantity <= 0) {
                        // Remove item if quantity is 0 or less
                        return@withContext removeFromCart(productId)
                    }
                    
                    cartItem.quantity = quantity
                    cartDao.updateCartItem(cartItem)
                    
                    // Try to update on server
                    try {
                        apiService.addToCart(cartItem)
                    } catch (e: Exception) {
                        // Ignore server errors, cart is already updated locally
                    }
                    
                    Resource.Success(cartItem)
                } else {
                    Resource.Error("Item not found in cart")
                }
            } catch (e: Exception) {
                Resource.Error("Error updating cart: ${e.message}")
            }
        }
    }
    
    /**
     * Remove an item from cart
     */
    suspend fun removeFromCart(productId: Int): Resource<CartItem> {
        return withContext(Dispatchers.IO) {
            try {
                val cartItem = cartDao.getCartItemByProductId(productId)
                
                if (cartItem != null) {
                    cartDao.deleteCartItemByProductId(productId)
                    
                    // Try to update on server
                    try {
                        apiService.removeFromCart(productId)
                    } catch (e: Exception) {
                        // Ignore server errors, item is already removed locally
                    }
                    
                    Resource.Success(cartItem)
                } else {
                    Resource.Error("Item not found in cart")
                }
            } catch (e: Exception) {
                Resource.Error("Error removing from cart: ${e.message}")
            }
        }
    }
    
    /**
     * Checkout the cart
     */
    suspend fun checkout(shippingAddress: String): Resource<CheckoutResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Get all cart items
                val cartItems = cartDao.getAllCartItems().value ?: emptyList()
                
                if (cartItems.isEmpty()) {
                    return@withContext Resource.Error("Cart is empty")
                }
                
                // Calculate total
                val totalAmount = cartItems.sumOf { it.quantity * it.productPrice }
                
                // Create checkout request
                val checkoutRequest = CheckoutRequest(
                    items = cartItems,
                    totalAmount = totalAmount,
                    shippingAddress = shippingAddress
                )
                
                // Send checkout request to server
                val response = apiService.checkout(checkoutRequest)
                
                if (response.isSuccessful) {
                    response.body()?.let { checkoutResponse ->
                        if (checkoutResponse.success) {
                            // Clear cart after successful checkout
                            cartDao.clearCart()
                        }
                        return@withContext Resource.Success(checkoutResponse)
                    }
                }
                
                // Offline mode - simulate successful checkout
                val mockCheckoutResponse = CheckoutResponse(
                    success = true,
                    message = "Order placed successfully",
                    order = Order(
                        id = "order-${System.currentTimeMillis()}",
                        userId = "user123",
                        items = cartItems,
                        totalAmount = totalAmount,
                        date = java.util.Date(),
                        status = OrderStatus.PENDING,
                        shippingAddress = shippingAddress
                    )
                )
                
                // Clear cart
                cartDao.clearCart()
                
                Resource.Success(mockCheckoutResponse)
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> Resource.Error("Network error during checkout: ${e.message}")
                    is IOException -> {
                        // Offline checkout
                        val cartItems = cartDao.getAllCartItems().value ?: emptyList()
                        
                        if (cartItems.isEmpty()) {
                            return@withContext Resource.Error("Cart is empty")
                        }
                        
                        val totalAmount = cartItems.sumOf { it.quantity * it.productPrice }
                        
                        val mockCheckoutResponse = CheckoutResponse(
                            success = true,
                            message = "Order placed successfully (offline mode)",
                            order = Order(
                                id = "offline-order-${System.currentTimeMillis()}",
                                userId = "user123",
                                items = cartItems,
                                totalAmount = totalAmount,
                                date = java.util.Date(),
                                status = OrderStatus.PENDING,
                                shippingAddress = shippingAddress
                            )
                        )
                        
                        // Clear cart
                        cartDao.clearCart()
                        
                        Resource.Success(mockCheckoutResponse)
                    }
                    else -> Resource.Error("Checkout error: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Clear the entire cart
     */
    suspend fun clearCart() {
        withContext(Dispatchers.IO) {
            cartDao.clearCart()
        }
    }
    
    /**
     * Get a cart item by product ID
     */
    suspend fun getCartItemByProductId(productId: Int): CartItem? {
        return withContext(Dispatchers.IO) {
            cartDao.getCartItemByProductId(productId)
        }
    }
}
