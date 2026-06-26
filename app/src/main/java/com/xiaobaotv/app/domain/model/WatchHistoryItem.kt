package com.xiaobaotv.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class WatchHistoryItem(
    val vodId: Int,
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
