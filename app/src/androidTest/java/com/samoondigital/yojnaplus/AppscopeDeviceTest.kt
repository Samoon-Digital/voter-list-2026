package com.samoondigital.yojnaplus

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.samoondigital.yojnaplus.core.analytics.AppscopeDelivery
import com.samoondigital.yojnaplus.core.analytics.AppscopeRepository
import com.samoondigital.yojnaplus.core.analytics.AppscopeSender
import com.samoondigital.yojnaplus.core.analytics.AppscopeStore
import com.samoondigital.yojnaplus.core.analytics.AppscopeVisit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.Instant
import java.util.UUID

class AppscopeDeviceTest {
    private lateinit var file: File
    private var job = SupervisorJob()
    private lateinit var store: AppscopeStore
    private val time = Instant.parse("2026-10-19T10:00:00Z").toEpochMilli()

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        file = File(context.cacheDir, "appscope-test-${UUID.randomUUID()}.preferences_pb")
        openStore()
    }

    private fun openStore() {
        job = SupervisorJob()
        store = AppscopeStore(PreferenceDataStoreFactory.create(
            scope = CoroutineScope(job + Dispatchers.IO), produceFile = { file },
        ))
    }

    @After fun tearDown() = runBlocking {
        job.cancelAndJoin()
        file.delete()
        Unit
    }

    @Test fun concurrentOfflineVisitsPersistWithOneInstallationAndDistinctEvents() = runBlocking {
        coroutineScope { (1..25).map { async { store.record("test-app", time + it) } }.awaitAll() }
        val visits = store.pending()
        assertEquals(25, visits.size)
        assertEquals(1, visits.map { it.installationId }.distinct().size)
        assertEquals(25, visits.map { it.eventId }.distinct().size)
        job.cancelAndJoin()
        openStore()
        assertEquals(visits, store.pending())
    }

    @Test fun failedUploadKeepsOriginalIdentityEventAndOccurrenceTimeForRetry() = runBlocking {
        store.record("test-app", time)
        val original = store.pending().single()
        var attempts = 0
        val received = mutableListOf<AppscopeVisit>()
        val repository = AppscopeRepository(store, AppscopeSender {
            received += it
            if (attempts++ == 0) AppscopeDelivery.Retry() else AppscopeDelivery.Accepted
        }, now = { time + 1000 })
        assertFalse(repository.sync())
        assertEquals(listOf(original), store.pending())
        assertTrue(repository.sync())
        assertTrue(store.pending().isEmpty())
        assertEquals(listOf(original, original), received)
    }

    @Test fun rateLimitSurvivesRestartAndPreventsEarlyRequests() = runBlocking {
        store.record("test-app", time)
        var now = time
        val retryAt = time + 300_000
        val initial = AppscopeRepository(store, AppscopeSender { AppscopeDelivery.Retry(retryAt) }, now = { now })
        assertFalse(initial.sync())
        job.cancelAndJoin()
        openStore()
        var requests = 0
        val restarted = AppscopeRepository(store, AppscopeSender { requests++; AppscopeDelivery.Accepted }, now = { now })
        assertFalse(restarted.sync())
        assertEquals(0, requests)
        now = retryAt
        assertTrue(restarted.sync())
        assertEquals(1, requests)
    }

    @Test fun queueIsBoundedAndExpiredVisitsDoNotBlockNewActivity() = runBlocking {
        repeat(AppscopeStore.MaxPending) { assertTrue(store.record("test-app", time + it)) }
        val installation = store.pending().first().installationId
        assertFalse(store.record("test-app", time + 1000))
        assertEquals(AppscopeStore.MaxPending, store.pending().size)
        assertTrue(store.record("test-app", time + AppscopeStore.MaxAgeMillis + 1000))
        assertEquals(1, store.pending().size)
        assertEquals(installation, store.pending().single().installationId)
    }

    @Test fun rejectedAndExpiredEventsAreRemovedWithoutEndlessRetry() = runBlocking {
        store.record("test-app", time)
        val rejected = AppscopeRepository(store, AppscopeSender { AppscopeDelivery.Rejected(422) }, now = { time })
        assertTrue(rejected.sync())
        assertTrue(store.pending().isEmpty())
        store.record("test-app", time)
        val expired = AppscopeRepository(store, AppscopeSender { fail("Expired event must not be uploaded"); AppscopeDelivery.Accepted },
            now = { time + AppscopeStore.MaxAgeMillis + 1 })
        assertTrue(expired.sync())
        assertTrue(store.pending().isEmpty())
    }

    @Test fun concurrentDrainsDoNotUploadTheSameQueuedVisitTwice() = runBlocking {
        repeat(10) { store.record("test-app", time + it) }
        val sent = mutableListOf<String>()
        val repository = AppscopeRepository(store, AppscopeSender { sent += it.eventId; AppscopeDelivery.Accepted }, now = { time + 100 })
        coroutineScope { listOf(async { repository.sync() }, async { repository.sync() }).awaitAll() }
        assertEquals(10, sent.size)
        assertEquals(10, sent.distinct().size)
        assertTrue(store.pending().isEmpty())
    }
}
