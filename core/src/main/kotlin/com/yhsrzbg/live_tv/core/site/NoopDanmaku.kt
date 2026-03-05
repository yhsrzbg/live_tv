package com.yhsrzbg.live_tv.core.site

import com.yhsrzbg.live_tv.core.api.LiveDanmaku
import com.yhsrzbg.live_tv.core.model.LiveMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NoopDanmaku : LiveDanmaku {
    private val bus = MutableSharedFlow<LiveMessage>(extraBufferCapacity = 64)

    override val heartbeatTimeSeconds: Int = 30
    override val messages: Flow<LiveMessage> = bus.asSharedFlow()

    override suspend fun start(args: Map<String, String>) {
        // Phase1: keep extension point for site-specific danmaku protocol.
    }

    override suspend fun stop() {
        // no-op
    }
}
