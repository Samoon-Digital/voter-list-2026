package com.samoondigital.yojnaplus.feature.downloads.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadDatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "yojna-plus.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDownloadRecordDao(database: AppDatabase): DownloadRecordDao =
        database.downloadRecordDao()
}
