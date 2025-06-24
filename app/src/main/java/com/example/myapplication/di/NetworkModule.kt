package com.example.onlineshopapp.di

import android.content.Context
import com.example.onlineshopapp.data.remote.AuthInterceptor
import com.example.onlineshopapp.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {    // Cung cấp HttpLoggingInterceptor
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Hoặc BASIC, HEAD
        }
    }
    
    // AuthInterceptor, OkHttpClient và Retrofit được cung cấp từ AppModule để tránh duplicate binding
    
    // ApiService được cung cấp từ AppModule để tránh duplicate binding
}