package com.samoondigital.yojnaplus.feature.results

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.samoondigital.yojnaplus.core.ui.components.AppToolbar
import com.samoondigital.yojnaplus.domain.model.Voter

@Composable
fun VoterResultsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VoterResultsViewModel = hiltViewModel(),
) {
    val voters by viewModel.results.collectAsState(initial = emptyList())
    val context = LocalContext.current

    Scaffold(
        topBar = { AppToolbar(title = "Search Results", onBack = onBack) },
    ) { padding ->
        if (voters.isEmpty()) {
            Box(
                modifier = modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No voter records found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                item {
                    Text(
                        "${voters.size} record${if (voters.size == 1) "" else "s"} found",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(voters, key = { it.epicNumber }) { voter ->
                    VoterCard(
                        voter = voter,
                        onDownload = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://electoralsearch.eci.gov.in/"),
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
                            )
                        },
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun VoterCard(voter: Voter, onDownload: () -> Unit) {
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
            VoterInfoRow("EPIC No.", voter.epicNumber)
            VoterInfoRow("Gender / Age", "${voter.gender}, ${voter.age} yrs")
            if (voter.stateName.isNotBlank()) VoterInfoRow("State", voter.stateName)
            VoterInfoRow("Assembly", voter.assembly.ifBlank { "—" })
            VoterInfoRow("Part / Serial", "${voter.partNumber} / ${voter.serialNumber}")
            if (voter.pollingStation.isNotBlank()) {
                VoterInfoRow("Polling Station", voter.pollingStation)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onDownload,
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
            modifier = Modifier.width(120.dp),
        )
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
