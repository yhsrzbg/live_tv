package com.yhsrzbg.live_tv.core.api

import com.yhsrzbg.live_tv.core.model.LiveMessage
import kotlinx.coroutines.flow.Flow

interface LiveDanmaku {
    val heartbeatTimeSeconds: Int
    val messages: Flow<LiveMessage>

    suspend fun start(args: Map<String, String>)
    suspend fun stop()
}
