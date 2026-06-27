package com.xiaobaotv.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE `query` NOT IN (SELECT `query` FROM search_history ORDER BY searchedAt DESC LIMIT 10)")
    suspend fun pruneOldSearches()

    @Transaction
    suspend fun insertAndPrune(search: SearchHistoryEntity) {
        insert(search)
        pruneOldSearches()
    }

    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE `query` = :query")
    suspend fun deleteByQuery(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}
