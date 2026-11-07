package com.qawmialumni.app.core.network.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageUploadResponseParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        isLenient = true
    }

    @Test
    fun `parses freeimage host success`() {
        val raw = """
            {"status_code":200,"success":{"message":"image uploaded","code":200},
             "image":{"name":"qa_1","extension":"webp","size":41234,"width":800,"height":600,
                      "url":"https://iili.io/abc.webp","display_url":"https://iili.io/abc.md.webp",
                      "image":{"filename":"abc.webp","mime":"image/webp","url":"https://iili.io/abc.webp"}},
             "status_txt":"OK"}
        """.trimIndent()
        val res = json.decodeFromString<FreeImageResponse>(raw)
        assertEquals(200, res.statusCode)
        assertEquals("https://iili.io/abc.webp", res.image?.url)
        assertNull(res.error)
    }

    @Test
    fun `parses freeimage host error`() {
        val raw = """{"status_code":400,"error":{"message":"Invalid API v1 key.","code":100},"status_txt":"Bad Request"}"""
        val res = json.decodeFromString<FreeImageResponse>(raw)
        assertNull(res.image)
        assertEquals("Invalid API v1 key.", res.error?.message)
    }

    @Test
    fun `parses imgbb success`() {
        val raw = """
            {"data":{"id":"2ndCYJK","url":"https://i.ibb.co/w04Prt6/qa.webp",
                     "display_url":"https://i.ibb.co/98W13PY/qa.webp","width":"800","height":600,"size":42000,
                     "image":{"filename":"qa.webp","mime":"image/webp","url":"https://i.ibb.co/w04Prt6/qa.webp"}},
             "success":true,"status":200}
        """.trimIndent()
        val res = json.decodeFromString<ImgBBResponse>(raw)
        assertTrue(res.success)
        assertEquals("https://i.ibb.co/w04Prt6/qa.webp", res.data?.url)
    }

    @Test
    fun `parses imgbb error`() {
        val raw = """{"status_code":400,"error":{"message":"Invalid API v1 key.","code":100},"status_txt":"Bad Request"}"""
        val res = json.decodeFromString<ImgBBResponse>(raw)
        assertFalse(res.success)
        assertEquals("Invalid API v1 key.", res.error?.message)
    }
}
