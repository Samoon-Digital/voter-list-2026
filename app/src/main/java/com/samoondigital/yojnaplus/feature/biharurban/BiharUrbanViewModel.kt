package com.samoondigital.yojnaplus.feature.biharurban

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
class BiharUrbanViewModel @Inject constructor(
    private val repository: BiharUrbanRepository,
    private val pdfDownloadManager: PdfDownloadManager,
    private val downloadRepository: DownloadRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BiharUrbanUiState())
    val state: StateFlow<BiharUrbanUiState> = _state.asStateFlow()

    private val events = Channel<BiharUrbanEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    init {
        repository.resetSession()
        loadDistricts()
    }

    fun selectDistrict(option: BiharUrbanOption) {
        _state.update {
            it.copy(
                step = BiharUrbanStep.Subdivision,
                selectedDistrict = option,
                selectedSubdivision = null,
                selectedMunicipality = null,
                subdivisions = emptyList(),
                municipalities = emptyList(),
                pdfLinks = emptyList(),
                message = null,
            )
        }
        viewModelScope.launch {
            runLoading("Loading subdivisions") {
                val subdivisions = repository.loadSubdivisions(option)
                _state.update {
                    it.copy(
                        subdivisions = subdivisions,
                        message = if (subdivisions.isEmpty()) "No subdivisions found" else null,
                    )
                }
            }
        }
    }

    fun selectSubdivision(option: BiharUrbanOption) {
        val district = _state.value.selectedDistrict ?: return
        _state.update {
            it.copy(
                step = BiharUrbanStep.Municipality,
                selectedSubdivision = option,
                selectedMunicipality = null,
                municipalities = emptyList(),
                pdfLinks = emptyList(),
                message = null,
            )
        }
        viewModelScope.launch {
            runLoading("Loading municipalities") {
                val municipalities = repository.loadMunicipalities(district, option)
                _state.update {
                    it.copy(
                        municipalities = municipalities,
                        message = if (municipalities.isEmpty()) "No municipalities found" else null,
                    )
                }
            }
        }
    }

    fun selectMunicipality(option: BiharUrbanOption) {
        val district = _state.value.selectedDistrict ?: return
        val subdivision = _state.value.selectedSubdivision ?: return
        _state.update {
            it.copy(
                step = BiharUrbanStep.PdfList,
                selectedMunicipality = option,
                pdfLinks = emptyList(),
                message = null,
            )
        }
        viewModelScope.launch {
            runLoading("Loading PDF list") {
                val links = repository.loadPdfLinks(district, subdivision, option)
                _state.update {
                    it.copy(
                        pdfLinks = links,
                        message = if (links.isEmpty()) "No PDF links found" else null,
                    )
                }
            }
        }
    }

    fun downloadPdf(link: BiharUrbanPdfLink) {
        val current = _state.value
        val district = current.selectedDistrict ?: return
        val subdivision = current.selectedSubdivision ?: return
        val municipality = current.selectedMunicipality ?: return
        if (current.isDownloading) return

        viewModelScope.launch {
            val recordId = downloadRepository.createPendingRecord(
                district = "Bihar - ${district.label}",
                assembly = subdivision.label,
                village = "${municipality.label} - ${link.label}",
                partNumber = 1,
            )
            downloadRepository.markDownloading(recordId)
            _state.update {
                it.copy(
                    isDownloading = true,
                    downloadProgress = 0,
                    downloadingUrl = link.url,
                    message = "Downloading ${link.label}",
                )
            }
            runCatching {
                pdfDownloadManager.downloadCdnPdf(
                    cdnPath = link.url,
                    fileNameOverride = link.fileName,
                ) { progress ->
                    updateProgress(recordId, progress)
                }
            }.onSuccess { downloaded ->
                downloadRepository.markCompleted(recordId, downloaded.fileName, downloaded.uri)
                _state.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgress = 0,
                        downloadingUrl = null,
                        message = "PDF downloaded successfully",
                    )
                }
                events.send(BiharUrbanEvent.OpenPdf(downloaded.uri, downloaded.fileName))
            }.onFailure { error ->
                if (!isActive) return@launch
                val message = error.userMessage("Download failed")
                downloadRepository.markFailed(recordId, message)
                _state.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgress = 0,
                        downloadingUrl = null,
                        message = message,
                    )
                }
            }
        }
    }

    fun goBack(): Boolean {
        val previous = when (_state.value.step) {
            BiharUrbanStep.District -> return false
            BiharUrbanStep.Subdivision -> BiharUrbanStep.District
            BiharUrbanStep.Municipality -> BiharUrbanStep.Subdivision
            BiharUrbanStep.PdfList -> BiharUrbanStep.Municipality
        }
        _state.update {
            it.copy(
                step = previous,
                isDownloading = false,
                downloadProgress = 0,
                downloadingUrl = null,
                message = null,
            )
        }
        return true
    }

    private fun loadDistricts() = viewModelScope.launch {
        runLoading("Loading Bihar districts") {
            val districts = repository.loadDistricts()
            _state.update { it.copy(districts = districts, message = if (districts.isEmpty()) "No districts found" else null) }
        }
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

    private fun updateProgress(recordId: String, progress: Int) {
        val safeProgress = progress.coerceIn(0, 100)
        _state.update { it.copy(downloadProgress = safeProgress) }
        viewModelScope.launch { downloadRepository.updateProgress(recordId, safeProgress) }
    }

    private fun Throwable.userMessage(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback
}
