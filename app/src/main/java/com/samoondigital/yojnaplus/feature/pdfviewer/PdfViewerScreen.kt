package com.samoondigital.yojnaplus.feature.pdfviewer

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.NavigateBefore
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle

@Composable
fun PdfViewerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PdfViewerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pdfView by remember { mutableStateOf<PDFView?>(null) }

    LaunchedEffect(state.requestedPage) {
        val page = state.requestedPage ?: return@LaunchedEffect
        pdfView?.jumpTo(page, true)
        viewModel.consumeRequestedPage()
    }

    LaunchedEffect(state.requestedZoom) {
        val zoom = state.requestedZoom ?: return@LaunchedEffect
        pdfView?.zoomWithAnimation(zoom)
        viewModel.onZoomChanged(zoom)
        viewModel.consumeRequestedZoom()
    }

    DisposableEffect(Unit) {
        onDispose {
            pdfView?.recycle()
            pdfView = null
        }
    }

    val viewerBackground = if (state.isDarkMode) Color(0xFF090B12) else MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(viewerBackground),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PDFView(viewContext, null).also { view ->
                    pdfView = view
                    view.setBackgroundColor(if (state.isDarkMode) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                    viewModel.setLoading()
                    view.fromUri(state.uri.toUri())
                        .defaultPage(state.currentPage)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .enableAntialiasing(true)
                        .enableAnnotationRendering(true)
                        .spacing(8)
                        .nightMode(state.isDarkMode)
                        .pageFling(false)
                        .pageSnap(false)
                        .autoSpacing(false)
                        .scrollHandle(DefaultScrollHandle(viewContext))
                        .onLoad { pageCount ->
                            viewModel.onLoaded(pageCount)
                            view.zoomTo(state.zoom)
                            view.jumpTo(state.currentPage.coerceIn(0, pageCount - 1), false)
                        }
                        .onPageChange { page, pageCount ->
                            viewModel.onPageChanged(page, pageCount)
                            viewModel.onZoomChanged(view.zoom)
                        }
                        .onPageScroll { _, _ ->
                            viewModel.onZoomChanged(view.zoom)
                        }
                        .onError { error ->
                            viewModel.onError(error.message ?: "Unable to open PDF")
                        }
                        .onPageError { page, error ->
                            viewModel.onError("Unable to render page ${page + 1}: ${error.message.orEmpty()}")
                        }
                        .load()
                }
            },
            update = { view ->
                view.setBackgroundColor(if (state.isDarkMode) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            },
        )

        PdfViewerTopBar(
            title = state.title,
            pageLabel = state.pageLabel,
            onBack = onBack,
            onSearch = viewModel::toggleSearch,
            onShare = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(state.uri))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share PDF"))
            },
        )

        AnimatedVisibility(
            visible = state.isSearchVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 82.dp, start = 16.dp, end = 16.dp),
        ) {
            SearchPanel(
                value = state.searchQuery,
                pageCount = state.pageCount,
                onValueChange = viewModel::updateSearch,
                onGo = viewModel::goToSearchPage,
            )
        }

        PdfViewerBottomBar(
            state = state,
            onPrevious = { viewModel.requestPage(state.currentPage - 1) },
            onNext = { viewModel.requestPage(state.currentPage + 1) },
            onZoomOut = viewModel::zoomOut,
            onZoomIn = viewModel::zoomIn,
            onToggleDark = viewModel::toggleDarkMode,
            onPageSelected = viewModel::requestPage,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 4.dp) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Loading PDF")
                    }
                }
            }
        }

        state.error?.let { error ->
            ErrorPanel(
                message = error,
                onBack = onBack,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun PdfViewerTopBar(
    title: String,
    pageLabel: String,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onShare: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = pageLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Outlined.Search, contentDescription = "Search")
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Outlined.Share, contentDescription = "Share")
            }
        }
    }
}

@Composable
private fun SearchPanel(
    value: String,
    pageCount: Int,
    onValueChange: (String) -> Unit,
    onGo: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("Go to page") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = onGo, enabled = value.toIntOrNull()?.let { it in 1..pageCount } == true) {
                Text("Go")
            }
        }
    }
}

@Composable
private fun PdfViewerBottomBar(
    state: PdfViewerUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    onToggleDark: () -> Unit,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconButton(onClick = onPrevious, enabled = state.currentPage > 0) {
                    Icon(Icons.AutoMirrored.Outlined.NavigateBefore, contentDescription = "Previous page")
                }
                Text(
                    text = state.pageLabel,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onNext, enabled = state.currentPage < state.pageCount - 1) {
                    Icon(Icons.AutoMirrored.Outlined.NavigateNext, contentDescription = "Next page")
                }
                IconButton(onClick = onZoomOut) {
                    Icon(Icons.Outlined.ZoomOut, contentDescription = "Zoom out")
                }
                IconButton(onClick = onZoomIn) {
                    Icon(Icons.Outlined.ZoomIn, contentDescription = "Zoom in")
                }
                IconButton(onClick = onToggleDark) {
                    Icon(Icons.Outlined.DarkMode, contentDescription = "Dark mode")
                }
            }
            AnimatedVisibility(visible = state.pageCount > 1) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.GridView, contentDescription = null, modifier = Modifier.size(18.dp))
                    repeat(state.pageCount.coerceAtMost(80)) { page ->
                        PageThumbnailChip(
                            page = page,
                            selected = page == state.currentPage,
                            onClick = { onPageSelected(page) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PageThumbnailChip(page: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .size(width = 44.dp, height = 34.dp)
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = (page + 1).toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ErrorPanel(message: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        modifier = modifier.padding(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            FilledTonalButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
