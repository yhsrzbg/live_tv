package com.yhsrzbg.live_tv.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HistoryEntity::class, FollowEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class LiveTvDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun followDao(): FollowDao
}
