package com.xiaobaotv.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val vodId: Int,
    val name: String,
    val pic: String,
    val remarks: String?,
    val type: String?,
    val addedAt: Long = System.currentTimeMillis()
)
