package com.samoondigital.yojnaplus.core.analytics

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.samoondigital.yojnaplus.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

internal sealed interface AppscopeDelivery {
    data object Accepted : AppscopeDelivery
    data class Rejected(val status: Int) : AppscopeDelivery
    data class Retry(val notBefore: Long = 0L) : AppscopeDelivery
}

internal fun interface AppscopeSender {
    fun send(visit: AppscopeVisit): AppscopeDelivery
}

/** Uses its own HTTP client: ECI headers/interceptors must never reach this endpoint. */
internal class AppscopeHttpSender(
    private val endpoint: String,
    private val key: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .followRedirects(false)
        .followSslRedirects(false)
        .build(),
) : AppscopeSender {
    override fun send(visit: AppscopeVisit): AppscopeDelivery {
        val request = Request.Builder().url(endpoint)
            .header("X-Tracking-Key", key)
            .post(Json.encodeToString(visit).toRequestBody("application/json".toMediaType()))
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                when (response.code) {
                    200, 201 -> AppscopeDelivery.Accepted
                    408, 429 -> AppscopeDelivery.Retry(retryAt(response.header("Retry-After")))
                    in 500..599 -> AppscopeDelivery.Retry(retryAt(response.header("Retry-After")))
                    in 400..499 -> AppscopeDelivery.Rejected(response.code)
                    else -> AppscopeDelivery.Retry()
                }
            }
        } catch (_: IOException) {
            AppscopeDelivery.Retry()
        }
    }

    private fun retryAt(value: String?): Long {
        val now = System.currentTimeMillis()
        value?.toLongOrNull()?.let { seconds ->
            return now + seconds.coerceIn(0, 7 * 24 * 60 * 60).times(1000)
        }
        return value?.let {
            runCatching { ZonedDateTime.parse(it, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() }.getOrNull()
        }?.coerceAtLeast(now) ?: 0L
    }
}

internal class AppscopeRepository(
    internal val store: AppscopeStore,
    private val sender: AppscopeSender,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val syncMutex = Mutex()

    /** Serial delivery prevents concurrent foreground/worker drains; server event IDs cover process death. */
    suspend fun sync(): Boolean = withContext(Dispatchers.IO) {
        syncMutex.withLock {
            if (store.retryNotBefore() > now()) return@withLock false
            val startedAt = System.nanoTime()
            repeat(50) {
                val visit = store.pending().firstOrNull() ?: return@withLock true
                if (now() - visit.occurredAtMillis > AppscopeStore.MaxAgeMillis) {
                    store.remove(visit.eventId)
                } else {
                    when (val result = sender.send(visit)) {
                        AppscopeDelivery.Accepted -> {
                            store.remove(visit.eventId)
                            Log.d(Tag, "visit delivered")
                        }
                        is AppscopeDelivery.Rejected -> {
                            store.remove(visit.eventId)
                            Log.w(Tag, "visit rejected status=${result.status}")
                        }
                        is AppscopeDelivery.Retry -> {
                            store.deferUntil(result.notBefore)
                            return@withLock false
                        }
                    }
                }
                if (System.nanoTime() - startedAt > TimeUnit.MINUTES.toNanos(1)) return@withLock false
            }
            store.pending().isEmpty()
        }
    }

    companion object {
        const val Tag = "Appscope"
        @Volatile private var instance: AppscopeRepository? = null

        fun get(context: Context): AppscopeRepository = instance ?: synchronized(this) {
            instance ?: AppscopeRepository(
                AppscopeStore(PreferenceDataStoreFactory.create(
                    scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                    produceFile = { context.applicationContext.noBackupFilesDir.resolve("appscope.preferences_pb") },
                )),
                AppscopeHttpSender(BuildConfig.APPSCOPE_ENDPOINT, BuildConfig.APPSCOPE_TRACKING_KEY),
            ).also { instance = it }
        }
    }
}
