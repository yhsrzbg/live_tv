package com.yhsrzbg.live_tv.core.site

import org.junit.Assert.assertEquals
import org.junit.Test

class BilibiliWbiSignerTest {
    @Test
    fun `mixin key follows bilibili mixin table`() {
        val origin = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+/"
        val key = BilibiliWbiSigner.mixinKey(origin)
        assertEquals("UVsc1ixGpYkF6dTJBRfXHjQtDCoNmMPn", key)
    }

    @Test
    fun `sign query filters reserved chars and appends w_rid`() {
        val result = BilibiliWbiSigner.signQuery(
            params = linkedMapOf(
                "foo" to "a!b(c)*",
                "bar" to "x y",
            ),
            mixinKey = "UVsc1ixGpYkF6dTJBRfXHjQtDCoNmMPn",
            unixSeconds = 1_700_000_000,
        )

        assertEquals("1700000000", result["wts"])
        assertEquals("64b7279c4a928d24563555010bfb6539", result["w_rid"])
    }
}
