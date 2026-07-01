package com.samoondigital.yojnaplus.feature.downloads.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadRecordDao {
    @Query("SELECT * FROM download_records ORDER BY downloadedAtMillis DESC")
    fun observeAll(): Flow<List<DownloadRecordEntity>>

    @Query("SELECT * FROM download_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DownloadRecordEntity?

    @Query("SELECT * FROM download_records")
    suspend fun getAllOnce(): List<DownloadRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: DownloadRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<DownloadRecordEntity>)

    @Update
    suspend fun update(record: DownloadRecordEntity)

    @Delete
    suspend fun delete(record: DownloadRecordEntity)
}
