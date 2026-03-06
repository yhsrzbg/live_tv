package com.yhsrzbg.live_tv.core.site

object DouyuParsers {
    fun parseHotNum(raw: String): Int {
        val v = raw.trim()
        if (v.isEmpty()) return 0
        return try {
            val base = v.removeSuffix("万").toDouble()
            if (v.endsWith("万")) (base * 10000).toInt() else base.toInt()
        } catch (_: Throwable) {
            0
        }
    }
}
