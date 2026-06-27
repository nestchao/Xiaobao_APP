package com.xiaobaotv.app.data.repository

import com.xiaobaotv.app.data.local.SearchHistoryDao
import com.xiaobaotv.app.data.local.SearchHistoryEntity
import com.xiaobaotv.app.domain.model.SearchHistoryItem
import com.xiaobaotv.app.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {

    override fun getRecentSearches(): Flow<List<SearchHistoryItem>> {
        return searchHistoryDao.getRecentSearches().map { list ->
            list.map { SearchHistoryItem(query = it.query, searchedAt = it.searchedAt) }
        }
    }

    override suspend fun addSearchQuery(query: String) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertAndPrune(
                SearchHistoryEntity(
                    query = query.trim(),
                    searchedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun deleteSearchQuery(query: String) {
        searchHistoryDao.deleteByQuery(query)
    }

    override suspend fun clearAll() {
        searchHistoryDao.clearAll()
    }
}
