package com.samoondigital.yojnaplus.feature.pdfviewer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Composable
fun PdfViewerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PdfViewerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchMessage by remember(state.uri) { mutableStateOf<String?>(null) }
    val documentState by produceState<PdfDocumentState>(PdfDocumentState.Loading, state.uri) {
        value = PdfDocumentState.Loading
        value = runCatching { PdfDocument.open(context, state.uri.toUri()) }
            .fold(
                onSuccess = { PdfDocumentState.Ready(it) },
                onFailure = { PdfDocumentState.Error(it.message ?: "Unable to open PDF") },
            )
    }

    DisposableEffect(documentState) {
        onDispose { (documentState as? PdfDocumentState.Ready)?.document?.close() }
    }

    LaunchedEffect(documentState) {
        when (val document = documentState) {
            is PdfDocumentState.Ready -> viewModel.onLoaded(document.document.pageCount)
            is PdfDocumentState.Error -> viewModel.onError(document.message)
            PdfDocumentState.Loading -> viewModel.setLoading()
        }
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = state.currentPage)
    LaunchedEffect(state.requestedPage) {
        val page = state.requestedPage ?: return@LaunchedEffect
        listState.animateScrollToItem(page)
        viewModel.consumeRequestedPage()
    }

    LaunchedEffect(listState, state.pageCount) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { page ->
                if (state.pageCount > 0) {
                    viewModel.onPageChanged(page.coerceIn(0, state.pageCount - 1), state.pageCount)
                }
            }
    }

    var zoom by remember(state.uri) { mutableFloatStateOf(state.zoom) }
    LaunchedEffect(state.zoom) { zoom = state.zoom }
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        zoom = (zoom * zoomChange).coerceIn(PdfViewerViewModel.MinZoom, PdfViewerViewModel.MaxZoom)
        viewModel.onZoomChanged(zoom)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (state.isDarkMode) Color(0xFF090B12) else MaterialTheme.colorScheme.background),
    ) {
        when (val document = documentState) {
            is PdfDocumentState.Ready -> PdfPages(
                document = document.document,
                zoom = zoom,
                listState = listState,
                onDoubleTap = {
                    zoom = if (zoom > 1.25f) 1f else 2f
                    viewModel.onZoomChanged(zoom)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(transformState),
            )
            is PdfDocumentState.Error -> ErrorPanel(
                message = document.message,
                onBack = onBack,
                modifier = Modifier.align(Alignment.Center),
            )
            PdfDocumentState.Loading -> Unit
        }

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
                message = searchMessage,
                onValueChange = viewModel::updateSearch,
                onSearch = {
                    val document = (documentState as? PdfDocumentState.Ready)?.document ?: return@SearchPanel
                    val query = state.searchQuery.trim()
                    if (query.isBlank()) return@SearchPanel
                    scope.launch {
                        searchMessage = "Searching"
                        val result = document.findText(query, state.currentPage)
                        if (result == null) {
                            searchMessage = if (Build.VERSION.SDK_INT >= 35) "No match found" else "Text search requires Android 15+"
                        } else {
                            searchMessage = null
                            viewModel.requestPage(result)
                        }
                    }
                },
            )
        }

        if (state.isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
private fun PdfPages(
    document: PdfDocument,
    zoom: Float,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val targetWidth = with(density) { (screenWidth - 20.dp).roundToPx().coerceAtLeast(320) }

    LazyColumn(
        state = listState,
        modifier = modifier
            .padding(top = 78.dp)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items((0 until document.pageCount).toList(), key = { it }) { page ->
            val bitmap by produceState<Bitmap?>(null, document, page, targetWidth) {
                value = document.renderPage(page, targetWidth)
            }
            Surface(
                color = Color.White,
                tonalElevation = 1.dp,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .graphicsLayer {
                        scaleX = zoom
                        scaleY = zoom
                    },
            ) {
                if (bitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(180.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp))
                    }
                } else {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Page ${page + 1}",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
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
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
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
                Icon(Icons.Outlined.Search, contentDescription = "Search text")
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
    message: String?,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("Search text") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = onSearch, enabled = value.isNotBlank()) {
                    Text("Search")
                }
            }
            message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun LoadingOverlay() {
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

private sealed interface PdfDocumentState {
    data object Loading : PdfDocumentState
    data class Ready(val document: PdfDocument) : PdfDocumentState
    data class Error(val message: String) : PdfDocumentState
}

private class PdfDocument private constructor(
    private val descriptor: ParcelFileDescriptor,
    private val renderer: PdfRenderer,
) {
    private val mutex = Mutex()
    val pageCount: Int = renderer.pageCount

    suspend fun renderPage(pageIndex: Int, targetWidth: Int): Bitmap = withContext(Dispatchers.IO) {
        mutex.withLock {
            renderer.openPage(pageIndex).use { page ->
                val scale = targetWidth.toFloat() / page.width.toFloat()
                val targetHeight = (page.height * scale).toInt().coerceAtLeast(1)
                Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
                    bitmap.eraseColor(AndroidColor.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                }
            }
        }
    }

    suspend fun findText(query: String, startPage: Int): Int? = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < 35) return@withContext null
        mutex.withLock {
            val start = startPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            val pages = (start until pageCount) + (0 until start)
            pages.firstOrNull { pageIndex ->
                renderer.openPage(pageIndex).use { page ->
                    page.searchText(query).isNotEmpty()
                }
            }
        }
    }

    fun close() {
        renderer.close()
        descriptor.close()
    }

    companion object {
        fun open(context: Context, uri: Uri): PdfDocument {
            val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
                ?: throw IllegalStateException("Unable to open PDF file")
            return PdfDocument(descriptor, PdfRenderer(descriptor))
        }
    }
}
