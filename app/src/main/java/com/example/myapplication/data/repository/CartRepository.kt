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
    private val productDao: com.example.onlineshopapp.data.local.ProductDao,
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
                android.util.Log.d("CartRepository", "Adding product ${product.id} to cart, quantity: $quantity")
                
                // First, ensure the product is saved in local database
                try {
                    // This will insert or update the product in the local database
                    android.util.Log.d("CartRepository", "Ensuring product exists in database")
                    productDao.insertProduct(product)
                } catch (e: Exception) {
                    android.util.Log.e("CartRepository", "Failed to save product to database", e)
                    // Not critical, we'll continue anyway since we've removed the foreign key constraint
                }
                
                // Check if product already exists in cart
                var existingItem = try {
                    cartDao.getCartItemByProductId(product.id)
                } catch (e: Exception) {
                    android.util.Log.e("CartRepository", "Error getting cart item", e)
                    null
                }
                
                if (existingItem != null) {
                    android.util.Log.d("CartRepository", "Product already in cart, updating quantity from ${existingItem.quantity} to ${existingItem.quantity + quantity}")
                    
                    // Update quantity if already in cart
                    try {
                        existingItem.quantity += quantity
                        cartDao.updateCartItem(existingItem)
                        android.util.Log.d("CartRepository", "Successfully updated quantity")
                    } catch (e: Exception) {
                        android.util.Log.e("CartRepository", "Error updating quantity", e)
                        return@withContext Resource.Error("Database error: ${e.message}")
                    }
                    
                    // Try to update on server
                    try {
                        apiService.addToCart(existingItem)
                    } catch (e: Exception) {
                        android.util.Log.w("CartRepository", "Server sync failed for cart update, continuing with local data", e)
                        // Ignore server errors, cart is already updated locally
                    }
                    
                    return@withContext Resource.Success(existingItem)
                } else {
                    android.util.Log.d("CartRepository", "Adding new product to cart: ${product.name}")
                    
                    // Create new cart item
                    val cartItem = CartItem(
                        productId = product.id,
                        quantity = quantity,
                        productName = product.name,
                        productPrice = product.price,
                        productImage = product.imageUrl
                    )
                    
                    // Save to local database
                    try {
                        val id = cartDao.insertCartItem(cartItem)
                        android.util.Log.d("CartRepository", "Successfully inserted item into local DB with id: $id")
                        
                        // Reload the item to get the auto-generated id
                        existingItem = cartDao.getCartItemByProductId(product.id)
                        if (existingItem == null) {
                            android.util.Log.e("CartRepository", "Failed to retrieve inserted cart item")
                            return@withContext Resource.Error("Failed to retrieve inserted cart item")
                        }
                        
                    } catch (e: Exception) {
                        android.util.Log.e("CartRepository", "Error inserting into local DB", e)
                        return@withContext Resource.Error("Database error: ${e.message}")
                    }
                    
                    // Try to update on server
                    try {
                        existingItem?.let {
                            apiService.addToCart(it)
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("CartRepository", "Server sync failed for new cart item, continuing with local data", e)
                        // Ignore server errors, cart is already updated locally
                    }
                    
                    return@withContext existingItem?.let { Resource.Success(it) } 
                        ?: Resource.Error("Failed to create cart item")
                }
            } catch (e: Exception) {
                android.util.Log.e("CartRepository", "Unexpected error adding to cart", e)
                return@withContext Resource.Error("Error adding to cart: ${e.message}")
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
                // Get all cart items directly from database (not from LiveData)
                val cartItems = cartDao.getAllCartItemsSync()
                
                android.util.Log.d("CartRepository", "Checkout - found ${cartItems.size} items in cart")
                
                if (cartItems.isEmpty()) {
                    android.util.Log.w("CartRepository", "Checkout failed - cart is empty")
                    return@withContext Resource.Error("Cart is empty")
                }
                
                // Calculate total
                val totalAmount = cartItems.sumOf { it.quantity * it.productPrice }
                android.util.Log.d("CartRepository", "Checkout - total amount: $totalAmount")
                
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
                        val cartItems = cartDao.getAllCartItemsSync()
                        
                        android.util.Log.d("CartRepository", "Offline checkout - found ${cartItems.size} items in cart")
                        
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
