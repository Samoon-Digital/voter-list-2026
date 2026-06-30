package com.samoondigital.yojnaplus.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.samoondigital.yojnaplus.api.EciGatewayApi
import com.samoondigital.yojnaplus.api.EciPublishedFileApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val GATEWAY_BASE_URL = "https://gateway-voters.eci.gov.in/"
    private const val PUBLISHED_FILE_BASE_URL = "https://gateway-vpd.eci.gov.in/"

    @Provides
    @Singleton
    @OptIn(ExperimentalSerializationApi::class)
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(headersInterceptor: EciHeadersInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectionPool(ConnectionPool(maxIdleConnections = 6, keepAliveDuration = 5, TimeUnit.MINUTES))
            .addInterceptor(headersInterceptor)
            .addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @GatewayRetrofit
    fun provideGatewayRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(GATEWAY_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    @PublishedFileRetrofit
    fun providePublishedFileRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(PUBLISHED_FILE_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideEciGatewayApi(@GatewayRetrofit retrofit: Retrofit): EciGatewayApi =
        retrofit.create(EciGatewayApi::class.java)

    @Provides
    @Singleton
    fun provideEciPublishedFileApi(@PublishedFileRetrofit retrofit: Retrofit): EciPublishedFileApi =
        retrofit.create(EciPublishedFileApi::class.java)
}
