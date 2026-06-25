package com.xiaobaotv.app.di

import android.app.Application
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.ExoDatabaseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@UnstableApi
@Module
@InstallIn(SingletonComponent::class)
object VideoCacheModule {

    @Provides
    @Singleton
    fun provideVideoCache(application: Application): Cache {
        val evictor = LeastRecentlyUsedCacheEvictor(200L * 1024 * 1024) // 200 MB
        val databaseProvider = ExoDatabaseProvider(application)
        return SimpleCache(
            application.cacheDir.resolve("exo_cache"),
            evictor,
            databaseProvider
        )
    }
}
