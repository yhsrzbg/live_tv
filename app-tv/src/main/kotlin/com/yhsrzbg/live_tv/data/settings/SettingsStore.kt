package com.yhsrzbg.live_tv.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsStore(
    context: Context,
    storeName: String = "live_tv_settings",
) {
    private val appContext = context.applicationContext
    private val settingsDataStore = PreferenceDataStoreFactory.create {
        appContext.preferencesDataStoreFile(storeName)
    }

    private object Keys {
        val danmakuEnabled = booleanPreferencesKey("danmaku_enabled")
        val qualityLevel = intPreferencesKey("quality_level")
        val scaleMode = intPreferencesKey("scale_mode")
        val playerCompatMode = booleanPreferencesKey("player_compat_mode")
    }

    val danmakuEnabled: Flow<Boolean> = settingsDataStore.data.map {
        it[Keys.danmakuEnabled] ?: true
    }

    val qualityLevel: Flow<Int> = settingsDataStore.data.map {
        it[Keys.qualityLevel] ?: 1
    }

    val scaleMode: Flow<Int> = settingsDataStore.data.map {
        it[Keys.scaleMode] ?: 0
    }

    val playerCompatMode: Flow<Boolean> = settingsDataStore.data.map {
        it[Keys.playerCompatMode] ?: false
    }

    suspend fun setDanmakuEnabled(enabled: Boolean) {
        settingsDataStore.edit { it[Keys.danmakuEnabled] = enabled }
    }

    suspend fun setQualityLevel(level: Int) {
        settingsDataStore.edit { it[Keys.qualityLevel] = level }
    }

    suspend fun setScaleMode(mode: Int) {
        settingsDataStore.edit { it[Keys.scaleMode] = mode }
    }

    suspend fun setPlayerCompatMode(enabled: Boolean) {
        settingsDataStore.edit { it[Keys.playerCompatMode] = enabled }
    }
}
