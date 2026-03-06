package com.yhsrzbg.live_tv.core.site

import com.yhsrzbg.live_tv.core.api.LiveDanmaku
import com.yhsrzbg.live_tv.core.model.LiveColor
import com.yhsrzbg.live_tv.core.model.LiveMessage
import com.yhsrzbg.live_tv.core.model.LiveMessageType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class DouyuDanmaku(
    private val client: OkHttpClient = OkHttpClient(),
) : LiveDanmaku {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bus = MutableSharedFlow<LiveMessage>(extraBufferCapacity = 128)

    private var socket: WebSocket? = null
    private var heartbeatJob: Job? = null

    override val heartbeatTimeSeconds: Int = 45
    override val messages: Flow<LiveMessage> = bus.asSharedFlow()

    override suspend fun start(args: Map<String, String>) {
        stop()
        val roomId = args["roomId"].orEmpty()
        if (roomId.isBlank()) return

        val request = Request.Builder()
            .url("wss://danmuproxy.douyu.com:8506")
            .header("User-Agent", DouyuSite.USER_AGENT)
            .build()

        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                webSocket.send(okio.ByteString.of(*serialize("type@=loginreq/roomid@=$roomId/")))
                webSocket.send(okio.ByteString.of(*serialize("type@=joingroup/rid@=$roomId/gid@=-9999/")))
                heartbeatJob = scope.launch {
                    while (isActive) {
                        delay(heartbeatTimeSeconds * 1000L)
                        webSocket.send(okio.ByteString.of(*serialize("type@=mrkl/")))
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
                decodeFrames(bytes.toByteArray())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                heartbeatJob?.cancel()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                heartbeatJob?.cancel()
            }
        })
    }

    override suspend fun stop() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        socket?.close(1000, "stop")
        socket = null
    }

    private fun serialize(body: String): ByteArray {
        val payload = body.toByteArray(Charsets.UTF_8)
        val packetLen = 4 + 4 + 2 + 1 + 1 + payload.size + 1
        val buf = ByteBuffer.allocate(packetLen).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(packetLen)
        buf.putInt(packetLen)
        buf.putShort(689)
        buf.put(0)
        buf.put(0)
        buf.put(payload)
        buf.put(0)
        return buf.array()
    }

    private fun decodeFrames(raw: ByteArray) {
        var offset = 0
        while (offset + 12 <= raw.size) {
            val len = ByteBuffer.wrap(raw, offset, 4).order(ByteOrder.LITTLE_ENDIAN).int
            if (len <= 0 || offset + len > raw.size) break
            val bodyLen = len - 9
            val bodyStart = offset + 12
            if (bodyLen > 0 && bodyStart + bodyLen <= raw.size) {
                val body = raw.copyOfRange(bodyStart, bodyStart + bodyLen)
                parseMessage(body.toString(Charsets.UTF_8))
            }
            offset += len
        }
    }

    private fun parseMessage(text: String) {
        val map = sttToMap(text) ?: return
        if (map["type"] != "chatmsg") return
        if (!map.containsKey("dms")) return

        val message = map["txt"].orEmpty()
        if (message.isBlank()) return
        val userName = map["nn"].orEmpty()

        val color = map["col"]?.toIntOrNull().toLiveColor()
        scope.launch {
            bus.emit(
                LiveMessage(
                    type = LiveMessageType.Chat,
                    userName = userName,
                    message = message,
                    color = color,
                )
            )
        }
    }

    private fun sttToMap(input: String): Map<String, String>? {
        val result = linkedMapOf<String, String>()
        val fields = input.split('/').filter { it.isNotBlank() }
        if (fields.isEmpty()) return null
        fields.forEach { field ->
            val idx = field.indexOf("@=")
            if (idx <= 0) return@forEach
            val key = field.substring(0, idx)
            val value = unescape(field.substring(idx + 2))
            result[key] = value
        }
        return result
    }

    private fun unescape(value: String): String = value.replace("@S", "/").replace("@A", "@")

    private fun Int?.toLiveColor(): LiveColor = when (this) {
        1 -> LiveColor(255, 0, 0)
        2 -> LiveColor(30, 135, 240)
        3 -> LiveColor(122, 200, 75)
        4 -> LiveColor(255, 127, 0)
        5 -> LiveColor(155, 57, 244)
        6 -> LiveColor(255, 105, 180)
        else -> LiveColor(255, 255, 255)
    }
}
