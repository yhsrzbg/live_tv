package com.yhsrzbg.live_tv.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class CoreHttpClient(
    private val client: OkHttpClient = OkHttpClient(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getText(
        url: String,
        query: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(buildUrl(url, query))
            .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
            .build()

        client.newCall(request).execute().use { response ->
            response.body?.string().orEmpty()
        }
    }

    suspend fun getJson(
        url: String,
        query: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): JsonObject = json.parseToJsonElement(getText(url, query, headers)).jsonObject

    suspend fun postFormJson(
        url: String,
        form: Map<String, String>,
        headers: Map<String, String> = emptyMap(),
    ): JsonObject = json.parseToJsonElement(postFormText(url, form, headers)).jsonObject

    suspend fun postFormText(
        url: String,
        form: Map<String, String>,
        headers: Map<String, String> = emptyMap(),
    ): String = withContext(Dispatchers.IO) {
        val body = FormBody.Builder().apply {
            form.forEach { (k, v) -> add(k, v) }
        }.build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
            .build()

        client.newCall(request).execute().use { response ->
            response.body?.string().orEmpty()
        }
    }

    private fun buildUrl(url: String, query: Map<String, String>): String {
        if (query.isEmpty()) return url
        val builder = requireNotNull(url.toHttpUrlOrNull()) { "Invalid url: $url" }.newBuilder()
        query.forEach { (k, v) -> builder.addQueryParameter(k, v) }
        return builder.build().toString()
    }
}
