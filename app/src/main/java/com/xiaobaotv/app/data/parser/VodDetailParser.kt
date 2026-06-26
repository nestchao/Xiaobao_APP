package com.xiaobaotv.app.data.parser

import com.xiaobaotv.app.domain.model.VodContent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

object VodDetailParser {

    private val TYPE_ID_REGEX = Regex("\\d+")
    private val DESCRIPTION_REGEX = Regex("\"description\"\\s*:\\s*\"([^\"]+)\"")

    fun parse(html: String, vodId: Int): VodContent? {
        return parseFromDocument(Jsoup.parse(html), vodId)
    }

    fun parseFromDocument(doc: Document, vodId: Int): VodContent? {
        return try {
            val name = doc.selectFirst("h1.title")?.text()
                ?: return null

            val pic = extractOgImage(doc)
            val (typeId, typeName) = extractGenre(doc)
            val area = extractLabeledText(doc, "地区：")
            val year = extractLabeledText(doc, "年份：")
            val remarks = extractRemarks(doc)
            val score = extractScore(doc)
            val actor = extractJoinedLinks(doc, "主演：")
            val director = extractJoinedLinks(doc, "导演：")
            val content = extractFullDescription(doc)

            VodContent(
                id = vodId,
                name = name,
                pic = if (pic.startsWith("http")) pic else "https://www.xiaobaotv.tv" + pic,
                actor = actor,
                director = director,
                area = area,
                year = year,
                content = content,
                remarks = remarks,
                score = score,
                typeId = typeId,
                typeName = typeName
            )
        } catch (e: Exception) {
            Timber.e(e, "Error parsing vod detail from detail page")
            null
        }
    }

    private fun extractOgImage(doc: Document): String {
        val el = doc.selectFirst("meta[property=og:image]")
        return el?.attr("content")?.trim() ?: ""
    }

    private fun extractGenre(doc: Document): Pair<Int, String?> {
        val el = doc.selectFirst("p.data a[href^=/movie/show/]")
        val href = el?.attr("href") ?: return Pair(0, null)
        val typeId = TYPE_ID_REGEX.find(href)?.value?.toIntOrNull() ?: 0
        val typeName = el.text().trim()
        return Pair(typeId, typeName)
    }

    private fun extractLabeledText(doc: Document, label: String): String? {
        // Find the span containing the label, then get the next sibling <a>
        val span = doc.selectFirst("span.text-muted:containsOwn($label)")
        return span?.nextElementSibling()?.text()?.trim()
    }

    private fun extractRemarks(doc: Document): String? {
        val span = doc.selectFirst("span.text-muted:containsOwn(更新：)")
        val red = span?.nextElementSibling()
        if (red != null && red.hasClass("text-red")) return red.text().trim()
        return null
    }

    private fun extractScore(doc: Document): String? {
        doc.selectFirst("span.branch")?.let { return it.text().trim() }
        return null
    }

    private fun extractJoinedLinks(doc: Document, label: String): String? {
        val span = doc.selectFirst("span.text-muted:containsOwn($label)")
        val links = span?.nextElementSiblings()?.filter { it.tagName() == "a" }
        if (links.isNullOrEmpty()) return null
        return links.joinToString(",") { it.text().trim() }
    }

    private fun extractFullDescription(doc: Document): String? {
        // Try hidden full description span first
        val hidden = doc.selectFirst("span[style*=\"display: none\"]")
        if (hidden != null) {
            val text = hidden.text().trim()
            if (text.length > 20) return text
        }
        // Fall back to JSON-LD
        val scripts = doc.select("script[type=application/ld+json]")
        for (script in scripts) {
            val json = script.html()
            val regex = DESCRIPTION_REGEX
            val desc = regex.find(json)?.groupValues?.get(1)
            if (desc != null) return desc.replace("\\/", "/")
        }
        return null
    }
}