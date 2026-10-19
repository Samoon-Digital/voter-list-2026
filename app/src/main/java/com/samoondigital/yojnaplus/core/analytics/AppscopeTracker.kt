package com.samoondigital.yojnaplus.core.analytics

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.samoondigital.yojnaplus.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Counts process foreground sessions, not Compose recompositions, screen changes or rotations. */
internal class AppscopeTracker private constructor(private val application: Application) : DefaultLifecycleObserver {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private val india = ZoneId.of("Asia/Kolkata")
    private var foreground = false
    private var lastDay: LocalDate? = null

    private val midnight = object : Runnable {
        override fun run() {
            if (!foreground) return
            val today = LocalDate.now(india)
            if (lastDay != today) {
                lastDay = today
                recordVisit()
            }
            scheduleMidnight()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        foreground = true
        lastDay = LocalDate.now(india)
        recordVisit()
        scheduleMidnight()
    }

    override fun onStop(owner: LifecycleOwner) {
        foreground = false
        handler.removeCallbacks(midnight)
    }

    private fun scheduleMidnight() {
        handler.removeCallbacks(midnight)
        val tomorrow = LocalDate.now(india).plusDays(1).atStartOfDay(india).toInstant().plusSeconds(1)
        handler.postDelayed(midnight, (tomorrow.toEpochMilli() - Instant.now().toEpochMilli()).coerceAtLeast(1000))
    }

    private fun recordVisit() {
        val occurredAt = System.currentTimeMillis()
        scope.launch {
            try {
                val repository = AppscopeRepository.get(application)
                if (!repository.store.record(BuildConfig.APPSCOPE_APP_ID, occurredAt)) {
                    Log.w(AppscopeRepository.Tag, "visit skipped reason=queue-full")
                    return@launch
                }
                Log.d(AppscopeRepository.Tag, "visit persisted")
                AppscopeSyncWorker.schedule(application)
                if (isOnline()) repository.sync()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                Log.w(AppscopeRepository.Tag, "visit deferred")
            }
        }
    }

    private fun isOnline(): Boolean {
        val connectivity = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivity.getNetworkCapabilities(connectivity.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    companion object {
        private var instance: AppscopeTracker? = null

        @Synchronized fun initialize(application: Application) {
            if (instance != null) return
            if (BuildConfig.APPSCOPE_TRACKING_KEY.isBlank()) {
                Log.w(AppscopeRepository.Tag, "tracking disabled: configure APPSCOPE_TRACKING_KEY")
                return
            }
            try {
                AppscopeSyncWorker.scheduleRecovery(application)
                AppscopeSyncWorker.schedule(application)
                instance = AppscopeTracker(application).also {
                    ProcessLifecycleOwner.get().lifecycle.addObserver(it)
                }
            } catch (_: Exception) {
                Log.w(AppscopeRepository.Tag, "tracking initialization deferred")
            }
        }
    }
}
