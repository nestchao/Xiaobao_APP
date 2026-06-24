package com.xiaobaotv.app.data.parser

import com.xiaobaotv.app.domain.model.VodContent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

object ShowPageParser {

    private const val BASE_URL = "https://www.xiaobaotv.tv"

    fun parse(html: String, typeId: Int): List<VodContent> {
        return try {
            val doc = Jsoup.parse(html)
            doc.select("div.myui-vodlist__box").mapNotNull { item ->
                val link = item.selectFirst("a.myui-vodlist__thumb") ?: return@mapNotNull null
                val href = link.attr("href")
                val id = Regex("""/movie/detail/(\d+)\.html""").find(href)
                    ?.groupValues?.get(1)?.toIntOrNull() ?: return@mapNotNull null
                val name = link.attr("title").trim()
                val pic = link.attr("data-original").trim()
                val remarks = item.selectFirst("span.pic-text.text-right")?.text()?.trim()
                val typeName = item.selectFirst("div.myui-vodlist__detail p.text-muted")?.text()?.trim()

                VodContent(
                    id = id,
                    name = name,
                    pic = if (pic.startsWith("http")) pic else BASE_URL + pic,
                    actor = null,
                    director = null,
                    area = null,
                    year = null,
                    content = null,
                    remarks = remarks,
                    score = null,
                    typeId = typeId,
                    typeName = typeName
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing show page for typeId=$typeId")
            emptyList()
        }
    }

    fun buildShowPageUrl(typeId: Int, page: Int): String {
        return if (page <= 1) {
            "$BASE_URL/movie/show/$typeId.html"
        } else {
            "$BASE_URL/movie/show/$typeId-$page.html"
        }
    }
}
