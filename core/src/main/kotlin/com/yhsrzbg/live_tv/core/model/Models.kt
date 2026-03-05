package com.yhsrzbg.live_tv.core.model

data class LiveCategory(
    val id: String,
    val name: String,
    val children: List<LiveSubCategory> = emptyList(),
)

data class LiveSubCategory(
    val id: String,
    val parentId: String,
    val name: String,
    val pic: String = "",
)

data class LiveCategoryResult(
    val hasMore: Boolean,
    val items: List<LiveRoomItem>,
)

data class LiveRoomItem(
    val roomId: String,
    val title: String,
    val cover: String,
    val userName: String,
    val online: Int,
)

data class LiveAnchorItem(
    val roomId: String,
    val avatar: String,
    val userName: String,
    val liveStatus: Boolean,
)

data class LiveSearchRoomResult(
    val hasMore: Boolean,
    val items: List<LiveRoomItem>,
)

data class LiveSearchAnchorResult(
    val hasMore: Boolean,
    val items: List<LiveAnchorItem>,
)

data class LiveRoomDetail(
    val roomId: String,
    val title: String,
    val cover: String,
    val userName: String,
    val userAvatar: String,
    val online: Int,
    val status: Boolean,
    val url: String,
    val introduction: String = "",
    val notice: String = "",
    val danmakuData: Map<String, String> = emptyMap(),
    val isRecord: Boolean = false,
    val showTime: String? = null,
)

data class LivePlayQuality(
    val quality: String,
    val data: String,
)

data class LivePlayUrl(
    val urls: List<String>,
    val headers: Map<String, String> = emptyMap(),
)

enum class LiveMessageType {
    Chat,
    Online,
    SuperChat,
}

data class LiveColor(
    val r: Int,
    val g: Int,
    val b: Int,
)

data class LiveMessage(
    val type: LiveMessageType,
    val message: String = "",
    val color: LiveColor = LiveColor(255, 255, 255),
    val data: Int = 0,
)
