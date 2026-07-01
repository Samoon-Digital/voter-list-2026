package com.samoondigital.yojnaplus.feature.pdfviewer

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfViewerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val uri: String = savedStateHandle.get<String>("uri").orEmpty()
    private val prefs = context.getSharedPreferences("pdf_viewer_state", Context.MODE_PRIVATE)
    private val key = uri.hashCode().toString()

    private val _state = MutableStateFlow(
        PdfViewerUiState(
            uri = uri,
            title = savedStateHandle.get<String>("title").orEmpty(),
            currentPage = prefs.getInt("${key}_page", 0),
            zoom = prefs.getFloat("${key}_zoom", 1f).coerceIn(MinZoom, MaxZoom),
        ),
    )
    val state: StateFlow<PdfViewerUiState> = _state.asStateFlow()

    fun setLoading() {
        _state.update { it.copy(isLoading = true, error = null) }
    }

    fun onLoaded(pageCount: Int) {
        _state.update {
            it.copy(
                isLoading = false,
                pageCount = pageCount,
                currentPage = it.currentPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0)),
                error = null,
            )
        }
    }

    fun onError(message: String) {
        _state.update { it.copy(isLoading = false, error = message) }
    }

    fun onPageChanged(page: Int, pageCount: Int) {
        _state.update { it.copy(currentPage = page, pageCount = pageCount, error = null) }
        persist()
    }

    fun onZoomChanged(zoom: Float) {
        _state.update { it.copy(zoom = zoom.coerceIn(MinZoom, MaxZoom)) }
        persist()
    }

    fun toggleSearch() {
        _state.update { it.copy(isSearchVisible = !it.isSearchVisible, searchQuery = "") }
    }

    fun updateSearch(value: String) {
        _state.update { it.copy(searchQuery = value.filter(Char::isDigit).take(4)) }
    }

    fun goToSearchPage() {
        val page = _state.value.searchQuery.toIntOrNull()?.minus(1) ?: return
        requestPage(page)
    }

    fun requestPage(page: Int) {
        val current = _state.value
        if (current.pageCount <= 0) return
        _state.update { it.copy(requestedPage = page.coerceIn(0, current.pageCount - 1)) }
    }

    fun consumeRequestedPage() {
        _state.update { it.copy(requestedPage = null) }
    }

    fun zoomIn() {
        _state.update { it.copy(requestedZoom = (it.zoom + 0.35f).coerceAtMost(MaxZoom)) }
    }

    fun zoomOut() {
        _state.update { it.copy(requestedZoom = (it.zoom - 0.35f).coerceAtLeast(MinZoom)) }
    }

    fun consumeRequestedZoom() {
        _state.update { it.copy(requestedZoom = null) }
    }

    fun toggleDarkMode() {
        _state.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    private fun persist() = viewModelScope.launch {
        val current = _state.value
        prefs.edit()
            .putInt("${key}_page", current.currentPage)
            .putFloat("${key}_zoom", current.zoom)
            .apply()
    }

    companion object {
        const val MinZoom = 1f
        const val MaxZoom = 5f
    }
}

data class PdfViewerUiState(
    val uri: String,
    val title: String,
    val isLoading: Boolean = true,
    val error: String? = null,
    val pageCount: Int = 0,
    val currentPage: Int = 0,
    val zoom: Float = 1f,
    val requestedPage: Int? = null,
    val requestedZoom: Float? = null,
    val isSearchVisible: Boolean = false,
    val searchQuery: String = "",
    val isDarkMode: Boolean = false,
) {
    val pageLabel: String
        get() = if (pageCount > 0) "${currentPage + 1} / $pageCount" else "-- / --"
}
