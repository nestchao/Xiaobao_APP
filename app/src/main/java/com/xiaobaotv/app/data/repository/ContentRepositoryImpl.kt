package com.xiaobaotv.app.data.repository

import com.xiaobaotv.app.data.cache.DetailPageCache
import com.xiaobaotv.app.data.local.VodContentDao
import com.xiaobaotv.app.data.local.toDomain
import com.xiaobaotv.app.data.local.toEntity
import com.xiaobaotv.app.data.model.toDomain
import com.xiaobaotv.app.data.parser.SearchPageParser
import com.xiaobaotv.app.data.parser.ShowPageParser
import com.xiaobaotv.app.data.parser.VodDetailParser
import com.xiaobaotv.app.data.remote.XiaobaoApi
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.repository.ContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val api: XiaobaoApi,
    private val client: OkHttpClient,
    private val detailCache: DetailPageCache,
    private val vodContentDao: VodContentDao
) : ContentRepository {

    private val baseUrl = "https://www.xiaobaotv.tv"
    private val vodCache = ConcurrentHashMap<Int, VodContent>()
    private val detailCacheTtl = 60 * 60 * 1000L // 1 hour

    override suspend fun getVodList(
        typeId: Int?,
        page: Int,
        limit: Int,
        query: String?,
        categoryId: Int?,
        ids: String?
    ): Result<List<VodContent>> {
        return try {
            // For search queries, scrape the HTML search results page
            // (the API endpoint ignores the wd parameter)
            if (!query.isNullOrBlank()) {
                Timber.d("Searching for: $query")
                val searchResults = fetchSearchResultsWithRetry(query, page)
                return Result.success(searchResults)
            }

            val response = api.getVodList(
                mid = 1,
                page = page,
                limit = limit,
                wd = query,
                tid = typeId,
                ids = ids
            )

            // If the API returns empty but we have a typeId, try the HTML show page
            if (response.list.isEmpty() && typeId != null) {
                Timber.d("API returned empty for tid=$typeId, falling back to show page HTML")
                val showPage = fetchShowPage(typeId, page)
                if (showPage.isNotEmpty()) {
                    return Result.success(showPage)
                }
            }

            Result.success(response.list.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVodDetail(id: Int): Result<VodContent> {
        // Check in-memory cache first
        vodCache[id]?.let { return Result.success(it) }

        // Check Room persistent cache
        val cached = vodContentDao.getById(id)
        val now = System.currentTimeMillis()
        if (cached != null && (now - cached.cachedAt) < detailCacheTtl) {
            val cachedVod = cached.toDomain()
            vodCache[id] = cachedVod
            return Result.success(cachedVod)
        }

        return try {
            val item = fetchWithRetry(id)
            if (item != null) {
                vodCache[id] = item
                // Store in Room for persistent caching
                vodContentDao.insert(item.toEntity())
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
            Timber.w("getVodDetail($id) attempt ${attempt + 1} failed")
            attempt++
            if (attempt < maxAttempts) delay(1000L * attempt)
        }
        Timber.e("getVodDetail($id) exhausted after $maxAttempts attempts")
        return null
    }

    private suspend fun fetchDetailPage(id: Int): VodContent? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/movie/detail/$id.html"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            // Detect rate limiting / server errors
            if (!response.isSuccessful) {
                Timber.e("HTTP ${response.code} for detail/$id.html")
                return@withContext null
            }

            val html = response.body?.string() ?: return@withContext null
            detailCache.put(id, html) // share with VideoRepositoryImpl
            VodDetailParser.parse(html, id)
        } catch (e: Exception) {
            Timber.e(e, "Exception fetching detail/$id.html")
            null
        }
    }

    private suspend fun fetchSearchResults(query: String, page: Int): List<VodContent> = withContext(Dispatchers.IO) {
        try {
            val url = SearchPageParser.buildSearchUrl(query, page)
            val request = Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Timber.e("HTTP ${response.code} for search page, query=$query, url=$url")
                return@withContext emptyList()
            }

            val html = response.body?.string() ?: return@withContext emptyList()
            val results = SearchPageParser.parse(html, query)

            // Log HTML snippet when parser returns nothing, to distinguish "no results" from parser failure
            if (results.isEmpty()) {
                Timber.w("Search returned empty for query=$query. HTML snippet: ${html.take(500)}")
            }

            results
        } catch (e: Exception) {
            Timber.e(e, "Exception fetching search page, query=$query")
            emptyList()
        }
    }

    /**
     * Fetch search results with retry and exponential backoff.
     * Retries once if the server returns empty (rate limiting or transient error).
     */
    private suspend fun fetchSearchResultsWithRetry(query: String, page: Int): List<VodContent> {
        var attempt = 0
        val maxAttempts = 2
        while (attempt < maxAttempts) {
            val result = fetchSearchResults(query, page)
            if (result.isNotEmpty()) return result
            Timber.w("Search for '$query' attempt ${attempt + 1} returned empty")
            attempt++
            if (attempt < maxAttempts) delay(1000L * attempt)
        }
        Timber.w("Search for '$query' exhausted after $maxAttempts attempts")
        return emptyList()
    }

    private suspend fun fetchShowPage(typeId: Int, page: Int): List<VodContent> = withContext(Dispatchers.IO) {
        try {
            val url = ShowPageParser.buildShowPageUrl(typeId, page)
            val request = Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Timber.e("HTTP ${response.code} for show/$typeId-$page.html")
                return@withContext emptyList()
            }

            val html = response.body?.string() ?: return@withContext emptyList()
            ShowPageParser.parse(html, typeId)
        } catch (e: Exception) {
            Timber.e(e, "Exception fetching show page typeId=$typeId page=$page")
            emptyList()
        }
    }
}
