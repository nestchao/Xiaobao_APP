package com.xiaobaotv.app.domain.repository

import com.xiaobaotv.app.domain.model.SearchHistoryItem
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun getRecentSearches(): Flow<List<SearchHistoryItem>>
    suspend fun addSearchQuery(query: String)
    suspend fun deleteSearchQuery(query: String)
    suspend fun clearAll()
}
