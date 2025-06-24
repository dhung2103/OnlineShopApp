package com.example.onlineshopapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.onlineshopapp.data.model.CartItem

/**
 * Data Access Object for cart items
 */
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): LiveData<List<CartItem>>
    
    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getCartItemByProductId(productId: Int): CartItem?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)
    
    @Update
    suspend fun updateCartItem(cartItem: CartItem)
    
    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)
    
    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItemByProductId(productId: Int)
    
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
    
    @Query("SELECT COUNT(*) FROM cart_items")
    fun getCartCount(): LiveData<Int>
    
    @Query("SELECT SUM(quantity * productPrice) FROM cart_items")
    fun getCartTotal(): LiveData<Double>
}
