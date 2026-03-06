package com.yhsrzbg.live_tv.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yhsrzbg.live_tv.core.SiteRegistry
import com.yhsrzbg.live_tv.data.db.FollowEntity
import com.yhsrzbg.live_tv.data.db.HistoryEntity
import com.yhsrzbg.live_tv.data.db.LiveTvDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LiveRepositoryFollowHistoryTest {
    @Test
    fun removeFollow_deletesItem() = runTest {
        val database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LiveTvDatabase::class.java,
        ).build()
        val repository = LiveRepository(
            database = database,
            registry = SiteRegistry.fromSites(emptyList()),
        )
        val id = "site-room-1"
        repository.upsertFollow(
            FollowEntity(
                id = id,
                siteId = "site",
                roomId = "room-1",
                userName = "u",
                face = "",
            )
        )

        assertTrue(repository.followExists(id))
        repository.removeFollow(id)
        assertFalse(repository.followExists(id))
        database.close()
    }

    @Test
    fun clearHistory_removesAllItems() = runTest {
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
                siteId = "site",
                roomId = "r1",
                userName = "u1",
                face = "",
            )
        )
        repository.addHistory(
            HistoryEntity(
                id = "2",
                siteId = "site",
                roomId = "r2",
                userName = "u2",
                face = "",
            )
        )
        assertEquals(2, repository.history().first().size)

        repository.clearHistory()

        assertEquals(0, repository.history().first().size)
        database.close()
    }
}
