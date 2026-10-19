package com.samoondigital.yojnaplus.core.analytics

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

class AppscopeSyncWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        if (AppscopeRepository.get(applicationContext).sync()) Result.success() else Result.retry()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        Log.w(AppscopeRepository.Tag, "sync deferred")
        Result.retry()
    }

    internal companion object {
        private val network = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "appscope-sync",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<AppscopeSyncWorker>()
                    .setConstraints(network)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 60, TimeUnit.SECONDS)
                    .build(),
            )
        }

        fun scheduleRecovery(context: Context) {
            // Recovers the small crash window between saving a visit and enqueueing its one-time job.
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "appscope-recovery",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<AppscopeSyncWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(network)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 60, TimeUnit.SECONDS)
                    .build(),
            )
        }
    }
}
