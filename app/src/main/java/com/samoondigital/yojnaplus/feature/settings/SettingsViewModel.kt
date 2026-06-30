package com.samoondigital.yojnaplus.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samoondigital.yojnaplus.data.preferences.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsDataStore,
) : ViewModel() {

    val state: StateFlow<SettingsState> =
        combine(settings.darkMode, settings.notificationsEnabled) { dark, notif ->
            SettingsState(darkMode = dark, notificationsEnabled = notif)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { settings.setDarkMode(enabled) }

    fun setNotifications(enabled: Boolean) =
        viewModelScope.launch { settings.setNotificationsEnabled(enabled) }
}
