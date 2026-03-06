package com.yhsrzbg.live_tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.yhsrzbg.live_tv.data.LiveRepository
import com.yhsrzbg.live_tv.data.settings.SettingsStore
import com.yhsrzbg.live_tv.ui.feature.category.CategoryDetailScreen
import com.yhsrzbg.live_tv.ui.feature.category.CategoryScreen
import com.yhsrzbg.live_tv.ui.feature.follow.FollowScreen
import com.yhsrzbg.live_tv.ui.feature.history.HistoryScreen
import com.yhsrzbg.live_tv.ui.feature.home.HomeScreen
import com.yhsrzbg.live_tv.ui.feature.hot.HotScreen
import com.yhsrzbg.live_tv.ui.feature.search.SearchScreen
import com.yhsrzbg.live_tv.ui.feature.settings.SettingsActionHandler
import com.yhsrzbg.live_tv.ui.feature.settings.SettingsScreen
import com.yhsrzbg.live_tv.ui.navigation.Route
import com.yhsrzbg.live_tv.ui.screen.LiveRoomScreen
import com.yhsrzbg.live_tv.ui.state.MainViewModel

@Composable
fun LiveTvApp(
    repository: LiveRepository,
    settingsStore: SettingsStore,
) {
    val navController = rememberNavController()
    val vm: MainViewModel = viewModel(factory = MainViewModelFactory(repository))
    val home by vm.homeState.collectAsState()
    val room by vm.roomState.collectAsState()
    val follow by vm.followState.collectAsState()
    val history by vm.historyState.collectAsState()
    val danmakuEnabled by settingsStore.danmakuEnabled.collectAsState(initial = true)
    val qualityLevel by settingsStore.qualityLevel.collectAsState(initial = 1)
    val scaleMode by settingsStore.scaleMode.collectAsState(initial = 0)
    val playerCompatMode by settingsStore.playerCompatMode.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    val settingsActions = remember(settingsStore) {
        SettingsActionHandler(
            setDanmakuEnabled = { settingsStore.setDanmakuEnabled(it) },
            setQualityLevel = { settingsStore.setQualityLevel(it) },
            setScaleMode = { settingsStore.setScaleMode(it) },
            setPlayerCompatMode = { settingsStore.setPlayerCompatMode(it) },
        )
    }

    NavHost(navController = navController, startDestination = Route.Home.value) {
        composable(Route.Home.value) {
            HomeScreen(
                sites = home.sites,
                onOpenHot = { navController.navigate(Route.hot(it)) },
                onOpenCategory = { navController.navigate(Route.category(it)) },
                onOpenSearch = { navController.navigate(Route.search(it)) },
            )
        }
        composable(
            Route.Hot.value,
            arguments = listOf(navArgument("siteId") { type = NavType.StringType })
        ) { backStack ->
            val siteId = backStack.arguments?.getString("siteId").orEmpty()
            HotScreen(
                siteId = siteId,
                load = { vm.hot(siteId) },
                onOpenRoom = { roomId -> navController.navigate(Route.room(siteId, roomId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Route.Category.value,
            arguments = listOf(navArgument("siteId") { type = NavType.StringType })
        ) { backStack ->
            val siteId = backStack.arguments?.getString("siteId").orEmpty()
            CategoryScreen(
                siteId = siteId,
                load = { vm.categoryEntries(siteId) },
                onOpenCategoryDetail = { categoryId, parentId ->
                    navController.navigate(Route.categoryDetail(siteId, categoryId, parentId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Route.CategoryDetail.value,
            arguments = listOf(
                navArgument("siteId") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("parentId") { type = NavType.StringType },
            ),
        ) { backStack ->
            val siteId = backStack.arguments?.getString("siteId").orEmpty()
            val categoryId = backStack.arguments?.getString("categoryId").orEmpty()
            val parentId = backStack.arguments?.getString("parentId").orEmpty()
            CategoryDetailScreen(
                siteId = siteId,
                categoryId = categoryId,
                parentId = parentId,
                load = { vm.categoryRooms(siteId, categoryId, parentId) },
                onOpenRoom = { roomId -> navController.navigate(Route.room(siteId, roomId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            Route.Search.value,
            arguments = listOf(navArgument("siteId") { type = NavType.StringType })
        ) { backStack ->
            val siteId = backStack.arguments?.getString("siteId").orEmpty()
            SearchScreen(
                siteId = siteId,
                search = { keyword -> vm.search(siteId, keyword) },
                onOpenRoom = { roomId -> navController.navigate(Route.room(siteId, roomId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Route.LiveRoom.value,
            arguments = listOf(
                navArgument("siteId") { type = NavType.StringType },
                navArgument("roomId") { type = NavType.StringType },
            )
        ) { backStack ->
            val siteId = backStack.arguments?.getString("siteId").orEmpty()
            val roomId = backStack.arguments?.getString("roomId").orEmpty()
            LaunchedEffect(siteId, roomId) {
                vm.loadRoom(siteId, roomId)
            }
            LiveRoomScreen(
                siteId = siteId,
                roomId = roomId,
                state = room,
                settingsStore = settingsStore,
                onToggleControls = vm::toggleControls,
                onShowSettings = { vm.setControls(true) },
                onShowFollow = { vm.followCurrent(siteId, roomId) },
                onPrevChannel = {},
                onNextChannel = {},
                onBack = { navController.popBackStack() },
            )
        }
        composable(Route.Follow.value) {
            FollowScreen(
                items = follow.items,
                onOpenRoom = { siteId, roomId -> navController.navigate(Route.room(siteId, roomId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Route.History.value) {
            HistoryScreen(
                items = history.items,
                onClear = vm::clearHistory,
                onOpenRoom = { siteId, roomId -> navController.navigate(Route.room(siteId, roomId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Route.Settings.value) {
            SettingsScreen(
                danmakuEnabled = danmakuEnabled,
                qualityLevel = qualityLevel,
                scaleMode = scaleMode,
                playerCompatMode = playerCompatMode,
                onToggleDanmaku = { enabled -> scope.launch { settingsActions.onDanmakuToggleClicked(enabled) } },
                onSetQualityLevel = { level -> scope.launch { settingsActions.onQualityLevelSelected(level) } },
                onSetScaleMode = { mode -> scope.launch { settingsActions.onScaleModeSelected(mode) } },
                onSetPlayerCompatMode = { enabled ->
                    scope.launch { settingsActions.onPlayerCompatModeToggled(enabled) }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
