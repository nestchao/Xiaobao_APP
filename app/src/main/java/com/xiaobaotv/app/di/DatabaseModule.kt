package com.xiaobaotv.app.di

import android.app.Application
import androidx.room.Room
import com.xiaobaotv.app.data.local.AppDatabase
import com.xiaobaotv.app.data.local.VodContentDao
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
    fun provideAppDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "xiaobao_cache.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideVodContentDao(database: AppDatabase): VodContentDao {
        return database.vodContentDao()
    }
}
