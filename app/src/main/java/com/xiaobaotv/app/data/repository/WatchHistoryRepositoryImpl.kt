package com.xiaobaotv.app.data.repository

import com.xiaobaotv.app.data.local.WatchHistoryDao
import com.xiaobaotv.app.data.local.toDomain
import com.xiaobaotv.app.data.local.toEntity
import com.xiaobaotv.app.domain.model.WatchHistoryItem
import com.xiaobaotv.app.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepositoryImpl @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao
) : WatchHistoryRepository {

    override fun getWatchHistory(): Flow<List<WatchHistoryItem>> {
        return watchHistoryDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getWatchHistoryItem(vodId: Int): WatchHistoryItem? {
        return watchHistoryDao.getById(vodId)?.toDomain()
    }

    override suspend fun saveWatchHistory(item: WatchHistoryItem) {
        watchHistoryDao.insertOrUpdate(item.toEntity())
    }

    override suspend fun deleteWatchHistory(vodId: Int) {
        watchHistoryDao.deleteById(vodId)
    }

    override suspend fun clearAll() {
        watchHistoryDao.clearAll()
    }
}
