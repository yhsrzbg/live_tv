package com.yhsrzbg.live_tv.ui.feature.follow

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yhsrzbg.live_tv.core.SiteRegistry
import com.yhsrzbg.live_tv.data.LiveRepository
import com.yhsrzbg.live_tv.data.db.FollowEntity
import com.yhsrzbg.live_tv.data.db.LiveTvDatabase
import com.yhsrzbg.live_tv.ui.state.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FollowStateTest {
    @Test
    fun follows_areOrderedByAddTimeDesc() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LiveTvDatabase::class.java,
        ).build()
        try {
            val repository = LiveRepository(
                database = database,
                registry = SiteRegistry.fromSites(emptyList()),
            )
            repository.upsertFollow(
                FollowEntity(
                    id = "old",
                    siteId = "s",
                    roomId = "1",
                    userName = "u1",
                    face = "",
                    addTime = 10L,
                )
            )
            repository.upsertFollow(
                FollowEntity(
                    id = "new",
                    siteId = "s",
                    roomId = "2",
                    userName = "u2",
                    face = "",
                    addTime = 20L,
                )
            )
            assertEquals(listOf("new", "old"), repository.followsSnapshot().map { it.id })
            val viewModel = MainViewModel(repository)
            viewModel.refreshFollows()
            advanceUntilIdle()
            assertEquals(listOf("new", "old"), viewModel.followState.value.items.map { it.id })
        } finally {
            Dispatchers.resetMain()
            database.close()
        }
    }
}
