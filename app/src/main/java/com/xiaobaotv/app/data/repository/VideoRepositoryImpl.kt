package com.xiaobaotv.app.data.repository

import com.xiaobaotv.app.data.cache.DetailPageCache
import com.xiaobaotv.app.data.parser.PlayPageParser
import com.xiaobaotv.app.domain.model.VideoSource
import com.xiaobaotv.app.domain.repository.VideoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.LinkedHashMap
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val detailCache: DetailPageCache,
    @Named("httpDispatcher") private val httpDispatcher: CoroutineDispatcher
) : VideoRepository {

    private val baseUrl = "https://www.xiaobaotv.tv"
    private val sourcesCache = object : LinkedHashMap<Int, List<VideoSource>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, List<VideoSource>>?): Boolean {
            return size > MAX_SOURCES_CACHE
        }
    }

    @Synchronized
    private fun getSourcesFromCache(vodId: Int): List<VideoSource>? = sourcesCache[vodId]

    @Synchronized
    private fun putSourcesInCache(vodId: Int, sources: List<VideoSource>) { sourcesCache[vodId] = sources }

    override suspend fun getVideoSources(vodId: Int): Result<List<VideoSource>> {
        // Check local cache first
        getSourcesFromCache(vodId)?.let { return Result.success(it) }
        return try {
            // Use shared detail cache to avoid duplicate requests.
            // getOrFetch handles concurrent access so parallel coroutines share one fetch.
            val html = detailCache.getOrFetch(vodId) {
                fetchWithRetry("$baseUrl/movie/detail/$vodId.html")
                    ?: throw Exception("Failed to fetch detail page")
            }
            val sources = withContext(httpDispatcher) {
                PlayPageParser.parseVideoSources(html)
            }
            if (sources.isEmpty()) {
                Timber.e("No episodes found in detail page for vodId=$vodId")
                return Result.failure(Exception("No episodes found for this show"))
            }
            putSourcesInCache(vodId, sources)
            Result.success(sources)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlaybackUrl(playPageUrl: String): Result<String> = withContext(httpDispatcher) {
        try {
            val fullUrl = if (playPageUrl.startsWith("http")) playPageUrl else "$baseUrl$playPageUrl"
            val html = fetchWithRetry(fullUrl)
                ?: return@withContext Result.failure(Exception("Failed to fetch play page"))
            val playbackUrl = PlayPageParser.extractPlaybackUrl(html)
            if (playbackUrl != null) {
                Result.success(playbackUrl)
            } else {
                Result.failure(Exception("Could not extract playback URL"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchWithRetry(url: String): String? {
        var attempt = 0
        val maxAttempts = 2
        while (attempt < maxAttempts) {
            try {
                val result = withContext(httpDispatcher) {
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            Timber.w("HTTP ${response.code} fetching $url (attempt ${attempt + 1})")
                            null
                        } else {
                            response.body?.string()
                        }
                    }
                }
                if (result != null) return result
                attempt++
                if (attempt < maxAttempts) delay(1000L * attempt)
            } catch (e: Exception) {
                Timber.w(e, "Network error fetching $url (attempt ${attempt + 1})")
                attempt++
                if (attempt < maxAttempts) delay(1000L * attempt)
            }
        }
        return null
    }

    companion object {
        private const val MAX_SOURCES_CACHE = 10
    }
}
