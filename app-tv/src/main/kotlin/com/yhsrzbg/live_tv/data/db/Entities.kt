package com.yhsrzbg.live_tv.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val siteId: String,
    val roomId: String,
    val userName: String,
    val face: String,
    val updateTime: Long = Instant.now().toEpochMilli(),
)

@Entity(tableName = "follow")
data class FollowEntity(
    @PrimaryKey val id: String,
    val siteId: String,
    val roomId: String,
    val userName: String,
    val face: String,
    val addTime: Long = Instant.now().toEpochMilli(),
)
