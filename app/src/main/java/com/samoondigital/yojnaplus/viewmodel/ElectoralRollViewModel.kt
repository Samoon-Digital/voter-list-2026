package com.samoondigital.yojnaplus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samoondigital.yojnaplus.model.AssemblyDto
import com.samoondigital.yojnaplus.model.CaptchaData
import com.samoondigital.yojnaplus.model.DistrictDto
import com.samoondigital.yojnaplus.model.PartDto
import com.samoondigital.yojnaplus.model.RollTypeDto
import com.samoondigital.yojnaplus.model.StateDto
import com.samoondigital.yojnaplus.pdf.DownloadedPdf
import com.samoondigital.yojnaplus.pdf.PdfDownloadManager
import com.samoondigital.yojnaplus.repository.ElectoralRollRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ElectoralRollViewModel @Inject constructor(
    private val repository: ElectoralRollRepository,
    private val pdfDownloadManager: PdfDownloadManager,
) : ViewModel() {
    private val currentYear = LocalDate.now().year
    private val rollTypesByYear = mutableMapOf<Int, List<RollTypeDto>>()
    private var downloadJob: Job? = null

    private val _state = MutableStateFlow(ElectoralRollUiState())
    val state: StateFlow<ElectoralRollUiState> = _state.asStateFlow()

    init {
        loadStates()
    }

    fun loadStates() = viewModelScope.launch {
        runLoading("Loading states") {
            _state.update { it.copy(states = repository.getStates()) }
        }
    }

    fun selectState(state: StateDto) {
        rollTypesByYear.clear()
        _state.update {
            it.resetAfterState().copy(
                step = ElectoralRollStep.Year,
                selectedState = state,
                message = null,
            )
        }
        loadAvailableYears(state.stateCd)
    }

    fun selectYear(year: Int) {
        val rollTypes = rollTypesByYear[year].orEmpty()
        _state.update {
            it.resetAfterYear().copy(
                step = ElectoralRollStep.RollType,
                selectedYear = year,
                rollTypes = rollTypes,
                selectedRollType = null,
                message = if (rollTypes.isEmpty()) "No roll type found for $year" else null,
            )
        }
    }

    fun selectRollType(rollType: RollTypeDto) {
        val stateCd = _state.value.selectedState?.stateCd
        _state.update {
            it.resetAfterRollType().copy(
                step = ElectoralRollStep.District,
                selectedRollType = rollType,
                message = null,
            )
        }
        if (stateCd != null) {
            loadDistricts(stateCd)
        }
    }

    fun selectDistrict(district: DistrictDto) {
        _state.update {
            it.resetAfterDistrict().copy(
                step = ElectoralRollStep.Assembly,
                selectedDistrict = district,
                message = null,
            )
        }
        loadAssemblies(district.districtCd)
    }

    fun selectAssembly(assembly: AssemblyDto) {
        _state.update {
            it.resetAfterAssembly().copy(
                step = ElectoralRollStep.Parts,
                selectedAssembly = assembly,
                message = null,
            )
        }
        loadLanguagesAndParts()
    }

    fun togglePart(partNumber: Int) {
        _state.update { current ->
            val selected = current.selectedPartNumbers
            val updated = if (partNumber in selected) {
                selected - partNumber
            } else {
                if (selected.size >= 10) {
                    return@update current.copy(message = "Maximum 10 parts are allowed at once")
                }
                selected + partNumber
            }
            current.copy(selectedPartNumbers = updated, message = null)
        }
    }

    fun showLanguageSheet() {
        val current = _state.value
        when {
            current.selectedPartNumbers.isEmpty() ->
                _state.update { it.copy(message = "Please select at least one part") }
            current.languages.isEmpty() ->
                _state.update { it.copy(message = "Language list is not loaded yet") }
            else ->
                _state.update { it.copy(isLanguageSheetVisible = true, message = null) }
        }
    }

    fun dismissLanguageSheet() {
        _state.update { it.copy(isLanguageSheetVisible = false) }
    }

    fun selectLanguage(code: String) {
        _state.update {
            it.copy(
                selectedLanguageCode = code,
                isLanguageSheetVisible = false,
                step = ElectoralRollStep.Captcha,
                message = null,
            )
        }
        refreshCaptcha()
    }

    fun refreshCaptcha() = viewModelScope.launch {
        _state.update { it.copy(isCaptchaLoading = true, captchaInput = "", message = null) }
        runCatching { repository.getCaptcha() }
            .onSuccess { captcha ->
                _state.update { it.copy(captcha = captcha, isCaptchaLoading = false) }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isCaptchaLoading = false,
                        message = error.userMessage("Unable to load captcha"),
                    )
                }
            }
    }

    fun updateCaptchaInput(value: String) {
        _state.update { it.copy(captchaInput = value.take(8), message = null) }
    }

    fun startDownloads() {
        if (downloadJob?.isActive == true) return
        downloadJob = viewModelScope.launch {
            downloadSelectedPdfs(retryOnlyFailed = false)
        }
    }

    fun retryDownloads() {
        if (downloadJob?.isActive == true) return
        downloadJob = viewModelScope.launch {
            val hasFileIds = _state.value.downloadItems.any {
                it.status == DownloadStatus.Failed && it.fileId != null
            }
            if (hasFileIds) {
                retryFailedFileDownloads()
            } else {
                downloadSelectedPdfs(retryOnlyFailed = false)
            }
        }
    }

    fun cancelDownloads() = viewModelScope.launch {
        downloadJob?.cancelAndJoin()
        _state.update { current ->
            current.copy(
                isDownloading = false,
                message = "Downloads cancelled",
                downloadItems = current.downloadItems.map {
                    if (it.status == DownloadStatus.Waiting || it.status == DownloadStatus.Downloading) {
                        it.copy(status = DownloadStatus.Cancelled)
                    } else {
                        it
                    }
                },
            ).withDownloadSummary()
        }
    }

    fun openPdf(downloadedPdf: DownloadedPdf) {
        runCatching { pdfDownloadManager.openPdf(downloadedPdf) }
            .onFailure { error ->
                _state.update { it.copy(message = error.userMessage("No PDF viewer found")) }
            }
    }

    fun openDownloadedPdfs() {
        val first = _state.value.downloadedPdfs.firstOrNull()
        if (first == null) {
            _state.update { it.copy(message = "No downloaded PDF found") }
            return
        }
        openPdf(first)
    }

    fun goBack(): Boolean {
        val previous = when (_state.value.step) {
            ElectoralRollStep.State -> return false
            ElectoralRollStep.Year -> ElectoralRollStep.State
            ElectoralRollStep.RollType -> ElectoralRollStep.Year
            ElectoralRollStep.District -> ElectoralRollStep.RollType
            ElectoralRollStep.Assembly -> ElectoralRollStep.District
            ElectoralRollStep.Parts -> ElectoralRollStep.Assembly
            ElectoralRollStep.Captcha -> ElectoralRollStep.Parts
            ElectoralRollStep.Success -> ElectoralRollStep.Captcha
        }
        _state.update { it.copy(step = previous, isLanguageSheetVisible = false, message = null) }
        return true
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun loadAvailableYears(stateCd: String) = viewModelScope.launch {
        runLoading("Loading available revision years") {
            val available = mutableListOf<Int>()
            val candidates = (currentYear downTo currentYear - 5).toList()
            candidates.forEach { year ->
                runCatching { repository.getRollTypes(stateCd, year) }
                    .onSuccess { rollTypes ->
                        if (rollTypes.isNotEmpty()) {
                            rollTypesByYear[year] = rollTypes
                            available += year
                        }
                    }
            }
            _state.update {
                it.copy(
                    years = available,
                    message = if (available.isEmpty()) "No electoral roll years found" else null,
                )
            }
        }
    }

    private fun loadDistricts(stateCd: String) = viewModelScope.launch {
        runLoading("Loading districts") {
            _state.update { it.copy(districts = repository.getDistricts(stateCd)) }
        }
    }

    private fun loadAssemblies(districtCd: String) = viewModelScope.launch {
        runLoading("Loading assembly constituencies") {
            _state.update { it.copy(assemblies = repository.getAssemblies(districtCd)) }
        }
    }

    private fun loadLanguagesAndParts() = viewModelScope.launch {
        val current = state.value
        val selectedState = current.selectedState ?: return@launch
        val selectedAssembly = current.selectedAssembly ?: return@launch
        val selectedRollType = current.selectedRollType ?: return@launch
        val selectedYear = current.selectedYear ?: return@launch

        runLoading("Loading village and part list") {
            val languages = repository.getLanguages(
                stateCd = selectedState.stateCd,
                acNumber = selectedAssembly.asmblyNo,
                rollType = selectedRollType,
            )
            val parts = repository.getParts(
                stateCd = selectedState.stateCd,
                acNumber = selectedAssembly.asmblyNo,
                rollType = selectedRollType,
                year = selectedYear,
            )
            _state.update {
                it.copy(
                    languages = languages,
                    selectedLanguageCode = null,
                    parts = parts,
                )
            }
        }
    }

    private suspend fun downloadSelectedPdfs(retryOnlyFailed: Boolean) {
        val current = state.value
        val validationError = current.validationError()
        if (validationError != null) {
            _state.update { it.copy(message = validationError) }
            return
        }

        val selectedState = current.selectedState ?: return
        val selectedDistrict = current.selectedDistrict ?: return
        val selectedAssembly = current.selectedAssembly ?: return
        val selectedRollType = current.selectedRollType ?: return
        val captcha = current.captcha ?: return
        val languageCode = current.selectedLanguageCode ?: return
        val selectedParts = current.selectedParts

        if (retryOnlyFailed) {
            retryFailedFileDownloads()
            return
        }

        val initialItems = selectedParts.map {
            ElectoralRollDownloadItem(
                partNumber = it.partNumber,
                partName = it.partName,
                status = DownloadStatus.Waiting,
            )
        }
        _state.update {
            it.copy(
                isDownloading = true,
                downloadProgress = 0,
                downloadedPdfs = emptyList(),
                downloadItems = initialItems,
                message = "Verifying captcha",
            )
        }

        runCatching {
            repository.generatePublishedPdfs(
                stateCd = selectedState.stateCd,
                districtCd = selectedDistrict.districtCd,
                acNumber = selectedAssembly.asmblyNo,
                selectedParts = selectedParts.map { it.partNumber },
                captcha = current.captchaInput.trim(),
                captchaId = captcha.id,
                languageCode = languageCode,
                rollType = selectedRollType,
            )
        }.onSuccess { batch ->
            val mapped = initialItems.mapIndexed { index, item ->
                item.copy(fileId = batch.fileIds.getOrNull(index))
            }
            _state.update { it.copy(downloadItems = mapped, message = "Captcha verified. Starting downloads") }
            _state.update { it.copy(isCdnBatch = batch.isCdn) }
            downloadFiles(batch.isCdn, mapped)
        }.onFailure { error ->
            _state.update {
                it.copy(
                    isDownloading = false,
                    downloadProgress = 0,
                    message = error.userMessage("Captcha verification failed"),
                    downloadItems = initialItems.map { item ->
                        item.copy(status = DownloadStatus.Failed, error = error.userMessage("Verification failed"))
                    },
                )
            }
            refreshCaptcha()
        }
    }

    private suspend fun retryFailedFileDownloads() {
        val current = _state.value
        val failedItems = current.downloadItems.filter {
            it.status == DownloadStatus.Failed && it.fileId != null
        }
        if (failedItems.isEmpty()) {
            downloadSelectedPdfs(retryOnlyFailed = false)
            return
        }
        _state.update {
            it.copy(
                isDownloading = true,
                message = "Retrying failed downloads",
                downloadItems = current.downloadItems.map { item ->
                    if (item.status == DownloadStatus.Failed && item.fileId != null) {
                        item.copy(status = DownloadStatus.Waiting, progress = 0, error = null)
                    } else {
                        item
                    }
                },
            ).withDownloadSummary()
        }
        downloadFiles(isCdn = current.isCdnBatch, items = failedItems)
    }

    private suspend fun downloadFiles(
        isCdn: Boolean,
        items: List<ElectoralRollDownloadItem>,
    ) {
        val targetItems = items.filter { it.fileId != null }
        if (targetItems.isEmpty()) {
            _state.update { it.copy(isDownloading = false, message = "No PDF files returned") }
            return
        }

        targetItems.forEach { item ->
            currentCoroutineContext().ensureActive()
            val fileId = item.fileId ?: return@forEach
            markItemDownloading(item.partNumber, fileId)
            val result = runCatching {
                if (isCdn) {
                    pdfDownloadManager.downloadCdnPdf(fileId) { progress ->
                        updateItemProgress(item.partNumber, progress)
                    }
                } else {
                    val file = repository.getPublishedFile(fileId)
                    pdfDownloadManager.saveBase64Pdf(file.base64Pdf, file.fileName) { progress ->
                        updateItemProgress(item.partNumber, progress)
                    }
                }
            }
            currentCoroutineContext().ensureActive()
            result.onSuccess { downloaded ->
                _state.update { current ->
                    current.copy(
                        downloadedPdfs = current.downloadedPdfs + downloaded,
                        downloadItems = current.downloadItems.map { existing ->
                            if (existing.partNumber == item.partNumber) {
                                existing.copy(
                                    status = DownloadStatus.Completed,
                                    progress = 100,
                                    fileName = downloaded.fileName,
                                    downloadedPdf = downloaded,
                                    error = null,
                                )
                            } else {
                                existing
                            }
                        },
                    ).withDownloadSummary()
                }
            }.onFailure { error ->
                _state.update { current ->
                    current.copy(
                        downloadItems = current.downloadItems.map { existing ->
                            if (existing.partNumber == item.partNumber) {
                                existing.copy(
                                    status = DownloadStatus.Failed,
                                    error = error.userMessage("Download failed"),
                                )
                            } else {
                                existing
                            }
                        },
                    ).withDownloadSummary()
                }
            }
        }

        _state.update { current ->
            val next = current.withDownloadSummary()
            val hasFailures = next.failedCount > 0
            next.copy(
                isDownloading = false,
                step = if (!hasFailures && next.completedCount > 0) ElectoralRollStep.Success else next.step,
                message = if (hasFailures) "Some PDFs failed. You can retry." else "All PDFs downloaded successfully",
            )
        }
        refreshCaptcha()
    }

    private fun markItemDownloading(partNumber: Int, fileId: String) {
        _state.update { current ->
            current.copy(
                message = "Downloading Part $partNumber",
                downloadItems = current.downloadItems.map {
                    if (it.partNumber == partNumber) {
                        it.copy(status = DownloadStatus.Downloading, fileId = fileId, progress = 0, error = null)
                    } else {
                        it
                    }
                },
            ).withDownloadSummary()
        }
    }

    private fun updateItemProgress(partNumber: Int, progress: Int) {
        _state.update { current ->
            current.copy(
                downloadItems = current.downloadItems.map {
                    if (it.partNumber == partNumber) it.copy(progress = progress.coerceIn(0, 100)) else it
                },
            ).withDownloadSummary()
        }
    }

    private suspend fun runLoading(message: String, block: suspend () -> Unit) {
        _state.update { it.copy(isLoading = true, message = message) }
        runCatching { block() }
            .onFailure { error ->
                _state.update { it.copy(message = error.userMessage("Request failed")) }
            }
        _state.update { it.copy(isLoading = false) }
    }

    private fun Throwable.userMessage(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback
}

enum class ElectoralRollStep {
    State,
    Year,
    RollType,
    District,
    Assembly,
    Parts,
    Captcha,
    Success,
}

enum class DownloadStatus {
    Waiting,
    Downloading,
    Completed,
    Failed,
    Cancelled,
}

data class ElectoralRollDownloadItem(
    val partNumber: Int,
    val partName: String,
    val fileId: String? = null,
    val fileName: String? = null,
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.Waiting,
    val error: String? = null,
    val downloadedPdf: DownloadedPdf? = null,
)

data class ElectoralRollUiState(
    val step: ElectoralRollStep = ElectoralRollStep.State,
    val years: List<Int> = emptyList(),
    val selectedYear: Int? = null,
    val states: List<StateDto> = emptyList(),
    val districts: List<DistrictDto> = emptyList(),
    val assemblies: List<AssemblyDto> = emptyList(),
    val rollTypes: List<RollTypeDto> = emptyList(),
    val languages: Map<String, String> = emptyMap(),
    val parts: List<PartDto> = emptyList(),
    val selectedState: StateDto? = null,
    val selectedDistrict: DistrictDto? = null,
    val selectedAssembly: AssemblyDto? = null,
    val selectedRollType: RollTypeDto? = null,
    val selectedLanguageCode: String? = null,
    val selectedPartNumbers: Set<Int> = emptySet(),
    val captcha: CaptchaData? = null,
    val captchaInput: String = "",
    val downloadedPdfs: List<DownloadedPdf> = emptyList(),
    val downloadItems: List<ElectoralRollDownloadItem> = emptyList(),
    val isLoading: Boolean = false,
    val isCaptchaLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val isLanguageSheetVisible: Boolean = false,
    val isCdnBatch: Boolean = false,
    val downloadProgress: Int = 0,
    val message: String? = null,
) {
    val stepNumber: Int
        get() = when (step) {
            ElectoralRollStep.State -> 1
            ElectoralRollStep.Year -> 2
            ElectoralRollStep.RollType -> 3
            ElectoralRollStep.District -> 4
            ElectoralRollStep.Assembly -> 5
            ElectoralRollStep.Parts -> 6
            ElectoralRollStep.Captcha -> 7
            ElectoralRollStep.Success -> 7
        }

    val selectedParts: List<PartDto>
        get() = parts.filter { it.partNumber in selectedPartNumbers }.sortedBy { it.partNumber }

    val completedCount: Int
        get() = downloadItems.count { it.status == DownloadStatus.Completed }

    val failedCount: Int
        get() = downloadItems.count { it.status == DownloadStatus.Failed }

    val currentFileName: String?
        get() = downloadItems.firstOrNull { it.status == DownloadStatus.Downloading }?.let {
            it.fileName ?: "Part ${it.partNumber}"
        }

    val hasFailedDownloads: Boolean
        get() = failedCount > 0

    fun validationError(): String? = when {
        selectedState == null -> "Please select State"
        selectedYear == null -> "Please select Year"
        selectedRollType == null -> "Please select Roll Type"
        selectedDistrict == null -> "Please select District"
        selectedAssembly == null -> "Please select Assembly Constituency"
        selectedPartNumbers.isEmpty() -> "Please select at least one part"
        selectedPartNumbers.size > 10 -> "Maximum 10 parts are allowed at once"
        selectedLanguageCode == null -> "Please select Download Language"
        captchaInput.isBlank() -> "Please enter captcha"
        captcha == null -> "Captcha not loaded"
        else -> null
    }

    fun resetAfterState(): ElectoralRollUiState = copy(
        selectedYear = null,
        selectedRollType = null,
        selectedDistrict = null,
        selectedAssembly = null,
        selectedLanguageCode = null,
        years = emptyList(),
        districts = emptyList(),
        assemblies = emptyList(),
        rollTypes = emptyList(),
        languages = emptyMap(),
        parts = emptyList(),
        selectedPartNumbers = emptySet(),
        downloadItems = emptyList(),
        downloadedPdfs = emptyList(),
        isCdnBatch = false,
    )

    fun resetAfterYear(): ElectoralRollUiState = copy(
        selectedRollType = null,
        selectedDistrict = null,
        selectedAssembly = null,
        selectedLanguageCode = null,
        districts = emptyList(),
        assemblies = emptyList(),
        languages = emptyMap(),
        parts = emptyList(),
        selectedPartNumbers = emptySet(),
        downloadItems = emptyList(),
        downloadedPdfs = emptyList(),
        isCdnBatch = false,
    )

    fun resetAfterRollType(): ElectoralRollUiState = copy(
        selectedDistrict = null,
        selectedAssembly = null,
        selectedLanguageCode = null,
        districts = emptyList(),
        assemblies = emptyList(),
        languages = emptyMap(),
        parts = emptyList(),
        selectedPartNumbers = emptySet(),
        downloadItems = emptyList(),
        downloadedPdfs = emptyList(),
        isCdnBatch = false,
    )

    fun resetAfterDistrict(): ElectoralRollUiState = copy(
        selectedAssembly = null,
        selectedLanguageCode = null,
        assemblies = emptyList(),
        languages = emptyMap(),
        parts = emptyList(),
        selectedPartNumbers = emptySet(),
        downloadItems = emptyList(),
        downloadedPdfs = emptyList(),
        isCdnBatch = false,
    )

    fun resetAfterAssembly(): ElectoralRollUiState = copy(
        selectedLanguageCode = null,
        languages = emptyMap(),
        parts = emptyList(),
        selectedPartNumbers = emptySet(),
        downloadItems = emptyList(),
        downloadedPdfs = emptyList(),
        isCdnBatch = false,
    )

    fun withDownloadSummary(): ElectoralRollUiState {
        if (downloadItems.isEmpty()) return copy(downloadProgress = 0)
        val total = downloadItems.size * 100
        val sum = downloadItems.sumOf {
            when (it.status) {
                DownloadStatus.Completed -> 100
                DownloadStatus.Downloading -> it.progress
                else -> 0
            }
        }
        return copy(downloadProgress = ((sum * 100) / total).coerceIn(0, 100))
    }
}
