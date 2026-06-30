package com.samoondigital.yojnaplus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.samoondigital.yojnaplus.data.local.dao.RecentSearchDao
import com.samoondigital.yojnaplus.data.local.entity.RecentSearchEntity

@Database(
    entities = [RecentSearchEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchDao

    companion object {
        const val NAME = "yojnaplus.db"
    }
}
