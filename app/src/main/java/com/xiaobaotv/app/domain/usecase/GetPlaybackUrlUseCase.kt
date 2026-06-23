package com.xiaobaotv.app.domain.usecase

import com.xiaobaotv.app.domain.repository.VideoRepository
import javax.inject.Inject

class GetPlaybackUrlUseCase @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(playPageUrl: String): Result<String> {
        return repository.getPlaybackUrl(playPageUrl)
    }
}
