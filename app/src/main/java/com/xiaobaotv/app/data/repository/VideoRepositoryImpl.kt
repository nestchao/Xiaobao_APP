package com.xiaobaotv.app.data.repository

import com.xiaobaotv.app.data.cache.DetailPageCache
import com.xiaobaotv.app.data.parser.PlayPageParser
import com.xiaobaotv.app.domain.model.VideoSource
import com.xiaobaotv.app.domain.repository.VideoRepository
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
class VideoRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val detailCache: DetailPageCache
) : VideoRepository {

    private val baseUrl = "https://www.xiaobaotv.tv"
    private val sourcesCache = ConcurrentHashMap<Int, List<VideoSource>>()

    override suspend fun getVideoSources(vodId: Int): Result<List<VideoSource>> = withContext(Dispatchers.IO) {
        // Check local cache first
        sourcesCache[vodId]?.let { return@withContext Result.success(it) }
        try {
            // Use shared detail cache to avoid duplicate requests
            val html = detailCache.get(vodId)
                ?: fetchWithRetry("$baseUrl/movie/detail/$vodId.html")
                ?: return@withContext Result.failure(Exception("Failed to fetch detail page"))
            val sources = PlayPageParser.parseVideoSources(html)
            if (sources.isEmpty()) {
                Timber.e("No episodes found in detail page for vodId=$vodId")
                return@withContext Result.failure(Exception("No episodes found for this show"))
            }
            sourcesCache[vodId] = sources
            Result.success(sources)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlaybackUrl(playPageUrl: String): Result<String> = withContext(Dispatchers.IO) {
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
                val request = Request.Builder().url(url).build()
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (!response.isSuccessful) {
                    Timber.w("HTTP ${response.code} fetching $url (attempt ${attempt + 1})")
                    attempt++
                    if (attempt < maxAttempts) delay(1000L * attempt)
                    continue
                }
                return response.body?.string()
            } catch (e: Exception) {
                Timber.w(e, "Network error fetching $url (attempt ${attempt + 1})")
                attempt++
                if (attempt < maxAttempts) delay(1000L * attempt)
            }
        }
        return null
    }
}
