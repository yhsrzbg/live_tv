package com.yhsrzbg.live_tv.core.site

import com.yhsrzbg.live_tv.core.api.LiveDanmaku
import com.yhsrzbg.live_tv.core.api.LiveSite
import com.yhsrzbg.live_tv.core.model.LiveAnchorItem
import com.yhsrzbg.live_tv.core.model.LiveCategory
import com.yhsrzbg.live_tv.core.model.LiveCategoryResult
import com.yhsrzbg.live_tv.core.model.LivePlayQuality
import com.yhsrzbg.live_tv.core.model.LivePlayUrl
import com.yhsrzbg.live_tv.core.model.LiveRoomDetail
import com.yhsrzbg.live_tv.core.model.LiveRoomItem
import com.yhsrzbg.live_tv.core.model.LiveSearchAnchorResult
import com.yhsrzbg.live_tv.core.model.LiveSearchRoomResult
import com.yhsrzbg.live_tv.core.model.LiveSubCategory
import com.yhsrzbg.live_tv.core.model.LiveSuperChatMessage
import com.yhsrzbg.live_tv.core.network.CoreHttpClient
import com.yhsrzbg.live_tv.core.util.array
import com.yhsrzbg.live_tv.core.util.asObj
import com.yhsrzbg.live_tv.core.util.int
import com.yhsrzbg.live_tv.core.util.obj
import com.yhsrzbg.live_tv.core.util.str

