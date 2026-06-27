package com.xiaobaotv.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_history",
    indices = [Index(value = ["searchedAt"], orders = [Index.Order.DESC])]
)
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)
