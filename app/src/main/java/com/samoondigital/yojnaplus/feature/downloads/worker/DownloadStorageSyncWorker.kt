package com.samoondigital.yojnaplus.feature.downloads.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.samoondigital.yojnaplus.feature.downloads.data.AppDatabase
import com.samoondigital.yojnaplus.feature.downloads.data.DownloadRepository

class DownloadStorageSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result =
        runCatching {
            val database = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "yojna-plus.db",
            ).build()
            try {
                DownloadRepository(applicationContext, database.downloadRecordDao())
                    .syncWithLocalStorage()
            } finally {
                database.close()
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )

    companion object {
        const val WORK_NAME = "download-storage-sync"
    }
}
