package com.samoondigital.yojnaplus.feature.downloads

import android.app.Activity

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.ads.InterstitialAdManager
import com.samoondigital.yojnaplus.core.ui.components.AdMobNativeAd
import com.samoondigital.yojnaplus.core.ui.components.AppToolbar
import com.samoondigital.yojnaplus.feature.downloads.data.DownloadStatusEntity
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    onOpenPdf: (uri: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DownloadsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is DownloadsEvent.OpenPdf -> InterstitialAdManager.showIfAvailable(activity) {
                    onOpenPdf(event.uri, event.title)
                }
            }
        }
    }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppToolbar(title = "Downloads", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        DownloadsContent(
            state = state,
            onSearch = viewModel::updateSearch,
            onSync = viewModel::syncNow,
            onOpen = viewModel::open,
            onShare = viewModel::share,
            onRename = viewModel::requestRename,
            onDelete = viewModel::delete,
            onRetry = viewModel::retry,
            onCancel = viewModel::cancel,
            onPause = viewModel::pause,
            onResume = viewModel::resume,
            modifier = Modifier.padding(padding),
        )
    }

    state.renameTarget?.let { target ->
        RenameDownloadDialog(
            fileName = target.fileName,
            onDismiss = viewModel::dismissRename,
            onConfirm = { viewModel.rename(target.id, it) },
        )
    }
}

@Composable
private fun DownloadsContent(
    state: DownloadsUiState,
    onSearch: (String) -> Unit,
    onSync: () -> Unit,
    onOpen: (String) -> Unit,
    onShare: (String) -> Unit,
    onRename: (DownloadItemUi) -> Unit,
    onDelete: (String) -> Unit,
    onRetry: (String) -> Unit,
    onCancel: (String) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onSearch,
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text("Search PDFs") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onSync) {
                Icon(Icons.Outlined.Sync, contentDescription = "Sync downloads")
            }
        }

        AnimatedContent(
            targetState = when {
                state.isLoading -> "loading"
                state.isEmpty -> "empty"
                else -> "list"
            },
            label = "downloads-state",
        ) { target ->
            when (target) {
                "loading" -> LoadingState()
                "empty" -> Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        EmptyState(hasQuery = state.query.isNotBlank())
                    }
                    AdMobNativeAd(
                        placementKey = "downloads-empty-${state.query.isNotBlank()}",
                        modifier = Modifier.padding(bottom = 18.dp),
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Text(
                            text = "${state.downloads.size} PDFs",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    itemsIndexed(state.downloads, key = { _, it -> it.id }) { index, download ->
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DownloadCard(
                                download = download,
                                onOpen = { onOpen(download.id) },
                                onShare = { onShare(download.id) },
                                onRename = { onRename(download) },
                                onDelete = { onDelete(download.id) },
                                onRetry = { onRetry(download.id) },
                                onCancel = { onCancel(download.id) },
                                onPause = { onPause(download.id) },
                                onResume = { onResume(download.id) },
                            )
                            if (index == 0) {
                                AdMobNativeAd(placementKey = "downloads-after-first-${download.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(hasQuery: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (hasQuery) "No matching PDFs" else "No downloaded PDFs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (hasQuery) "Try another search" else "Completed downloads will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DownloadCard(
    download: DownloadItemUi,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    ElevatedCard(
        onClick = onOpen,
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.PictureAsPdf,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                DownloadActionMenu(
                    download = download,
                    onOpen = onOpen,
                    onShare = onShare,
                    onRename = onRename,
                    onDelete = onDelete,
                    onRetry = onRetry,
                    onCancel = onCancel,
                    onPause = onPause,
                    onResume = onResume,
                )
            }
            Text(
                text = download.downloadedAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AnimatedVisibility(visible = download.status == DownloadStatusEntity.Downloading) {
                LinearProgressIndicator(
                    progress = { download.progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            download.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun DownloadActionMenu(
    download: DownloadItemUi,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "Download actions")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ActionItem("Open", Icons.AutoMirrored.Outlined.OpenInNew) {
                expanded = false
                onOpen()
            }
            ActionItem("Share", Icons.Outlined.Share) {
                expanded = false
                onShare()
            }
            ActionItem("Rename", Icons.Outlined.DriveFileRenameOutline) {
                expanded = false
                onRename()
            }
            when (download.status) {
                DownloadStatusEntity.Downloading -> {
                    ActionItem("Pause", Icons.Outlined.PauseCircle) {
                        expanded = false
                        onPause()
                    }
                    ActionItem("Cancel", Icons.Outlined.Cancel) {
                        expanded = false
                        onCancel()
                    }
                }
                DownloadStatusEntity.Failed, DownloadStatusEntity.Cancelled, DownloadStatusEntity.Missing -> {
                    ActionItem("Retry", Icons.Outlined.Refresh) {
                        expanded = false
                        onRetry()
                    }
                    ActionItem("Resume", Icons.Outlined.PlayCircle) {
                        expanded = false
                        onResume()
                    }
                }
                else -> Unit
            }
            ActionItem("Delete", Icons.Outlined.Delete) {
                expanded = false
                onDelete()
            }
        }
    }
}

@Composable
private fun ActionItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        onClick = onClick,
    )
}

@Composable
private fun RenameDownloadDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember(fileName) { mutableStateOf(fileName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename PDF") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(value) }, enabled = value.isNotBlank()) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
