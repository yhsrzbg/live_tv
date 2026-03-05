package com.yhsrzbg.live_tv

import android.app.Application
import androidx.room.Room
import com.yhsrzbg.live_tv.data.db.LiveTvDatabase

class LiveTvApplication : Application() {
    lateinit var database: LiveTvDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            LiveTvDatabase::class.java,
            "live_tv.db"
        ).fallbackToDestructiveMigration().build()
    }
}
