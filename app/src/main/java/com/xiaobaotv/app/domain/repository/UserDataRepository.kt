package com.xiaobaotv.app.domain.repository

import com.xiaobaotv.app.domain.model.WatchlistItem
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    fun getWatchlist(userId: String): Flow<List<WatchlistItem>>
    suspend fun addToWatchlist(userId: String, item: WatchlistItem)
    suspend fun removeFromWatchlist(userId: String, vodId: Int)
    suspend fun isInWatchlist(userId: String, vodId: Int): Boolean
}
