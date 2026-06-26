package com.xiaobaotv.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watch_history",
    indices = [Index(value = ["lastWatchedAt"], orders = [Index.Order.DESC])]
)
data class WatchHistoryEntity(
    @PrimaryKey val vodId: Int,
    val name: String,
    val pic: String,
    val sourceIndex: Int,
    val sourceName: String,
    val episodeIndex: Int,
    val episodeName: String,
    val positionMs: Long,
    val durationMs: Long,
    val lastWatchedAt: Long = System.currentTimeMillis()
)
