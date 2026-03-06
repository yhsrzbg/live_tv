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
import kotlin.random.Random

class DouyuSite(
    private val http: CoreHttpClient = CoreHttpClient(),
) : LiveSite {
    override val id: String = "douyu"
    override val name: String = "Douyu"

    override fun danmaku(): LiveDanmaku = DouyuDanmaku()

    override suspend fun categories(): List<LiveCategory> {
        val root = http.getJson("https://m.douyu.com/api/cate/list")
        val data = root.obj("data").orEmpty()
        val subByParent = data.array("cate2Info")
            .mapNotNull { it.asObj() }
            .groupBy { it.str("cate1Id") }

        return data.array("cate1Info").mapNotNull { e ->
            val item = e.asObj() ?: return@mapNotNull null
            val parentId = item.str("cate1Id")
            LiveCategory(
                id = parentId,
                name = item.str("cate1Name"),
                children = subByParent[parentId].orEmpty().map { sub ->
                    LiveSubCategory(
                        id = sub.str("cate2Id"),
                        parentId = parentId,
                        name = sub.str("cate2Name"),
                        pic = sub.str("icon"),
                    )
                }
            )
        }
    }

    override suspend fun recommendRooms(page: Int): LiveCategoryResult {
        val root = http.getJson("https://www.douyu.com/japi/weblist/apinc/allpage/6/$page")
        return parseRoomList(root)
    }

    override suspend fun categoryRooms(categoryId: String, parentId: String, page: Int): LiveCategoryResult {
        val root = http.getJson("https://www.douyu.com/gapi/rkc/directory/mixList/2_${categoryId}/$page")
        return parseRoomList(root)
    }

    override suspend fun searchRooms(keyword: String, page: Int): LiveSearchRoomResult {
        val did = randomHex(32)
        val root = http.getJson(
            "https://www.douyu.com/japi/search/api/searchShow",
            query = mapOf("kw" to keyword, "page" to page.toString(), "pageSize" to "20"),
            headers = mapOf(
                "referer" to "https://www.douyu.com/search/",
                "cookie" to "dy_did=$did;acf_did=$did",
                "user-agent" to USER_AGENT,
            )
        )
        val items = root.obj("data")?.array("relateShow").orEmpty().mapNotNull { e ->
            val o = e.asObj() ?: return@mapNotNull null
            LiveRoomItem(
                roomId = o.str("rid"),
                title = o.str("roomName"),
                cover = o.str("roomSrc"),
                userName = o.str("nickName"),
                online = DouyuParsers.parseHotNum(o.str("hot")),
            )
        }
        return LiveSearchRoomResult(hasMore = items.isNotEmpty(), items = items)
    }

    override suspend fun searchAnchors(keyword: String, page: Int): LiveSearchAnchorResult {
        val did = randomHex(32)
        val root = http.getJson(
            "https://www.douyu.com/japi/search/api/searchUser",
            query = mapOf(
                "kw" to keyword,
                "page" to page.toString(),
                "pageSize" to "20",
                "filterType" to "1",
            ),
            headers = mapOf(
                "referer" to "https://www.douyu.com/search/",
                "cookie" to "dy_did=$did;acf_did=$did",
                "user-agent" to USER_AGENT,
            )
        )
        val items = root.obj("data")?.array("relateUser").orEmpty().mapNotNull { e ->
            val o = e.asObj() ?: return@mapNotNull null
            val anchor = o.obj("anchorInfo").orEmpty()
            val live = anchor.str("isLive").toIntOrNull() == 1
            val roomType = anchor.str("roomType").toIntOrNull() ?: 0
            LiveAnchorItem(
                roomId = anchor.str("rid"),
                avatar = anchor.str("avatar"),
                userName = anchor.str("nickName"),
                liveStatus = live && roomType == 0,
            )
        }
        return LiveSearchAnchorResult(
            hasMore = items.isNotEmpty(),
            items = items,
        )
    }

    override suspend fun roomDetail(roomId: String): LiveRoomDetail {
        val room = http.getJson(
            "https://www.douyu.com/swf_api/h5room/$roomId",
            headers = mapOf("referer" to "https://www.douyu.com/$roomId", "user-agent" to USER_AGENT)
        ).obj("data").orEmpty()

        val status = room.str("show_status") == "1"
        val title = room.str("room_name")
        return LiveRoomDetail(
            roomId = room.str("room_id").ifBlank { roomId },
            title = title,
            cover = room.str("room_src"),
            userName = room.str("nickname"),
            userAvatar = room.str("owner_avatar"),
            online = room.int("online"),
            status = status,
            url = "https://www.douyu.com/$roomId",
            introduction = room.str("show_details"),
            showTime = room.str("show_time").ifBlank { null },
            danmakuData = mapOf(
                "roomId" to room.str("room_id").ifBlank { roomId },
            ),
        )
    }

    override suspend fun playQualities(detail: LiveRoomDetail): List<LivePlayQuality> {
        val room = http.getJson(
            "https://www.douyu.com/swf_api/h5room/${detail.roomId}",
            headers = mapOf("referer" to "https://www.douyu.com/${detail.roomId}", "user-agent" to USER_AGENT)
        ).obj("data").orEmpty()

        val rates = room.array("multirates")
        if (rates.isEmpty()) {
            return listOf(LivePlayQuality(quality = "default", data = "default"))
        }
        return rates.mapNotNull { e ->
            val o = e.asObj() ?: return@mapNotNull null
            LivePlayQuality(
                quality = o.str("name").ifBlank { "rate-${o.str("type")}" },
                data = o.str("type").ifBlank { "0" },
            )
        }
    }

    override suspend fun playUrls(detail: LiveRoomDetail, quality: LivePlayQuality): LivePlayUrl {
        val room = http.getJson(
            "https://www.douyu.com/swf_api/h5room/${detail.roomId}",
            headers = mapOf("referer" to "https://www.douyu.com/${detail.roomId}", "user-agent" to USER_AGENT)
        ).obj("data").orEmpty()

        val direct = "${room.str("rtmp_url")}/${room.str("rtmp_live")}".trim('/')
        if (direct.isNotBlank()) {
            return LivePlayUrl(urls = listOf(direct), headers = mapOf("user-agent" to USER_AGENT))
        }

        val cdn = room.array("cdnsWithName").firstOrNull()?.asObj()?.str("cdn").orEmpty()
        val fallback = if (cdn.isNotBlank()) "https://$cdn.douyucdn2.cn/live/${detail.roomId}.m3u8" else ""
        return LivePlayUrl(
            urls = listOfNotNull(fallback.takeIf { it.isNotBlank() }),
            headers = mapOf("user-agent" to USER_AGENT, "referer" to "https://www.douyu.com/${detail.roomId}")
        )
    }

    override suspend fun liveStatus(roomId: String): Boolean {
        val room = http.getJson("https://www.douyu.com/swf_api/h5room/$roomId").obj("data").orEmpty()
        return room.str("show_status") == "1"
    }

    private fun parseRoomList(root: kotlinx.serialization.json.JsonObject): LiveCategoryResult {
        val data = root.obj("data").orEmpty()
        val list = data.array("rl")
        return LiveCategoryResult(
            hasMore = data.int("pgcnt") > data.int("nowPage"),
            items = list.mapNotNull { e ->
                val o = e.asObj() ?: return@mapNotNull null
                if (o.int("type") != 1) return@mapNotNull null
                LiveRoomItem(
                    roomId = o.str("rid"),
                    title = o.str("rn"),
                    cover = o.str("rs16"),
                    userName = o.str("nn"),
                    online = o.int("ol"),
                )
            }
        )
    }

    private fun randomHex(size: Int): String {
        val chars = "0123456789abcdef"
        return buildString(size) { repeat(size) { append(chars[Random.nextInt(chars.length)]) } }
    }

    companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}

private fun kotlinx.serialization.json.JsonObject?.orEmpty(): kotlinx.serialization.json.JsonObject =
    this ?: kotlinx.serialization.json.buildJsonObject { }
