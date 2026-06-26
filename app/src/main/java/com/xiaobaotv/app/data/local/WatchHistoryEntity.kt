package com.xiaobaotv.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
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
