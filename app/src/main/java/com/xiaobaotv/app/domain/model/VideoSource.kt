package com.xiaobaotv.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class VideoSourceList(
    val vodId: Int,
    val sources: List<VideoSource>
)

data class VideoSource(
    val name: String,
    val episodes: List<Episode>
)

data class Episode(
    val index: Int,
    val name: String,
    val url: String // This will be the play page URL or extracted video URL
)
