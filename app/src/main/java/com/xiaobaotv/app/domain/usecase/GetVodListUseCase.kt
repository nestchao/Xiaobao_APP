package com.xiaobaotv.app.domain.usecase

import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.repository.ContentRepository
import javax.inject.Inject

class GetVodListUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(
        typeId: Int? = null,
        page: Int = 1,
        limit: Int = 20,
        query: String? = null,
        categoryId: Int? = null
    ): Result<List<VodContent>> {
        return repository.getVodList(typeId, page, limit, query, categoryId)
    }
}
