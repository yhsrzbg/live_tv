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

class HuyaSite(
    private val http: CoreHttpClient = CoreHttpClient(),
) : LiveSite {
    override val id: String = "huya"
    override val name: String = "Huya"

    override fun danmaku(): LiveDanmaku = HuyaDanmaku()

    override suspend fun categories(): List<LiveCategory> {
        val top = listOf(
            LiveCategory(id = "1", name = "网游"),
            LiveCategory(id = "2", name = "单机"),
            LiveCategory(id = "8", name = "娱乐"),
            LiveCategory(id = "3", name = "手游"),
        )

        return top.map { parent ->
            val root = http.getJson(
                "https://live.cdn.huya.com/liveconfig/game/bussLive",
                query = mapOf("bussType" to parent.id)
            )
            val children = root.array("data").mapNotNull { e ->
                val o = e.asObj() ?: return@mapNotNull null
                val gid = o.str("gid").substringBefore(',').ifBlank { o.str("gid") }
                LiveSubCategory(
                    id = gid,
                    parentId = parent.id,
                    name = o.str("gameFullName"),
                    pic = o.str("gameImg"),
                )
            }
            parent.copy(children = children)
        }
    }

    override suspend fun recommendRooms(page: Int): LiveCategoryResult {
        val root = http.getJson(
            "https://www.huya.com/cache.php",
            query = mapOf("m" to "LiveList", "do" to "getLiveListByPage", "tagAll" to "0", "page" to page.toString())
        )
        return parseLiveList(root)
    }

    override suspend fun categoryRooms(categoryId: String, parentId: String, page: Int): LiveCategoryResult {
        val root = http.getJson(
            "https://www.huya.com/cache.php",
            query = mapOf(
                "m" to "LiveList",
                "do" to "getLiveListByPage",
                "tagAll" to "0",
                "gameId" to categoryId,
                "page" to page.toString(),
            )
        )
        return parseLiveList(root)
    }

    override suspend fun searchRooms(keyword: String, page: Int): LiveSearchRoomResult {
        val root = http.getJson(
            "https://search.cdn.huya.com/",
            query = mapOf(
                "m" to "Search",
                "do" to "getSearchContent",
                "q" to keyword,
                "uid" to "0",
                "v" to "4",
                "typ" to "-5",
                "livestate" to "0",
                "rows" to "20",
                "start" to ((page - 1) * 20).toString(),
            )
        )

        val docs = root.obj("response")?.obj("3")
        val items = docs?.array("docs").orEmpty().mapNotNull { e ->
            val o = e.asObj() ?: return@mapNotNull null
            LiveRoomItem(
                roomId = o.str("room_id"),
                title = o.str("game_introduction").ifBlank { o.str("game_roomName") },
                cover = o.str("game_screenshot"),
                userName = o.str("game_nick"),
                online = o.int("game_total_count"),
            )
        }
        val hasMore = docs?.int("numFound") ?: 0 > page * 20
        return LiveSearchRoomResult(hasMore = hasMore, items = items)
    }

    override suspend fun searchAnchors(keyword: String, page: Int): LiveSearchAnchorResult {
        val root = http.getJson(
            "https://search.cdn.huya.com/",
            query = mapOf(
                "m" to "Search",
                "do" to "getSearchContent",
                "q" to keyword,
                "uid" to "0",
                "v" to "1",
                "typ" to "-5",
                "livestate" to "0",
                "rows" to "20",
                "start" to ((page - 1) * 20).toString(),
            )
        )
        val docs = root.obj("response")?.obj("1")
        val items = docs?.array("docs").orEmpty().mapNotNull { e ->
            val o = e.asObj() ?: return@mapNotNull null
            LiveAnchorItem(
                roomId = o.str("room_id"),
                avatar = o.str("game_avatarUrl180"),
                userName = o.str("game_nick"),
                liveStatus = o.str("gameLiveOn").equals("true", true) || o.int("gameLiveOn") == 1,
            )
        }
        val hasMore = (docs?.int("numFound") ?: 0) > page * 20
        return LiveSearchAnchorResult(hasMore = hasMore, items = items)
    }

    override suspend fun roomDetail(roomId: String): LiveRoomDetail {
        val data = profile(roomId)
        val roomInfo = data.obj("liveData").orEmpty()
        val profile = data.obj("profileInfo").orEmpty()
        val (topSid, subSid) = fetchChannelIds(roomId)
        val ayyuid = roomInfo.int("lYyid").takeIf { it > 0 }
            ?: roomInfo.int("yyid").takeIf { it > 0 }
            ?: 0
        return LiveRoomDetail(
            roomId = roomInfo.str("profileRoom").ifBlank { roomId },
            title = roomInfo.str("introduction").ifBlank { roomInfo.str("roomName") },
            cover = roomInfo.str("screenshot"),
            userName = roomInfo.str("nick").ifBlank { profile.str("nick") },
            userAvatar = roomInfo.str("avatar180").ifBlank { profile.str("avatar180") },
            online = roomInfo.int("totalCount"),
            status = roomInfo.int("eLiveStatus") == 2 || roomInfo.str("liveStatus") == "ON",
            url = "https://www.huya.com/$roomId",
            introduction = roomInfo.str("introduction"),
            danmakuData = if (ayyuid > 0 && topSid > 0 && subSid > 0) {
                mapOf(
                    "ayyuid" to ayyuid.toString(),
                    "topSid" to topSid.toString(),
                    "subSid" to subSid.toString(),
                )
            } else {
                emptyMap()
            },
        )
    }

    override suspend fun playQualities(detail: LiveRoomDetail): List<LivePlayQuality> {
        val stream = profile(detail.roomId).obj("stream").orEmpty().obj("flv").orEmpty()
        val rates = stream.array("rateArray")
        if (rates.isEmpty()) {
            return listOf(LivePlayQuality("原画", "0"))
        }
        return rates.mapNotNull { e ->
            val o = e.asObj() ?: return@mapNotNull null
            LivePlayQuality(
                quality = o.str("sDisplayName").ifBlank { "原画" },
                data = o.str("iBitRate").ifBlank { "0" },
            )
        }
    }

    override suspend fun playUrls(detail: LiveRoomDetail, quality: LivePlayQuality): LivePlayUrl {
        val stream = profile(detail.roomId).obj("stream").orEmpty().obj("flv").orEmpty()
        val urls = stream.array("multiLine").mapNotNull { e ->
            val o = e.asObj() ?: return@mapNotNull null
            val url = o.str("url")
            if (quality.data != "0" && url.isNotBlank()) "$url&ratio=${quality.data}" else url
        }.distinct()

        return LivePlayUrl(
            urls = urls,
            headers = mapOf("user-agent" to USER_AGENT, "referer" to "https://www.huya.com/${detail.roomId}")
        )
    }

    override suspend fun liveStatus(roomId: String): Boolean {
        val data = profile(roomId).obj("liveData").orEmpty()
        return data.int("eLiveStatus") == 2 || data.str("liveStatus") == "ON"
    }

    private suspend fun profile(roomId: String) =
        http.getJson("https://mp.huya.com/cache.php", query = mapOf("m" to "Live", "do" to "profileRoom", "roomid" to roomId)).obj("data").orEmpty()

    private suspend fun fetchChannelIds(roomId: String): Pair<Int, Int> {
        val html = runCatching {
            http.getText(
                "https://m.huya.com/$roomId",
                headers = mapOf("user-agent" to MOBILE_USER_AGENT),
            )
        }.getOrDefault("")
        val topSid = Regex("lChannelId\"\\s*:\\s*([0-9]+)")
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: 0
        val subSid = Regex("lSubChannelId\"\\s*:\\s*([0-9]+)")
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: 0
        return topSid to if (subSid > 0) subSid else topSid
    }

    private fun parseLiveList(root: kotlinx.serialization.json.JsonObject): LiveCategoryResult {
        val data = root.obj("data").orEmpty()
        val page = data.int("page")
        val totalPage = data.int("totalPage")
        val items = data.array("datas").mapNotNull { e ->
            val o = e.asObj() ?: return@mapNotNull null
            LiveRoomItem(
                roomId = o.str("profileRoom"),
                title = o.str("introduction").ifBlank { o.str("roomName") },
                cover = o.str("screenshot"),
                userName = o.str("nick"),
                online = o.int("totalCount"),
            )
        }
        return LiveCategoryResult(hasMore = page < totalPage, items = items)
    }

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
        private const val MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
    }
}

private fun kotlinx.serialization.json.JsonObject?.orEmpty(): kotlinx.serialization.json.JsonObject =
    this ?: kotlinx.serialization.json.buildJsonObject { }
