package com.xiaobaotv.app.data.parser

import com.xiaobaotv.app.domain.model.VodContent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

object VodDetailParser {

    private data class PlayerData(
        val vodName: String = "",
        val vodActor: String = "",
        val vodDirector: String = "",
        val vodClass: String = ""
    )

    fun parse(html: String, vodId: Int): VodContent? {
        return try {
            val doc = Jsoup.parse(html)
            val playerData = extractPlayerData(doc)

            val name = playerData?.vodName
                ?: extractJsonLdName(doc)
                ?: extractTitleName(doc)
                ?: return null

            val pic = extractOgImage(doc)
            val content = extractDescription(doc)
            val (typeId, typeName, area, year) = extractNavInfo(doc)

            VodContent(
                id = vodId,
                name = name,
                pic = if (pic.startsWith("http")) pic else "https://www.xiaobaotv.tv" + pic,
                actor = playerData?.vodActor?.ifBlank { null },
                director = playerData?.vodDirector?.ifBlank { null },
                area = area,
                year = year,
                content = content,
                remarks = null,
                score = null,
                typeId = typeId,
                typeName = typeName
            )
        } catch (e: Exception) {
            Timber.e(e, "Error parsing vod detail from play page")
            null
        }
    }

    // -- player_aaaa ----------------------------------------------

    private fun extractPlayerData(doc: Document): PlayerData? {
        val scripts = doc.select("script[type=text/javascript]")
        for (script in scripts) {
            val content = script.html()
            val marker = "var player_aaaa="
            val start = content.indexOf(marker)
            if (start < 0) continue
            val jsonStart = start + marker.length
            val jsonEnd = findMatchingBrace(content, jsonStart)
            if (jsonEnd < 0) continue
            val raw = content.substring(jsonStart, jsonEnd + 1)
            val decoded = decodeUnicode(raw)
            return PlayerData(
                vodName = extractStr(decoded, "vod_name"),
                vodActor = extractStr(decoded, "vod_actor"),
                vodDirector = extractStr(decoded, "vod_director"),
                vodClass = extractStr(decoded, "vod_class")
            )
        }
        return null
    }

    private fun findMatchingBrace(s: String, startIdx: Int): Int {
        if (startIdx >= s.length || s[startIdx] != '{') return -1
        var depth = 0
        var inStr = false
        var esc = false
        for (i in startIdx until s.length) {
            val c = s[i]
            if (esc) { esc = false; continue }
            if (c == '\\' && inStr) { esc = true; continue }
            if (c == '"' ) { inStr = !inStr; continue }
            if (!inStr) {
                if (c == '{') depth++
                else if (c == '}') {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }

    private fun decodeUnicode(s: String): String {
        val sb = StringBuilder(s.length)
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\' && i + 5 < s.length && s[i + 1] == 'u') {
                val hex = s.substring(i + 2, i + 6)
                sb.append(hex.toInt(16).toChar())
                i += 6
            } else {
                sb.append(s[i])
                i++
            }
        }
        return sb.toString()
    }

    private fun extractStr(json: String, field: String): String {
        val regex = Regex("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"")
        return regex.find(json)?.groupValues?.get(1)?.replace("\\/", "/") ?: ""
    }

    // -- JSON-LD --------------------------------------------------

    private fun extractJsonLdName(doc: Document): String? {
        val scripts = doc.select("script[type=application/ld+json]")
        for (script in scripts) {
            val json = script.html()
            val regex = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"")
            return regex.find(json)?.groupValues?.get(1)?.replace("\\/", "/")
        }
        return null
    }

    private fun extractJsonLdDescription(doc: Document): String? {
        val scripts = doc.select("script[type=application/ld+json]")
        for (script in scripts) {
            val json = script.html()
            val regex = Regex("\"description\"\\s*:\\s*\"([^\"]+)\"")
            val desc = regex.find(json)?.groupValues?.get(1)
            if (desc != null) return desc.replace("\\/", "/")
        }
        return null
    }

    // -- HTML elements --------------------------------------------

    private fun extractTitleName(doc: Document): String? {
        val title = doc.title()
        if (title.isBlank()) return null
        val idx = title.indexOf(" - ")
        return if (idx > 0) title.substring(0, idx) else title.substringBefore(" | ")
    }

    private fun extractOgImage(doc: Document): String {
        val el = doc.selectFirst("meta[property=og:image]")
        return el?.attr("content")?.trim() ?: ""
    }

    private fun extractDescription(doc: Document): String? {
        val ldDesc = extractJsonLdDescription(doc)
        if (!ldDesc.isNullOrBlank() && ldDesc.length > 20) return ldDesc
        val el = doc.selectFirst("meta[property=og:description]")
        val ogDesc = el?.attr("content")?.trim()
        if (!ogDesc.isNullOrBlank()) return ogDesc
        return null
    }

    // -- Navigation: type / area / year ---------------------------

    private fun extractNavInfo(doc: Document): NavInfo {
        val link = doc.selectFirst("a.text-muted[href^=/movie/show/]")
        val parentDiv = link?.parent() ?: return NavInfo()
        val anchors = parentDiv.select("a.text-muted")

        var typeId = 0
        var typeName: String? = null
        var area: String? = null
        var year: String? = null

        for (a in anchors) {
            val href = a.attr("href")
            val text = a.text().trim()
            when {
                href.matches(Regex("/movie/show/\\d+\\.html")) -> {
                    typeId = Regex("\\d+").find(href)?.value?.toIntOrNull() ?: 0
                    typeName = text
                }
                href.contains("/area/") -> area = text
                href.contains("/year/") -> year = text
            }
        }
        return NavInfo(typeId, typeName, area, year)
    }

    private data class NavInfo(
        val typeId: Int = 0,
        val typeName: String? = null,
        val area: String? = null,
        val year: String? = null
    )
}
