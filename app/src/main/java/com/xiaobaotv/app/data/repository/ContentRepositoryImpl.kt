package com.xiaobaotv.app.data.repository

import com.xiaobaotv.app.data.model.toDomain
import com.xiaobaotv.app.data.parser.VodDetailParser
import com.xiaobaotv.app.data.remote.XiaobaoApi
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.repository.ContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
            // Try play page first (fast, always works for any content)
            val fromPlayPage = fetchDetailFromPlayPage(id)
            if (fromPlayPage != null) return Result.success(fromPlayPage)

            // Fallback: search listing pages 1-10
            val item = coroutineScope {
                val deferreds = (1..10).map { page ->
                    async { api.getVodList(mid = 1, page = page, limit = 200).list.find { it.vodId == id } }
                }
                var result: com.xiaobaotv.app.data.model.VodItem? = null
                for (d in deferreds) {
                    result = d.await()
                    if (result != null) break
                }
                result
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

    private suspend fun fetchDetailFromPlayPage(id: Int): VodContent? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/movie/play/$id-1-1.html"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext null
            VodDetailParser.parse(html, id)
        } catch (e: Exception) {
            null
        }
    }
}
