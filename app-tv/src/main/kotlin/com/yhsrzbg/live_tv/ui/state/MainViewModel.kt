package com.yhsrzbg.live_tv.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yhsrzbg.live_tv.core.model.LivePlayQuality
import com.yhsrzbg.live_tv.core.model.LiveRoomDetail
import com.yhsrzbg.live_tv.core.model.LiveRoomItem
import com.yhsrzbg.live_tv.data.LiveRepository
import com.yhsrzbg.live_tv.data.db.FollowEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val sites: List<String> = emptyList(),
)

data class RoomUiState(
    val detail: LiveRoomDetail? = null,
    val qualities: List<LivePlayQuality> = emptyList(),
    val selectedQuality: Int = 0,
    val streamUrl: String = "",
    val showControls: Boolean = false,
)

class MainViewModel(
    private val repository: LiveRepository,
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _roomState = MutableStateFlow(RoomUiState())
    val roomState: StateFlow<RoomUiState> = _roomState.asStateFlow()

    init {
        _homeState.value = HomeUiState(sites = repository.allSites().map { it.id })
    }

    suspend fun hot(siteId: String): List<LiveRoomItem> = repository.hotRooms(siteId).items

    suspend fun search(siteId: String, keyword: String): List<LiveRoomItem> = repository.search(siteId, keyword)

    fun loadRoom(siteId: String, roomId: String) {
        viewModelScope.launch {
            val detail = repository.roomDetail(siteId, roomId)
            val qualities = repository.playQualities(siteId, detail)
            val preferred = qualities.getOrNull(0)
            val stream = preferred?.let { repository.playUrls(siteId, detail, it).urls.firstOrNull().orEmpty() }.orEmpty()

            _roomState.value = RoomUiState(
                detail = detail,
                qualities = qualities,
                selectedQuality = 0,
                streamUrl = stream,
                showControls = false,
            )

            repository.addHistory(
                com.yhsrzbg.live_tv.data.db.HistoryEntity(
                    id = "$siteId-$roomId",
                    siteId = siteId,
                    roomId = roomId,
                    userName = detail.userName,
                    face = detail.userAvatar,
                )
            )
        }
    }

    fun toggleControls() {
        _roomState.update { it.copy(showControls = !it.showControls) }
    }

    fun setControls(show: Boolean) {
        _roomState.update { it.copy(showControls = show) }
    }

    fun followCurrent(siteId: String, roomId: String) {
        val detail = _roomState.value.detail ?: return
        viewModelScope.launch {
            repository.upsertFollow(
                FollowEntity(
                    id = "$siteId-$roomId",
                    siteId = siteId,
                    roomId = roomId,
                    userName = detail.userName,
                    face = detail.userAvatar,
                )
            )
        }
    }

    fun unfollowCurrent(siteId: String, roomId: String) {
        viewModelScope.launch {
            repository.removeFollow("$siteId-$roomId")
        }
    }
}
