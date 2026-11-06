package com.samoondigital.yojnaplus.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class DownloadCompletionState(val id: Long? = null, val ready: Boolean = false)

/** One ad opportunity and one navigation per saved batch, retained across activity recreation. */
internal class DownloadCompletionTransition {
    private val _state = MutableStateFlow(DownloadCompletionState())
    val state = _state.asStateFlow()
    private var sequence = 0L
    private var navigationConsumed = false

    fun begin(download: ElectoralRollUiState): Long? {
        if (!download.isSuccessfulDownloadBatch || _state.value.id != null) return null
        val id = ++sequence
        navigationConsumed = false
        _state.value = DownloadCompletionState(id)
        return id
    }

    fun isActive(id: Long, download: ElectoralRollUiState): Boolean =
        _state.value.id == id && !navigationConsumed && download.isSuccessfulDownloadBatch

    fun finishAd(id: Long, download: ElectoralRollUiState) {
        if (isActive(id, download)) _state.value = _state.value.copy(ready = true)
    }

    fun consumeNavigation(id: Long, download: ElectoralRollUiState): Boolean {
        if (!_state.value.ready || !isActive(id, download)) return false
        navigationConsumed = true
        return true
    }

    fun cancel() {
        _state.value = DownloadCompletionState()
        navigationConsumed = false
    }
}
