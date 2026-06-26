package com.xiaobaotv.app.data.di

import android.app.Application
import com.squareup.moshi.Moshi
import com.xiaobaotv.app.BuildConfig
import com.xiaobaotv.app.data.remote.XiaobaoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://www.xiaobaotv.tv/"
    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT = 30L

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(application: Application): OkHttpClient {
        val cache = Cache(application.cacheDir.resolve("http_cache"), 10L * 1024 * 1024) // 10 MB
        val builder = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build()
                val response = chain.proceed(request)
                // Force-cache HTML responses for 5 minutes at the HTTP layer.
                // The upstream scrape pages don't set Cache-Control headers,
                // so without this interceptor the 10MB OkHttp cache is unused.
                if (response.header("Content-Type")?.contains("text/html") == true) {
                    response.newBuilder()
                        .header("Cache-Control", "max-age=300")
                        .build()
                } else {
                    response
                }
            }

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                Timber.d("OkHttp: $message")
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    @Named("httpDispatcher")
    fun provideHttpDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO.limitedParallelism(2)
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideXiaobaoApi(retrofit: Retrofit): XiaobaoApi {
        return retrofit.create(XiaobaoApi::class.java)
    }
}
