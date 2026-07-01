package com.samoondigital.yojnaplus.feature.downloads.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DownloadRecordEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadRecordDao(): DownloadRecordDao
}
