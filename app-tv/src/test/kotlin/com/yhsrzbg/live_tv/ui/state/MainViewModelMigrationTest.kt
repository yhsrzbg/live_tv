package com.yhsrzbg.live_tv.ui.state

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yhsrzbg.live_tv.core.SiteRegistry
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
import com.yhsrzbg.live_tv.data.LiveRepository
import com.yhsrzbg.live_tv.data.db.LiveTvDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MainViewModelMigrationTest {
    @Test
    fun loadRoom_writesHistoryAndSelectsPreferredQuality() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fakeSite = MainViewModelMigrationFakeSite()
        val database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LiveTvDatabase::class.java,
        ).build()
        val repository = LiveRepository(
            database = database,
            registry = SiteRegistry.fromSites(listOf(fakeSite)),
        )
        val viewModel = MainViewModel(repository)

        viewModel.loadRoom(siteId = "fake", roomId = "room-1", preferredQualityLevel = 1)
        advanceUntilIdle()

        assertEquals("room-1", viewModel.roomState.value.detail?.roomId)
        assertEquals(1, viewModel.roomState.value.selectedQuality)
        assertEquals("url-720p", viewModel.roomState.value.streamUrl)
        assertEquals(listOf("fake-room-1"), repository.history().first().map { it.id })

        database.close()
        Dispatchers.resetMain()
    }
}

private class MainViewModelMigrationFakeSite : LiveSite {
    override val id: String = "fake"
    override val name: String = "Fake"

    override fun danmaku(): LiveDanmaku = object : LiveDanmaku {
        override val heartbeatTimeSeconds: Int = 0
        override val messages: Flow<LiveMessage> = emptyFlow<LiveMessage>()
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
        LiveSearchAnchorResult(false, emptyList<LiveAnchorItem>())

    override suspend fun roomDetail(roomId: String): LiveRoomDetail = LiveRoomDetail(
        roomId = roomId,
        title = "Title-$roomId",
        cover = "",
        userName = "anchor-$roomId",
        userAvatar = "avatar-$roomId",
        online = 100,
        status = true,
        url = "",
    )

    override suspend fun playQualities(detail: LiveRoomDetail): List<LivePlayQuality> = listOf(
        LivePlayQuality(quality = "360p", data = "360p"),
        LivePlayQuality(quality = "720p", data = "720p"),
    )

    override suspend fun playUrls(detail: LiveRoomDetail, quality: LivePlayQuality): LivePlayUrl =
        LivePlayUrl(urls = listOf("url-${quality.data}"))

    override suspend fun liveStatus(roomId: String): Boolean = true
}
