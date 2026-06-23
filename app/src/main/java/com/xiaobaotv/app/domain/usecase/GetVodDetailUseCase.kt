package com.xiaobaotv.app.domain.usecase

import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.repository.ContentRepository
import javax.inject.Inject

class GetVodDetailUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(id: Int): Result<VodContent> {
        return repository.getVodDetail(id)
    }
}
