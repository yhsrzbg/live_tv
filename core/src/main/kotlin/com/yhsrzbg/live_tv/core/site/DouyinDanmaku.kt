package com.yhsrzbg.live_tv.core.site

import com.google.protobuf.ByteString
import com.yhsrzbg.live_tv.core.api.LiveDanmaku
import com.yhsrzbg.live_tv.core.model.LiveMessage
import com.yhsrzbg.live_tv.core.model.LiveMessageType
import douyin.Douyin
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream
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

class DouyinDanmaku(
    private val client: OkHttpClient = OkHttpClient(),
) : LiveDanmaku {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bus = MutableSharedFlow<LiveMessage>(extraBufferCapacity = 128)

    private var socket: WebSocket? = null
    private var heartbeatJob: Job? = null

    override val heartbeatTimeSeconds: Int = 10
    override val messages: Flow<LiveMessage> = bus.asSharedFlow()

    override suspend fun start(args: Map<String, String>) {
        stop()

        val roomId = args["roomId"].orEmpty()
        val webRid = args["webRid"].orEmpty().ifBlank { roomId }
        val userId = args["userId"].orEmpty().ifBlank { randomNumeric(12) }
        val cookie = args["cookie"].orEmpty()

        if (roomId.isBlank()) return

        val signature = runCatching {
            DouyinSignature.signWebSocket(roomId, userId, DouyinSite.USER_AGENT)
        }.getOrDefault("")

        val ts = System.currentTimeMillis()
        val query = linkedMapOf(
            "app_name" to "douyin_web",
            "version_code" to "180800",
            "webcast_sdk_version" to "1.3.0",
            "update_version_code" to "1.3.0",
            "compress" to "gzip",
            "cursor" to "h-1_t-${ts}_r-1_d-1_u-1",
            "host" to "https://live.douyin.com",
            "aid" to "6383",
            "live_id" to "1",
            "did_rule" to "3",
            "debug" to "false",
            "maxCacheMessageNumber" to "20",
            "endpoint" to "live_pc",
            "support_wrds" to "1",
            "im_path" to "/webcast/im/fetch/",
            "user_unique_id" to userId,
            "device_platform" to "web",
            "cookie_enabled" to "true",
            "screen_width" to "1920",
            "screen_height" to "1080",
            "browser_language" to "zh-CN",
            "browser_platform" to "Win32",
            "browser_name" to "Mozilla",
            "browser_version" to DouyinSite.USER_AGENT.removePrefix("Mozilla/"),
            "browser_online" to "true",
            "tz_name" to "Asia/Shanghai",
            "identity" to "audience",
            "room_id" to roomId,
            "heartbeatDuration" to "0",
        )
        if (signature.isNotBlank()) query["signature"] = signature

        val url = "wss://webcast3-ws-web-lq.douyin.com/webcast/im/push/v2/?" +
            query.entries.joinToString("&") { "${it.key}=${encode(it.value)}" }

        val req = Request.Builder()
            .url(url)
            .header("User-Agent", DouyinSite.USER_AGENT)
            .header("Origin", "https://live.douyin.com")
            .apply {
                if (cookie.isNotBlank()) header("Cookie", cookie)
                header("Referer", "https://live.douyin.com/$webRid")
            }
            .build()

        socket = client.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                sendHeartBeat(webSocket)
                heartbeatJob = scope.launch {
                    while (isActive) {
                        delay(heartbeatTimeSeconds * 1000L)
                        sendHeartBeat(webSocket)
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
                decode(bytes.toByteArray())
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

    private fun sendHeartBeat(webSocket: WebSocket) {
        val frame = Douyin.PushFrame.newBuilder()
            .setPayloadType("hb")
            .build()
        webSocket.send(okio.ByteString.of(*frame.toByteArray()))
    }

    private fun sendAck(webSocket: WebSocket?, logId: Long, internalExt: String) {
        if (webSocket == null || internalExt.isBlank()) return
        val frame = Douyin.PushFrame.newBuilder()
            .setPayloadType("ack")
            .setLogId(logId)
            .setPayload(ByteString.copyFromUtf8(internalExt))
            .build()
        webSocket.send(okio.ByteString.of(*frame.toByteArray()))
    }

    private fun decode(raw: ByteArray) {
        runCatching {
            val frame = Douyin.PushFrame.parseFrom(raw)
            val payloadBytes = if (frame.payloadEncoding.equals("gzip", ignoreCase = true)) {
                ungzip(frame.payload.toByteArray())
            } else {
                frame.payload.toByteArray()
            }

            val response = Douyin.Response.parseFrom(payloadBytes)
            if (response.needAck) {
                sendAck(socket, frame.logId, response.internalExt)
            }

            response.messagesListList.forEach { msg ->
                when (msg.method) {
                    "WebcastChatMessage" -> {
                        val chat = Douyin.ChatMessage.parseFrom(msg.payload)
                        scope.launch {
                            bus.emit(
                                LiveMessage(
                                    type = LiveMessageType.Chat,
                                    message = chat.content,
                                )
                            )
                        }
                    }

                    "WebcastRoomUserSeqMessage" -> {
                        val seq = Douyin.RoomUserSeqMessage.parseFrom(msg.payload)
                        scope.launch {
                            bus.emit(
                                LiveMessage(
                                    type = LiveMessageType.Online,
                                    data = seq.totalUser.toInt(),
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun ungzip(data: ByteArray): ByteArray {
        GZIPInputStream(ByteArrayInputStream(data)).use { gzip ->
            return gzip.readBytes()
        }
    }

    private fun encode(value: String): String = java.net.URLEncoder.encode(value, Charsets.UTF_8).replace("+", "%20")

    private fun randomNumeric(length: Int): String {
        val digits = "0123456789"
        return buildString(length) { repeat(length) { append(digits.random()) } }
    }
}
