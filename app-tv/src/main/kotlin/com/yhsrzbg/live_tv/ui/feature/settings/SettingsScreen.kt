package com.yhsrzbg.live_tv.ui.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack) { Text("Back") }
            Text("Settings", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsTab.values().forEach { tab ->
                Button(onClick = { activeTab = tab }) {
                    Text(tab.name)
                }
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
            Button(onClick = { onSetQualityLevel((qualityLevel - 1).coerceAtLeast(0)) }) { Text("-") }
            Button(onClick = { onSetQualityLevel(qualityLevel + 1) }) { Text("+") }
        }
        Text("Scale Mode: $scaleMode", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onSetScaleMode((scaleMode - 1).coerceAtLeast(0)) }) { Text("-") }
            Button(onClick = { onSetScaleMode(scaleMode + 1) }) { Text("+") }
        }
        Button(onClick = { onSetPlayerCompatMode(!playerCompatMode) }) {
            Text("Player Compat: ${if (playerCompatMode) "ON" else "OFF"}")
        }
    }
}

@Composable
private fun DanmakuSettings(
    danmakuEnabled: Boolean,
    onToggleDanmaku: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Danmaku: ${if (danmakuEnabled) "ON" else "OFF"}", color = Color.White)
        Button(onClick = { onToggleDanmaku(!danmakuEnabled) }) {
            Text(if (danmakuEnabled) "Disable Danmaku" else "Enable Danmaku")
        }
    }
}
