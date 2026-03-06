package com.yhsrzbg.live_tv.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY updateTime DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}

@Dao
interface FollowDao {
    @Query("SELECT * FROM follow ORDER BY addTime DESC")
    fun observeAll(): Flow<List<FollowEntity>>

    @Query("SELECT * FROM follow ORDER BY addTime DESC")
    suspend fun all(): List<FollowEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM follow WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: FollowEntity)

    @Query("DELETE FROM follow WHERE id = :id")
    suspend fun remove(id: String)
}
