package com.xiaobaotv.app.domain.repository

import com.xiaobaotv.app.domain.model.VodContent

interface ContentRepository {
    suspend fun getVodList(
        typeId: Int? = null,
        page: Int = 1,
        limit: Int = 20,
        query: String? = null,
        categoryId: Int? = null,
        ids: String? = null
    ): Result<List<VodContent>>

    suspend fun getVodDetail(id: Int): Result<VodContent>
}
