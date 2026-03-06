package com.yhsrzbg.live_tv.core

import com.yhsrzbg.live_tv.core.api.LiveDanmaku
import com.yhsrzbg.live_tv.core.api.LiveSite
import com.yhsrzbg.live_tv.core.model.LiveAnchorItem
import com.yhsrzbg.live_tv.core.model.LiveCategory
import com.yhsrzbg.live_tv.core.model.LiveCategoryResult
import com.yhsrzbg.live_tv.core.model.LiveMessage
import com.yhsrzbg.live_tv.core.model.LivePlayQuality
import com.yhsrzbg.live_tv.core.model.LivePlayUrl
import com.yhsrzbg.live_tv.core.model.LiveRoomDetail
import com.yhsrzbg.live_tv.core.model.LiveSearchAnchorResult
import com.yhsrzbg.live_tv.core.model.LiveSearchRoomResult
import com.yhsrzbg.live_tv.core.model.LiveSuperChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LiveSiteContractTest {

    @Test
    fun `live site contract includes super chat messages`() = runBlocking {
        val site = FakeSite()
        val messages = site.superChatMessages("1000")
        assertEquals(1, messages.size)
        assertEquals("alice", messages.first().userName)
    }
}

private class FakeSite : LiveSite {
    override val id: String = "fake"
    override val name: String = "Fake"

    override fun danmaku(): LiveDanmaku = object : LiveDanmaku {
        override val heartbeatTimeSeconds: Int = 0
        override val messages: Flow<LiveMessage> = emptyFlow()
        override suspend fun start(args: Map<String, String>) = Unit
        override suspend fun stop() = Unit
    }

    override suspend fun categories(): List<LiveCategory> = emptyList()
    override suspend fun recommendRooms(page: Int): LiveCategoryResult = LiveCategoryResult(false, emptyList())
    override suspend fun categoryRooms(categoryId: String, parentId: String, page: Int): LiveCategoryResult =
        LiveCategoryResult(false, emptyList())

    override suspend fun searchRooms(keyword: String, page: Int): LiveSearchRoomResult =
        LiveSearchRoomResult(false, emptyList())

    override suspend fun searchAnchors(keyword: String, page: Int): LiveSearchAnchorResult =
        LiveSearchAnchorResult(false, emptyList())

    override suspend fun roomDetail(roomId: String): LiveRoomDetail = LiveRoomDetail(
        roomId = roomId,
        title = "",
        cover = "",
        userName = "",
        userAvatar = "",
        online = 0,
        status = false,
        url = "",
    )

    override suspend fun playQualities(detail: LiveRoomDetail): List<LivePlayQuality> = emptyList()
    override suspend fun playUrls(detail: LiveRoomDetail, quality: LivePlayQuality): LivePlayUrl = LivePlayUrl(emptyList())
    override suspend fun liveStatus(roomId: String): Boolean = false

    override suspend fun superChatMessages(roomId: String): List<LiveSuperChatMessage> {
        return listOf(
            LiveSuperChatMessage(
                userName = "alice",
                face = "",
                message = "hello",
                price = 30,
                startTime = 1_700_000_000_000,
                endTime = 1_700_000_060_000,
                backgroundColor = "#ff0000",
                backgroundBottomColor = "#000000",
            )
        )
    }
}

