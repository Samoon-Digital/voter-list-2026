package com.samoondigital.yojnaplus.feature.pdfviewer

import android.content.Context
import android.net.Uri
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
    private val uri: String = Uri.decode(savedStateHandle.get<String>("uri").orEmpty())
    private val prefs = context.getSharedPreferences("pdf_viewer_state", Context.MODE_PRIVATE)
    private val key = uri.hashCode().toString()

    private val _state = MutableStateFlow(
        PdfViewerUiState(
            uri = uri,
            title = Uri.decode(savedStateHandle.get<String>("title").orEmpty()),
            currentPage = prefs.getInt("${key}_page", 0),
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
                currentPage = if (pageCount > 0) {
                    it.currentPage.coerceIn(0, pageCount - 1)
                } else {
                    it.currentPage.coerceAtLeast(0)
                },
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

    fun toggleDarkMode() {
        _state.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    private fun persist() = viewModelScope.launch {
        val current = _state.value
        prefs.edit()
            .putInt("${key}_page", current.currentPage)
            .apply()
    }
}

data class PdfViewerUiState(
    val uri: String,
    val title: String,
    val isLoading: Boolean = true,
    val error: String? = null,
    val pageCount: Int = 0,
    val currentPage: Int = 0,
    val isDarkMode: Boolean = false,
) {
    val pageLabel: String
        get() = if (pageCount > 0) "${currentPage + 1} / $pageCount" else "-- / --"
}
