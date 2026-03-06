package com.yhsrzbg.live_tv.ui.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yhsrzbg.live_tv.ui.component.TvActionButton
import com.yhsrzbg.live_tv.ui.component.TvTopBar

enum class SettingsTab { Player, Danmaku, Follow, Account, About }

class SettingsActionHandler(
    private val setDanmakuEnabled: suspend (Boolean) -> Unit,
    private val setQualityLevel: suspend (Int) -> Unit,
    private val setScaleMode: suspend (Int) -> Unit,
    private val setPlayerCompatMode: suspend (Boolean) -> Unit,
) {
    suspend fun onDanmakuToggleClicked(enabled: Boolean) = setDanmakuEnabled(enabled)
    suspend fun onQualityLevelSelected(level: Int) = setQualityLevel(level)
    suspend fun onScaleModeSelected(mode: Int) = setScaleMode(mode)
    suspend fun onPlayerCompatModeToggled(enabled: Boolean) = setPlayerCompatMode(enabled)
}

@Composable
fun SettingsScreen(
    danmakuEnabled: Boolean,
    qualityLevel: Int,
    scaleMode: Int,
    playerCompatMode: Boolean,
    onToggleDanmaku: (Boolean) -> Unit,
    onSetQualityLevel: (Int) -> Unit,
    onSetScaleMode: (Int) -> Unit,
    onSetPlayerCompatMode: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    var activeTab by remember { mutableStateOf(SettingsTab.Player) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TvTopBar(title = "Settings") {
            TvActionButton(text = "Back", onClick = onBack)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsTab.values().forEach { tab ->
                TvActionButton(text = tab.name, onClick = { activeTab = tab })
            }
        }

        when (activeTab) {
            SettingsTab.Player -> PlayerSettings(
                qualityLevel = qualityLevel,
                scaleMode = scaleMode,
                playerCompatMode = playerCompatMode,
                onSetQualityLevel = onSetQualityLevel,
                onSetScaleMode = onSetScaleMode,
                onSetPlayerCompatMode = onSetPlayerCompatMode,
            )

            SettingsTab.Danmaku -> DanmakuSettings(
                danmakuEnabled = danmakuEnabled,
                onToggleDanmaku = onToggleDanmaku,
            )

            SettingsTab.Follow -> Text("Follow settings placeholder", color = Color.White)
            SettingsTab.Account -> Text("Account settings placeholder", color = Color.White)
            SettingsTab.About -> Text("About placeholder", color = Color.White)
        }
    }
}

@Composable
private fun PlayerSettings(
    qualityLevel: Int,
    scaleMode: Int,
    playerCompatMode: Boolean,
    onSetQualityLevel: (Int) -> Unit,
    onSetScaleMode: (Int) -> Unit,
    onSetPlayerCompatMode: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Quality: $qualityLevel", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TvActionButton(text = "-", onClick = { onSetQualityLevel((qualityLevel - 1).coerceAtLeast(0)) })
            TvActionButton(text = "+", onClick = { onSetQualityLevel(qualityLevel + 1) })
        }
        Text("Scale Mode: $scaleMode", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TvActionButton(text = "-", onClick = { onSetScaleMode((scaleMode - 1).coerceAtLeast(0)) })
            TvActionButton(text = "+", onClick = { onSetScaleMode(scaleMode + 1) })
        }
        TvActionButton(
            text = "Player Compat: ${if (playerCompatMode) "ON" else "OFF"}",
            onClick = { onSetPlayerCompatMode(!playerCompatMode) },
        )
    }
}

@Composable
private fun DanmakuSettings(
    danmakuEnabled: Boolean,
    onToggleDanmaku: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Danmaku: ${if (danmakuEnabled) "ON" else "OFF"}", color = Color.White)
        TvActionButton(
            text = if (danmakuEnabled) "Disable Danmaku" else "Enable Danmaku",
            onClick = { onToggleDanmaku(!danmakuEnabled) },
        )
    }
}
