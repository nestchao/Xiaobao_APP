package com.xiaobaotv.app.domain.repository

import com.xiaobaotv.app.domain.model.VideoSource

interface VideoRepository {
    suspend fun getVideoSources(vodId: Int): Result<List<VideoSource>>
    suspend fun getPlaybackUrl(playPageUrl: String): Result<String>
}
