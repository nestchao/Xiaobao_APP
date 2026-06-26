package com.xiaobaotv.app.data.repository

import com.xiaobaotv.app.data.local.WatchlistDao
import com.xiaobaotv.app.data.local.WatchlistEntity
import com.xiaobaotv.app.domain.model.WatchlistItem
import com.xiaobaotv.app.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao
) : UserDataRepository {

    override fun getWatchlist(userId: String): Flow<List<WatchlistItem>> {
        return watchlistDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addToWatchlist(userId: String, item: WatchlistItem) {
        watchlistDao.insert(item.toEntity())
    }

    override suspend fun removeFromWatchlist(userId: String, vodId: Int) {
        watchlistDao.delete(vodId)
    }

    override suspend fun isInWatchlist(userId: String, vodId: Int): Boolean {
        return watchlistDao.exists(vodId)
    }
}

private fun WatchlistEntity.toDomain(): WatchlistItem = WatchlistItem(
    vodId = vodId,
    name = name,
    pic = pic,
    remarks = remarks,
    type = type,
    addedAt = addedAt
)

private fun WatchlistItem.toEntity(): WatchlistEntity = WatchlistEntity(
    vodId = vodId,
    name = name,
    pic = pic,
    remarks = remarks,
    type = type,
    addedAt = addedAt
)
