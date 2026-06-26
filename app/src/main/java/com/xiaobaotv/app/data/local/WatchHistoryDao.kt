package com.xiaobaotv.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: WatchHistoryEntity)

    @Query("SELECT * FROM watch_history WHERE vodId = :vodId")
    suspend fun getById(vodId: Int): WatchHistoryEntity?

    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAll(): Flow<List<WatchHistoryEntity>>

    @Query("DELETE FROM watch_history WHERE vodId = :vodId")
    suspend fun deleteById(vodId: Int)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}
