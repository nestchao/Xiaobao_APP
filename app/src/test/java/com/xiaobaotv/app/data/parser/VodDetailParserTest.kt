package com.xiaobaotv.app.data.parser

import com.xiaobaotv.app.domain.model.VodContent
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class VodDetailParserTest {

    private fun loadPage(name: String): String {
        val path = "src/test/resources/pages/$name"
        // Search from module dir first, then project root
        val file = File(path)
            .takeIf { it.exists() }
            ?: File("app/$path")
        return file.readText(Charsets.UTF_8)
    }

    @Test
    fun parse_animeDetailPage_returnsAllFields() {
        val html = loadPage("detail_anime_35265187.html")
        val result: VodContent? = VodDetailParser.parse(html, 35265187)

        assertNotNull("Parser should return VodContent", result)
        result?.let {
            assertEquals(35265187, it.id)
            assertEquals("和班上第二可爱的女孩子成为了朋友", it.name)
            assertEquals("5.6", it.score)
            assertEquals("日本", it.area)
            assertEquals("2026", it.year)
            assertEquals(403, it.typeId)
            assertEquals("日韩动漫", it.typeName)
            assertEquals(
                "石谷春贵,石见舞菜香,铃代纱弓,长谷川育美",
                it.actor
            )
            assertEquals("橘秀树", it.director)
            assertTrue("Description should be present", it.content?.length ?: 0 > 50)
            assertNotNull("Pic should be present", it.pic)
            assertTrue("Pic should be a valid URL", it.pic.startsWith("https://"))
        }
    }

    @Test
    fun parse_animeDetailPage_hasRemarks() {
        val html = loadPage("detail_anime_35265187.html")
        val result = VodDetailParser.parse(html, 35265187)
        assertNotNull("Remarks should be present", result?.remarks)
        assertTrue("Remarks should contain episode info", result?.remarks?.contains("更新至") == true)
    }
}
