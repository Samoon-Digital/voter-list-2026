package com.samoondigital.yojnaplus.feature.search

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.core.common.UiState
import com.samoondigital.yojnaplus.core.ui.components.AppToolbar
import com.samoondigital.yojnaplus.core.ui.components.PrimaryButton
import com.samoondigital.yojnaplus.domain.model.INDIA_STATES
import com.samoondigital.yojnaplus.domain.model.SearchType
import com.samoondigital.yojnaplus.domain.model.Voter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectoralSearchScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ElectoralSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                // ─── Search type tabs ────────────────────────────────────────
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

            // ─── Primary query field ─────────────────────────────────────────
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
                        keyboardType = if (state.selectedType == SearchType.MOBILE) {
                            KeyboardType.Number
                        } else {
                            KeyboardType.Text
                        },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ─── Name/DOB extra fields ───────────────────────────────────────
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
                item {
                    GenderDropdown(
                        selected = state.gender,
                        onSelect = viewModel::onGenderChange,
                    )
                }
                item {
                    StateDropdown(
                        selectedCode = state.selectedStateCd,
                        onSelect = viewModel::onStateChange,
                    )
                }
            }

            // ─── State dropdown for Mobile (optional) ────────────────────────
            if (state.selectedType == SearchType.MOBILE && !state.otpStep) {
                item {
                    StateDropdown(
                        label = "State (optional)",
                        selectedCode = state.selectedStateCd,
                        onSelect = viewModel::onStateChange,
                    )
                }
            }

            // ─── Captcha section (hidden once OTP step is active) ────────────
            if (!state.otpStep) {
                item {
                    CaptchaSection(
                        captchaState = state.captcha,
                        captchaInput = state.captchaInput,
                        onCaptchaInputChange = viewModel::onCaptchaInputChange,
                        onRefresh = viewModel::refreshCaptcha,
                    )
                }
            }

            // ─── Send OTP / Search button ────────────────────────────────────
            if (!state.otpStep) {
                item {
                    PrimaryButton(
                        text = if (state.selectedType == SearchType.MOBILE) "Send OTP" else "Search",
                        onClick = viewModel::onSearch,
                    )
                }
                // Show OTP send state errors
                if (state.otpSendState is UiState.Error) {
                    item {
                        Text(
                            text = (state.otpSendState as UiState.Error).message,
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
            }

            // ─── OTP input (Mobile only, after OTP sent) ─────────────────────
            if (state.otpStep) {
                item {
                    Text(
                        "OTP sent to ${state.query}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item {
                    OutlinedTextField(
                        value = state.otp,
                        onValueChange = viewModel::onOtpChange,
                        label = { Text("Enter OTP") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton(
                            text = "Verify OTP",
                            onClick = viewModel::onVerifyOtp,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = {
                            viewModel.onTypeChange(SearchType.MOBILE)
                        }) { Text("Resend") }
                    }
                }
            }

            // ─── Results ────────────────────────────────────────────────────
            item { Spacer(Modifier.height(4.dp)) }

            when (val results = state.results) {
                is UiState.Idle -> Unit

                is UiState.Loading -> item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> item {
                    Text(
                        text = results.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                is UiState.Success -> {
                    if (results.data.isEmpty()) {
                        item {
                            Text(
                                "No voter records found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(results.data, key = { it.epicNumber }) { voter ->
                            VoterResultCard(voter = voter, onDownloadPdf = { epicNo ->
                                val url = "https://electoralsearch.eci.gov.in/"
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    },
                                )
                            })
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ─── Captcha Section ─────────────────────────────────────────────────────────

@Composable
private fun CaptchaSection(
    captchaState: UiState<com.samoondigital.yojnaplus.domain.model.CaptchaData>,
    captchaInput: String,
    onCaptchaInputChange: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
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
                            Text("?", style = MaterialTheme.typography.titleLarge)
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
            label = { Text("Enter Captcha") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(label) },
                onClick = { onSelect("NA"); expanded = false },
            )
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
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

// ─── Voter Result Card ────────────────────────────────────────────────────────

@Composable
private fun VoterResultCard(voter: Voter, onDownloadPdf: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(voter.name, style = MaterialTheme.typography.titleMedium)
            if (voter.relativeName.isNotBlank()) {
                Text(
                    "S/o D/o W/o: ${voter.relativeName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(Modifier.height(6.dp))
            VoterInfoRow("EPIC", voter.epicNumber)
            VoterInfoRow("Gender / Age", "${voter.gender}, ${voter.age} yrs")
            if (voter.stateName.isNotBlank()) VoterInfoRow("State", voter.stateName)
            VoterInfoRow(
                "Assembly",
                voter.assembly.ifBlank { "—" },
            )
            VoterInfoRow(
                "Part / Serial",
                "${voter.partNumber} / ${voter.serialNumber}",
            )
            if (voter.pollingStation.isNotBlank()) {
                VoterInfoRow("Polling Station", voter.pollingStation)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { onDownloadPdf(voter.epicNumber) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Download Voter Slip")
            }
        }
    }
}

@Composable
private fun VoterInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp),
        )
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
