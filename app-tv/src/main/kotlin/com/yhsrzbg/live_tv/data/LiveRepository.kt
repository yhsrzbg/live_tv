package com.yhsrzbg.live_tv.data

import com.yhsrzbg.live_tv.core.SiteRegistry
import com.yhsrzbg.live_tv.core.api.LiveSite
import com.yhsrzbg.live_tv.core.model.LiveCategoryResult
import com.yhsrzbg.live_tv.core.model.LivePlayQuality
import com.yhsrzbg.live_tv.core.model.LivePlayUrl
import com.yhsrzbg.live_tv.core.model.LiveRoomDetail
import com.yhsrzbg.live_tv.core.model.LiveRoomItem
import com.yhsrzbg.live_tv.data.db.FollowEntity
import com.yhsrzbg.live_tv.data.db.HistoryEntity
import com.yhsrzbg.live_tv.data.db.LiveTvDatabase
import kotlinx.coroutines.flow.Flow

class LiveRepository(
    private val database: LiveTvDatabase,
    private val registry: SiteRegistry = SiteRegistry.default(),
) {
    fun allSites(): List<LiveSite> = registry.supportedSites()

    fun site(siteId: String): LiveSite = requireNotNull(registry.site(siteId)) { "Unsupported site: $siteId" }

    suspend fun hotRooms(siteId: String, page: Int = 1): LiveCategoryResult = site(siteId).recommendRooms(page)

    suspend fun roomDetail(siteId: String, roomId: String): LiveRoomDetail = site(siteId).roomDetail(roomId)

    suspend fun playQualities(siteId: String, detail: LiveRoomDetail): List<LivePlayQuality> = site(siteId).playQualities(detail)

    suspend fun playUrls(siteId: String, detail: LiveRoomDetail, quality: LivePlayQuality): LivePlayUrl =
        site(siteId).playUrls(detail, quality)

    suspend fun search(siteId: String, keyword: String, page: Int = 1): List<LiveRoomItem> =
        site(siteId).searchRooms(keyword, page).items

    suspend fun categories(siteId: String) = site(siteId).categories()

    suspend fun categoryRooms(siteId: String, categoryId: String, parentId: String, page: Int = 1): LiveCategoryResult =
        site(siteId).categoryRooms(categoryId, parentId, page)

    fun follows(): Flow<List<FollowEntity>> = database.followDao().observeAll()

    suspend fun upsertFollow(item: FollowEntity) = database.followDao().upsert(item)

    suspend fun removeFollow(id: String) = database.followDao().remove(id)

    suspend fun followExists(id: String): Boolean = database.followDao().exists(id)

    fun history(): Flow<List<HistoryEntity>> = database.historyDao().observeAll()

    suspend fun addHistory(item: HistoryEntity) = database.historyDao().upsert(item)
}
