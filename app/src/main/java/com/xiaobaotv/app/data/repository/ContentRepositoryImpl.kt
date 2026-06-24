package com.xiaobaotv.app.data.repository

import com.xiaobaotv.app.data.model.toDomain
import com.xiaobaotv.app.data.remote.XiaobaoApi
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.repository.ContentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val api: XiaobaoApi
) : ContentRepository {

    override suspend fun getVodList(
        typeId: Int?,
        page: Int,
        limit: Int,
        query: String?,
        categoryId: Int?,
        ids: String?
    ): Result<List<VodContent>> {
        return try {
            val response = api.getVodList(
                mid = 1,
                page = page,
                limit = limit,
                wd = query,
                tid = typeId,
                ids = ids
            )
            Result.success(response.list.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVodDetail(id: Int): Result<VodContent> {
        return try {
            // The API does not honor the ids parameter, so we search by pagination.
            // Fetch first 100 items (page 1), find by ID. If not found, check page 2.
            val response = api.getVodList(mid = 1, page = 1, limit = 100)
            var item = response.list.find { it.vodId == id }

            if (item == null) {
                val response2 = api.getVodList(mid = 1, page = 2, limit = 100)
                item = response2.list.find { it.vodId == id }
            }

            if (item != null) {
                Result.success(item.toDomain())
            } else {
                Result.failure(Exception("Not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
