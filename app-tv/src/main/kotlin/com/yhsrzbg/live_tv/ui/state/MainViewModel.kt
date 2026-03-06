package com.yhsrzbg.live_tv.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yhsrzbg.live_tv.core.model.LivePlayQuality
import com.yhsrzbg.live_tv.core.model.LiveRoomDetail
import com.yhsrzbg.live_tv.core.model.LiveRoomItem
import com.yhsrzbg.live_tv.core.model.LiveSubCategory
import com.yhsrzbg.live_tv.data.LiveRepository
import com.yhsrzbg.live_tv.data.db.FollowEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

data class FollowItemUiState(
    val id: String,
    val siteId: String,
    val roomId: String,
    val userName: String,
    val face: String,
    val addTime: Long,
)

data class FollowUiState(
    val items: List<FollowItemUiState> = emptyList(),
)

data class HistoryItemUiState(
    val id: String,
    val siteId: String,
    val roomId: String,
    val userName: String,
    val face: String,
    val updateTime: Long,
)

data class HistoryUiState(
    val items: List<HistoryItemUiState> = emptyList(),
)

class MainViewModel(
    private val repository: LiveRepository,
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _roomState = MutableStateFlow(RoomUiState())
    val roomState: StateFlow<RoomUiState> = _roomState.asStateFlow()

    private val _followState = MutableStateFlow(FollowUiState())
    val followState: StateFlow<FollowUiState> = _followState.asStateFlow()

    private val _historyState = MutableStateFlow(HistoryUiState())
    val historyState: StateFlow<HistoryUiState> = _historyState.asStateFlow()

    init {
        _homeState.value = HomeUiState(sites = repository.allSites().map { it.id })
        refreshFollows()
        refreshHistory()
    }

    suspend fun hot(siteId: String): List<LiveRoomItem> = repository.hotRooms(siteId).items

    suspend fun categoryEntries(siteId: String): List<LiveSubCategory> {
        return repository.categories(siteId).flatMap { top ->
            if (top.children.isNotEmpty()) {
                top.children
            } else {
                listOf(
                    LiveSubCategory(
                        id = top.id,
                        parentId = top.id,
                        name = top.name,
                    )
                )
            }
        }
    }

    suspend fun categoryRooms(siteId: String, categoryId: String, parentId: String): List<LiveRoomItem> =
        repository.categoryRooms(siteId, categoryId, parentId).items

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
            refreshHistory()
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
            refreshFollows()
        }
    }

    fun unfollowCurrent(siteId: String, roomId: String) {
        viewModelScope.launch {
            repository.removeFollow("$siteId-$roomId")
            refreshFollows()
        }
    }

    fun refreshFollows() {
        viewModelScope.launch {
            val follows = repository.followsSnapshot()
            _followState.value = FollowUiState(
                items = follows.map { item ->
                    FollowItemUiState(
                        id = item.id,
                        siteId = item.siteId,
                        roomId = item.roomId,
                        userName = item.userName,
                        face = item.face,
                        addTime = item.addTime,
                    )
                }
            )
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            val history = repository.history().first()
            _historyState.value = HistoryUiState(
                items = history.map { item ->
                    HistoryItemUiState(
                        id = item.id,
                        siteId = item.siteId,
                        roomId = item.roomId,
                        userName = item.userName,
                        face = item.face,
                        updateTime = item.updateTime,
                    )
                }
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            refreshHistory()
        }
    }
}
