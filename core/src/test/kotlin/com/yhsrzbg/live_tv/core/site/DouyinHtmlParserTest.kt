package com.yhsrzbg.live_tv.core.site

import com.yhsrzbg.live_tv.core.util.obj
import com.yhsrzbg.live_tv.core.util.str
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DouyinHtmlParserTest {
    @Test
    fun `extract state from escaped render data`() {
        val payload = "{\\\"state\\\":{\\\"appStore\\\":{\\\"needInital\\\":false},\\\"roomStore\\\":{\\\"roomInfo\\\":{\\\"room\\\":{\\\"title\\\":\\\"test room\\\",\\\"status\\\":2},\\\"web_rid\\\":\\\"123456\\\"}}}}"
        val html = "<html><body>$payload</body></html>"

        val state = DouyinHtmlParser.extractState(html)
        assertNotNull(state)
        assertEquals("123456", state!!.obj("roomStore")!!.obj("roomInfo")!!.str("web_rid"))
        assertEquals("test room", state.obj("roomStore")!!.obj("roomInfo")!!.obj("room")!!.str("title"))
    }

    @Test
    fun `extract m3u8 candidates`() {
        val src = "foo https:\\/\\/example.com\\/live\\/a.m3u8 bar https:\\/\\/example.com\\/live\\/a.m3u8"
        val list = DouyinHtmlParser.pickM3u8Candidates(src)
        assertEquals(1, list.size)
        assertEquals("https://example.com/live/a.m3u8", list.first())
    }
}
