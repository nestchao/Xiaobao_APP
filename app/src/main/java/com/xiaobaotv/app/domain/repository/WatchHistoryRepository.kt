package com.xiaobaotv.app.domain.repository

import com.xiaobaotv.app.domain.model.WatchHistoryItem
import kotlinx.coroutines.flow.Flow

interface WatchHistoryRepository {
    fun getWatchHistory(): Flow<List<WatchHistoryItem>>
    suspend fun getWatchHistoryItem(vodId: Int): WatchHistoryItem?
    suspend fun saveWatchHistory(item: WatchHistoryItem)
    suspend fun deleteWatchHistory(vodId: Int)
    suspend fun clearAll()
}
