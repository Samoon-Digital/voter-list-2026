package com.samoondigital.yojnaplus.feature.webview

import android.webkit.URLUtil
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
class ChandigarhWebViewViewModel @Inject constructor(
    private val pdfDownloadManager: PdfDownloadManager,
    private val downloadRepository: DownloadRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(WebPdfDownloadUiState())
    val state: StateFlow<WebPdfDownloadUiState> = _state.asStateFlow()

    private val events = Channel<WebPdfDownloadEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    fun downloadPdf(
        url: String,
        contentDisposition: String?,
        mimeType: String?,
        district: String = "Chandigarh",
        assembly: String = "Intensive revision 2002",
    ) {
        if (_state.value.isDownloading || !url.startsWith("http", ignoreCase = true)) return
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType).let { guessed ->
            if (guessed.endsWith(".pdf", ignoreCase = true)) guessed else "$guessed.pdf"
        }
        download(url = url, fileName = fileName, district = district, assembly = assembly)
    }

    private fun download(
        url: String,
        fileName: String,
        district: String,
        assembly: String,
    ) = viewModelScope.launch {
        val recordId = downloadRepository.createPendingRecord(
            district = district,
            assembly = assembly,
            village = fileName.removeSuffix(".pdf"),
            partNumber = 0,
        )
        downloadRepository.markDownloading(recordId)
        _state.update {
            it.copy(
                isDownloading = true,
                progress = 0,
                message = "Downloading PDF",
            )
        }

        runCatching {
            pdfDownloadManager.downloadCdnPdf(url) { progress ->
                updateProgress(recordId, progress)
            }
        }.onSuccess { downloaded ->
            downloadRepository.markCompleted(recordId, downloaded.fileName, downloaded.uri)
            _state.update {
                it.copy(
                    isDownloading = false,
                    progress = 100,
                    message = "PDF downloaded successfully",
                )
            }
            events.send(WebPdfDownloadEvent.OpenPdf(downloaded.uri, downloaded.fileName))
        }.onFailure { error ->
            if (!isActive) return@launch
            val message = error.message?.takeIf { it.isNotBlank() } ?: "Download failed"
            downloadRepository.markFailed(recordId, message)
            _state.update {
                it.copy(
                    isDownloading = false,
                    progress = 0,
                    message = message,
                )
            }
        }
    }

    private fun updateProgress(recordId: String, progress: Int) {
        val safeProgress = progress.coerceIn(0, 100)
        _state.update { it.copy(progress = safeProgress, message = "Downloading PDF") }
        viewModelScope.launch { downloadRepository.updateProgress(recordId, safeProgress) }
    }
}

data class WebPdfDownloadUiState(
    val isDownloading: Boolean = false,
    val progress: Int = 0,
    val message: String? = null,
)

sealed interface WebPdfDownloadEvent {
    data class OpenPdf(val uri: String, val title: String) : WebPdfDownloadEvent
}
