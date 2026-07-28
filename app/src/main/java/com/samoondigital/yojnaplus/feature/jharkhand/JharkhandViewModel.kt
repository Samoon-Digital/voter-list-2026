package com.samoondigital.yojnaplus.feature.jharkhand

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

@HiltViewModel
class JharkhandViewModel @Inject constructor(
    private val repository: JharkhandRepository,
    private val pdfDownloadManager: PdfDownloadManager,
    private val downloadRepository: DownloadRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(JharkhandUiState())
    val state: StateFlow<JharkhandUiState> = _state.asStateFlow()

    private val events = Channel<JharkhandEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    init {
        loadDistricts()
    }

    fun selectDistrict(district: JharkhandDistrict) {
        _state.update {
            it.resetAfterDistrict().copy(
                step = JharkhandStep.Assembly,
                selectedDistrict = district,
                message = null,
            )
        }
        loadAssemblies(district)
    }

    fun selectAssembly(assembly: JharkhandAssembly) {
        val district = _state.value.selectedDistrict ?: return
        _state.update {
            it.resetAfterAssembly().copy(
                step = JharkhandStep.Part,
                selectedAssembly = assembly,
                message = null,
            )
        }
        loadParts(district, assembly)
    }

    fun selectPart(part: JharkhandPart) {
        val district = _state.value.selectedDistrict ?: return
        val assembly = _state.value.selectedAssembly ?: return
        _state.update {
            it.copy(
                step = JharkhandStep.Captcha,
                selectedPart = part,
                captchaInput = "",
                captchaImageBase64 = null,
                message = null,
            )
        }
        loadCaptcha(district, assembly, part)
    }

    fun updateCaptchaInput(value: String) {
        _state.update { it.copy(captchaInput = value.take(8), message = null) }
    }

    fun refreshCaptcha() {
        val current = _state.value
        val district = current.selectedDistrict ?: return
        val assembly = current.selectedAssembly ?: return
        val part = current.selectedPart ?: return
        viewModelScope.launch {
            runLoading("Refreshing captcha") {
                val captcha = repository.refreshCaptcha(district, assembly, part)
                _state.update { it.copy(captchaImageBase64 = captcha.imageBase64, captchaInput = "") }
            }
        }
    }

    fun confirmCaptcha() {
        val current = _state.value
        val district = current.selectedDistrict ?: return
        val assembly = current.selectedAssembly ?: return
        val part = current.selectedPart ?: return
        val captcha = current.captchaInput.trim()
        if (captcha.isBlank() || current.isDownloading) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isDownloading = true,
                    downloadProgress = 0,
                    message = "Verifying captcha",
                )
            }
            runCatching {
                val pdf = repository.downloadPdf(district, assembly, part, captcha)
                val recordId = downloadRepository.createPendingRecord(
                    district = district.name,
                    assembly = assembly.displayName,
                    village = part.displayName,
                    partNumber = part.partNumber,
                )
                downloadRepository.markDownloading(recordId)
                val downloaded = pdfDownloadManager.savePdfBytes(pdf.bytes, pdf.fileName) { progress ->
                    updateProgress(recordId, progress)
                }
                downloadRepository.markCompleted(recordId, downloaded.fileName, downloaded.uri)
                downloaded
            }.onSuccess { downloaded ->
                _state.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgress = 100,
                        message = "PDF downloaded successfully",
                        captchaInput = "",
                    )
                }
                events.send(JharkhandEvent.OpenPdf(downloaded.uri, downloaded.fileName))
            }.onFailure { error ->
                if (!isActive) return@launch
                _state.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgress = 0,
                        captchaInput = "",
                        message = error.userMessage("Download failed"),
                    )
                }
                refreshCaptcha()
            }
        }
    }

    fun goBack(): Boolean {
        val previous = when (_state.value.step) {
            JharkhandStep.District -> return false
            JharkhandStep.Assembly -> JharkhandStep.District
            JharkhandStep.Part -> JharkhandStep.Assembly
            JharkhandStep.Captcha -> JharkhandStep.Part
        }
        _state.update { it.copy(step = previous, message = null, isDownloading = false) }
        return true
    }

    private fun loadDistricts() = viewModelScope.launch {
        runLoading("Loading Jharkhand districts") {
            _state.update { it.copy(districts = repository.getDistricts()) }
        }
    }

    private fun loadAssemblies(district: JharkhandDistrict) = viewModelScope.launch {
        runLoading("Loading assemblies") {
            _state.update { it.copy(assemblies = repository.getAssemblies(district)) }
        }
    }

    private fun loadParts(district: JharkhandDistrict, assembly: JharkhandAssembly) = viewModelScope.launch {
        runLoading("Loading parts") {
            val parts = repository.getParts(district, assembly)
            _state.update {
                it.copy(
                    parts = parts,
                    message = if (parts.isEmpty()) "No parts found" else null,
                )
            }
        }
    }

    private fun loadCaptcha(
        district: JharkhandDistrict,
        assembly: JharkhandAssembly,
        part: JharkhandPart,
    ) = viewModelScope.launch {
        runLoading("Loading captcha") {
            val captcha = repository.prepareCaptcha(district, assembly, part)
            _state.update { it.copy(captchaImageBase64 = captcha.imageBase64, captchaInput = "") }
        }
    }

    private fun updateProgress(recordId: String, progress: Int) {
        val safeProgress = progress.coerceIn(0, 100)
        _state.update { it.copy(downloadProgress = safeProgress) }
        viewModelScope.launch { downloadRepository.updateProgress(recordId, safeProgress) }
    }

    private suspend fun runLoading(message: String?, block: suspend () -> Unit) {
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
                        message = error.userMessage("Request failed"),
                    )
                }
            }
    }

    private fun Throwable.userMessage(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback
}

enum class JharkhandStep {
    District,
    Assembly,
    Part,
    Captcha,
}

data class JharkhandUiState(
    val step: JharkhandStep = JharkhandStep.District,
    val districts: List<JharkhandDistrict> = emptyList(),
    val assemblies: List<JharkhandAssembly> = emptyList(),
    val parts: List<JharkhandPart> = emptyList(),
    val selectedDistrict: JharkhandDistrict? = null,
    val selectedAssembly: JharkhandAssembly? = null,
    val selectedPart: JharkhandPart? = null,
    val captchaImageBase64: String? = null,
    val captchaInput: String = "",
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val message: String? = null,
) {
    val stepNumber: Int
        get() = when (step) {
            JharkhandStep.District -> 1
            JharkhandStep.Assembly -> 2
            JharkhandStep.Part -> 3
            JharkhandStep.Captcha -> 4
        }

    fun resetAfterDistrict(): JharkhandUiState = copy(
        selectedAssembly = null,
        selectedPart = null,
        assemblies = emptyList(),
        parts = emptyList(),
        captchaImageBase64 = null,
        captchaInput = "",
    )

    fun resetAfterAssembly(): JharkhandUiState = copy(
        selectedPart = null,
        parts = emptyList(),
        captchaImageBase64 = null,
        captchaInput = "",
    )
}

sealed interface JharkhandEvent {
    data class OpenPdf(val uri: String, val title: String) : JharkhandEvent
}
