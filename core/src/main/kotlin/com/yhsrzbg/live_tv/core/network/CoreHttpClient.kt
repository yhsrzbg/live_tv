package com.yhsrzbg.live_tv.core.network

import okhttp3.OkHttpClient
import okhttp3.Request

class CoreHttpClient(
    private val client: OkHttpClient = OkHttpClient(),
) {
    fun get(url: String, headers: Map<String, String> = emptyMap()): String {
        val builder = Request.Builder().url(url)
        headers.forEach { (k, v) -> builder.addHeader(k, v) }
        client.newCall(builder.build()).execute().use { response ->
            return response.body?.string().orEmpty()
        }
    }
}
