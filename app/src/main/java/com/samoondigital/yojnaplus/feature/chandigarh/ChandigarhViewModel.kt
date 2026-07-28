package com.samoondigital.yojnaplus.feature.chandigarh

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
class ChandigarhViewModel @Inject constructor(
    private val repository: ChandigarhRepository,
    private val pdfDownloadManager: PdfDownloadManager,
    private val downloadRepository: DownloadRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ChandigarhUiState())
    val state: StateFlow<ChandigarhUiState> = _state.asStateFlow()

    private val events = Channel<ChandigarhEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    init {
        loadAreas()
    }

    fun selectArea(area: ChandigarhArea) {
        _state.update {
            it.copy(
                step = ChandigarhStep.PollingStation,
                selectedArea = area,
                pollingStations = emptyList(),
                message = null,
            )
        }
        loadPollingStations(area)
    }

    fun requestDownload(station: ChandigarhPollingStation) {
        if (_state.value.downloadingPsNumber != null) return
        downloadStation(station)
    }

    fun goBack(): Boolean {
        return when (_state.value.step) {
            ChandigarhStep.Area -> false
            ChandigarhStep.PollingStation -> {
                _state.update {
                    it.copy(
                        step = ChandigarhStep.Area,
                        message = null,
                        downloadingPsNumber = null,
                    )
                }
                true
            }
        }
    }

    private fun loadAreas() = viewModelScope.launch {
        runLoading("Loading Chandigarh sectors") {
            val areas = repository.getAreas()
            _state.update {
                it.copy(
                    areas = areas,
                    message = if (areas.isEmpty()) "No sectors found" else null,
                )
            }
        }
    }

    private fun loadPollingStations(area: ChandigarhArea) = viewModelScope.launch {
        runLoading("Loading polling stations") {
            val stations = repository.getPollingStations(area)
            _state.update {
                it.copy(
                    pollingStations = stations,
                    message = if (stations.isEmpty()) "No polling stations found" else null,
                )
            }
        }
    }

    private fun downloadStation(station: ChandigarhPollingStation) = viewModelScope.launch {
        val area = _state.value.selectedArea ?: return@launch
        val recordId = downloadRepository.createPendingRecord(
            district = "Chandigarh",
            assembly = area.name,
            village = station.name,
            partNumber = station.psNumber,
        )

        _state.update {
            it.copy(
                downloadingPsNumber = station.psNumber,
                downloadProgress = 0,
                message = "Downloading PS ${station.psNumber}",
            )
        }
        downloadRepository.markDownloading(recordId)

        runCatching {
            pdfDownloadManager.downloadCdnPdf(station.pdfUrl) { progress ->
                updateProgress(recordId, station.psNumber, progress)
            }
        }.onSuccess { downloaded ->
            downloadRepository.markCompleted(recordId, downloaded.fileName, downloaded.uri)
            _state.update {
                it.copy(
                    downloadingPsNumber = null,
                    downloadProgress = 100,
                    message = "PDF downloaded successfully",
                )
            }
            events.send(ChandigarhEvent.OpenPdf(downloaded.uri, "Chandigarh PS ${station.psNumber}"))
        }.onFailure { error ->
            if (!isActive) return@launch
            val message = error.message?.takeIf { it.isNotBlank() } ?: "Download failed"
            downloadRepository.markFailed(recordId, message)
            _state.update {
                it.copy(
                    downloadingPsNumber = null,
                    downloadProgress = 0,
                    message = message,
                )
            }
        }
    }

    private fun updateProgress(recordId: String, psNumber: Int, progress: Int) {
        val safeProgress = progress.coerceIn(0, 100)
        _state.update {
            it.copy(
                downloadingPsNumber = psNumber,
                downloadProgress = safeProgress,
            )
        }
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
}

enum class ChandigarhStep {
    Area,
    PollingStation,
}

data class ChandigarhUiState(
    val step: ChandigarhStep = ChandigarhStep.Area,
    val areas: List<ChandigarhArea> = emptyList(),
    val pollingStations: List<ChandigarhPollingStation> = emptyList(),
    val selectedArea: ChandigarhArea? = null,
    val isLoading: Boolean = false,
    val downloadingPsNumber: Int? = null,
    val downloadProgress: Int = 0,
    val message: String? = null,
) {
    val stepNumber: Int
        get() = when (step) {
            ChandigarhStep.Area -> 1
            ChandigarhStep.PollingStation -> 2
        }
}

sealed interface ChandigarhEvent {
    data class OpenPdf(val uri: String, val title: String) : ChandigarhEvent
}