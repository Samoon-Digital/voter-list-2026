package com.samoondigital.yojnaplus.core.analytics

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID

@Serializable
internal data class AppscopeVisit(
    @SerialName("app_id") val appId: String,
    @SerialName("installation_id") val installationId: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("occurred_at") val occurredAt: String,
) {
    val occurredAtMillis: Long get() = Instant.parse(occurredAt).toEpochMilli()
}

/** The installation identity and complete events share one atomic, no-backup DataStore. */
internal class AppscopeStore(private val dataStore: DataStore<Preferences>) {
    suspend fun record(appId: String, occurredAt: Long): Boolean {
        var recorded = false
        dataStore.edit { preferences ->
            val pending = decode(preferences).filter { occurredAt - it.occurredAtMillis <= MaxAgeMillis }
            if (pending.size < MaxPending) {
                val installation = preferences[InstallationKey] ?: UUID.randomUUID().toString()
                preferences[InstallationKey] = installation
                val visit = AppscopeVisit(appId, installation, UUID.randomUUID().toString(), Instant.ofEpochMilli(occurredAt).toString())
                preferences[QueueKey] = Json.encodeToString(pending + visit)
                recorded = true
            } else {
                preferences[QueueKey] = Json.encodeToString(pending)
            }
        }
        return recorded
    }

    suspend fun pending(): List<AppscopeVisit> = decode(dataStore.data.first())

    suspend fun remove(eventId: String) {
        dataStore.edit { preferences ->
            preferences[QueueKey] = Json.encodeToString(decode(preferences).filterNot { it.eventId == eventId })
        }
    }

    suspend fun retryNotBefore(): Long = dataStore.data.first()[RetryKey] ?: 0L

    suspend fun deferUntil(timestamp: Long) {
        dataStore.edit { it[RetryKey] = timestamp }
    }

    private fun decode(preferences: Preferences): List<AppscopeVisit> =
        preferences[QueueKey]?.let { Json.decodeFromString<List<AppscopeVisit>>(it) }.orEmpty()

    companion object {
        const val MaxPending = 500
        const val MaxAgeMillis = 7L * 24 * 60 * 60 * 1000
        private val InstallationKey = stringPreferencesKey("installation_id")
        private val QueueKey = stringPreferencesKey("pending_visits")
        private val RetryKey = longPreferencesKey("retry_not_before")
    }
}
