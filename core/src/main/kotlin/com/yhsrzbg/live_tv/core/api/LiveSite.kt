package com.yhsrzbg.live_tv.core.api

import com.yhsrzbg.live_tv.core.model.LiveAnchorItem
import com.yhsrzbg.live_tv.core.model.LiveCategory
import com.yhsrzbg.live_tv.core.model.LiveCategoryResult
import com.yhsrzbg.live_tv.core.model.LivePlayQuality
import com.yhsrzbg.live_tv.core.model.LivePlayUrl
import com.yhsrzbg.live_tv.core.model.LiveRoomDetail
import com.yhsrzbg.live_tv.core.model.LiveSearchAnchorResult
import com.yhsrzbg.live_tv.core.model.LiveSearchRoomResult

interface LiveSite {
    val id: String
    val name: String

    fun danmaku(): LiveDanmaku

    suspend fun categories(): List<LiveCategory>
    suspend fun recommendRooms(page: Int = 1): LiveCategoryResult
    suspend fun categoryRooms(categoryId: String, parentId: String, page: Int = 1): LiveCategoryResult
    suspend fun searchRooms(keyword: String, page: Int = 1): LiveSearchRoomResult
    suspend fun searchAnchors(keyword: String, page: Int = 1): LiveSearchAnchorResult
    suspend fun roomDetail(roomId: String): LiveRoomDetail
    suspend fun playQualities(detail: LiveRoomDetail): List<LivePlayQuality>
    suspend fun playUrls(detail: LiveRoomDetail, quality: LivePlayQuality): LivePlayUrl
    suspend fun liveStatus(roomId: String): Boolean
}
