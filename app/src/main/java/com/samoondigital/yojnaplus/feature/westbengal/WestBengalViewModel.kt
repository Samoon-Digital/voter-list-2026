package com.samoondigital.yojnaplus.feature.westbengal

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
import kotlin.random.Random
import javax.inject.Inject

@HiltViewModel
class WestBengalViewModel @Inject constructor(
    private val repository: WestBengalRepository,
    private val pdfDownloadManager: PdfDownloadManager,
    private val downloadRepository: DownloadRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(WestBengalUiState())
    val state: StateFlow<WestBengalUiState> = _state.asStateFlow()

    private val events = Channel<WestBengalEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    init {
        loadDistricts()
    }

    fun selectDistrict(district: WestBengalDistrict) {
        _state.update {
            it.resetAfterDistrict().copy(
                step = WestBengalStep.Assembly,
                selectedDistrict = district,
                message = null,
            )
        }
        loadAssemblies(district)
    }

    fun selectAssembly(assembly: WestBengalAssembly) {
        _state.update {
            it.resetAfterAssembly().copy(
                step = WestBengalStep.PollingStation,
                selectedAssembly = assembly,
                message = null,
            )
        }
        loadParts(assembly)
    }

    fun selectPart(part: WestBengalPart) {
        if (_state.value.isDownloading) return
        _state.update {
            it.copy(
                step = WestBengalStep.Captcha,
                selectedPart = part,
                captchaCode = newCaptchaCode(),
                captchaInput = "",
                message = null,
            )
        }
    }

    fun updateCaptchaInput(value: String) {
        _state.update { it.copy(captchaInput = value.uppercase().take(5), message = null) }
    }

    fun refreshCaptcha() {
        if (_state.value.isDownloading) return
        _state.update { it.copy(captchaCode = newCaptchaCode(), captchaInput = "", message = null) }
    }

    fun confirmCaptcha() {
        val current = _state.value
        val district = current.selectedDistrict ?: return
        val assembly = current.selectedAssembly ?: return
        val part = current.selectedPart ?: return
        if (current.isDownloading) return
        if (current.captchaInput.trim() != current.captchaCode) {
            _state.update {
                it.copy(
                    captchaCode = newCaptchaCode(),
                    captchaInput = "",
                    message = "Incorrect captcha, try again.",
                )
            }
            return
        }
        downloadPart(district, assembly, part)
    }

    fun goBack(): Boolean {
        val previous = when (_state.value.step) {
            WestBengalStep.District -> return false
            WestBengalStep.Assembly -> WestBengalStep.District
            WestBengalStep.PollingStation -> WestBengalStep.Assembly
            WestBengalStep.Captcha -> WestBengalStep.PollingStation
        }
        _state.update { it.copy(step = previous, message = null, isDownloading = false) }
        return true
    }

    private fun loadDistricts() = viewModelScope.launch {
        runLoading("Loading West Bengal districts") {
            val districts = repository.getDistricts()
            _state.update {
                it.copy(
                    districts = districts,
                    message = if (districts.isEmpty()) "No districts found" else null,
                )
            }
        }
    }

    private fun loadAssemblies(district: WestBengalDistrict) = viewModelScope.launch {
        runLoading("Loading assemblies") {
            val assemblies = repository.getAssemblies(district)
            _state.update {
                it.copy(
                    assemblies = assemblies,
                    message = if (assemblies.isEmpty()) "No assemblies found" else null,
                )
            }
        }
    }

    private fun loadParts(assembly: WestBengalAssembly) = viewModelScope.launch {
        runLoading("Loading polling stations") {
            val parts = repository.getParts(assembly)
            _state.update {
                it.copy(
                    parts = parts,
                    message = if (parts.isEmpty()) "No polling stations found" else null,
                )
            }
        }
    }

    private fun downloadPart(
        district: WestBengalDistrict,
        assembly: WestBengalAssembly,
        part: WestBengalPart,
    ) = viewModelScope.launch {
        val recordId = downloadRepository.createPendingRecord(
            district = district.name,
            assembly = assembly.displayName,
            village = part.pollingStationName,
            partNumber = part.psNumber,
        )
        _state.update {
            it.copy(
                isDownloading = true,
                downloadProgress = 0,
                message = "Downloading PS ${part.psNumber}",
            )
        }
        downloadRepository.markDownloading(recordId)
        runCatching {
            pdfDownloadManager.downloadCdnPdf(
                cdnPath = repository.pdfUrl(part),
                fileNameOverride = readableFileName(district, assembly, part),
            ) { progress ->
                updateProgress(recordId, progress)
            }
        }.onSuccess { downloaded ->
            downloadRepository.markCompleted(recordId, downloaded.fileName, downloaded.uri)
            _state.update {
                it.copy(
                    isDownloading = false,
                    downloadProgress = 100,
                    captchaInput = "",
                    message = "PDF downloaded successfully",
                )
            }
            events.send(WestBengalEvent.OpenPdf(downloaded.uri, downloaded.fileName))
        }.onFailure { error ->
            if (!isActive) return@launch
            val message = error.message?.takeIf { it.isNotBlank() } ?: "Download failed"
            downloadRepository.markFailed(recordId, message)
            _state.update {
                it.copy(
                    isDownloading = false,
                    downloadProgress = 0,
                    captchaCode = newCaptchaCode(),
                    captchaInput = "",
                    message = message,
                )
            }
        }
    }

    private fun updateProgress(recordId: String, progress: Int) {
        val safeProgress = progress.coerceIn(0, 100)
        _state.update { it.copy(downloadProgress = safeProgress) }
        viewModelScope.launch { downloadRepository.updateProgress(recordId, safeProgress) }
    }

    private suspend fun runLoading(message: String, block: suspend () -> Unit) {
        _state.update { it.copy(isLoading = true, message = message) }
        runCatching { block() }
            .onSuccess {
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        message = current.message.takeUnless { it == message },
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = error.message?.takeIf { it.isNotBlank() } ?: "Request failed",
                    )
                }
            }
    }

    private fun readableFileName(
        district: WestBengalDistrict,
        assembly: WestBengalAssembly,
        part: WestBengalPart,
    ): String = "West Bengal 2002 - ${district.name} - ${assembly.displayName} - PS ${part.psNumber}.pdf"

    private fun newCaptchaCode(): String =
        buildString {
            repeat(5) { append(CAPTCHA_CHARS[Random.nextInt(CAPTCHA_CHARS.length)]) }
        }

    private companion object {
        const val CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}

