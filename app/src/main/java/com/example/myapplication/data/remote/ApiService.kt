package com.example.onlineshopapp.data.remote

import com.example.onlineshopapp.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * API Service interface for network calls
 */
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>
    
    @GET("products")
    suspend fun getProducts(): Response<List<Product>>
    
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Response<Product>
    
    @GET("cart")
    suspend fun getCart(): Response<List<CartItem>>
    
    @POST("cart/add")
    suspend fun addToCart(@Body cartItem: CartItem): Response<CartItem>
    
    @DELETE("cart/{productId}")
    suspend fun removeFromCart(@Path("productId") productId: Int): Response<Void>
    
    @POST("checkout")
    suspend fun checkout(@Body checkoutRequest: CheckoutRequest): Response<CheckoutResponse>
    
    @GET("storeLocations")
    suspend fun getStoreLocations(): Response<List<StoreLocation>>
    
    @GET("chat/messages")
    suspend fun getChatMessages(): Response<List<ChatMessage>>
    
    @POST("chat/send")
    suspend fun sendChatMessage(@Body messageRequest: ChatMessageRequest): Response<ChatMessage>
    
//    companion object {
//        fun create(baseUrl: String): ApiService {
//            val logger = HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            }
//
//            val client = OkHttpClient.Builder()
//                .addInterceptor(logger)
//                .addInterceptor(AuthInterceptor())
//                .connectTimeout(15, TimeUnit.SECONDS)
//                .readTimeout(15, TimeUnit.SECONDS)
//                .build()
//
//            return Retrofit.Builder()
//                .baseUrl(baseUrl)
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//                .create(ApiService::class.java)
//        }
//    }
}
