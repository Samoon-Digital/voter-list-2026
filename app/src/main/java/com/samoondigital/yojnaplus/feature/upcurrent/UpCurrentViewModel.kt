package com.samoondigital.yojnaplus.feature.upcurrent

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
import kotlin.math.roundToInt

@HiltViewModel
class UpRuralVoterListViewModel @Inject constructor(
    repository: UpCurrentRepository,
    pdfDownloadManager: PdfDownloadManager,
    downloadRepository: DownloadRepository,
) : UpCurrentViewModel(
    repository = repository,
    pdfDownloadManager = pdfDownloadManager,
    downloadRepository = downloadRepository,
    initialFlow = UpCurrentFlow.Rural,
)

@HiltViewModel
class UpUrbanVoterListViewModel @Inject constructor(
    repository: UpCurrentRepository,
    pdfDownloadManager: PdfDownloadManager,
    downloadRepository: DownloadRepository,
) : UpCurrentViewModel(
    repository = repository,
    pdfDownloadManager = pdfDownloadManager,
    downloadRepository = downloadRepository,
    initialFlow = UpCurrentFlow.Urban,
)

open class UpCurrentViewModel(
    private val repository: UpCurrentRepository,
    private val pdfDownloadManager: PdfDownloadManager,
    private val downloadRepository: DownloadRepository,
    initialFlow: UpCurrentFlow,
) : ViewModel() {
    private val _state = MutableStateFlow(
        UpCurrentUiState(
            flow = initialFlow,
            step = if (initialFlow == UpCurrentFlow.Rural) UpCurrentStep.District else UpCurrentStep.UrbanBodyType,
        ),
    )
    val state: StateFlow<UpCurrentUiState> = _state.asStateFlow()

    private val events = Channel<UpCurrentEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    init {
        if (initialFlow == UpCurrentFlow.Rural) {
            repository.resetRuralSession()
            loadRuralDistricts()
        } else {
            repository.resetUrbanSession()
            loadUrbanBodyTypes()
        }
    }

    fun selectRuralDistrict(option: UpSecOption) {
        _state.update {
            it.copy(
                step = UpCurrentStep.Block,
                selectedDistrict = option,
                selectedBlock = null,
                selectedGramPanchayat = null,
                blocks = emptyList(),
                gramPanchayats = emptyList(),
                captchaBytes = null,
                captchaInput = "",
                message = null,
            )
        }
        viewModelScope.launch {
            runLoading("Loading blocks") {
                val blocks = repository.loadRuralBlocks(option)
                _state.update { it.copy(blocks = blocks, message = if (blocks.isEmpty()) "No blocks found" else null) }
            }
        }
    }

    fun selectRuralBlock(option: UpSecOption) {
        val district = _state.value.selectedDistrict ?: return
        _state.update {
            it.copy(
                step = UpCurrentStep.GramPanchayat,
                selectedBlock = option,
                selectedGramPanchayat = null,
                gramPanchayats = emptyList(),
                captchaBytes = null,
                captchaInput = "",
                message = null,
            )
        }
        viewModelScope.launch {
            runLoading("Loading gram panchayats") {
                val gramPanchayats = repository.loadRuralGramPanchayats(district, option)
                _state.update {
                    it.copy(
                        gramPanchayats = gramPanchayats,
                        message = if (gramPanchayats.isEmpty()) "No gram panchayats found" else null,
                    )
                }
            }
        }
    }

    fun selectRuralGramPanchayat(option: UpSecOption) {
        _state.update {
            it.copy(
                step = UpCurrentStep.Captcha,
                selectedGramPanchayat = option,
                captchaBytes = null,
                captchaInput = "",
                message = null,
            )
        }
        loadRuralCaptcha()
    }

    fun selectUrbanBodyType(option: UpSecOption) {
        _state.update {
            it.copy(
                step = UpCurrentStep.UrbanDistrict,
                selectedUrbanBodyType = option,
                selectedUrbanDistrict = null,
                selectedUrbanUlb = null,
                selectedUrbanWard = null,
                selectedUrbanDownloadOption = null,
                urbanDistricts = emptyList(),
                urbanUlbs = emptyList(),
                urbanWards = emptyList(),
                urbanDownloadOptions = emptyList(),
                captchaBytes = null,
                captchaInput = "",
                message = null,
            )
        }
        viewModelScope.launch {
            runLoading("Loading urban districts") {
                val districts = repository.loadUrbanDistricts(option)
                _state.update { it.copy(urbanDistricts = districts, message = if (districts.isEmpty()) "No districts found" else null) }
            }
        }
    }

    fun selectUrbanDistrict(option: UpSecOption) {
        val bodyType = _state.value.selectedUrbanBodyType ?: return
        _state.update {
            it.copy(
                step = UpCurrentStep.UrbanUlb,
                selectedUrbanDistrict = option,
                selectedUrbanUlb = null,
                selectedUrbanWard = null,
                selectedUrbanDownloadOption = null,
                urbanUlbs = emptyList(),
                urbanWards = emptyList(),
                urbanDownloadOptions = emptyList(),
                captchaBytes = null,
                captchaInput = "",
                message = null,
            )
        }
        viewModelScope.launch {
            runLoading("Loading urban local bodies") {
                val ulbs = repository.loadUrbanUlbs(bodyType, option)
                _state.update { it.copy(urbanUlbs = ulbs, message = if (ulbs.isEmpty()) "No urban local bodies found" else null) }
            }
        }
    }

    fun selectUrbanUlb(option: UpSecOption) {
        val current = _state.value
        val bodyType = current.selectedUrbanBodyType ?: return
        val district = current.selectedUrbanDistrict ?: return
        _state.update {
            it.copy(
                step = UpCurrentStep.UrbanWard,
                selectedUrbanUlb = option,
                selectedUrbanWard = null,
                selectedUrbanDownloadOption = null,
                urbanWards = emptyList(),
                urbanDownloadOptions = emptyList(),
                captchaBytes = null,
                captchaInput = "",
                message = null,
            )
        }
        viewModelScope.launch {
            runLoading("Loading wards") {
                val wards = repository.loadUrbanWards(bodyType, district, option)
                _state.update { it.copy(urbanWards = wards, message = if (wards.isEmpty()) "No wards found" else null) }
            }
        }
    }

    fun selectUrbanWard(option: UpSecOption) {
        _state.update {
            it.copy(
                step = UpCurrentStep.Captcha,
                selectedUrbanWard = option,
                selectedUrbanDownloadOption = null,
                urbanDownloadOptions = emptyList(),
                captchaBytes = null,
                captchaInput = "",
                message = null,
            )
        }
        loadUrbanCaptcha()
    }

    fun updateCaptchaInput(value: String) {
        _state.update {
            it.copy(
                captchaInput = value.filter { char -> char.code in 33..126 }.take(8),
                message = null,
            )
        }
    }

    fun refreshCaptcha() {
        if (_state.value.flow == UpCurrentFlow.Rural) {
            loadRuralCaptcha()
        } else {
            loadUrbanCaptcha()
        }
    }

    fun submitCaptcha() {
        val current = _state.value
        if (current.captchaInput.trim().isBlank() || current.isDownloading) {
            _state.update { it.copy(message = "Captcha enter karein.") }
            return
        }
        if (current.flow == UpCurrentFlow.Rural) {
            submitRuralCaptcha()
        } else {
            submitUrbanCaptcha()
        }
    }

    fun downloadUrbanPdf(option: UpUrbanDownloadOption) {
        val current = _state.value
        val district = current.selectedUrbanDistrict ?: return
        val ulb = current.selectedUrbanUlb ?: return
        val ward = current.selectedUrbanWard ?: return
        viewModelScope.launch {
            val recordId = downloadRepository.createPendingRecord(
                district = "Uttar Pradesh - ${district.label}",
                assembly = ulb.label,
                village = "${ward.label} - ${option.label}",
                partNumber = 1,
            )
            downloadRepository.markDownloading(recordId)
            _state.update {
                it.copy(
                    selectedUrbanDownloadOption = option,
                    isDownloading = true,
                    downloadProgress = 0,
                    message = "Downloading ${option.label}",
                )
            }
            runCatching {
                repository.downloadUrbanPdf(option) { downloadedBytes, totalBytes ->
                    updateProgress(recordId, downloadedBytes.progressPercent(totalBytes))
                }
            }.onSuccess { result ->
                when (result) {
                    is UpSubmitResult.Pdf -> saveDownloadedPdf(
                        recordId = recordId,
                        payload = result.pdf,
                        title = result.pdf.fileName,
                    )
                    is UpSubmitResult.ServerMessage -> {
                        downloadRepository.markFailed(recordId, result.message)
                        _state.update {
                            it.copy(
                                isDownloading = false,
                                selectedUrbanDownloadOption = null,
                                message = result.message,
                            )
                        }
                    }
                    is UpSubmitResult.UrbanDownloadOptions -> Unit
                }
            }.onFailure { error ->
                if (!isActive) return@launch
                val message = error.userMessage("Download failed")
                downloadRepository.markFailed(recordId, message)
                _state.update {
                    it.copy(
                        isDownloading = false,
                        selectedUrbanDownloadOption = null,
                        downloadProgress = 0,
                        message = message,
                    )
                }
            }
        }
    }

    fun goBack(): Boolean {
        val current = _state.value
        val previous = when (current.flow) {
            UpCurrentFlow.Rural -> when (current.step) {
                UpCurrentStep.District -> return false
                UpCurrentStep.Block -> UpCurrentStep.District
                UpCurrentStep.GramPanchayat -> UpCurrentStep.Block
                UpCurrentStep.Captcha -> UpCurrentStep.GramPanchayat
                else -> UpCurrentStep.District
            }
            UpCurrentFlow.Urban -> when (current.step) {
                UpCurrentStep.UrbanBodyType -> return false
                UpCurrentStep.UrbanDistrict -> UpCurrentStep.UrbanBodyType
                UpCurrentStep.UrbanUlb -> UpCurrentStep.UrbanDistrict
                UpCurrentStep.UrbanWard -> UpCurrentStep.UrbanUlb
                UpCurrentStep.Captcha -> UpCurrentStep.UrbanWard
                UpCurrentStep.UrbanPdfType -> UpCurrentStep.Captcha
                else -> UpCurrentStep.UrbanBodyType
            }
        }
        _state.update {
            it.copy(
                step = previous,
                isDownloading = false,
                message = null,
            )
        }
        return true
    }

    private fun loadRuralDistricts() = viewModelScope.launch {
        runLoading("Loading Uttar Pradesh districts") {
            val districts = repository.loadRuralDistricts()
            _state.update { it.copy(districts = districts, message = if (districts.isEmpty()) "No districts found" else null) }
        }
    }

    private fun loadUrbanBodyTypes() = viewModelScope.launch {
        runLoading("Loading urban body types") {
            val bodyTypes = repository.loadUrbanBodyTypes()
            _state.update { it.copy(urbanBodyTypes = bodyTypes, message = if (bodyTypes.isEmpty()) "No body types found" else null) }
        }
    }

    private fun loadRuralCaptcha() = viewModelScope.launch {
        runLoading("Loading captcha") {
            val captcha = repository.loadRuralCaptcha()
            _state.update { it.copy(captchaBytes = captcha, captchaInput = "") }
        }
    }

    private fun loadUrbanCaptcha() = viewModelScope.launch {
        runLoading("Loading captcha") {
            val captcha = repository.loadUrbanCaptcha()
            _state.update { it.copy(captchaBytes = captcha, captchaInput = "") }
        }
    }

    private fun submitRuralCaptcha() {
        val current = _state.value
        val district = current.selectedDistrict ?: return
        val block = current.selectedBlock ?: return
        val gramPanchayat = current.selectedGramPanchayat ?: return
        viewModelScope.launch {
            val recordId = downloadRepository.createPendingRecord(
                district = "Uttar Pradesh - ${district.label}",
                assembly = block.label,
                village = gramPanchayat.label,
                partNumber = 1,
            )
            downloadRepository.markDownloading(recordId)
            _state.update {
                it.copy(
                    isDownloading = true,
                    downloadProgress = 0,
                    message = "Verifying captcha",
                )
            }
            runCatching {
                repository.submitRural(
                    district = district,
                    block = block,
                    gramPanchayat = gramPanchayat,
                    captcha = current.captchaInput.trim(),
                ) { downloadedBytes, totalBytes ->
                    updateProgress(recordId, downloadedBytes.progressPercent(totalBytes))
                }
            }.onSuccess { result ->
                when (result) {
                    is UpSubmitResult.Pdf -> saveDownloadedPdf(recordId, result.pdf, result.pdf.fileName)
                    is UpSubmitResult.ServerMessage -> {
                        downloadRepository.markFailed(recordId, result.message)
                        _state.update {
                            it.copy(
                                isDownloading = false,
                                downloadProgress = 0,
                                captchaInput = "",
                                message = result.message,
                            )
                        }
                        loadRuralCaptcha()
                    }
                    is UpSubmitResult.UrbanDownloadOptions -> Unit
                }
            }.onFailure { error ->
                if (!isActive) return@launch
                val message = error.userMessage("Download failed")
                downloadRepository.markFailed(recordId, message)
                _state.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgress = 0,
                        captchaInput = "",
                        message = message,
                    )
                }
                loadRuralCaptcha()
            }
        }
    }

    private fun submitUrbanCaptcha() {
        val current = _state.value
        val bodyType = current.selectedUrbanBodyType ?: return
        val district = current.selectedUrbanDistrict ?: return
        val ulb = current.selectedUrbanUlb ?: return
        val ward = current.selectedUrbanWard ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isDownloading = false,
                    downloadProgress = 0,
                    message = "Verifying captcha",
                )
            }
            runCatching {
                repository.submitUrban(
                    bodyType = bodyType,
                    district = district,
                    ulb = ulb,
                    ward = ward,
                    captcha = current.captchaInput.trim(),
                )
            }.onSuccess { result ->
                when (result) {
                    is UpSubmitResult.Pdf -> {
                        val recordId = downloadRepository.createPendingRecord(
                            district = "Uttar Pradesh - ${district.label}",
                            assembly = ulb.label,
                            village = ward.label,
                            partNumber = 1,
                        )
                        downloadRepository.markDownloading(recordId)
                        saveDownloadedPdf(recordId, result.pdf, result.pdf.fileName)
                    }
                    is UpSubmitResult.UrbanDownloadOptions -> {
                        _state.update {
                            it.copy(
                                step = UpCurrentStep.UrbanPdfType,
                                urbanDownloadOptions = result.options,
                                selectedUrbanDownloadOption = null,
                                isLoading = false,
                                message = null,
                            )
                        }
                    }
                    is UpSubmitResult.ServerMessage -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                captchaBytes = null,
                                captchaInput = "",
                                message = result.message,
                            )
                        }
                        loadUrbanCaptcha()
                    }
                }
            }.onFailure { error ->
                if (!isActive) return@launch
                _state.update {
                    it.copy(
                        isLoading = false,
                        captchaBytes = null,
                        captchaInput = "",
                        message = error.userMessage("Verification failed"),
                    )
                }
                loadUrbanCaptcha()
            }
        }
    }

    private suspend fun saveDownloadedPdf(
        recordId: String,
        payload: UpPdfPayload,
        title: String,
    ) {
        _state.update { it.copy(isDownloading = true, isLoading = false, message = "Saving PDF") }
        val downloaded = pdfDownloadManager.savePdfBytes(payload.bytes, payload.fileName) { progress ->
            updateProgress(recordId, progress)
        }
        downloadRepository.markCompleted(recordId, downloaded.fileName, downloaded.uri)
        _state.update {
            it.copy(
                isLoading = false,
                isDownloading = false,
                downloadProgress = 100,
                captchaInput = "",
                message = "PDF downloaded successfully",
            )
        }
        events.send(UpCurrentEvent.OpenPdf(downloaded.uri, title))
    }

    private fun updateProgress(recordId: String, progress: Int) {
        _state.update { it.copy(downloadProgress = progress.coerceIn(0, 100)) }
        viewModelScope.launch { downloadRepository.updateProgress(recordId, progress.coerceIn(0, 100)) }
    }

    private suspend fun runLoading(message: String, block: suspend () -> Unit) {
        _state.update {
            it.copy(
                isLoading = true,
                downloadProgress = 0,
                message = message,
            )
        }
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

    private fun Long.progressPercent(totalBytes: Long?): Int {
        return totalBytes
            ?.takeIf { it > 0L }
            ?.let { total -> ((toDouble() / total.toDouble()) * 100.0).roundToInt() }
            ?.coerceIn(0, 100)
            ?: _state.value.downloadProgress
    }

    private fun Throwable.userMessage(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback
}
