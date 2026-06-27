package com.xiaobaotv.app.di

import android.app.Application
import androidx.room.Room
import com.xiaobaotv.app.data.local.AppDatabase
import com.xiaobaotv.app.data.local.SearchHistoryDao
import com.xiaobaotv.app.data.local.UserPreferences
import com.xiaobaotv.app.data.local.VodContentDao
import com.xiaobaotv.app.data.local.WatchHistoryDao
import com.xiaobaotv.app.data.local.WatchlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideUserPreferences(application: Application): UserPreferences {
        return UserPreferences(application)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "xiaobao_cache.db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideVodContentDao(database: AppDatabase): VodContentDao {
        return database.vodContentDao()
    }

    @Provides
    @Singleton
    fun provideWatchlistDao(database: AppDatabase): WatchlistDao {
        return database.watchlistDao()
    }

    @Provides
    @Singleton
    fun provideWatchHistoryDao(database: AppDatabase): WatchHistoryDao {
        return database.watchHistoryDao()
    }

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }
}
