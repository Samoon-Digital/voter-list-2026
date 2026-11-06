package com.samoondigital.yojnaplus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samoondigital.yojnaplus.data.preferences.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Activity-scoped state: drives the app-wide theme from persisted preferences. */
@HiltViewModel
class MainViewModel @Inject constructor(
    settings: SettingsDataStore,
) : ViewModel() {

    val state: StateFlow<MainUiState> = settings.darkMode
        .map { MainUiState(darkMode = it, isThemeReady = true) }
        .catch { error ->
            if (error !is IOException) throw error
            emit(MainUiState(darkMode = false, isThemeReady = true))
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState())
}

data class MainUiState(val darkMode: Boolean? = null, val isThemeReady: Boolean = false)
