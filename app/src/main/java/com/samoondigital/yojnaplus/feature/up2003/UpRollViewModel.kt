package com.samoondigital.yojnaplus.feature.up2003

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samoondigital.yojnaplus.feature.downloads.data.DownloadRepository
import com.samoondigital.yojnaplus.pdf.PdfDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class UpRollViewModel @Inject constructor(
    private val repository: UpRollRepository,
    private val pdfDownloadManager: PdfDownloadManager,
    private val downloadRepository: DownloadRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UpRollUiState())
    val state: StateFlow<UpRollUiState> = _state.asStateFlow()

    private val events = Channel<UpRollEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    init {
        loadDistricts()
    }

    fun selectDistrict(district: UpDistrict) {
        _state.update {
            it.resetAfterDistrict().copy(
                step = UpRollStep.Assembly,
                selectedDistrict = district,
                message = null,
            )
        }
        loadAssemblies(district.id)
    }

    fun selectAssembly(assembly: UpAssembly) {
        val districtId = _state.value.selectedDistrict?.id ?: return
        _state.update {
            it.resetAfterAssembly().copy(
                step = UpRollStep.PollingStation,
                selectedAssembly = assembly,
                message = null,
            )
        }
        loadPollingStations(districtId, assembly.acNumber)
    }

    fun requestDownload(station: UpPollingStation) {
        if (_state.value.downloadingPartNumber != null) return
        _state.update {
            it.copy(
                selectedStation = station,
                captchaText = createCaptcha(),
                captchaInput = "",
                isCaptchaVisible = true,
                message = null,
            )
        }
    }

    fun updateCaptchaInput(value: String) {
        _state.update { it.copy(captchaInput = value.take(5).uppercase(), message = null) }
    }

    fun refreshCaptcha() {
        _state.update { it.copy(captchaText = createCaptcha(), captchaInput = "", message = null) }
    }

    fun dismissCaptcha() {
        _state.update {
            it.copy(
                isCaptchaVisible = false,
                selectedStation = null,
                captchaInput = "",
                captchaText = "",
            )
        }
    }

    fun confirmCaptcha() {
        val current = _state.value
        val station = current.selectedStation ?: return
        if (!current.captchaInput.equals(current.captchaText, ignoreCase = true)) {
            _state.update { it.copy(message = "Incorrect captcha. Try again.", captchaInput = "") }
            refreshCaptcha()
            return
        }
        _state.update {
            it.copy(
                isCaptchaVisible = false,
                captchaInput = "",
                captchaText = "",
            )
        }
        downloadStation(station)
    }

    fun goBack(): Boolean {
        val previous = when (_state.value.step) {
            UpRollStep.District -> return false
            UpRollStep.Assembly -> UpRollStep.District
            UpRollStep.PollingStation -> UpRollStep.Assembly
        }
        _state.update { it.copy(step = previous, message = null, isCaptchaVisible = false) }
        return true
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun loadDistricts() = viewModelScope.launch {
        runLoading("Loading Uttar Pradesh districts") {
            _state.update { it.copy(districts = repository.getDistricts()) }
        }
    }

    private fun loadAssemblies(districtId: String) = viewModelScope.launch {
        runLoading("Loading assemblies") {
            _state.update { it.copy(assemblies = repository.getAssemblies(districtId)) }
        }
    }

    private fun loadPollingStations(districtId: String, acNumber: Int) = viewModelScope.launch {
        runLoading("Loading polling stations") {
            val stations = repository.getPollingStations(districtId, acNumber)
            _state.update {
                it.copy(
                    pollingStations = stations,
                    message = if (stations.isEmpty()) "No polling stations found" else null,
                )
            }
        }
    }

    private fun downloadStation(station: UpPollingStation) = viewModelScope.launch {
        val selectedDistrict = _state.value.selectedDistrict ?: return@launch
        val selectedAssembly = _state.value.selectedAssembly ?: return@launch
        val recordId = downloadRepository.createPendingRecord(
            district = selectedDistrict.name,
            assembly = selectedAssembly.displayName,
            village = station.name,
            partNumber = station.partNumber,
        )

        _state.update {
            it.copy(
                downloadingPartNumber = station.partNumber,
                downloadProgress = 0,
                message = "Downloading Part ${station.partNumber}",
            )
        }
        downloadRepository.markDownloading(recordId)

        runCatching {
            pdfDownloadManager.downloadCdnPdf(station.pdfUrl) { progress ->
                updateProgress(recordId, station.partNumber, progress)
            }
        }.onSuccess { downloaded ->
            downloadRepository.markCompleted(recordId, downloaded.fileName, downloaded.uri)
            _state.update {
                it.copy(
                    downloadingPartNumber = null,
                    downloadProgress = 100,
                    message = "PDF downloaded successfully",
                )
            }
            events.send(UpRollEvent.OpenPdf(downloaded.uri, "UP 2003 Part ${station.partNumber}"))
        }.onFailure { error ->
            if (!isActive) return@launch
            val message = error.userMessage("Download failed")
            downloadRepository.markFailed(recordId, message)
            _state.update {
                it.copy(
                    downloadingPartNumber = null,
                    downloadProgress = 0,
                    message = message,
                )
            }
        }
    }

    private fun updateProgress(recordId: String, partNumber: Int, progress: Int) {
        _state.update {
            it.copy(
                downloadingPartNumber = partNumber,
                downloadProgress = progress.coerceIn(0, 100),
            )
        }
        viewModelScope.launch { downloadRepository.updateProgress(recordId, progress) }
    }

    private suspend fun runLoading(message: String?, block: suspend () -> Unit) {
        _state.update { it.copy(isLoading = true, message = message) }
        runCatching { block() }
            .onFailure { error ->
                _state.update { it.copy(message = error.userMessage("Request failed")) }
            }
        _state.update { it.copy(isLoading = false) }
    }

    private fun createCaptcha(): String =
        buildString {
            repeat(5) {
                append(CaptchaChars[Random.nextInt(CaptchaChars.length)])
            }
        }

    private fun Throwable.userMessage(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback

    private companion object {
        const val CaptchaChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}

enum class UpRollStep {
    District,
    Assembly,
    PollingStation,
}

data class UpRollUiState(
    val step: UpRollStep = UpRollStep.District,
    val districts: List<UpDistrict> = emptyList(),
    val assemblies: List<UpAssembly> = emptyList(),
    val pollingStations: List<UpPollingStation> = emptyList(),
    val selectedDistrict: UpDistrict? = null,
    val selectedAssembly: UpAssembly? = null,
    val selectedStation: UpPollingStation? = null,
    val isLoading: Boolean = false,
    val isCaptchaVisible: Boolean = false,
    val captchaText: String = "",
    val captchaInput: String = "",
    val downloadingPartNumber: Int? = null,
    val downloadProgress: Int = 0,
    val message: String? = null,
) {
    val stepNumber: Int
        get() = when (step) {
            UpRollStep.District -> 1
            UpRollStep.Assembly -> 2
            UpRollStep.PollingStation -> 3
        }

    fun resetAfterDistrict(): UpRollUiState = copy(
        selectedAssembly = null,
        selectedStation = null,
        assemblies = emptyList(),
        pollingStations = emptyList(),
        isCaptchaVisible = false,
    )

    fun resetAfterAssembly(): UpRollUiState = copy(
        selectedStation = null,
        pollingStations = emptyList(),
        isCaptchaVisible = false,
    )
}

sealed interface UpRollEvent {
    data class OpenPdf(val uri: String, val title: String) : UpRollEvent
}
