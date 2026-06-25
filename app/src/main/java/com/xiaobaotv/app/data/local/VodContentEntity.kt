package com.xiaobaotv.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vod_content")
data class VodContentEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val pic: String,
    val actor: String?,
    val director: String?,
    val area: String?,
    val year: String?,
    val content: String?,
    val remarks: String?,
    val score: String?,
    val typeId: Int,
    val typeName: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
