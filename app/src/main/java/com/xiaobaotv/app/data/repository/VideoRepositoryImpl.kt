package com.xiaobaotv.app.data.repository

import com.xiaobaotv.app.data.parser.PlayPageParser
import com.xiaobaotv.app.domain.model.VideoSource
import com.xiaobaotv.app.domain.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val client: OkHttpClient
) : VideoRepository {

    private val baseUrl = "https://www.xiaobaotv.tv"

    override suspend fun getVideoSources(vodId: Int): Result<List<VideoSource>> = withContext(Dispatchers.IO) {
        try {
            // Fetch the first play page to get source list and episode list
            // We use 1-1 as a default starting point
            val url = "$baseUrl/movie/play/$vodId-1-1.html"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))

            val sources = PlayPageParser.parseVideoSources(html)
            Result.success(sources)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlaybackUrl(playPageUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fullUrl = if (playPageUrl.startsWith("http")) playPageUrl else "$baseUrl$playPageUrl"
            val request = Request.Builder().url(fullUrl).build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))

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
}
