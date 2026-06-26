package com.xiaobaotv.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watchlist",
    indices = [Index(value = ["addedAt"], orders = [Index.Order.DESC])]
)
data class WatchlistEntity(
    @PrimaryKey val vodId: Int,
    val name: String,
    val pic: String,
    val remarks: String?,
    val type: String?,
    val addedAt: Long = System.currentTimeMillis()
)
