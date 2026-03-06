package com.yhsrzbg.live_tv.core.site

import java.security.MessageDigest
import java.util.Locale

object BilibiliWbiSigner {
    private val mixinKeyEncTab = listOf(
        46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35,
        27, 43, 5, 49, 33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13,
        37, 48, 7, 16, 24, 55, 40, 61, 26, 17, 0, 1, 60, 51, 30, 4,
        22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11, 36, 20, 34, 44, 52,
    )

    fun mixinKey(origin: String): String = buildString {
        mixinKeyEncTab.forEach { idx ->
            if (idx < origin.length) append(origin[idx])
        }
    }.take(32)

    fun signQuery(
        params: LinkedHashMap<String, String>,
        mixinKey: String,
        unixSeconds: Long,
    ): LinkedHashMap<String, String> {
        val clean = linkedMapOf<String, String>()
        params.forEach { (k, v) -> clean[k] = v.filterNot { it in "!'()*" } }
        clean["wts"] = unixSeconds.toString()

        val query = clean.entries
            .sortedBy { it.key }
            .joinToString("&") { (k, v) -> "$k=${encodeQuery(v)}" }

        clean["w_rid"] = md5(query + mixinKey)
        return LinkedHashMap(clean)
    }

    private fun encodeQuery(value: String): String = java.net.URLEncoder.encode(value, Charsets.UTF_8)
        .replace("+", "%20")

    private fun md5(src: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(src.toByteArray())
        return digest.joinToString("") { "%02x".format(Locale.US, it) }
    }
}
