package com.xiaobaotv.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE vodId = :vodId")
    suspend fun delete(vodId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE vodId = :vodId)")
    suspend fun exists(vodId: Int): Boolean
}
