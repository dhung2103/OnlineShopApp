package com.example.onlineshopapp.di

import android.content.Context
import androidx.room.Room
import com.example.onlineshopapp.data.local.AppDatabase
import com.example.onlineshopapp.data.remote.ApiService
import com.example.onlineshopapp.data.remote.AuthInterceptor
import com.example.onlineshopapp.data.remote.MockApiHelper
import com.example.onlineshopapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Dagger-Hilt module that provides application-wide dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "online_shop_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(@ApplicationContext context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMockApiHelper(@ApplicationContext context: Context): MockApiHelper {
        return MockApiHelper(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        appDatabase: AppDatabase,
        apiService: ApiService,
        @ApplicationContext context: Context,
        authInterceptor: AuthInterceptor
    ): com.example.onlineshopapp.data.repository.AuthRepository {
        return com.example.onlineshopapp.data.repository.AuthRepository(
            appDatabase.userDao(),
            apiService,
            context,
            authInterceptor
        )
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        appDatabase: AppDatabase,
        apiService: ApiService,
        @ApplicationContext context: Context
    ): com.example.onlineshopapp.data.repository.ProductRepository {
        return com.example.onlineshopapp.data.repository.ProductRepository(
            appDatabase.productDao(),
            apiService,
            context
        )
    }

    @Provides
    @Singleton
    fun provideCartRepository(
        appDatabase: AppDatabase,
        apiService: ApiService,
        @ApplicationContext context: Context
    ): com.example.onlineshopapp.data.repository.CartRepository {
        return com.example.onlineshopapp.data.repository.CartRepository(
            appDatabase.cartDao(),
            apiService,
            context
        )
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        apiService: ApiService,
        @ApplicationContext context: Context
    ): com.example.onlineshopapp.data.repository.ChatRepository {
        return com.example.onlineshopapp.data.repository.ChatRepository(
            apiService,
            context
        )
    }

    @Provides
    @Singleton
    fun provideStoreRepository(
        apiService: ApiService,
        @ApplicationContext context: Context
    ): com.example.onlineshopapp.data.repository.StoreRepository {
        return com.example.onlineshopapp.data.repository.StoreRepository(
            apiService,
            context
        )
    }
}
