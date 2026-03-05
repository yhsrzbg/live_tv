package com.yhsrzbg.live_tv.ui.screen

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import com.yhsrzbg.live_tv.data.settings.SettingsStore
import com.yhsrzbg.live_tv.input.RemoteIntent
import com.yhsrzbg.live_tv.input.RemoteKeyMapper
import com.yhsrzbg.live_tv.ui.state.RoomUiState
import kotlinx.coroutines.delay

@Composable
fun LiveRoomScreen(
    siteId: String,
    roomId: String,
    state: RoomUiState,
    settingsStore: SettingsStore,
    onToggleControls: () -> Unit,
    onShowSettings: () -> Unit,
    onShowFollow: () -> Unit,
    onPrevChannel: () -> Unit,
    onNextChannel: () -> Unit,
    onBack: () -> Unit,
) {
    val danmakuEnabled by settingsStore.danmakuEnabled.collectAsState(initial = true)

    LaunchedEffect(state.showControls) {
        if (state.showControls) {
            delay(5000)
            onToggleControls()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (RemoteKeyMapper.map(event.nativeKeyEvent.keyCode)) {
                    RemoteIntent.ToggleControls -> {
                        onToggleControls(); true
                    }

                    RemoteIntent.OpenSettings -> {
                        onShowSettings(); true
                    }

                    RemoteIntent.OpenFollowList -> {
                        onShowFollow(); true
                    }

                    RemoteIntent.PrevChannel -> {
                        onPrevChannel(); true
                    }

                    RemoteIntent.NextChannel -> {
                        onNextChannel(); true
                    }

                    RemoteIntent.Back -> {
                        onBack(); true
                    }

                    RemoteIntent.None -> false
                }
            }
    ) {
        Text(
            text = "播放源: ${state.streamUrl.ifBlank { "加载中..." }}",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
        )

        if (state.showControls) {
            ControlsOverlay(
                siteId = siteId,
                roomId = roomId,
                title = state.detail?.title.orEmpty(),
                userName = state.detail?.userName.orEmpty(),
                danmakuEnabled = danmakuEnabled,
            )
        }
    }
}

@Composable
private fun ControlsOverlay(
    siteId: String,
    roomId: String,
    title: String,
    userName: String,
    danmakuEnabled: Boolean,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCC000000))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title.ifBlank { "$siteId/$roomId" }, color = Color.White)
            Text(text = userName, color = Color.White)
        }

        Box(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCC000000))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("↑ 上一频道  ↓ 下一频道", color = Color.White)
            Text("← 关注列表  → 设置", color = Color.White)
            Text("弹幕: ${if (danmakuEnabled) "开" else "关"}", color = Color.White)
        }
    }
}