class BilibiliSite(
    private val http: CoreHttpClient = CoreHttpClient(),
) : LiveSite {
    override val id: String = "bilibili"
    override val name: String = "Bilibili"

    private var buvid3: String = ""
    private var buvid4: String = ""
    private var imgKey: String = ""
    private var subKey: String = ""

    override fun danmaku(): LiveDanmaku = BilibiliDanmaku()

    override suspend fun categories(): List<LiveCategory> {
        val root = http.getJson(
            url = "https://api.live.bilibili.com/room/v1/Area/getList",
            query = mapOf("need_entrance" to "1", "parent_id" to "0"),
            headers = headers(),
        )
        return root.array("data").mapNotNull { e ->
            val item = e.asObj() ?: return@mapNotNull null
            LiveCategory(
                id = item.str("id"),
                name = item.str("name"),
                children = item.array("list").mapNotNull { c ->
                    val sub = c.asObj() ?: return@mapNotNull null
                    LiveSubCategory(
                        id = sub.str("id"),
                        parentId = sub.str("parent_id"),
                        name = sub.str("name"),
                        pic = sub.str("pic"),
                    )
                }
            )
        }
    }

    override suspend fun recommendRooms(page: Int): LiveCategoryResult {
        val signed = signedParams(
            linkedMapOf(
                "platform" to "web",
                "sort" to "online",
                "page_size" to "30",
                "page" to page.toString(),
            )
        )
        val root = http.getJson(
            "https://api.live.bilibili.com/xlive/web-interface/v1/second/getListByArea",
            query = signed,
            headers = headers(),
        )
        val list = root.obj("data")?.array("list").orEmpty()
        return LiveCategoryResult(
            hasMore = list.isNotEmpty(),
            items = list.mapNotNull { parseRoomItem(it.asObj()) }
        )
    }

    override suspend fun categoryRooms(categoryId: String, parentId: String, page: Int): LiveCategoryResult {
        val signed = signedParams(
            linkedMapOf(
                "platform" to "web",
                "parent_area_id" to parentId,
                "area_id" to categoryId,
                "page" to page.toString(),
            )
        )
        val root = http.getJson(
            "https://api.live.bilibili.com/xlive/web-interface/v1/second/getList",
            query = signed,
            headers = headers(),
        )
        val data = root.obj("data")
        return LiveCategoryResult(
            hasMore = data?.int("has_more") == 1,
            items = data?.array("list").orEmpty().mapNotNull { parseRoomItem(it.asObj()) }
        )
    }

    override suspend fun searchRooms(keyword: String, page: Int): LiveSearchRoomResult {
        val root = http.getJson(
            "https://api.bilibili.com/x/web-interface/search/type",
            query = mapOf(
                "search_type" to "live",
                "keyword" to keyword,
                "highlight" to "0",
                "page" to page.toString(),
            ),
            headers = headers(),
        )
        val rooms = root.obj("data")?.obj("result")?.array("live_room").orEmpty()
        return LiveSearchRoomResult(
            hasMore = rooms.size >= 40,
            items = rooms.mapNotNull { parseRoomItem(it.asObj()) }
        )
    }

    override suspend fun searchAnchors(keyword: String, page: Int): LiveSearchAnchorResult {
        val root = http.getJson(
            "https://api.bilibili.com/x/web-interface/search/type",
            query = mapOf(
                "search_type" to "live_user",
                "keyword" to keyword,
                "highlight" to "0",
                "page" to page.toString(),
            ),
            headers = headers(),
        )
        val list = root.obj("data")?.array("result").orEmpty()
        return LiveSearchAnchorResult(
            hasMore = list.size >= 40,
            items = list.mapNotNull { e ->
                val o = e.asObj() ?: return@mapNotNull null
                LiveAnchorItem(
                    roomId = o.str("roomid"),
                    avatar = o.str("uface").prefixHttps(),
                    userName = o.str("uname").stripHtml(),
                    liveStatus = o.int("is_live") == 1,
                )
            }
        )
    }

    override suspend fun roomDetail(roomId: String): LiveRoomDetail {
        val signed = signedParams(linkedMapOf("room_id" to roomId))
        val data = http.getJson(
            "https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom",
            query = signed,
            headers = headers(),
        ).obj("data") ?: error("Bilibili room detail missing data")

        val room = data.obj("room_info").orEmpty()
        val anchor = data.obj("anchor_info")?.obj("base_info").orEmpty()
        val realRoomId = room.str("room_id")
        val danmaku = runCatching {
            http.getJson(
                "https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo",
                query = mapOf("id" to realRoomId),
                headers = headers(),
            ).obj("data").orEmpty()
        }.getOrElse { kotlinx.serialization.json.buildJsonObject { } }

        val host = danmaku.array("host_list")
            .firstOrNull()
            ?.asObj()
            ?.str("host")
            .orEmpty()
            .ifBlank { "broadcastlv.chat.bilibili.com" }

        return LiveRoomDetail(
            roomId = realRoomId,
            title = room.str("title"),
            cover = room.str("cover"),
            userName = anchor.str("uname"),
            userAvatar = anchor.str("face"),
            online = room.int("online"),
            status = room.int("live_status") == 1,
            url = "https://live.bilibili.com/$realRoomId",
            introduction = room.str("description"),
            showTime = room.str("live_start_time").ifBlank { null },
            danmakuData = mapOf(
                "roomId" to realRoomId,
                "token" to danmaku.str("token"),
                "serverHost" to host,
                "buvid" to buvid3,
                "uid" to "0",
                "cookie" to "buvid3=$buvid3;buvid4=$buvid4;",
            ),
        )
    }

    override suspend fun playQualities(detail: LiveRoomDetail): List<LivePlayQuality> {
        val data = roomPlayInfo(detail.roomId)
        val playUrl = data.obj("playurl_info")?.obj("playurl")
        val qualityMap = mutableMapOf<String, String>()
        playUrl?.array("g_qn_desc")?.forEach { item ->
            val o = item.asObj() ?: return@forEach
            qualityMap[o.str("qn")] = o.str("desc")
        }
        val qns = playUrl
            ?.array("stream")
            ?.firstOrNull()
            ?.asObj()
            ?.array("format")
            ?.firstOrNull()
            ?.asObj()
            ?.array("codec")
            ?.firstOrNull()
            ?.asObj()
            ?.array("accept_qn")
            .orEmpty()

        return qns.mapNotNull { qn ->
            val key = qn.toString().trim('"')
            if (key.isBlank()) return@mapNotNull null
            LivePlayQuality(quality = qualityMap[key] ?: key, data = key)
        }
    }

    override suspend fun playUrls(detail: LiveRoomDetail, quality: LivePlayQuality): LivePlayUrl {
        val data = roomPlayInfo(detail.roomId, quality.data)
        val streams = data.obj("playurl_info")?.obj("playurl")?.array("stream").orEmpty()
        val urls = mutableListOf<String>()

        streams.forEach { s ->
            val stream = s.asObj() ?: return@forEach
            stream.array("format").forEach { f ->
                val format = f.asObj() ?: return@forEach
                format.array("codec").forEach { c ->
                    val codec = c.asObj() ?: return@forEach
                    val baseUrl = codec.str("base_url")
                    codec.array("url_info").forEach { info ->
                        val u = info.asObj() ?: return@forEach
                        val raw = "${u.str("host")}$baseUrl${u.str("extra")}".trim()
                        if (raw.isNotBlank()) urls += raw
                    }
                }
            }
        }

        return LivePlayUrl(
            urls = urls.distinct(),
            headers = mapOf(
                "referer" to "https://live.bilibili.com",
                "user-agent" to USER_AGENT,
            )
        )
    }

    override suspend fun liveStatus(roomId: String): Boolean {
        val root = http.getJson(
            "https://api.live.bilibili.com/room/v1/Room/get_info",
            query = mapOf("room_id" to roomId),
            headers = headers(),
        )
        return root.obj("data")?.int("live_status") == 1
    }

    override suspend fun superChatMessages(roomId: String): List<LiveSuperChatMessage> {
        val root = http.getJson(
            "https://api.live.bilibili.com/av/v1/SuperChat/getMessageList",
            query = mapOf("room_id" to roomId),
            headers = headers(),
        )
        return root.obj("data")
            ?.array("list")
            .orEmpty()
            .mapNotNull { e ->
                val o = e.asObj() ?: return@mapNotNull null
                val user = o.obj("user_info").orEmpty()
                LiveSuperChatMessage(
                    userName = user.str("uname"),
                    face = user.str("face"),
                    message = o.str("message"),
                    price = o.int("price"),
                    startTime = o.str("start_time").toLongOrNull()?.times(1000L) ?: 0L,
                    endTime = o.str("end_time").toLongOrNull()?.times(1000L) ?: 0L,
                    backgroundColor = o.str("background_color"),
                    backgroundBottomColor = o.str("background_bottom_color"),
                )
            }
    }

    private suspend fun roomPlayInfo(roomId: String, qn: String? = null) = http.getJson(
        "https://api.live.bilibili.com/xlive/web-room/v2/index/getRoomPlayInfo",
        query = buildMap {
            put("room_id", roomId)
            put("protocol", "0,1")
            put("format", "0,1,2")
            put("codec", "0,1")
            put("platform", "web")
            if (!qn.isNullOrBlank()) put("qn", qn)
        },
        headers = headers(),
    ).obj("data").orEmpty()

    private fun parseRoomItem(o: kotlinx.serialization.json.JsonObject?): LiveRoomItem? {
        if (o == null) return null
        return LiveRoomItem(
            roomId = o.str("roomid"),
            title = o.str("title").stripHtml(),
            cover = o.str("cover").prefixHttps(),
            userName = o.str("uname").stripHtml(),
            online = o.int("online"),
        )
    }

    private suspend fun headers(): Map<String, String> {
        ensureBuvid()
        return mapOf(
            "user-agent" to USER_AGENT,
            "referer" to "https://live.bilibili.com/",
            "cookie" to "buvid3=$buvid3;buvid4=$buvid4;",
        )
    }

    private suspend fun ensureBuvid() {
        if (buvid3.isNotBlank() && buvid4.isNotBlank()) return
        runCatching {
            val data = http.getJson("https://api.bilibili.com/x/frontend/finger/spi").obj("data").orEmpty()
            buvid3 = data.str("b_3")
            buvid4 = data.str("b_4")
        }
        if (buvid3.isBlank()) buvid3 = ""
        if (buvid4.isBlank()) buvid4 = ""
    }

    private suspend fun signedParams(params: LinkedHashMap<String, String>): LinkedHashMap<String, String> {
        ensureWbiKeys()
        val mixin = BilibiliWbiSigner.mixinKey(imgKey + subKey)
        return BilibiliWbiSigner.signQuery(params, mixin, System.currentTimeMillis() / 1000)
    }

    private suspend fun ensureWbiKeys() {
        if (imgKey.isNotBlank() && subKey.isNotBlank()) return
        val data = http.getJson(
            "https://api.bilibili.com/x/web-interface/nav",
            headers = headers(),
        ).obj("data").orEmpty().obj("wbi_img").orEmpty()

        imgKey = data.str("img_url").substringAfterLast('/').substringBefore('.')
        subKey = data.str("sub_url").substringAfterLast('/').substringBefore('.')
    }

    private fun String.prefixHttps(): String = when {
        startsWith("//") -> "https:$this"
        else -> this
    }

    private fun String.stripHtml(): String = replace(Regex("<.*?>"), "")

    companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}

private fun kotlinx.serialization.json.JsonObject?.orEmpty(): kotlinx.serialization.json.JsonObject =
    this ?: kotlinx.serialization.json.buildJsonObject { }
