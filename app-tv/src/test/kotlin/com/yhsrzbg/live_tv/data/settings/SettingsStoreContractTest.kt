package com.yhsrzbg.live_tv.data.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsStoreContractTest {
    @Test
    fun defaultScaleMode_isContain() = runTest {
        val store = newStore("default-scale")

        assertEquals(0, store.scaleMode.first())
    }

    @Test
    fun defaultPlayerCompatMode_isFalse() = runTest {
        val store = newStore("default-compat")

        assertFalse(store.playerCompatMode.first())
    }

    @Test
    fun setScaleMode_updatesValue() = runTest {
        val store = newStore("set-scale")

        store.setScaleMode(2)

        assertEquals(2, store.scaleMode.first())
    }

    @Test
    fun setPlayerCompatMode_updatesValue() = runTest {
        val store = newStore("set-compat")

        store.setPlayerCompatMode(true)

        assertTrue(store.playerCompatMode.first())
    }

    private fun newStore(suffix: String): SettingsStore {
        val appContext = getApplicationContext<Context>()
        return SettingsStore(
            context = appContext,
            storeName = "live_tv_settings_test_${suffix}_${System.nanoTime()}",
        )
    }
}
