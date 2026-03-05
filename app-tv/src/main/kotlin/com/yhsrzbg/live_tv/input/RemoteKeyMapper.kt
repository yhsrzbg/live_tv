package com.yhsrzbg.live_tv.input

import android.view.KeyEvent

sealed interface RemoteIntent {
    data object ToggleControls : RemoteIntent
    data object OpenSettings : RemoteIntent
    data object OpenFollowList : RemoteIntent
    data object PrevChannel : RemoteIntent
    data object NextChannel : RemoteIntent
    data object Back : RemoteIntent
    data object None : RemoteIntent
}

object RemoteKeyMapper {
    fun map(keyCode: Int): RemoteIntent {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_SPACE,
            KeyEvent.KEYCODE_NUMPAD_ENTER -> RemoteIntent.ToggleControls

            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_M -> RemoteIntent.OpenSettings

            KeyEvent.KEYCODE_DPAD_LEFT -> RemoteIntent.OpenFollowList
            KeyEvent.KEYCODE_DPAD_UP -> RemoteIntent.PrevChannel
            KeyEvent.KEYCODE_DPAD_DOWN -> RemoteIntent.NextChannel
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_ESCAPE -> RemoteIntent.Back
            else -> RemoteIntent.None
        }
    }
}
