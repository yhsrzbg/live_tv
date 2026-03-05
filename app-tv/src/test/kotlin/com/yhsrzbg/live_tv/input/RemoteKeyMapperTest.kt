package com.yhsrzbg.live_tv.input

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteKeyMapperTest {

    @Test
    fun `map enter and center to toggle controls`() {
        assertEquals(RemoteIntent.ToggleControls, RemoteKeyMapper.map(KeyEvent.KEYCODE_DPAD_CENTER))
        assertEquals(RemoteIntent.ToggleControls, RemoteKeyMapper.map(KeyEvent.KEYCODE_ENTER))
    }

    @Test
    fun `map dpad arrows to legacy live room behavior`() {
        assertEquals(RemoteIntent.PrevChannel, RemoteKeyMapper.map(KeyEvent.KEYCODE_DPAD_UP))
        assertEquals(RemoteIntent.NextChannel, RemoteKeyMapper.map(KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals(RemoteIntent.OpenFollowList, RemoteKeyMapper.map(KeyEvent.KEYCODE_DPAD_LEFT))
        assertEquals(RemoteIntent.OpenSettings, RemoteKeyMapper.map(KeyEvent.KEYCODE_DPAD_RIGHT))
    }
}
