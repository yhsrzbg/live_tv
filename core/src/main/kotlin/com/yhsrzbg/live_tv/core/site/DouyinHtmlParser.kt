package com.yhsrzbg.live_tv.core.site

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

object DouyinHtmlParser {
    private val json = Json { ignoreUnknownKeys = true }
    private const val ESCAPED_STATE_START = "{\\\"state\\\":{\\\"appStore"
    private const val PLAIN_STATE_START = "{\"state\":{\"appStore"

    fun extractState(html: String): JsonObject? {
        val start = html.indexOf(ESCAPED_STATE_START).takeIf { it >= 0 } ?: html.indexOf(PLAIN_STATE_START)
        if (start < 0) return null

        val normalized = html.substring(start)
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")

        val jsonStart = normalized.indexOf('{')
        if (jsonStart < 0) return null
        val jsonEnd = findJsonObjectEnd(normalized, jsonStart)
        if (jsonEnd < 0) return null

        val parsed = runCatching {
            json.parseToJsonElement(normalized.substring(jsonStart, jsonEnd + 1)) as? JsonObject
        }.getOrNull() ?: return null
        return parsed["state"] as? JsonObject
    }

    fun pickM3u8Candidates(rawStateText: String): List<String> {
        val regex = Regex("https?:\\\\/\\\\/[^\"\\s]+\\.m3u8")
        return regex.findAll(rawStateText)
            .map { it.value.replace("\\\\/", "/").replace("\\/", "/") }
            .distinct()
            .toList()
    }

    fun emptyState(): JsonObject = buildJsonObject { }

    private fun findJsonObjectEnd(text: String, start: Int): Int {
        var depth = 0
        var inString = false
        var escaped = false
        for (i in start until text.length) {
            val ch = text[i]
            if (escaped) {
                escaped = false
                continue
            }
            when (ch) {
                '\\' -> if (inString) escaped = true
                '"' -> inString = !inString
                '{' -> if (!inString) depth++
                '}' -> if (!inString) {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }
}

