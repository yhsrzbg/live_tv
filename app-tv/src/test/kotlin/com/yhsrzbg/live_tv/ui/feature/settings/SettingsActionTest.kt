package com.yhsrzbg.live_tv.ui.feature.settings

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsActionTest {
    @Test
    fun clickDanmakuToggle_updatesStore() = runTest {
        var captured: Boolean? = null
        val actions = SettingsActionHandler(
            setDanmakuEnabled = { captured = it },
            setQualityLevel = {},
            setScaleMode = {},
            setPlayerCompatMode = {},
        )

        actions.onDanmakuToggleClicked(false)

        assertEquals(false, captured)
    }
}
