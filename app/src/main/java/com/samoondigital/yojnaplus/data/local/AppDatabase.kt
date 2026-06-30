package com.samoondigital.yojnaplus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.samoondigital.yojnaplus.data.local.dao.RecentSearchDao
import com.samoondigital.yojnaplus.data.local.dao.VoterResultDao
import com.samoondigital.yojnaplus.data.local.entity.RecentSearchEntity
import com.samoondigital.yojnaplus.data.local.entity.VoterResultEntity

@Database(
    entities = [RecentSearchEntity::class, VoterResultEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun voterResultDao(): VoterResultDao

    companion object {
        const val NAME = "yojnaplus.db"
    }
}
