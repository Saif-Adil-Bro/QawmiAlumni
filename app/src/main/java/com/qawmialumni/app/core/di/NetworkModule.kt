package com.qawmialumni.app.core.di

import android.util.Log
import com.qawmialumni.app.BuildConfig
import com.qawmialumni.app.core.network.FreeImageHostApi
import com.qawmialumni.app.core.network.ImgBBApi
import com.qawmialumni.app.core.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val FREEIMAGE = "freeimage"
    private const val IMGBB = "imgbb"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(Constants.NETWORK_CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(Constants.NETWORK_IO_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(Constants.NETWORK_IO_TIMEOUT_SEC, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        // BuildConfig.DEBUG is a compile-time constant: R8 strips this branch (and the
        // logging-interceptor classes) from release builds entirely.
        if (BuildConfig.DEBUG) {
            val logger = HttpLoggingInterceptor { message ->
                // Never leak API keys into logcat.
                Log.d("OkHttp", message.replace(KEY_REGEX, "key=***"))
            }.apply { level = HttpLoggingInterceptor.Level.BASIC }
            builder.addInterceptor(logger)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    @Named(FREEIMAGE)
    fun provideFreeImageRetrofit(client: OkHttpClient, json: Json): Retrofit =
        buildRetrofit(Constants.FREEIMAGE_BASE_URL, client, json)

    @Provides
    @Singleton
    @Named(IMGBB)
    fun provideImgBBRetrofit(client: OkHttpClient, json: Json): Retrofit =
        buildRetrofit(Constants.IMGBB_BASE_URL, client, json)

    @Provides
    @Singleton
    fun provideFreeImageHostApi(@Named(FREEIMAGE) retrofit: Retrofit): FreeImageHostApi =
        retrofit.create(FreeImageHostApi::class.java)

    @Provides
    @Singleton
    fun provideImgBBApi(@Named(IMGBB) retrofit: Retrofit): ImgBBApi =
        retrofit.create(ImgBBApi::class.java)

    private fun buildRetrofit(baseUrl: String, client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    private val KEY_REGEX = Regex("key=[^&\\s]+")
}
