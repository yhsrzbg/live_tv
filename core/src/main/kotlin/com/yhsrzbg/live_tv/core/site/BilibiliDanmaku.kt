package com.yhsrzbg.live_tv.core.site

import com.yhsrzbg.live_tv.core.api.LiveDanmaku
import com.yhsrzbg.live_tv.core.model.LiveColor
import com.yhsrzbg.live_tv.core.model.LiveMessage
import com.yhsrzbg.live_tv.core.model.LiveMessageType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.InflaterInputStream
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.brotli.dec.BrotliInputStream

class BilibiliDanmaku(
    private val client: OkHttpClient = OkHttpClient(),
) : LiveDanmaku {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bus = MutableSharedFlow<LiveMessage>(extraBufferCapacity = 128)

    private var socket: WebSocket? = null
    private var heartbeatJob: Job? = null

    override val heartbeatTimeSeconds: Int = 60
    override val messages: Flow<LiveMessage> = bus.asSharedFlow()

    override suspend fun start(args: Map<String, String>) {
        stop()

        val roomId = args["roomId"].orEmpty()
        if (roomId.isBlank()) return

        val token = args["token"].orEmpty()
        val serverHost = args["serverHost"].orEmpty().ifBlank { "broadcastlv.chat.bilibili.com" }
        val buvid = args["buvid"].orEmpty()
        val uid = args["uid"]?.toLongOrNull() ?: 0L
        val cookie = args["cookie"].orEmpty()

        val request = Request.Builder()
            .url("wss://$serverHost/sub")
            .header("User-Agent", BilibiliSite.USER_AGENT)
            .apply {
                if (cookie.isNotBlank()) header("Cookie", cookie)
            }
            .build()

        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val joinPayload = """
                    {
                      "uid": $uid,
                      "roomid": ${roomId.toLongOrNull() ?: 0L},
                      "protover": 3,
                      "buvid": "$buvid",
                      "platform": "web",
                      "type": 2,
                      "key": "$token"
                    }
                """.trimIndent().replace("\n", "")
                webSocket.send(okio.ByteString.of(*encodeFrame(joinPayload.encodeToByteArray(), operation = 7)))

                heartbeatJob = scope.launch {
                    while (isActive) {
                        delay(heartbeatTimeSeconds * 1000L)
                        webSocket.send(okio.ByteString.of(*encodeFrame(byteArrayOf(), operation = 2)))
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

    private fun decodeFrames(raw: ByteArray) {
        var offset = 0
        while (offset + 16 <= raw.size) {
            val packetLen = readIntBig(raw, offset)
            if (packetLen <= 0 || offset + packetLen > raw.size) break

            val headerLen = readShortBig(raw, offset + 4).toInt()
            val version = readShortBig(raw, offset + 6).toInt()
            val operation = readIntBig(raw, offset + 8)
            val bodyStart = offset + headerLen
            val body = if (bodyStart in 0..(offset + packetLen)) {
                raw.copyOfRange(bodyStart, offset + packetLen)
            } else {
                byteArrayOf()
            }

            when (operation) {
                3 -> {
                    if (body.size >= 4) {
                        val online = readIntBig(body, 0)
                        scope.launch {
                            bus.emit(LiveMessage(type = LiveMessageType.Online, data = online))
                        }
                    }
                }

                5 -> {
                    val decoded = when (version) {
                        2 -> inflate(body)
                        3 -> brotli(body)
                        else -> body
                    }
                    if (version == 2 || version == 3) {
                        decodeFrames(decoded)
                    } else {
                        parseJsonMessages(decoded)
                    }
                }
            }
            offset += packetLen
        }
    }

    private fun parseJsonMessages(body: ByteArray) {
        val text = body.toString(Charsets.UTF_8)
        val chunks = text.split(Regex("[\\x00-\\x1f]+"))
            .asSequence()
            .map { it.trim() }
            .filter { it.startsWith("{") && it.endsWith("}") }
            .toList()
        chunks.forEach { raw ->
            runCatching {
                val obj = Json.parseToJsonElement(raw).jsonObject
                val cmd = obj["cmd"]?.jsonPrimitive?.content.orEmpty()
                if (cmd.contains("DANMU_MSG")) {
                    val info = obj["info"] as? JsonArray ?: return@runCatching
                    val message = info.getOrNull(1)?.jsonPrimitive?.content.orEmpty()
                    val userName = info.getOrNull(2)?.jsonArray?.getOrNull(1)?.jsonPrimitive?.content.orEmpty()
                    val color = info.getOrNull(0)?.jsonArray?.getOrNull(3)?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    scope.launch {
                        bus.emit(
                            LiveMessage(
                                type = LiveMessageType.Chat,
                                userName = userName,
                                message = message,
                                color = color.toLiveColor(),
                                data = 0,
                            )
                        )
                    }
                }
            }
        }
    }

    private fun encodeFrame(payload: ByteArray, operation: Int): ByteArray {
        val headerLen = 16
        val packetLen = headerLen + payload.size
        val buffer = ByteBuffer.allocate(packetLen).order(ByteOrder.BIG_ENDIAN)
        buffer.putInt(packetLen)
        buffer.putShort(headerLen.toShort())
        buffer.putShort(1)
        buffer.putInt(operation)
        buffer.putInt(1)
        buffer.put(payload)
        return buffer.array()
    }

    private fun inflate(input: ByteArray): ByteArray {
        InflaterInputStream(ByteArrayInputStream(input)).use { stream ->
            val out = ByteArrayOutputStream()
            val buf = ByteArray(4096)
            while (true) {
                val n = stream.read(buf)
                if (n <= 0) break
                out.write(buf, 0, n)
            }
            return out.toByteArray()
        }
    }

    private fun brotli(input: ByteArray): ByteArray {
        BrotliInputStream(ByteArrayInputStream(input)).use { stream ->
            val out = ByteArrayOutputStream()
            val buf = ByteArray(4096)
            while (true) {
                val n = stream.read(buf)
                if (n <= 0) break
                out.write(buf, 0, n)
            }
            return out.toByteArray()
        }
    }

    private fun readIntBig(data: ByteArray, offset: Int): Int =
        ByteBuffer.wrap(data, offset, 4).order(ByteOrder.BIG_ENDIAN).int

    private fun readShortBig(data: ByteArray, offset: Int): Short =
        ByteBuffer.wrap(data, offset, 2).order(ByteOrder.BIG_ENDIAN).short

    private fun Int.toLiveColor(): LiveColor {
        if (this <= 0) return LiveColor(255, 255, 255)
        val r = (this shr 16) and 0xff
        val g = (this shr 8) and 0xff
        val b = this and 0xff
        return LiveColor(r, g, b)
    }
}
