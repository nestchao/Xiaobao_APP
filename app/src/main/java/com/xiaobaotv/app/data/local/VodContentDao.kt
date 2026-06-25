package com.xiaobaotv.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface VodContentDao {

    @Query("SELECT * FROM vod_content WHERE id = :id")
    suspend fun getById(id: Int): VodContentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VodContentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VodContentEntity)

    @Query("DELETE FROM vod_content WHERE cachedAt < :expiryThreshold")
    suspend fun deleteStale(expiryThreshold: Long)

    @Query("DELETE FROM vod_content")
    suspend fun clearAll()
}
