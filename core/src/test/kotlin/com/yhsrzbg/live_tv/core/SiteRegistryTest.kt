package com.yhsrzbg.live_tv.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SiteRegistryTest {

    @Test
    fun `registry should expose four default sites`() {
        val registry = SiteRegistry.default()
        val ids = registry.supportedSites().map { it.id }.toSet()

        assertEquals(setOf("bilibili", "douyu", "huya", "douyin"), ids)
    }

    @Test
    fun `lookup should find known site`() {
        val registry = SiteRegistry.default()

        val site = registry.site("bilibili")

        assertNotNull(site)
    }
}
