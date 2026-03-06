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
import com.yhsrzbg.live_tv.core.network.CoreHttpClient
import com.yhsrzbg.live_tv.core.util.array
import com.yhsrzbg.live_tv.core.util.asObj
import com.yhsrzbg.live_tv.core.util.int
import com.yhsrzbg.live_tv.core.util.obj
import com.yhsrzbg.live_tv.core.util.str
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import kotlinx.serialization.json.JsonObject

class DouyinSite(
    private val http: CoreHttpClient = CoreHttpClient(),
) : LiveSite {
    override val id: String = "douyin"
    override val name: String = "Douyin"

    override fun danmaku(): LiveDanmaku = DouyinDanmaku()

    override suspend fun categories(): List<LiveCategory> {
        val html = http.getText("https://live.douyin.com/", headers = defaultHeaders())
        val state = DouyinHtmlParser.extractState(html) ?: return fallbackCategories()
        val recommend = state.obj("homeStore")?.array("recommendCategoryData").orEmpty()

        val grouped = linkedMapOf<String, MutableList<LiveSubCategory>>()
        recommend.forEach { e ->
            val o = e.asObj() ?: return@forEach
            val partition = o.obj("partition") ?: return@forEach
            val first = o.obj("first_node") ?: partition
            val second = o.obj("second_node") ?: partition

            val parentId = "${first.str("id_str")},${first.int("type")}".ifBlank { "0,0" }
            val sub = LiveSubCategory(
                id = "${second.str("id_str")},${second.int("type")}".ifBlank { parentId },
                parentId = parentId,
                name = second.str("title").ifBlank { partition.str("title") },
                pic = "",
            )
            grouped.getOrPut(parentId) { mutableListOf() }.add(sub)
        }

        if (grouped.isEmpty()) return fallbackCategories()
        return grouped.map { (parentId, subs) ->
            val name = subs.firstOrNull()?.name ?: "推荐"
            LiveCategory(id = parentId, name = name, children = subs.distinctBy { it.id })
        }
    }

    override suspend fun recommendRooms(page: Int): LiveCategoryResult =
        categoryRooms(categoryId = "720", parentId = "1", page = page)

    override suspend fun categoryRooms(categoryId: String, parentId: String, page: Int): LiveCategoryResult {
        val url = signUrl(
            base = "https://live.douyin.com/webcast/web/partition/detail/room/v2/",
            params = linkedMapOf(
                "aid" to "6383",
                "app_name" to "douyin_web",
                "live_id" to "1",
                "device_platform" to "web",
                "language" to "zh-CN",
                "enter_from" to "link_share",
                "cookie_enabled" to "true",
                "screen_width" to "1980",
                "screen_height" to "1080",
                "browser_language" to "zh-CN",
                "browser_platform" to "Win32",
                "browser_name" to "Chrome",
                "browser_version" to "126.0.0.0",
                "browser_online" to "true",
                "count" to "15",
                "offset" to ((page - 1) * 15).toString(),
                "partition" to categoryId,
                "partition_type" to parentId,
                "req_from" to "2",
            )
        )
        val root = runCatching {
            http.getJson(url, headers = apiHeaders("https://live.douyin.com/"))
        }.getOrElse { return LiveCategoryResult(false, emptyList()) }

        val data = root.obj("data")?.array("data").orEmpty()
        val items = data.mapNotNull { e ->
            val o = e.asObj() ?: return@mapNotNull null
            val room = o.obj("room").orEmptyObj()
            val owner = room.obj("owner").orEmptyObj()
            LiveRoomItem(
                roomId = o.str("web_rid").ifBlank { owner.str("web_rid") },
                title = room.str("title"),
                cover = room.obj("cover")?.array("url_list")?.firstOrNull()?.toString()?.trim('"').orEmpty(),
                userName = owner.str("nickname"),
                online = parseDisplayNum(room.obj("room_view_stats")?.str("display_value").orEmpty()),
            )
        }
        return LiveCategoryResult(hasMore = data.size >= 15, items = items)
    }

    override suspend fun searchRooms(keyword: String, page: Int): LiveSearchRoomResult {
        val url = signUrl(
            base = "https://www.douyin.com/aweme/v1/web/live/search/",
            params = linkedMapOf(
                "device_platform" to "webapp",
                "aid" to "6383",
                "channel" to "channel_pc_web",
                "search_channel" to "aweme_live",
                "keyword" to keyword,
                "search_source" to "switch_tab",
                "query_correct_type" to "1",
                "is_filter_search" to "0",
                "from_group_id" to "",
                "offset" to ((page - 1) * 10).toString(),
                "count" to "10",
                "pc_client_type" to "1",
                "version_code" to "170400",
                "version_name" to "17.4.0",
                "cookie_enabled" to "true",
                "screen_width" to "1980",
                "screen_height" to "1080",
                "browser_language" to "zh-CN",
                "browser_platform" to "Win32",
                "browser_name" to "Chrome",
                "browser_version" to "126.0.0.0",
                "browser_online" to "true",
                "engine_name" to "Blink",
                "engine_version" to "126.0.0.0",
                "os_name" to "Windows",
                "os_version" to "10",
                "cpu_core_num" to "12",
                "device_memory" to "8",
                "platform" to "PC",
                "downlink" to "10",
                "effective_type" to "4g",
                "round_trip_time" to "100",
                "webid" to "7382872326016435738",
            )
        )
        val root = runCatching {
            http.getJson(
                url = url,
                headers = apiHeaders("https://www.douyin.com/search/${encode(keyword)}?type=live")
            )
        }.getOrElse { return LiveSearchRoomResult(false, emptyList()) }

        val list = root.array("data")
        val items = list.mapNotNull { e ->
            val item = e.asObj() ?: return@mapNotNull null
            val raw = item.obj("lives")?.str("rawdata").orEmpty()
            val rawObj = runCatching {
                kotlinx.serialization.json.Json.parseToJsonElement(raw) as? JsonObject
            }.getOrNull() ?: return@mapNotNull null

            val owner = rawObj.obj("owner").orEmptyObj()
            LiveRoomItem(
                roomId = owner.str("web_rid"),
                title = rawObj.str("title"),
                cover = rawObj.obj("cover")?.array("url_list")?.firstOrNull()?.toString()?.trim('"').orEmpty(),
                userName = owner.str("nickname"),
                online = parseDisplayNum(rawObj.obj("stats")?.str("total_user").orEmpty()),
            )
        }
        return LiveSearchRoomResult(hasMore = items.size >= 10, items = items)
    }

    override suspend fun searchAnchors(keyword: String, page: Int): LiveSearchAnchorResult =
        LiveSearchAnchorResult(hasMore = false, items = emptyList<LiveAnchorItem>())

    override suspend fun roomDetail(roomId: String): LiveRoomDetail {
        if (roomId.length <= 16) {
            fetchRoomDetailByApiWebRid(roomId)?.let { return it }
        } else {
            fetchRoomDetailByRoomId(roomId)?.let { return it }
        }

        val state = fetchRoomState(roomId)
        val roomStore = state.obj("roomStore").orEmptyObj()
        val roomInfo = roomStore.obj("roomInfo").orEmptyObj()
        val room = roomInfo.obj("room").orEmptyObj()
        val anchor = roomInfo.obj("anchor").orEmptyObj()
        val owner = room.obj("owner").orEmptyObj()

        val webRid = roomInfo.str("web_rid").ifBlank { roomId }
        val status = room.int("status") == 2

        val userId = state.obj("userStore")?.obj("odin")?.str("user_unique_id").orEmpty().ifBlank {
            randomNumeric(12)
        }
        return LiveRoomDetail(
            roomId = webRid,
            title = room.str("title").ifBlank { "Douyin Live $webRid" },
            cover = if (status) room.obj("cover")?.array("url_list")?.firstOrNull()?.toString()?.trim('"').orEmpty() else "",
            userName = if (status) owner.str("nickname") else anchor.str("nickname"),
            userAvatar = if (status) owner.obj("avatar_thumb")?.array("url_list")?.firstOrNull()?.toString()?.trim('"').orEmpty()
            else anchor.obj("avatar_thumb")?.array("url_list")?.firstOrNull()?.toString()?.trim('"').orEmpty(),
            online = room.obj("room_view_stats")?.str("display_value")?.toIntOrNull() ?: 0,
            status = status,
            url = "https://live.douyin.com/$webRid",
            introduction = owner.str("signature"),
            danmakuData = mapOf(
                "roomId" to room.str("id_str").ifBlank { webRid },
                "webRid" to webRid,
                "userId" to userId,
                "cookie" to DEFAULT_COOKIE,
            ),
        )
    }

    override suspend fun playQualities(detail: LiveRoomDetail): List<LivePlayQuality> {
        val state = fetchRoomState(detail.roomId)
        val room = state.obj("roomStore")?.obj("roomInfo")?.obj("room").orEmptyObj()
        val stream = room.obj("stream_url").orEmptyObj()

        val liveCore = stream.obj("live_core_sdk_data")
        val qualities = mutableListOf<LivePlayQuality>()

        liveCore?.obj("pull_data")?.obj("options")?.array("qualities")?.forEach { q ->
            val o = q.asObj() ?: return@forEach
            val key = o.str("sdk_key").ifBlank { o.str("name") }
            if (key.isNotBlank()) qualities += LivePlayQuality(quality = o.str("name").ifBlank { key }, data = key)
        }

        if (qualities.isNotEmpty()) return qualities.distinctBy { it.data }
        val m3u8 = DouyinHtmlParser.pickM3u8Candidates(stream.toString())
        return if (m3u8.isNotEmpty()) listOf(LivePlayQuality("default", "default")) else emptyList()
    }

    override suspend fun playUrls(detail: LiveRoomDetail, quality: LivePlayQuality): LivePlayUrl {
        val state = fetchRoomState(detail.roomId)
        val room = state.obj("roomStore")?.obj("roomInfo")?.obj("room").orEmptyObj()
        val stream = room.obj("stream_url").orEmptyObj()
        val urls = mutableListOf<String>()

        val pullData = stream.obj("live_core_sdk_data")?.obj("pull_data")
        val streamDataText = pullData?.str("stream_data").orEmpty()

        if (streamDataText.startsWith("{")) {
            runCatching {
                val data = (kotlinx.serialization.json.Json.parseToJsonElement(streamDataText)
                    as? kotlinx.serialization.json.JsonObject)
                    ?.obj("data")
                    ?.obj(quality.data)
                    ?.obj("main")
                data?.str("hls")?.takeIf { it.isNotBlank() }?.let { urls += it }
                data?.str("flv")?.takeIf { it.isNotBlank() }?.let { urls += it }
            }
        }

        stream.obj("hls_pull_url_map")?.let { map ->
            map.forEach { (_, v) -> v.toString().trim('"').takeIf { it.contains(".m3u8") }?.let { urls += it } }
        }
        stream.obj("flv_pull_url")?.let { map ->
            map.forEach { (_, v) -> v.toString().trim('"').takeIf { it.contains(".flv") || it.contains(".m3u8") }?.let { urls += it } }
        }

        if (urls.isEmpty()) {
            urls += DouyinHtmlParser.pickM3u8Candidates(stream.toString())
        }

        return LivePlayUrl(
            urls = urls.distinct(),
            headers = mapOf("user-agent" to USER_AGENT, "referer" to "https://live.douyin.com/"),
        )
    }

    override suspend fun liveStatus(roomId: String): Boolean = runCatching {
        fetchRoomDetailByApiWebRid(roomId)?.status ?: run {
            val state = fetchRoomState(roomId)
            state.obj("roomStore")?.obj("roomInfo")?.obj("room")?.int("status") == 2
        }
    }.getOrDefault(false)

    private suspend fun fetchRoomState(webRid: String): JsonObject {
        val html = http.getText("https://live.douyin.com/$webRid", headers = defaultHeaders())
        return DouyinHtmlParser.extractState(html) ?: DouyinHtmlParser.emptyState()
    }

    private fun fallbackCategories(): List<LiveCategory> = listOf(
        LiveCategory(id = "720,1", name = "推荐", children = listOf(LiveSubCategory("720,1", "720,1", "推荐"))),
    )

    private fun defaultHeaders(): Map<String, String> = mapOf(
        "user-agent" to USER_AGENT,
        "referer" to "https://live.douyin.com/",
        "cookie" to DEFAULT_COOKIE,
    )

    private fun apiHeaders(referer: String): Map<String, String> = mapOf(
        "user-agent" to USER_AGENT,
        "referer" to referer,
        "cookie" to DEFAULT_COOKIE,
        "accept" to "application/json, text/plain, */*",
        "accept-language" to "zh-CN,zh;q=0.9,en;q=0.8",
    )

    private fun buildUrl(base: String, params: Map<String, String>): String {
        val b = requireNotNull(base.toHttpUrlOrNull()) { "Invalid url: $base" }.newBuilder()
        params.forEach { (k, v) -> b.addQueryParameter(k, v) }
        return b.build().toString()
    }

    private fun signUrl(base: String, params: LinkedHashMap<String, String>): String {
        val raw = buildUrl(base, params)
        return runCatching { DouyinSignature.appendABogus(raw, USER_AGENT) }.getOrDefault(raw)
    }

    private suspend fun fetchRoomDetailByApiWebRid(webRid: String): LiveRoomDetail? {
        val url = signUrl(
            base = "https://live.douyin.com/webcast/room/web/enter/",
            params = linkedMapOf(
                "aid" to "6383",
                "app_name" to "douyin_web",
                "live_id" to "1",
                "device_platform" to "web",
                "language" to "zh-CN",
                "browser_language" to "zh-CN",
                "browser_platform" to "Win32",
                "browser_name" to "Chrome",
                "browser_version" to "126.0.0.0",
                "web_rid" to webRid,
            )
        )
        val root = runCatching { http.getJson(url, headers = apiHeaders("https://live.douyin.com/$webRid")) }.getOrNull()
            ?: return null
        val data = root.obj("data") ?: return null
        val roomData = data.array("data").firstOrNull()?.asObj() ?: return null
        val userData = data.obj("user").orEmptyObj()

        val roomStatus = roomData.int("status") == 2
        val owner = roomData.obj("owner").orEmptyObj()
        val roomIdReal = roomData.str("id_str").ifBlank { webRid }
        val userId = randomNumeric(12)
        return LiveRoomDetail(
            roomId = webRid,
            title = roomData.str("title"),
            cover = if (roomStatus) roomData.obj("cover")?.array("url_list")?.firstOrNull()?.toString()?.trim('"').orEmpty() else "",
            userName = if (roomStatus) owner.str("nickname") else userData.str("nickname"),
            userAvatar = if (roomStatus) owner.obj("avatar_thumb")?.array("url_list")?.firstOrNull()?.toString()?.trim('"').orEmpty()
            else userData.obj("avatar_thumb")?.array("url_list")?.firstOrNull()?.toString()?.trim('"').orEmpty(),
            online = if (roomStatus) parseDisplayNum(roomData.obj("room_view_stats")?.str("display_value").orEmpty()) else 0,
            status = roomStatus,
            url = "https://live.douyin.com/$webRid",
            introduction = owner.str("signature"),
            danmakuData = mapOf(
                "roomId" to roomIdReal,
                "webRid" to webRid,
                "userId" to userId,
                "cookie" to DEFAULT_COOKIE,
            ),
        )
    }

    private suspend fun fetchRoomDetailByRoomId(roomId: String): LiveRoomDetail? {
        val root = runCatching {
            http.getJson(
                url = "https://webcast.amemv.com/webcast/room/reflow/info/",
                query = mapOf(
                    "type_id" to "0",
                    "live_id" to "1",
                    "room_id" to roomId,
                    "sec_user_id" to "",
                    "version_code" to "99.99.99",
                    "app_id" to "6383",
                ),
                headers = apiHeaders("https://live.douyin.com/")
            )
        }.getOrNull() ?: return null

        val room = root.obj("data")?.obj("room") ?: return null
        val owner = room.obj("owner").orEmptyObj()
        val webRid = owner.str("web_rid").ifBlank { roomId }
        val roomStatus = room.int("status") == 2
        if (!roomStatus) return fetchRoomDetailByApiWebRid(webRid)

        val userId = randomNumeric(12)
        return LiveRoomDetail(
            roomId = webRid,
            title = room.str("title"),
            cover = room.obj("cover")?.array("url_list")?.firstOrNull()?.toString()?.trim('"').orEmpty(),
            userName = owner.str("nickname"),
            userAvatar = owner.obj("avatar_thumb")?.array("url_list")?.firstOrNull()?.toString()?.trim('"').orEmpty(),
            online = parseDisplayNum(room.obj("room_view_stats")?.str("display_value").orEmpty()),
            status = true,
            url = "https://live.douyin.com/$webRid",
            introduction = owner.str("signature"),
            danmakuData = mapOf(
                "roomId" to roomId,
                "webRid" to webRid,
                "userId" to userId,
                "cookie" to DEFAULT_COOKIE,
            ),
        )
    }

    private fun parseDisplayNum(raw: String): Int {
        val s = raw.trim()
        if (s.isEmpty()) return 0
        return try {
            if (s.endsWith("万")) (s.removeSuffix("万").toDouble() * 10_000).toInt()
            else s.toDouble().toInt()
        } catch (_: Throwable) {
            0
        }
    }

    companion object {
        const val USER_AGENT: String =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

        private const val DEFAULT_COOKIE =
            "ttwid=1%7CFM2vMfUxhN8dJx-SDih5txXr60UOL_EA4JO3r5fVCuw%7C1745038060%7C6446f46dcf416f8f8536763647f1f8c5f3322b3ce2f3675cc6fa0d13b7d950ff"
    }

    private fun randomNumeric(length: Int): String {
        val digits = "0123456789"
        return buildString(length) { repeat(length) { append(digits.random()) } }
    }

    private fun encode(value: String): String = java.net.URLEncoder.encode(value, Charsets.UTF_8).replace("+", "%20")
}

private fun JsonObject?.orEmptyObj(): JsonObject = this ?: kotlinx.serialization.json.buildJsonObject { }


