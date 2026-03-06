package com.yhsrzbg.live_tv.core.site

import com.qq.tars.protocol.tars.TarsInputStream
import com.qq.tars.protocol.tars.TarsOutputStream
import com.qq.tars.protocol.tars.TarsStructBase
import com.yhsrzbg.live_tv.core.api.LiveDanmaku
import com.yhsrzbg.live_tv.core.model.LiveColor
import com.yhsrzbg.live_tv.core.model.LiveMessage
import com.yhsrzbg.live_tv.core.model.LiveMessageType
import java.util.Base64
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

class HuyaDanmaku(
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
        val ayyuid = args["ayyuid"]?.toLongOrNull() ?: 0L
        val topSid = args["topSid"]?.toLongOrNull() ?: 0L
        val subSid = args["subSid"]?.toLongOrNull() ?: topSid
        if (ayyuid <= 0L || topSid <= 0L || subSid <= 0L) return

        val request = Request.Builder()
            .url("wss://cdnws.api.huya.com")
            .header("User-Agent", UA)
            .build()

        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                webSocket.send(okio.ByteString.of(*buildJoinData(ayyuid, topSid, subSid)))
                heartbeatJob = scope.launch {
                    while (isActive) {
                        delay(heartbeatTimeSeconds * 1000L)
                        webSocket.send(okio.ByteString.of(*HEARTBEAT))
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

    private fun buildJoinData(ayyuid: Long, tid: Long, sid: Long): ByteArray {
        val req = TarsOutputStream()
        req.write(ayyuid, 0)
        req.write(true, 1)
        req.write("", 2)
        req.write("", 3)
        req.write(tid, 4)
        req.write(sid, 5)
        req.write(0, 6)
        req.write(0, 7)

        val cmd = TarsOutputStream()
        cmd.write(1, 0)
        cmd.write(req.toByteArray(), 1)
        return cmd.toByteArray()
    }

    private fun decode(raw: ByteArray) {
        runCatching {
            val frame = TarsInputStream(raw)
            val type = frame.read(0, 0, false)
            if (type != 7) return
            val payload = frame.read(byteArrayOf(), 1, false)
            val push = HYPushMessage()
            push.readFrom(TarsInputStream(payload))

            when (push.uri) {
                1400 -> {
                    val message = HYMessage()
                    message.readFrom(TarsInputStream(push.msg))
                    if (message.content.isNotBlank()) {
                        scope.launch {
                            bus.emit(
                                LiveMessage(
                                    type = LiveMessageType.Chat,
                                    message = message.content,
                                    color = message.bulletFormat.fontColor.toLiveColor(),
                                )
                            )
                        }
                    }
                }

                8006 -> {
                    val online = TarsInputStream(push.msg).read(0, 0, false)
                    scope.launch {
                        bus.emit(
                            LiveMessage(
                                type = LiveMessageType.Online,
                                data = online,
                            )
                        )
                    }
                }
            }
        }
    }

    private fun Int.toLiveColor(): LiveColor {
        if (this <= 0) return LiveColor(255, 255, 255)
        val r = (this shr 16) and 0xff
        val g = (this shr 8) and 0xff
        val b = this and 0xff
        return LiveColor(r, g, b)
    }

    companion object {
        private const val UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
        private val HEARTBEAT: ByteArray = Base64.getDecoder().decode("ABQdAAwsNgBM")
    }
}

private class HYPushMessage : TarsStructBase() {
    var pushType: Int = 0
    var uri: Int = 0
    var msg: ByteArray = byteArrayOf()
    var protocolType: Int = 0

    override fun writeTo(out: TarsOutputStream) = Unit

    override fun readFrom(input: TarsInputStream) {
        pushType = input.read(pushType, 0, false)
        uri = input.read(uri, 1, false)
        msg = input.read(byteArrayOf(), 2, false)
        protocolType = input.read(protocolType, 3, false)
    }
}

private class HYSender : TarsStructBase() {
    var uid: Long = 0
    var lMid: Long = 0
    var nickName: String = ""
    var gender: Int = 0

    override fun writeTo(out: TarsOutputStream) = Unit

    override fun readFrom(input: TarsInputStream) {
        uid = input.read(uid, 0, false)
        lMid = input.read(lMid, 1, false)
        nickName = input.read(nickName, 2, false)
        gender = input.read(gender, 3, false)
    }
}

private class HYBulletFormat : TarsStructBase() {
    var fontColor: Int = 0
    var fontSize: Int = 4
    var textSpeed: Int = 0
    var transitionType: Int = 1

    override fun writeTo(out: TarsOutputStream) = Unit

    override fun readFrom(input: TarsInputStream) {
        fontColor = input.read(fontColor, 0, false)
        fontSize = input.read(fontSize, 1, false)
        textSpeed = input.read(textSpeed, 2, false)
        transitionType = input.read(transitionType, 3, false)
    }
}

private class HYMessage : TarsStructBase() {
    var userInfo: HYSender = HYSender()
    var content: String = ""
    var bulletFormat: HYBulletFormat = HYBulletFormat()

    override fun writeTo(out: TarsOutputStream) = Unit

    override fun readFrom(input: TarsInputStream) {
        userInfo = (input.read(userInfo, 0, false) as? HYSender) ?: HYSender()
        content = input.read(content, 3, false)
        bulletFormat = (input.read(bulletFormat, 6, false) as? HYBulletFormat) ?: HYBulletFormat()
    }
}

