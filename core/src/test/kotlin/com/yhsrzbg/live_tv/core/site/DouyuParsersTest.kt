package com.yhsrzbg.live_tv.core.site

import org.junit.Assert.assertEquals
import org.junit.Test

class DouyuParsersTest {
    @Test
    fun `parse hot num supports wan suffix`() {
        assertEquals(23000, DouyuParsers.parseHotNum("2.3万"))
        assertEquals(800, DouyuParsers.parseHotNum("800"))
        assertEquals(0, DouyuParsers.parseHotNum("-"))
    }
}
