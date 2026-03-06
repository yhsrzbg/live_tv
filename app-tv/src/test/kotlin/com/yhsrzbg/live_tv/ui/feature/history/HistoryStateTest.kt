package com.yhsrzbg.live_tv.ui.feature.history

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yhsrzbg.live_tv.core.SiteRegistry
import com.yhsrzbg.live_tv.data.LiveRepository
import com.yhsrzbg.live_tv.data.db.HistoryEntity
import com.yhsrzbg.live_tv.data.db.LiveTvDatabase
import com.yhsrzbg.live_tv.ui.state.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class HistoryStateTest {
    @Test
    fun clearHistory_removesAllItems() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LiveTvDatabase::class.java,
        ).build()
        val repository = LiveRepository(
            database = database,
            registry = SiteRegistry.fromSites(emptyList()),
        )
        repository.addHistory(
            HistoryEntity(
                id = "1",
                siteId = "s",
                roomId = "r1",
                userName = "u1",
                face = "",
            )
        )
        repository.addHistory(
            HistoryEntity(
                id = "2",
                siteId = "s",
                roomId = "r2",
                userName = "u2",
                face = "",
            )
        )
        assertEquals(2, repository.history().first().size)

        val viewModel = MainViewModel(repository)
        viewModel.clearHistory()
        advanceUntilIdle()

        assertEquals(0, repository.history().first().size)
        database.close()
        Dispatchers.resetMain()
    }
}
