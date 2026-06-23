package com.xiaobaotv.app.domain.usecase

import com.xiaobaotv.app.domain.model.VideoSource
import com.xiaobaotv.app.domain.repository.VideoRepository
import javax.inject.Inject

class GetVideoSourcesUseCase @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(vodId: Int): Result<List<VideoSource>> {
        return repository.getVideoSources(vodId)
    }
}
