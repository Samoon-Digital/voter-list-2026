package com.samoondigital.yojnaplus.feature.downloads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.ads.InterstitialAdManager
import com.samoondigital.yojnaplus.core.ui.components.AppToolbar
import com.samoondigital.yojnaplus.feature.downloads.data.DownloadStatusEntity
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    onDownloadList: () -> Unit,
    onOpenPdf: (uri: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    bottomBannerHeight: Dp = 0.dp,
    viewModel: DownloadsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    var deleteTarget by remember { mutableStateOf<DownloadItemUi?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(onBack = onBack)

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
        topBar = { AppToolbar(title = stringResource(R.string.ui_downloads_title), onBack = onBack) },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = bottomBannerHeight + 12.dp),
            )
        },
    ) { padding ->
        DownloadsContent(
            state = state,
            onSearch = viewModel::updateSearch,
            onSync = viewModel::syncNow,
            onOpen = viewModel::open,
            onShare = viewModel::share,
            onRename = viewModel::requestRename,
            onDelete = { id -> deleteTarget = state.downloads.firstOrNull { it.id == id } },
            onRetry = { onDownloadList() },
            onDownloadList = onDownloadList,
            onCancel = viewModel::cancel,
            bottomContentPadding = bottomBannerHeight,
            modifier = Modifier.padding(padding),
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.ui_delete_title)) },
            text = { Text(stringResource(R.string.ui_delete_message, target.fileName)) },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(target.id); deleteTarget = null }) { Text(stringResource(R.string.ui_delete)) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.ui_cancel)) } },
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
internal fun DownloadsContent(
    state: DownloadsUiState,
    onSearch: (String) -> Unit,
    onSync: () -> Unit,
    onOpen: (String) -> Unit,
    onShare: (String) -> Unit,
    onRename: (DownloadItemUi) -> Unit,
    onDelete: (String) -> Unit,
    onRetry: (String) -> Unit,
    onDownloadList: () -> Unit,
    onCancel: (String) -> Unit,
    bottomContentPadding: Dp,
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
                placeholder = { Text(stringResource(R.string.ui_search_pdfs)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onSync) {
                Icon(Icons.Outlined.Sync, contentDescription = stringResource(R.string.ui_sync))
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
                "empty" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(hasQuery = state.query.isNotBlank(), onDownloadList = onDownloadList, onClearSearch = { onSearch("") })
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = bottomContentPadding + 18.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.ui_pdf_count, state.downloads.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    itemsIndexed(state.downloads, key = { _, it -> it.id }) { _, download ->
                        DownloadCard(
                            download = download,
                            onOpen = { onOpen(download.id) },
                            onShare = { onShare(download.id) },
                            onRename = { onRename(download) },
                            onDelete = { onDelete(download.id) },
                            onRetry = { onRetry(download.id) },
                            onCancel = { onCancel(download.id) },
                        )
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
private fun EmptyState(hasQuery: Boolean, onDownloadList: () -> Unit, onClearSearch: () -> Unit) {
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
                text = stringResource(if (hasQuery) R.string.ui_no_matching_pdfs else R.string.ui_no_pdfs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(if (hasQuery) R.string.ui_try_search else R.string.ui_no_pdfs_help),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )
            Button(
                onClick = if (hasQuery) onClearSearch else onDownloadList,
                modifier = Modifier.heightIn(min = 56.dp),
            ) { Text(stringResource(if (hasQuery) R.string.ui_clear_search else R.string.ui_download_list)) }
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
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = download.status == DownloadStatusEntity.Completed, onClick = onOpen),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val sourceDescription = download.sourceDescription
                Icon(
                    Icons.Outlined.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(22.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.fileName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (sourceDescription.isNotBlank()) {
                        Text(
                            text = sourceDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                DownloadActionMenu(
                    download = download,
                    onOpen = onOpen,
                    onShare = onShare,
                    onRename = onRename,
                    onDelete = onDelete,
                    onRetry = onRetry,
                    onCancel = onCancel,
                )
            }
            Text(
                text = when (download.status) {
                    DownloadStatusEntity.Completed -> stringResource(R.string.ui_status_saved, download.fileSize)
                    DownloadStatusEntity.Downloading -> stringResource(R.string.ui_status_downloading, download.progress)
                    DownloadStatusEntity.Waiting -> stringResource(R.string.ui_status_waiting)
                    DownloadStatusEntity.Failed -> stringResource(R.string.ui_status_failed)
                    DownloadStatusEntity.Missing -> stringResource(R.string.ui_status_missing)
                    DownloadStatusEntity.Cancelled -> stringResource(R.string.ui_status_cancelled)
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (download.status in listOf(DownloadStatusEntity.Failed, DownloadStatusEntity.Missing)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
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
            if (download.status == DownloadStatusEntity.Completed) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onOpen, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.ui_open_pdf))
                    }
                    TextButton(onClick = onShare, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.ui_share))
                    }
                }
            } else if (download.status in listOf(DownloadStatusEntity.Failed, DownloadStatusEntity.Cancelled, DownloadStatusEntity.Missing)) {
                Button(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.ui_download_again)) }
            }
            download.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        HorizontalDivider()
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
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.ui_actions))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (download.status == DownloadStatusEntity.Completed) {
                ActionItem(stringResource(R.string.ui_open_pdf), Icons.AutoMirrored.Outlined.OpenInNew) {
                    expanded = false
                    onOpen()
                }
                ActionItem(stringResource(R.string.ui_share), Icons.Outlined.Share) {
                    expanded = false
                    onShare()
                }
            }
            ActionItem(stringResource(R.string.ui_rename), Icons.Outlined.DriveFileRenameOutline) {
                expanded = false
                onRename()
            }
            when (download.status) {
                DownloadStatusEntity.Downloading -> {
                    ActionItem(stringResource(R.string.ui_cancel), Icons.Outlined.Cancel) {
                        expanded = false
                        onCancel()
                    }
                }
                DownloadStatusEntity.Failed, DownloadStatusEntity.Cancelled, DownloadStatusEntity.Missing -> {
                    ActionItem(stringResource(R.string.ui_download_again), Icons.Outlined.Refresh) {
                        expanded = false
                        onRetry()
                    }

                }
                else -> Unit
            }
            ActionItem(stringResource(R.string.ui_delete), Icons.Outlined.Delete) {
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
        title = { Text(stringResource(R.string.ui_rename_title)) },
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
                Text(stringResource(R.string.ui_rename))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ui_cancel))
            }
        },
    )
}
private val DownloadItemUi.sourceDescription: String
    get() = listOf(district, assembly, village)
        .map { it.trim() }
        .filter { it.isNotBlank() && it != "--" }
        .distinct()
        .joinToString(" / ")

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
