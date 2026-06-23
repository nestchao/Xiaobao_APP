package com.xiaobaotv.app.domain.model

data class WatchlistItem(
    val vodId: Int,
    val name: String,
    val pic: String,
    val remarks: String?,
    val type: String?,
    val addedAt: Long = System.currentTimeMillis()
)
