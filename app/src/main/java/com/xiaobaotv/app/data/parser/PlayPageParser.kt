package com.xiaobaotv.app.data.parser

import com.xiaobaotv.app.domain.model.Episode
import com.xiaobaotv.app.domain.model.VideoSource
import org.jsoup.Jsoup
import timber.log.Timber

object PlayPageParser {

    fun parseVideoSources(html: String): List<VideoSource> {
        val sources = mutableListOf<VideoSource>()
        try {
            val doc = Jsoup.parse(html)

            // 1. Find source tabs
            val tabElements = doc.select(".myui-panel_hd li a")
            val tabNames = tabElements.map { it.text() }

            // 2. Find episode lists for each source
            val episodeLists = doc.select(".myui-content__list")

            episodeLists.forEachIndexed { index, ul ->
                val name = if (index < tabNames.size) tabNames[index] else "线路 ${index + 1}"
                val episodes = ul.select("li a").mapIndexed { epIndex, a ->
                    Episode(
                        index = epIndex + 1,
                        name = a.text(),
                        url = a.attr("href")
                    )
                }
                sources.add(VideoSource(name, episodes))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing play page")
        }
        return sources
    }

    /**
     * Extract the actual .m3u8 or .mp4 URL from a play page HTML.
     * This usually requires parsing the "player_xxxx" Javascript variable.
     */
    fun extractPlaybackUrl(html: String): String? {
        try {
            // Pattern to match "url":"http...m3u8" or similar in script tags
            // CMS has multiple "url" keys; the LAST one in the HTML is the video URL
            // (first is the CMS config: url:www.xiaobaotv.tv)
            val regex = "\"url\":\"([^\"]+)\"".toRegex()
            val matches = regex.findAll(html).toList()
            val rawUrl = if (matches.isNotEmpty()) matches.last().groupValues[1] else null

            if (rawUrl != null) {
                // Decode unicode escapes if any (e.g. \/ -> /)
                return rawUrl.replace("\\/", "/")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting playback URL")
        }
        return null
    }
}
