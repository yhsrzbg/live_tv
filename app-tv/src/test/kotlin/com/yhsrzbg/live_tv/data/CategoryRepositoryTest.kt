package com.yhsrzbg.live_tv.data

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
import com.yhsrzbg.live_tv.core.model.LiveRoomItem
import com.yhsrzbg.live_tv.core.model.LiveSearchAnchorResult
import com.yhsrzbg.live_tv.core.model.LiveSearchRoomResult
import com.yhsrzbg.live_tv.data.db.LiveTvDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CategoryRepositoryTest {
    @Test
    fun categoryRooms_delegatesToSiteCategoryApi() = runTest {
        val expected = LiveCategoryResult(
            hasMore = false,
            items = listOf(
                LiveRoomItem(
                    roomId = "r-1",
                    title = "Title",
                    cover = "cover",
                    userName = "anchor",
                    online = 123,
                )
            ),
        )
        val fakeSite = FakeSite(result = expected)
        val database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LiveTvDatabase::class.java,
        ).build()
        val repository = LiveRepository(
            database = database,
            registry = SiteRegistry.fromSites(listOf(fakeSite)),
        )

        val result = repository.categoryRooms(
            siteId = "fake",
            categoryId = "cate",
            parentId = "parent",
            page = 2,
        )

        assertEquals(expected, result)
        assertEquals("cate", fakeSite.lastCategoryId)
        assertEquals("parent", fakeSite.lastParentId)
        assertEquals(2, fakeSite.lastPage)

        database.close()
    }
}

private class FakeSite(private val result: LiveCategoryResult) : LiveSite {
    override val id: String = "fake"
    override val name: String = "Fake"

    var lastCategoryId: String? = null
    var lastParentId: String? = null
    var lastPage: Int? = null

    override fun danmaku(): LiveDanmaku = object : LiveDanmaku {
        override val heartbeatTimeSeconds: Int = 0
        override val messages: Flow<LiveMessage> = emptyFlow<LiveMessage>()
        override suspend fun start(args: Map<String, String>) = Unit
        override suspend fun stop() = Unit
    }

    override suspend fun categories(): List<LiveCategory> = emptyList()

    override suspend fun recommendRooms(page: Int): LiveCategoryResult = LiveCategoryResult(false, emptyList())

    override suspend fun categoryRooms(categoryId: String, parentId: String, page: Int): LiveCategoryResult {
        lastCategoryId = categoryId
        lastParentId = parentId
        lastPage = page
        return result
    }

    override suspend fun searchRooms(keyword: String, page: Int): LiveSearchRoomResult =
        LiveSearchRoomResult(false, emptyList())

    override suspend fun searchAnchors(keyword: String, page: Int): LiveSearchAnchorResult =
        LiveSearchAnchorResult(false, emptyList<LiveAnchorItem>())

    override suspend fun roomDetail(roomId: String): LiveRoomDetail {
        error("Not used")
    }

    override suspend fun playQualities(detail: LiveRoomDetail): List<LivePlayQuality> = emptyList()

    override suspend fun playUrls(detail: LiveRoomDetail, quality: LivePlayQuality): LivePlayUrl = LivePlayUrl(emptyList())

    override suspend fun liveStatus(roomId: String): Boolean = false
}
