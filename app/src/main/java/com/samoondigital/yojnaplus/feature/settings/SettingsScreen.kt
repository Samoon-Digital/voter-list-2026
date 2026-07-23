package com.samoondigital.yojnaplus.feature.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.BuildConfig
import com.samoondigital.yojnaplus.ads.AdManager
import com.samoondigital.yojnaplus.ads.ConsentManager

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    var inspectorTapCount by rememberSaveable { mutableIntStateOf(0) }
    var inspectorUnlocked by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(20.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        SettingToggle(
            title = "Dark mode",
            subtitle = "Use a dark theme",
            checked = state.darkMode,
            onCheckedChange = viewModel::setDarkMode,
        )
        HorizontalDivider()
        SettingToggle(
            title = "Notifications",
            subtitle = "Receive voter list update alerts",
            checked = state.notificationsEnabled,
            onCheckedChange = viewModel::setNotifications,
        )
        HorizontalDivider()

        if (ConsentManager.isPrivacyOptionsRequired()) {
            SettingAction(
                title = "Privacy choices",
                subtitle = "Manage advertising privacy preferences",
                onClick = { activity?.let(ConsentManager::showPrivacyOptions) },
            )
            HorizontalDivider()
        }
        if (BuildConfig.DEBUG && inspectorUnlocked) {
            SettingAction(
                title = "Ad Inspector",
                subtitle = "Inspect production ad requests and adapters",
                onClick = { activity?.let(AdManager::openAdInspector) },
            )
            HorizontalDivider()
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable(enabled = BuildConfig.DEBUG) {
                inspectorTapCount += 1
                if (inspectorTapCount >= 7) inspectorUnlocked = true
            },
        )
    }
}

@Composable
private fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingAction(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onClick) {
            Text("Open")
        }
    }
}
