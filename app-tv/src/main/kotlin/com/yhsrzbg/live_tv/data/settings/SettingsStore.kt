package com.yhsrzbg.live_tv.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "live_tv_settings")

class SettingsStore(private val context: Context) {
    private object Keys {
        val danmakuEnabled = booleanPreferencesKey("danmaku_enabled")
        val qualityLevel = intPreferencesKey("quality_level")
    }

    val danmakuEnabled: Flow<Boolean> = context.settingsDataStore.data.map {
        it[Keys.danmakuEnabled] ?: true
    }

    val qualityLevel: Flow<Int> = context.settingsDataStore.data.map {
        it[Keys.qualityLevel] ?: 1
    }

    suspend fun setDanmakuEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.danmakuEnabled] = enabled }
    }

    suspend fun setQualityLevel(level: Int) {
        context.settingsDataStore.edit { it[Keys.qualityLevel] = level }
    }
}
