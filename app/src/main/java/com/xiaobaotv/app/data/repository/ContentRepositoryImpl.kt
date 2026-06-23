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
            val response = api.getVodList(mid = 1, ids = id.toString())
            val item = response.list.find { it.vodId == id }
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
