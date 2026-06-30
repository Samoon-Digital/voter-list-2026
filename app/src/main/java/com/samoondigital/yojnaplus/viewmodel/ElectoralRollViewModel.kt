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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ElectoralRollViewModel @Inject constructor(
    private val repository: ElectoralRollRepository,
    private val pdfDownloadManager: PdfDownloadManager,
) : ViewModel() {
    private val currentYear = LocalDate.now().year

    private val _state = MutableStateFlow(
        ElectoralRollUiState(
            years = listOf(currentYear, currentYear - 1, currentYear - 2),
            selectedYear = currentYear,
        ),
    )
    val state: StateFlow<ElectoralRollUiState> = _state.asStateFlow()

    init {
        loadStates()
        refreshCaptcha()
    }

    fun loadStates() = viewModelScope.launch {
        runLoading("Loading states") {
            val states = repository.getStates()
            _state.update { it.copy(states = states) }
        }
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

    fun selectYear(year: Int) {
        _state.update {
            it.copy(
                selectedYear = year,
                selectedRollType = null,
                rollTypes = emptyList(),
                languages = emptyMap(),
                parts = emptyList(),
                selectedPartNumbers = emptySet(),
                downloadedPdfs = emptyList(),
            )
        }
        state.value.selectedState?.let { loadRollTypes(it.stateCd, year) }
    }

    fun selectState(state: StateDto) {
        val selectedYear = _state.value.selectedYear
        _state.update {
            it.copy(
                selectedState = state,
                selectedDistrict = null,
                selectedAssembly = null,
                selectedRollType = null,
                selectedLanguageCode = null,
                districts = emptyList(),
                assemblies = emptyList(),
                rollTypes = emptyList(),
                languages = emptyMap(),
                parts = emptyList(),
                selectedPartNumbers = emptySet(),
                downloadedPdfs = emptyList(),
                message = null,
            )
        }
        loadDistricts(state.stateCd)
        loadRollTypes(state.stateCd, selectedYear)
    }

    fun selectRollType(rollType: RollTypeDto) {
        _state.update {
            it.copy(
                selectedRollType = rollType,
                selectedDistrict = null,
                selectedAssembly = null,
                selectedLanguageCode = null,
                assemblies = emptyList(),
                languages = emptyMap(),
                parts = emptyList(),
                selectedPartNumbers = emptySet(),
                downloadedPdfs = emptyList(),
                message = null,
            )
        }
    }

    fun selectDistrict(district: DistrictDto) {
        _state.update {
            it.copy(
                selectedDistrict = district,
                selectedAssembly = null,
                assemblies = emptyList(),
                languages = emptyMap(),
                parts = emptyList(),
                selectedPartNumbers = emptySet(),
                downloadedPdfs = emptyList(),
                message = null,
            )
        }
        loadAssemblies(district.districtCd)
    }

    fun selectAssembly(assembly: AssemblyDto) {
        _state.update {
            it.copy(
                selectedAssembly = assembly,
                selectedLanguageCode = null,
                languages = emptyMap(),
                parts = emptyList(),
                selectedPartNumbers = emptySet(),
                downloadedPdfs = emptyList(),
                message = null,
            )
        }
        loadLanguagesAndParts()
    }

    fun selectLanguage(code: String) {
        _state.update { it.copy(selectedLanguageCode = code, message = null) }
    }

    fun updateCaptchaInput(value: String) {
        _state.update { it.copy(captchaInput = value.take(6), message = null) }
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

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    fun downloadSelectedPdfs() = viewModelScope.launch {
        val current = state.value
        val validationError = current.validationError()
        if (validationError != null) {
            _state.update { it.copy(message = validationError) }
            return@launch
        }

        val selectedState = current.selectedState ?: return@launch
        val selectedDistrict = current.selectedDistrict ?: return@launch
        val selectedAssembly = current.selectedAssembly ?: return@launch
        val selectedRollType = current.selectedRollType ?: return@launch
        val captcha = current.captcha ?: return@launch
        val languageCode = current.selectedLanguageCode ?: return@launch

        _state.update {
            it.copy(
                isDownloading = true,
                downloadProgress = 0,
                downloadedPdfs = emptyList(),
                message = "Generating PDF request",
            )
        }

        runCatching {
            repository.generatePublishedPdfs(
                stateCd = selectedState.stateCd,
                districtCd = selectedDistrict.districtCd,
                acNumber = selectedAssembly.asmblyNo,
                selectedParts = current.selectedPartNumbers.sorted(),
                captcha = current.captchaInput.trim(),
                captchaId = captcha.id,
                languageCode = languageCode,
                rollType = selectedRollType,
            )
        }.onSuccess { batch ->
            val downloads = mutableListOf<DownloadedPdf>()
            batch.fileIds.forEachIndexed { index, fileId ->
                val downloaded = if (batch.isCdn) {
                    pdfDownloadManager.downloadCdnPdf(fileId) { progress ->
                        updateDownloadProgress(index, batch.fileIds.size, progress)
                    }
                } else {
                    val file = repository.getPublishedFile(fileId)
                    pdfDownloadManager.saveBase64Pdf(file.base64Pdf, file.fileName) { progress ->
                        updateDownloadProgress(index, batch.fileIds.size, progress)
                    }
                }
                downloads += downloaded
                _state.update { it.copy(downloadedPdfs = downloads.toList()) }
            }
            _state.update {
                it.copy(
                    isDownloading = false,
                    downloadProgress = 100,
                    message = "PDF downloaded successfully",
                )
            }
            refreshCaptcha()
        }.onFailure { error ->
            _state.update {
                it.copy(
                    isDownloading = false,
                    downloadProgress = 0,
                    message = error.userMessage("Unable to download PDF"),
                )
            }
            refreshCaptcha()
        }
    }

    fun openPdf(downloadedPdf: DownloadedPdf) {
        runCatching { pdfDownloadManager.openPdf(downloadedPdf) }
            .onFailure { error ->
                _state.update { it.copy(message = error.userMessage("No PDF viewer found")) }
            }
    }

    private fun loadDistricts(stateCd: String) = viewModelScope.launch {
        runLoading("Loading districts") {
            val districts = repository.getDistricts(stateCd)
            _state.update { it.copy(districts = districts) }
        }
    }

    private fun loadAssemblies(districtCd: String) = viewModelScope.launch {
        runLoading("Loading assembly list") {
            val assemblies = repository.getAssemblies(districtCd)
            _state.update { it.copy(assemblies = assemblies) }
        }
    }

    private fun loadRollTypes(stateCd: String, year: Int) = viewModelScope.launch {
        runLoading("Loading roll types") {
            val rollTypes = repository.getRollTypes(stateCd, year)
            _state.update {
                it.copy(
                    rollTypes = rollTypes,
                    selectedRollType = rollTypes.firstOrNull(),
                )
            }
        }
    }

    private fun loadLanguagesAndParts() = viewModelScope.launch {
        val current = state.value
        val selectedState = current.selectedState ?: return@launch
        val selectedAssembly = current.selectedAssembly ?: return@launch
        val selectedRollType = current.selectedRollType ?: return@launch
        runLoading("Loading parts and languages") {
            val languages = repository.getLanguages(
                stateCd = selectedState.stateCd,
                acNumber = selectedAssembly.asmblyNo,
                rollType = selectedRollType,
            )
            val parts = repository.getParts(
                stateCd = selectedState.stateCd,
                acNumber = selectedAssembly.asmblyNo,
                rollType = selectedRollType,
                year = current.selectedYear,
            )
            _state.update {
                it.copy(
                    languages = languages,
                    selectedLanguageCode = languages.keys.firstOrNull(),
                    parts = parts,
                )
            }
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

    private fun updateDownloadProgress(index: Int, total: Int, itemProgress: Int) {
        val progress = (((index * 100) + itemProgress) / total).coerceIn(0, 100)
        _state.update { it.copy(downloadProgress = progress, message = "Downloading PDF $progress%") }
    }

    private fun Throwable.userMessage(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback
}

data class ElectoralRollUiState(
    val years: List<Int> = emptyList(),
    val selectedYear: Int,
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
    val isLoading: Boolean = false,
    val isCaptchaLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val message: String? = null,
) {
    val currentStep: Int
        get() = when {
            selectedState == null -> 1
            selectedRollType == null -> 2
            selectedDistrict == null -> 3
            selectedAssembly == null -> 4
            selectedLanguageCode == null -> 5
            selectedPartNumbers.isEmpty() -> 6
            captchaInput.isBlank() -> 7
            else -> 8
        }

    fun validationError(): String? = when {
        selectedState == null -> "Please select State"
        selectedRollType == null -> "Please select Roll Type"
        selectedDistrict == null -> "Please select District"
        selectedAssembly == null -> "Please select Assembly Constituency"
        selectedLanguageCode == null -> "Please select Language"
        selectedPartNumbers.isEmpty() -> "Please select Part"
        selectedPartNumbers.size > 10 -> "Maximum 10 parts are allowed at once"
        captchaInput.isBlank() -> "Please enter valid Captcha"
        captcha == null -> "Captcha not loaded"
        else -> null
    }
}
