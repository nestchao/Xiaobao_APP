package com.xiaobaotv.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class WatchlistItem(
    val vodId: Int,
    val name: String,
    val pic: String,
    val remarks: String?,
    val type: String?,
    val addedAt: Long = System.currentTimeMillis()
)
