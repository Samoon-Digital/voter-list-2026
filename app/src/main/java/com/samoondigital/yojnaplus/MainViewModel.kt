package com.samoondigital.yojnaplus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samoondigital.yojnaplus.data.preferences.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Activity-scoped state: drives the app-wide theme from persisted preferences. */
@HiltViewModel
class MainViewModel @Inject constructor(
    settings: SettingsDataStore,
) : ViewModel() {

    val darkMode: StateFlow<Boolean?> = settings.darkMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
