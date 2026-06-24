package com.xiaobaotv.app.data.parser

import com.xiaobaotv.app.domain.model.VodContent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import timber.log.Timber
import java.net.URLEncoder

object SearchPageParser {

    private const val BASE_URL = "https://www.xiaobaotv.tv"

    fun parse(html: String, query: String): List<VodContent> {
        return try {
            val doc = Jsoup.parse(html)
            doc.select("ul#searchList li.clearfix").mapNotNull { item ->
                val link = item.selectFirst("a.myui-vodlist__thumb") ?: return@mapNotNull null
                val href = link.attr("href")
                val id = Regex("""/movie/detail/(\d+)\.html""").find(href)
                    ?.groupValues?.get(1)?.toIntOrNull() ?: return@mapNotNull null
                val name = link.attr("title").trim()
                val pic = link.attr("data-original").trim()
                val remarks = item.selectFirst("span.pic-text.text-right")?.text()?.trim()
                val year = link.selectFirst("span.pic-tag-top span.tag")?.text()?.trim()

                val detailDiv = item.selectFirst("div.detail")
                val director = detailDiv?.let { extractLabeledText(it, "导演：") }
                val actor = detailDiv?.let { extractLabeledText(it, "主演：") }
                val typeName = detailDiv?.let { extractLabeledText(it, "分类：") }
                val area = detailDiv?.let { extractLabeledText(it, "地区：") }
                val yearFromDetail = detailDiv?.let { extractLabeledText(it, "年份：") }
                val content = detailDiv?.let { div ->
                    val p = div.selectFirst("p.hidden-xs")
                    p?.ownText()?.trim()?.removePrefix("简介：")
                }

                VodContent(
                    id = id,
                    name = name,
                    pic = if (pic.startsWith("http")) pic else BASE_URL + pic,
                    actor = actor,
                    director = director,
                    area = area,
                    year = year ?: yearFromDetail,
                    content = content,
                    remarks = remarks,
                    score = null,
                    typeId = 0,
                    typeName = typeName
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing search page for query=$query")
            emptyList()
        }
    }

    fun buildSearchUrl(query: String, page: Int = 1): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return if (page <= 1) {
            "$BASE_URL/search/wd/$encoded.html"
        } else {
            "$BASE_URL/search/page/$page/wd/$encoded.html"
        }
    }

    private fun extractLabeledText(element: Element, label: String): String? {
        val span = element.selectFirst("span.text-muted:containsOwn($label)")
        val next = span?.nextSibling()
        return if (next is TextNode) next.text().trim().takeIf { it.isNotEmpty() } else null
    }
}
