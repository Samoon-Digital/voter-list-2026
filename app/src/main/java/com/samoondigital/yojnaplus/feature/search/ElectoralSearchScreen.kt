package com.samoondigital.yojnaplus.feature.search

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.core.common.UiState
import com.samoondigital.yojnaplus.core.ui.components.AppToolbar
import com.samoondigital.yojnaplus.core.ui.components.PrimaryButton
import com.samoondigital.yojnaplus.domain.model.INDIA_STATES
import com.samoondigital.yojnaplus.domain.model.RecentSearchItem
import com.samoondigital.yojnaplus.domain.model.SearchType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectoralSearchScreen(
    onBack: () -> Unit,
    onNavigateToResults: (searchType: String, query: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ElectoralSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()

    // Observe navigation events
    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is NavEvent.GoToResults -> onNavigateToResults(event.searchType, event.query)
            }
        }
    }

    // Captcha dialog
    if (state.showCaptchaDialog) {
        CaptchaDialog(
            captchaState = state.captcha,
            captchaInput = state.captchaInput,
            onCaptchaInputChange = viewModel::onCaptchaInputChange,
            onRefresh = viewModel::refreshCaptcha,
            onSubmit = viewModel::onCaptchaSubmit,
            onDismiss = viewModel::onCaptchaDialogDismiss,
        )
    }

    // OTP dialog
    if (state.showOtpDialog) {
        OtpDialog(
            mobile = state.query,
            otp = state.otp,
            onOtpChange = viewModel::onOtpChange,
            onVerify = viewModel::onVerifyOtp,
            onDismiss = viewModel::onOtpDialogDismiss,
            isLoading = state.searchState is UiState.Loading,
        )
    }

    Scaffold(
        topBar = { AppToolbar(title = "Electoral Search", onBack = onBack) },
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SearchType.entries.forEach { type ->
                        FilterChip(
                            selected = state.selectedType == type,
                            onClick = { viewModel.onTypeChange(type) },
                            label = { Text(type.label) },
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    label = {
                        Text(
                            when (state.selectedType) {
                                SearchType.MOBILE -> "Mobile Number"
                                SearchType.EPIC -> "EPIC Number"
                                SearchType.NAME_DOB -> "First Name"
                            },
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (state.selectedType == SearchType.MOBILE) KeyboardType.Number
                        else KeyboardType.Text,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (state.selectedType == SearchType.NAME_DOB) {
                item {
                    OutlinedTextField(
                        value = state.lastName,
                        onValueChange = viewModel::onLastNameChange,
                        label = { Text("Last Name (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    OutlinedTextField(
                        value = state.relationName,
                        onValueChange = viewModel::onRelationNameChange,
                        label = { Text("Relative's First Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    OutlinedTextField(
                        value = state.dob,
                        onValueChange = viewModel::onDobChange,
                        label = { Text("Date of Birth (DD/MM/YYYY)") },
                        placeholder = { Text("01/01/1990") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item { GenderDropdown(selected = state.gender, onSelect = viewModel::onGenderChange) }
                item { StateDropdown(selectedCode = state.selectedStateCd, onSelect = viewModel::onStateChange) }
            }

            if (state.selectedType == SearchType.MOBILE) {
                item {
                    StateDropdown(
                        label = "State (optional)",
                        selectedCode = state.selectedStateCd,
                        onSelect = viewModel::onStateChange,
                    )
                }
            }

            item {
                PrimaryButton(
                    text = if (state.selectedType == SearchType.MOBILE) "Send OTP" else "Search",
                    onClick = viewModel::onSearch,
                )
            }

            when (val s = state.searchState) {
                is UiState.Loading -> item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> item {
                    Text(
                        s.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                else -> Unit
            }

            if (state.otpSendState is UiState.Error) {
                item {
                    Text(
                        (state.otpSendState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (state.otpSendState is UiState.Loading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Recent searches section
            if (recentSearches.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Recent Searches", style = MaterialTheme.typography.labelLarge)
                        TextButton(onClick = viewModel::clearRecent) { Text("Clear") }
                    }
                }
                recentSearches.forEach { searchItem ->
                    item(key = searchItem.query) { RecentSearchRow(item = searchItem) }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ─── Captcha Dialog ───────────────────────────────────────────────────────────

@Composable
private fun CaptchaDialog(
    captchaState: UiState<com.samoondigital.yojnaplus.domain.model.CaptchaData>,
    captchaInput: String,
    onCaptchaInputChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Captcha") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 160.dp, height = 56.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (captchaState) {
                            is UiState.Loading -> CircularProgressIndicator(Modifier.size(28.dp))
                            is UiState.Success -> {
                                val bitmap = remember(captchaState.data.imageBase64) {
                                    runCatching {
                                        val bytes = Base64.decode(captchaState.data.imageBase64, Base64.DEFAULT)
                                        if (bytes.isEmpty()) null
                                        else BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                    }.getOrNull()
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Captcha",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Captcha",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            "Image unavailable",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                            is UiState.Error -> Text(
                                "Failed to load",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                            is UiState.Idle -> Unit
                        }
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh captcha")
                    }
                }
                OutlinedTextField(
                    value = captchaInput,
                    onValueChange = onCaptchaInputChange,
                    label = { Text("Enter Captcha Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { TextButton(onClick = onSubmit) { Text("Submit") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ─── OTP Dialog ───────────────────────────────────────────────────────────────

@Composable
private fun OtpDialog(
    mobile: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter OTP") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "OTP sent to $mobile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = otp,
                    onValueChange = onOtpChange,
                    label = { Text("OTP") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (isLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(28.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onVerify, enabled = !isLoading) { Text("Verify") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ─── State Dropdown ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StateDropdown(
    selectedCode: String,
    onSelect: (String) -> Unit,
    label: String = "Select State",
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = INDIA_STATES.find { it.code == selectedCode }?.name ?: label

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = if (selectedCode == "NA") "" else selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(label) }, onClick = { onSelect("NA"); expanded = false })
            INDIA_STATES.forEach { state ->
                DropdownMenuItem(
                    text = { Text(state.name) },
                    onClick = { onSelect(state.code); expanded = false },
                )
            }
        }
    }
}

// ─── Gender Dropdown ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdown(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("M" to "Male", "F" to "Female", "T" to "Third Gender")
    var expanded by remember { mutableStateOf(false) }
    val label = options.find { it.first == selected }?.second ?: "Select Gender"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Gender") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onSelect(code); expanded = false },
                )
            }
        }
    }
}

// ─── Recent Search Row ────────────────────────────────────────────────────────

@Composable
private fun RecentSearchRow(item: RecentSearchItem) {
    val typeLabel = when (item.searchType) {
        "MOBILE" -> "Mobile"
        "EPIC" -> "EPIC"
        "NAME_DOB" -> "Name/DOB"
        else -> item.searchType
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                item.query,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                typeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
