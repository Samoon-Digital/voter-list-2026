package com.samoondigital.yojnaplus.feature.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samoondigital.yojnaplus.feature.downloads.data.DownloadRecordEntity
import com.samoondigital.yojnaplus.feature.downloads.data.DownloadRepository
import com.samoondigital.yojnaplus.feature.downloads.data.DownloadStatusEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: DownloadRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val records = MutableStateFlow<List<DownloadRecordEntity>>(emptyList())
    private val _state = MutableStateFlow(DownloadsUiState(isLoading = true))
    val state: StateFlow<DownloadsUiState> = _state.asStateFlow()

    private val events = Channel<DownloadsEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    init {
        repository.enqueueStorageSync()
        viewModelScope.launch {
            repository.syncWithLocalStorage()
            _state.update { it.copy(isLoading = false) }
        }
        viewModelScope.launch {
            repository.observeDownloads().collect { records.value = it }
        }
        viewModelScope.launch {
            combine(records, query) { allRecords, searchQuery ->
                val filtered = allRecords
                    .filter { it.matches(searchQuery) }
                    .map { it.toUi() }
                DownloadsUiState(
                    isLoading = false,
                    query = searchQuery,
                    downloads = filtered,
                    totalCount = allRecords.size,
                    message = _state.value.message,
                    renameTarget = _state.value.renameTarget,
                )
            }.collect { next -> _state.value = next }
        }
    }

    fun updateSearch(value: String) {
        query.value = value
    }

    fun requestRename(download: DownloadItemUi) {
        _state.update { it.copy(renameTarget = download) }
    }

    fun dismissRename() {
        _state.update { it.copy(renameTarget = null) }
    }

    fun rename(id: String, name: String) = viewModelScope.launch {
        runCatching { repository.rename(id, name) }
            .onSuccess {
                _state.update { it.copy(renameTarget = null, message = "PDF renamed") }
            }
            .onFailure { error ->
                _state.update { it.copy(message = error.userMessage("Unable to rename PDF")) }
            }
    }

    fun delete(id: String) = viewModelScope.launch {
        runCatching { repository.delete(id) }
            .onSuccess { _state.update { it.copy(message = "PDF deleted") } }
            .onFailure { error -> _state.update { it.copy(message = error.userMessage("Unable to delete PDF")) } }
    }

    fun share(id: String) {
        val record = records.value.firstOrNull { it.id == id } ?: return
        if (record.status.toStatus() != DownloadStatusEntity.Completed || record.uri == null) {
            _state.update { it.copy(message = "PDF file is not available to share") }
            return
        }
        runCatching { repository.share(record) }
            .onFailure { error -> _state.update { it.copy(message = error.userMessage("Unable to share PDF")) } }
    }

    fun open(id: String) = viewModelScope.launch {
        val record = records.value.firstOrNull { it.id == id } ?: return@launch
        if (record.status.toStatus() != DownloadStatusEntity.Completed || record.uri == null) {
            _state.update { it.copy(message = "PDF file is not available") }
            return@launch
        }
        events.send(DownloadsEvent.OpenPdf(record.uri, record.fileName))
    }

    fun cancel(id: String) = viewModelScope.launch {
        repository.markCancelled(id)
        _state.update { it.copy(message = "Download cancelled") }
    }

    fun retry(id: String) {
        val record = records.value.firstOrNull { it.id == id } ?: return
        _state.update {
            it.copy(message = "Retry from Voter List Download for ${record.village}")
        }
    }

    fun pause(id: String) {
        val record = records.value.firstOrNull { it.id == id } ?: return
        _state.update {
            it.copy(message = "Pause is not supported for ${record.fileName}")
        }
    }

    fun resume(id: String) {
        val record = records.value.firstOrNull { it.id == id } ?: return
        _state.update {
            it.copy(message = "Resume from Voter List Download for ${record.village}")
        }
    }

    fun syncNow() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        repository.syncWithLocalStorage()
        _state.update { it.copy(isLoading = false, message = "Downloads synced") }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun DownloadRecordEntity.matches(searchQuery: String): Boolean {
        val term = searchQuery.trim()
        if (term.isBlank()) return true
        return listOf(fileName, district, assembly, village, status)
            .any { it.matchesSearchQuery(term) }
    }

    private fun DownloadRecordEntity.toUi(): DownloadItemUi =
        DownloadItemUi(
            id = id,
            fileName = fileName,
            district = district,
            assembly = assembly,
            village = village,
            fileSize = fileSizeBytes.formatBytes(),
            downloadedAt = downloadedAtMillis.formatDate(),
            status = status.toStatus(),
            progress = progress,
            errorMessage = errorMessage,
        )

    private fun Long?.formatBytes(): String {
        val bytes = this ?: return "--"
        if (bytes < 1024L) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024.0) return "%.1f KB".format(kb)
        return "%.1f MB".format(kb / 1024.0)
    }

    private fun Long.formatDate(): String =
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(this))

    private fun String.toStatus(): DownloadStatusEntity =
        runCatching { DownloadStatusEntity.valueOf(this) }.getOrDefault(DownloadStatusEntity.Failed)

    private fun String.matchesSearchQuery(query: String): Boolean {
        val target = normalizedForSearch()
        val needle = query.normalizedForSearch()
        return needle.isBlank() ||
            target.contains(needle) ||
            target.replace(" ", "").contains(needle.replace(" ", ""))
    }

    private fun String.normalizedForSearch(): String = lowercase()
        .replace(Regex("[^\\p{L}\\p{Nd}]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun Throwable.userMessage(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback
}

data class DownloadsUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val downloads: List<DownloadItemUi> = emptyList(),
    val totalCount: Int = 0,
    val message: String? = null,
    val renameTarget: DownloadItemUi? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && downloads.isEmpty()
}

data class DownloadItemUi(
    val id: String,
    val fileName: String,
    val district: String,
    val assembly: String,
    val village: String,
    val fileSize: String,
    val downloadedAt: String,
    val status: DownloadStatusEntity,
    val progress: Int,
    val errorMessage: String?,
)

sealed interface DownloadsEvent {
    data class OpenPdf(val uri: String, val title: String) : DownloadsEvent
}
