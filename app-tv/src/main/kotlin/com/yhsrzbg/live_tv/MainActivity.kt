package com.yhsrzbg.live_tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.yhsrzbg.live_tv.data.LiveRepository
import com.yhsrzbg.live_tv.data.settings.SettingsStore
import com.yhsrzbg.live_tv.ui.LiveTvApp
import com.yhsrzbg.live_tv.ui.theme.LiveTvTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LiveTvApplication
        setContent {
            LiveTvTheme {
                val repository = remember { LiveRepository(app.database) }
                val settingsStore = remember { SettingsStore(this@MainActivity) }
                LiveTvApp(repository = repository, settingsStore = settingsStore)
            }
        }
    }
}
