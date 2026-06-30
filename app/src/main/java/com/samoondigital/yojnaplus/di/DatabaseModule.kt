package com.samoondigital.yojnaplus.di

import android.content.Context
import androidx.room.Room
import com.samoondigital.yojnaplus.data.local.AppDatabase
import com.samoondigital.yojnaplus.data.local.dao.RecentSearchDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideRecentSearchDao(db: AppDatabase): RecentSearchDao = db.recentSearchDao()
}
