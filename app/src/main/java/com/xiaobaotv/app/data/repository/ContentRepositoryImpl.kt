package com.xiaobaotv.app.data.repository

import com.xiaobaotv.app.data.model.toDomain
import com.xiaobaotv.app.data.parser.VodDetailParser
import com.xiaobaotv.app.data.remote.XiaobaoApi
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.repository.ContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val api: XiaobaoApi,
    private val client: OkHttpClient
) : ContentRepository {

    private val baseUrl = "https://www.xiaobaotv.tv"
    private val detailCache = ConcurrentHashMap<Int, VodContent>()

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
        // Check cache first
        detailCache[id]?.let { return Result.success(it) }

        return try {
            val item = fetchWithRetry(id)
            if (item != null) {
                detailCache[id] = item
                Result.success(item)
            } else {
                Result.failure(Exception("Not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch detail page with retry and exponential backoff.
     * Retries once if the server returns a 5xx/429 status (rate limiting).
     */
    private suspend fun fetchWithRetry(id: Int): VodContent? {
        var attempt = 0
        val maxAttempts = 2
        while (attempt < maxAttempts) {
            val result = fetchDetailPage(id)
            if (result != null) return result
            attempt++
            if (attempt < maxAttempts) delay(1000L * attempt) // 1s, then 2s
        }
        return null
    }

    private suspend fun fetchDetailPage(id: Int): VodContent? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/movie/detail/$id.html"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            // Detect rate limiting / server errors
            if (!response.isSuccessful) return@withContext null

            val html = response.body?.string() ?: return@withContext null
            VodDetailParser.parse(html, id)
        } catch (e: Exception) {
            null
        }
    }
}
