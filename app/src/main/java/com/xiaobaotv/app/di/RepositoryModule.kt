package com.xiaobaotv.app.di

import com.xiaobaotv.app.data.repository.ContentRepositoryImpl
import com.xiaobaotv.app.data.repository.SearchHistoryRepositoryImpl
import com.xiaobaotv.app.data.repository.VideoRepositoryImpl
import com.xiaobaotv.app.data.repository.WatchHistoryRepositoryImpl
import com.xiaobaotv.app.domain.repository.ContentRepository
import com.xiaobaotv.app.domain.repository.SearchHistoryRepository
import com.xiaobaotv.app.domain.repository.VideoRepository
import com.xiaobaotv.app.domain.repository.WatchHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContentRepository(
        contentRepositoryImpl: ContentRepositoryImpl
    ): ContentRepository

    @Binds
    @Singleton
    abstract fun bindVideoRepository(
        videoRepositoryImpl: VideoRepositoryImpl
    ): VideoRepository

    @Binds
    @Singleton
    abstract fun bindWatchHistoryRepository(
        watchHistoryRepositoryImpl: WatchHistoryRepositoryImpl
    ): WatchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(
        searchHistoryRepositoryImpl: SearchHistoryRepositoryImpl
    ): SearchHistoryRepository
}