enum class WestBengalStep {
    District,
    Assembly,
    PollingStation,
    Captcha,
}

data class WestBengalUiState(
    val step: WestBengalStep = WestBengalStep.District,
    val districts: List<WestBengalDistrict> = emptyList(),
    val assemblies: List<WestBengalAssembly> = emptyList(),
    val parts: List<WestBengalPart> = emptyList(),
    val selectedDistrict: WestBengalDistrict? = null,
    val selectedAssembly: WestBengalAssembly? = null,
    val selectedPart: WestBengalPart? = null,
    val captchaCode: String = "",
    val captchaInput: String = "",
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val message: String? = null,
) {
    val stepNumber: Int
        get() = when (step) {
            WestBengalStep.District -> 1
            WestBengalStep.Assembly -> 2
            WestBengalStep.PollingStation -> 3
            WestBengalStep.Captcha -> 4
        }

    fun resetAfterDistrict(): WestBengalUiState = copy(
        selectedAssembly = null,
        selectedPart = null,
        assemblies = emptyList(),
        parts = emptyList(),
        captchaCode = "",
        captchaInput = "",
    )

    fun resetAfterAssembly(): WestBengalUiState = copy(
        selectedPart = null,
        parts = emptyList(),
        captchaCode = "",
        captchaInput = "",
    )
}

sealed interface WestBengalEvent {
    data class OpenPdf(val uri: String, val title: String) : WestBengalEvent
}
