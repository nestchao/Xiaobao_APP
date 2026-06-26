package com.xiaobaotv.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class VodContent(
    val id: Int,
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
    val typeName: String?
)
