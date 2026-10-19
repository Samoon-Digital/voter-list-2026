package com.samoondigital.yojnaplus

import android.content.Context
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.samoondigital.yojnaplus.api.EciGatewayApi
import com.samoondigital.yojnaplus.api.EciPublishedFileApi
import com.samoondigital.yojnaplus.feature.downloads.data.AppDatabase
import com.samoondigital.yojnaplus.feature.downloads.data.DownloadRepository
import com.samoondigital.yojnaplus.model.*
import com.samoondigital.yojnaplus.pdf.PdfDownloadManager
import com.samoondigital.yojnaplus.repository.ElectoralRollRepository
import com.samoondigital.yojnaplus.utils.EciCrypto
import com.samoondigital.yojnaplus.viewmodel.ElectoralRollStep
import com.samoondigital.yojnaplus.viewmodel.ElectoralRollViewModel
import java.time.LocalDate
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.*
import org.junit.Test

class SelectionRequestDeviceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun oldAssemblyResultCannotReplaceNewDistrictOrFinishItsLoading() = runBlocking {
        val old = DeferredResponse<JsonElement>()
        val latest = DeferredResponse<JsonElement>()
        val api = object : TestGateway() {
            override suspend fun getAssemblies(districtCd: String): JsonElement =
                if (districtCd == "old") old.await() else latest.await()
        }
        withViewModel(api) { vm ->
            main { vm.selectDistrict(DistrictDto("old")) }
            old.waitUntilStarted()
            main { vm.goBack(); vm.selectDistrict(DistrictDto("new")) }
            latest.waitUntilStarted()
            main { old.succeed(Json.parseToJsonElement("""[{"asmblyNo":1,"asmblyName":"Old"}]""")) }
            main {
                assertTrue(vm.state.value.isLoading)
                assertTrue(vm.state.value.assemblies.isEmpty())
                assertNull(vm.state.value.message)
            }
            main { latest.succeed(Json.parseToJsonElement("""[{"asmblyNo":2,"asmblyName":"New"}]""")) }
            val state = withTimeout(5_000) { vm.state.first { !it.isLoading } }
            assertEquals("new", state.selectedDistrict?.districtCd)
            assertEquals(listOf(2), state.assemblies.map { it.asmblyNo })
        }
    }

    @Test
    fun cancelledRequestErrorCannotReplaceNewSelectionMessage() = runBlocking {
        val old = DeferredResponse<JsonElement>()
        val api = object : TestGateway() {
            override suspend fun getAssemblies(districtCd: String): JsonElement =
                if (districtCd == "old") old.await() else JsonArray(emptyList())
        }
        withViewModel(api) { vm ->
            main { vm.selectDistrict(DistrictDto("old")) }
            old.waitUntilStarted()
            main { vm.goBack(); vm.selectDistrict(DistrictDto("new")) }
            main { old.fail(IllegalStateException("Obsolete server error")) }
            assertNull(vm.state.value.message)
            assertFalse(vm.state.value.isLoading)
        }
    }

    @Test
    fun oldYearResponseCannotPolluteNewStatesRollTypeCache() = runBlocking {
        val old = DeferredResponse<EciEnvelope<List<RollTypeDto>>>()
        val newRoll = RollTypeDto("new", rollTypeRefId = "new", pdfGenType = "test", displayName = "New roll")
        var calls = 0
        val api = object : TestGateway() {
            override suspend fun getPublishErollType(
                stateCd: String, year: String, misKey: String, acceptYek: String, acceptRotcev: String,
            ): EciEnvelope<List<RollTypeDto>> = if (++calls == 1) old.await() else EciEnvelope(payload = listOf(newRoll))
        }
        withViewModel(api) { vm ->
            main { vm.selectState(StateDto("old", "Old")) }
            old.waitUntilStarted()
            main { vm.goBack(); vm.selectState(StateDto("new", "New")) }
            withTimeout(5_000) { vm.state.first { !it.isLoading } }
            val oldRoll = newRoll.copy(id = "old", rollTypeRefId = "old", displayName = "Old roll")
            main { old.succeed(EciEnvelope(payload = listOf(oldRoll))) }
            main { vm.selectYear(LocalDate.now().year) }
            assertEquals(listOf(newRoll), vm.state.value.rollTypes)
            assertEquals("new", vm.state.value.selectedState?.stateCd)
        }
    }

    @Test
    fun backingOutOfPartsCancelsLanguagesWithoutStartingOldPartRequest() = runBlocking {
        val old = DeferredResponse<EciEnvelope<Map<String, String>>>()
        var partCalls = 0
        val roll = RollTypeDto("roll", rollTypeRefId = "roll", pdfGenType = "test", displayName = "Roll")
        val api = object : TestGateway() {
            override suspend fun getPublishErollType(
                stateCd: String, year: String, misKey: String, acceptYek: String, acceptRotcev: String,
            ) = EciEnvelope(payload = listOf(roll))
            override suspend fun getAcLanguages(body: AcLanguageRequest) = old.await()
            override suspend fun getPublishPartList(body: EncryptedBody): EciEnvelope<List<PartDto>> {
                partCalls++
                return EciEnvelope(payload = listOf(PartDto(partNumber = 1, partName = "Old part")))
            }
        }
        withViewModel(api) { vm ->
            main { vm.selectState(StateDto("test", "Test")) }
            withTimeout(5_000) { vm.state.first { !it.isLoading } }
            main {
                vm.selectYear(LocalDate.now().year)
                vm.selectRollType(roll)
                vm.selectDistrict(DistrictDto("district"))
                vm.selectAssembly(AssemblyDto(1, "Old assembly"))
            }
            old.waitUntilStarted()
            main { vm.goBack(); old.succeed(EciEnvelope(payload = mapOf("ENG" to "English"))) }
            assertEquals(ElectoralRollStep.Assembly, vm.state.value.step)
            assertTrue(vm.state.value.languages.isEmpty())
            assertTrue(vm.state.value.parts.isEmpty())
            assertFalse(vm.state.value.isLoading)
            assertEquals(0, partCalls)
        }
    }

    @Test
    fun cancelledCaptchaFailureDoesNotEndNewCaptchaLoading() = runBlocking {
        val old = DeferredResponse<CaptchaEncryptedDto>()
        val latest = DeferredResponse<CaptchaEncryptedDto>()
        var calls = 0
        val api = object : TestGateway() {
            override suspend fun getCaptcha(purpose: String) = if (++calls == 1) old.await() else latest.await()
        }
        withViewModel(api) { vm ->
            main { vm.selectLanguage("ENG") }
            old.waitUntilStarted()
            main { vm.refreshCaptcha() }
            latest.waitUntilStarted()
            main { old.fail(IllegalStateException("Old captcha failure")) }
            assertTrue(vm.state.value.isCaptchaLoading)
            assertNull(vm.state.value.message)
            main { latest.fail(IllegalStateException("Current captcha failure")) }
            assertFalse(vm.state.value.isCaptchaLoading)
            assertEquals("Current captcha failure", vm.state.value.message)
        }
    }

    private suspend fun withViewModel(api: EciGatewayApi, block: suspend (ElectoralRollViewModel) -> Unit) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val json = Json { ignoreUnknownKeys = true }
        val files = object : EciPublishedFileApi {
            override suspend fun getPublishedFile(fileId: String): PublishedFileDto = error("Not used")
        }
        val store = ViewModelStore()
        lateinit var vm: ElectoralRollViewModel
        main {
            vm = ElectoralRollViewModel(
                ElectoralRollRepository(api, files, EciCrypto(json), json), PdfDownloadManager(context),
                DownloadRepository(context, database.downloadRecordDao()),
            )
            store.put("test", vm)
        }
        try { block(vm) } finally { main { store.clear() }; database.close() }
    }

    private fun main(block: () -> Unit) = instrumentation.runOnMainSync(block)

    // Deliberately ignores Job cancellation, like a callback that has already reached the main queue.
    private class DeferredResponse<T> {
        private val started = CompletableDeferred<Unit>()
        private lateinit var continuation: Continuation<T>
        suspend fun await(): T = suspendCoroutine {
            continuation = it
            started.complete(Unit)
        }
        suspend fun waitUntilStarted() = withTimeout(5_000) { started.await() }
        fun succeed(value: T) = continuation.resume(value)
        fun fail(error: Exception) = continuation.resumeWithException(error)
    }

    private open class TestGateway : EciGatewayApi {
        override suspend fun getStates(): JsonElement = JsonArray(emptyList())
        override suspend fun getDistricts(stateCd: String): JsonElement = JsonArray(emptyList())
        override suspend fun getAssemblies(districtCd: String): JsonElement = JsonArray(emptyList())
        override suspend fun getCaptcha(purpose: String): CaptchaEncryptedDto = error("Not used")
        override suspend fun getPublishErollType(
            stateCd: String, year: String, misKey: String, acceptYek: String, acceptRotcev: String,
        ): EciEnvelope<List<RollTypeDto>> = EciEnvelope(payload = emptyList())
        override suspend fun getAcLanguages(body: AcLanguageRequest): EciEnvelope<Map<String, String>> = error("Not used")
        override suspend fun getPublishPartList(body: EncryptedBody): EciEnvelope<List<PartDto>> = error("Not used")
        override suspend fun generatePublishedPdfs(body: EncryptedBody): EciEnvelope<List<JsonElement>> = error("Not used")
    }
}
