package com.yhsrzbg.live_tv.core.site

import java.security.MessageDigest
import java.util.Locale
import kotlin.random.Random
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

object DouyinSignature {
    private val webMsSdkScript: String by lazy { readResource("douyin_webmssdk.js") }
    private val aBogusScript: String by lazy { readResource("douyin_abogus.js") }

    fun signWebSocket(roomId: String, userUniqueId: String, userAgent: String): String {
        val msStub = md5(
            listOf(
                "live_id=1",
                "aid=6383",
                "version_code=180800",
                "webcast_sdk_version=1.3.0",
                "room_id=$roomId",
                "sub_room_id=",
                "sub_channel_id=",
                "did_rule=3",
                "user_unique_id=$userUniqueId",
                "device_platform=web",
                "device_type=",
                "ac=",
                "identity=audience",
            ).joinToString(",")
        )

        val signature = evalStringFunction(webMsSdkScript, "getMSSDKSignature", arrayOf(msStub, userAgent))
        return signature.filter { it != '-' && it != '=' }
    }

    fun appendABogus(url: String, userAgent: String): String {
        val msToken = randomAlphaNum(107)
        val query = "$url&msToken=$msToken".substringAfter('?')
        val aBogus = evalStringFunction(aBogusScript, "getABogus", arrayOf(query, userAgent))
        return "$url&msToken=${encode(msToken)}&a_bogus=${encode(aBogus)}"
    }

    private fun evalStringFunction(script: String, fnName: String, args: Array<Any>): String {
        val cx = Context.enter()
        return try {
            cx.optimizationLevel = -1
            val scope: Scriptable = cx.initStandardObjects()
            cx.evaluateString(scope, script, "douyin-$fnName.js", 1, null)
            val fn = scope.get(fnName, scope)
            val result = (fn as org.mozilla.javascript.Function).call(cx, scope, scope, args)
            Context.toString(result)
        } finally {
            Context.exit()
        }
    }

    private fun randomAlphaNum(len: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return buildString(len) { repeat(len) { append(chars[Random.nextInt(chars.length)]) } }
    }

    private fun readResource(name: String): String {
        val loader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
        val stream = loader.getResourceAsStream(name)
            ?: error("Resource not found: $name")
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    private fun encode(value: String): String = java.net.URLEncoder.encode(value, Charsets.UTF_8).replace("+", "%20")

    private fun md5(src: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(src.toByteArray())
        return digest.joinToString("") { "%02x".format(Locale.US, it) }
    }
}
