package com.samoondigital.yojnaplus.feature.pdfviewer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import io.legere.pdfiumandroid.PdfDocument as PdfiumDocument
import io.legere.pdfiumandroid.PdfiumCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun PdfViewerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PdfViewerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchMessage by remember(state.uri) { mutableStateOf<String?>(null) }
    val documentState by produceState<PdfDocumentState>(PdfDocumentState.Loading, state.uri) {
        value = PdfDocumentState.Loading
        value = try {
            PdfDocumentState.Ready(PdfDocument.open(context, state.uri.toUri()))
        } catch (error: Throwable) {
            PdfDocumentState.Error(error.message ?: "Unable to open PDF")
        }
    }

    val readyDocument = (documentState as? PdfDocumentState.Ready)?.document
    DisposableEffect(readyDocument) {
        onDispose { readyDocument?.close() }
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
                            searchMessage = "No match found"
                        } else {
                            searchMessage = "Found on page ${result + 1}"
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
            val pageState by produceState<PageRenderState>(PageRenderState.Loading, document, page, targetWidth) {
                value = document.renderPage(page, targetWidth)
                    ?.let(PageRenderState::Ready)
                    ?: PageRenderState.Error("Unable to render page ${page + 1}")
            }
            val bitmap = (pageState as? PageRenderState.Ready)?.bitmap
            DisposableEffect(bitmap) {
                onDispose { bitmap?.recycle() }
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
                when (val renderedPage = pageState) {
                    PageRenderState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .size(180.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp))
                        }
                    }
                    is PageRenderState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .size(180.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = renderedPage.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    is PageRenderState.Ready -> {
                        Image(
                            bitmap = renderedPage.bitmap.asImageBitmap(),
                            contentDescription = "Page ${page + 1}",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
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
        color = Color.Transparent,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PdfViewerPurpleDark, PdfViewerPurple, Color(0xFF2E1B98)),
                        start = Offset.Zero,
                        end = Offset(850f, 260f),
                    ),
                ),
        ) {
            PdfViewerHeaderArtwork(modifier = Modifier.matchParentSize())
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                    )
                    Text(
                        text = pageLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                }
                IconButton(onClick = onSearch) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search text",
                        tint = Color.White,
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfViewerHeaderArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val wave = Path().apply {
            moveTo(0f, h * 0.58f)
            cubicTo(w * 0.18f, h * 0.28f, w * 0.32f, h * 0.82f, w * 0.52f, h * 0.52f)
            cubicTo(w * 0.68f, h * 0.30f, w * 0.82f, h * 0.78f, w, h * 0.46f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(wave, Color(0xFF5B49D7).copy(alpha = 0.26f))

        val zigzag = Path().apply {
            moveTo(w * 0.08f, h * 0.18f)
            lineTo(w * 0.20f, h * 0.08f)
            lineTo(w * 0.32f, h * 0.20f)
            lineTo(w * 0.45f, h * 0.10f)
            lineTo(w * 0.58f, h * 0.22f)
            lineTo(w * 0.72f, h * 0.12f)
            lineTo(w * 0.86f, h * 0.24f)
        }
        drawPath(zigzag, Color.White.copy(alpha = 0.08f))
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

private sealed interface PageRenderState {
    data object Loading : PageRenderState
    data class Ready(val bitmap: Bitmap) : PageRenderState
    data class Error(val message: String) : PageRenderState
}

private class PdfDocument private constructor(
    private val cacheFile: File,
    private val descriptor: ParcelFileDescriptor,
    private val document: PdfiumDocument,
) {
    private val mutex = Mutex()
    val pageCount: Int = document.getPageCount()

    suspend fun renderPage(pageIndex: Int, targetWidth: Int): Bitmap? = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (closed.get()) return@withLock null
            runCatching {
                document.openPage(pageIndex).use { page ->
                    renderPageBitmap(page, targetWidth)
                }
            }.getOrElse { error ->
                Log.w(PdfViewerLogTag, "Unable to render PDF page ${pageIndex + 1}", error)
                null
            }
        }
    }

    suspend fun findText(query: String, startPage: Int): Int? = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (closed.get()) return@withLock null
            val normalizedQuery = query.normalizedForSearch()
            val start = startPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            val pages = (start until pageCount) + (0 until start)
            findWithPlatformRenderer(normalizedQuery, pages)
                ?: findWithPdfium(normalizedQuery, pages)
                ?: findWithOcr(normalizedQuery, pages)
        }
    }

    private fun findWithPlatformRenderer(query: String, pages: Iterable<Int>): Int? {
        if (Build.VERSION.SDK_INT < 35) return null
        return runCatching {
            val searchDescriptor = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                ?: return null
            searchDescriptor.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    pages.firstOrNull { pageIndex ->
                        renderer.openPage(pageIndex).use { page ->
                            page.searchText(query).isNotEmpty()
                        }
                    }
                }
            }
        }.getOrElse { error ->
            Log.w(PdfViewerLogTag, "Unable to search PDF with platform renderer", error)
            null
        }
    }

    private fun findWithPdfium(query: String, pages: Iterable<Int>): Int? {
        return pages.firstOrNull { pageIndex ->
            runCatching {
                document.openPage(pageIndex).use { page ->
                    page.openTextPage().use { textPage ->
                        textPage.findStart(query, emptySet(), 0)?.use { result ->
                            result.findNext()
                        } == true
                    }
                }
            }.getOrElse { error ->
                Log.w(PdfViewerLogTag, "Unable to search PDF page ${pageIndex + 1}", error)
                false
            }
        }
    }

    private fun findWithOcr(query: String, pages: Iterable<Int>): Int? {
        val recognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        return try {
            pages.firstOrNull { pageIndex ->
                if (closed.get()) return@firstOrNull false
                runCatching {
                    document.openPage(pageIndex).use { page ->
                        val bitmap = renderPageBitmap(page, OcrRenderWidth)
                        try {
                            val image = InputImage.fromBitmap(bitmap, 0)
                            val result = Tasks.await(recognizer.process(image))
                            result.text.normalizedForSearch().contains(query, ignoreCase = true)
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }.getOrElse { error ->
                    Log.w(PdfViewerLogTag, "Unable to OCR PDF page ${pageIndex + 1}", error)
                    false
                }
            }
        } finally {
            recognizer.close()
        }
    }

    private fun renderPageBitmap(page: io.legere.pdfiumandroid.PdfPage, targetWidth: Int): Bitmap {
        val pageWidth = page.getPageWidthPoint().coerceAtLeast(1)
        val pageHeight = page.getPageHeightPoint().coerceAtLeast(1)
        val scale = targetWidth.toFloat() / pageWidth.toFloat()
        val targetHeight = (pageHeight * scale).toInt().coerceAtLeast(1)
        return Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
            bitmap.eraseColor(AndroidColor.WHITE)
            page.renderPageBitmap(
                bitmap = bitmap,
                startX = 0,
                startY = 0,
                drawSizeX = targetWidth,
                drawSizeY = targetHeight,
                renderAnnot = true,
            )
        }
    }

    fun close() {
        if (closed.compareAndSet(false, true)) {
            document.close()
            runCatching { descriptor.close() }
            runCatching { cacheFile.delete() }
        }
    }

    private val closed = AtomicBoolean(false)

    companion object {
        suspend fun open(context: Context, uri: Uri): PdfDocument = withContext(Dispatchers.IO) {
            val cacheFile = copyToViewerCache(context, uri)
            val descriptor = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                ?: throw IllegalStateException("Unable to open PDF file")
            try {
                val document = PdfiumCore(context.applicationContext).newDocument(descriptor)
                if (document.getPageCount() <= 0) {
                    document.close()
                    throw IllegalStateException("PDF has no pages")
                }
                PdfDocument(cacheFile, descriptor, document)
            } catch (error: Throwable) {
                runCatching { descriptor.close() }
                runCatching { cacheFile.delete() }
                throw IllegalStateException(error.message ?: "Unable to render PDF", error)
            }
        }

        private fun copyToViewerCache(context: Context, uri: Uri): File {
            val dir = File(context.cacheDir, "pdf_viewer").apply { mkdirs() }
            val target = File(dir, "viewer-${uri.toString().hashCode()}.pdf")
            if (uri.scheme == "http" || uri.scheme == "https") {
                copyRemotePdf(uri, target)
            } else {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } ?: throw IllegalStateException("Unable to read PDF file")
            }

            if (target.length() == 0L) {
                target.delete()
                throw IllegalStateException("PDF file is empty")
            }
            if (!target.looksLikePdf()) {
                target.delete()
                throw IllegalStateException("Invalid PDF file")
            }
            return target
        }

        private fun copyRemotePdf(uri: Uri, target: File) {
            val connection = URL(uri.toString().replace(" ", "%20")).openConnection() as HttpURLConnection
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/126 Mobile Safari/537.36",
            )
            connection.inputStream.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            connection.disconnect()
        }

        private fun File.looksLikePdf(): Boolean {
            val header = ByteArray(1024)
            val read = inputStream().use { it.read(header) }.coerceAtLeast(0)
            val text = header.decodeToString(endIndex = read)
            return text.contains("%PDF-")
        }
    }
}

private const val PdfViewerLogTag = "PdfViewerScreen"
private val PdfViewerPurpleDark = Color(0xFF2B137F)
private val PdfViewerPurple = Color(0xFF4B2DBF)
private const val OcrRenderWidth = 1800

private fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFC)
        .replace("\u200C", "")
        .replace("\u200D", "")
