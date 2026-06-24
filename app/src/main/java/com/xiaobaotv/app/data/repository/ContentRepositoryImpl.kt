package com.xiaobaotv.app.data.repository

import com.xiaobaotv.app.data.model.toDomain
import com.xiaobaotv.app.data.parser.VodDetailParser
import com.xiaobaotv.app.data.remote.XiaobaoApi
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.repository.ContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val api: XiaobaoApi,
    private val client: OkHttpClient
) : ContentRepository {

    private val baseUrl = "https://www.xiaobaotv.tv"

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
            val item = fetchDetailFromPlayPage(id)
            if (item != null) {
                Result.success(item)
            } else {
                Result.failure(Exception("Not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchDetailFromPlayPage(id: Int): VodContent? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/movie/detail/$id.html"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext null
            VodDetailParser.parse(html, id)
        } catch (e: Exception) {
            null
        }
    }
}
