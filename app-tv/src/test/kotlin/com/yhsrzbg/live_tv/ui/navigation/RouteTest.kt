package com.yhsrzbg.live_tv.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteTest {
    @Test
    fun followRoute_hasExpectedPattern() {
        assertEquals("follow", Route.Follow.value)
    }

    @Test
    fun historyRoute_hasExpectedPattern() {
        assertEquals("history", Route.History.value)
    }

    @Test
    fun settingsRoute_hasExpectedPattern() {
        assertEquals("settings", Route.Settings.value)
    }
}
