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

abstract class StubLiveSite(
    final override val id: String,
    final override val name: String,
) : LiveSite {

    override fun danmaku(): LiveDanmaku = NoopDanmaku()

    override suspend fun categories(): List<LiveCategory> {
        return listOf(
            LiveCategory(
                id = "1",
                name = "$name ����",
                children = listOf(
                    LiveSubCategory(id = "11", parentId = "1", name = "�Ƽ�"),
                    LiveSubCategory(id = "12", parentId = "1", name = "��Ϸ")
                )
            )
        )
    }

    override suspend fun recommendRooms(page: Int): LiveCategoryResult {
        return LiveCategoryResult(
            hasMore = page < 5,
            items = sampleRooms(page)
        )
    }

    override suspend fun categoryRooms(categoryId: String, parentId: String, page: Int): LiveCategoryResult {
        return LiveCategoryResult(
            hasMore = page < 5,
            items = sampleRooms(page)
        )
    }

    override suspend fun searchRooms(keyword: String, page: Int): LiveSearchRoomResult {
        return LiveSearchRoomResult(
            hasMore = page < 3,
            items = sampleRooms(page).filter { it.title.contains(keyword, ignoreCase = true) || keyword.isBlank() }
        )
    }

    override suspend fun searchAnchors(keyword: String, page: Int): LiveSearchAnchorResult {
        val anchors = sampleRooms(page).map {
            LiveAnchorItem(
                roomId = it.roomId,
                avatar = it.cover,
                userName = it.userName,
                liveStatus = true,
            )
        }.filter { it.userName.contains(keyword, ignoreCase = true) || keyword.isBlank() }

        return LiveSearchAnchorResult(hasMore = page < 3, items = anchors)
    }

    override suspend fun roomDetail(roomId: String): LiveRoomDetail {
        return LiveRoomDetail(
            roomId = roomId,
            title = "$name ֱ���� $roomId",
            cover = "",
            userName = "$name ����",
            userAvatar = "",
            online = 1024,
            status = true,
            url = "https://example.com/$id/$roomId",
        )
    }

    override suspend fun playQualities(detail: LiveRoomDetail): List<LivePlayQuality> {
        return listOf(
            LivePlayQuality("����", "uhd"),
            LivePlayQuality("����", "hd"),
            LivePlayQuality("����", "sd"),
        )
    }

    override suspend fun playUrls(detail: LiveRoomDetail, quality: LivePlayQuality): LivePlayUrl {
        return LivePlayUrl(
            urls = listOf("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"),
            headers = mapOf("User-Agent" to "LiveTV/1.0")
        )
    }

    override suspend fun liveStatus(roomId: String): Boolean = true

    private fun sampleRooms(page: Int): List<LiveRoomItem> {
        return (1..20).map {
            val seq = ((page - 1) * 20) + it
            LiveRoomItem(
                roomId = "$id-$seq",
                title = "$name ���� $seq",
                cover = "",
                userName = "$name ����$seq",
                online = 1000 + seq,
            )
        }
    }
}
