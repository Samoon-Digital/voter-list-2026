package com.samoondigital.yojnaplus.feature.pdfviewer

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.pdf.PdfDocument
import androidx.pdf.viewer.fragment.PdfViewerFragment
import com.rajat.pdfviewer.PdfRendererView
import com.rajat.pdfviewer.util.CacheStrategy
import com.samoondigital.yojnaplus.R
import com.samoondigital.yojnaplus.core.ui.components.AppToolbar
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun PdfViewerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PdfViewerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val useAndroidXPdfViewer = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    var androidXPdfFragment by remember(state.uri) { mutableStateOf<AppPdfViewerFragment?>(null) }
    var toolbarHeight by remember { mutableStateOf(128.dp) }
    val density = LocalDensity.current
    val handleBack = {
        val fragment = androidXPdfFragment
        if (fragment?.isTextSearchActive == true) {
            fragment.isTextSearchActive = false
        } else {
            onBack()
        }
    }
    BackHandler(onBack = handleBack)
    val sourceState by produceState<PdfSourceState>(PdfSourceState.Loading, state.uri) {
        value = PdfSourceState.Loading
        value = runCatching { PdfSourceState.Ready(preparePdfViewerUri(context, state.uri.toUri())) }
            .getOrElse { error -> PdfSourceState.Error(error.message ?: "Unable to open PDF") }
    }

    LaunchedEffect(sourceState) {
        when (val source = sourceState) {
            PdfSourceState.Loading -> viewModel.setLoading()
            is PdfSourceState.Ready -> {
                if (useAndroidXPdfViewer) viewModel.setLoading() else viewModel.onLoaded(pageCount = 0)
            }
            is PdfSourceState.Error -> viewModel.onError(source.message)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (state.isDarkMode) Color(0xFF090B12) else MaterialTheme.colorScheme.background),
    ) {
        when (val source = sourceState) {
            is PdfSourceState.Ready -> {
                if (useAndroidXPdfViewer) {
                    AndroidXPdfPages(
                        uri = source.uri,
                        onReady = { androidXPdfFragment = it },
                        onLoaded = { viewModel.onLoaded(pageCount = 0) },
                        onError = viewModel::onError,
                        modifier = Modifier.fillMaxSize().padding(top = toolbarHeight),
                    )
                } else {
                    LegacyPdfPages(
                        uri = source.uri,
                        currentPage = state.currentPage,
                        onLoading = viewModel::setLoading,
                        onLoaded = viewModel::onLoaded,
                        onPageChanged = viewModel::onPageChanged,
                        onError = viewModel::onError,
                        modifier = Modifier.fillMaxSize().padding(top = toolbarHeight),
                    )
                }
            }
            is PdfSourceState.Error -> ErrorPanel(
                message = source.message,
                onBack = onBack,
                modifier = Modifier.align(Alignment.Center),
            )
            PdfSourceState.Loading -> Unit
        }
        PdfViewerTopBar(
            title = state.title,
            pageLabel = if (state.pageCount > 0) state.pageLabel else stringResource(R.string.ui_pdf_ready),
            modifier = Modifier.onSizeChanged { size -> toolbarHeight = with(density) { size.height.toDp() } },
            onBack = handleBack,
            onSearch = if (useAndroidXPdfViewer && androidXPdfFragment?.isDocumentReady == true) {
                { androidXPdfFragment?.isTextSearchActive = true }
            } else {
                null
            },
            onShare = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(state.uri))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share PDF"))
            },
        )

        if (state.isLoading) {
            LoadingOverlay()
        }

    }
}

@Composable
private fun AndroidXPdfPages(
    uri: Uri,
    onReady: (AppPdfViewerFragment) -> Unit,
    onLoaded: (Int) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    if (fragmentActivity == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Unable to open PDF viewer")
        }
        return
    }

    val fragmentManager = fragmentActivity.supportFragmentManager
    val containerId = rememberSaveable(uri) { View.generateViewId() }
    val fragmentTag = remember(uri) { "androidx-pdf-viewer-${uri.hashCode()}" }

    DisposableEffect(fragmentManager, fragmentTag) {
        onDispose {
            val fragment = fragmentManager.findFragmentByTag(fragmentTag) ?: return@onDispose
            (fragment as? AppPdfViewerFragment)?.apply {
                onDocumentLoaded = null
                onDocumentError = null
            }
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            FrameLayout(viewContext).apply { id = containerId }
        },
        update = { container ->
            val existing = fragmentManager.findFragmentByTag(fragmentTag) as? AppPdfViewerFragment
            // A restored Fragment can create its view before Compose adds this container.
            // Reattach through FragmentManager so it owns the view and its lifecycle again.
            if (existing != null && existing.view?.parent !== container) {
                fragmentManager.beginTransaction().detach(existing).commitNowAllowingStateLoss()
                fragmentManager.beginTransaction().attach(existing).commitNowAllowingStateLoss()
            }
            val fragment = existing
                ?: AppPdfViewerFragment().also { newFragment ->
                    fragmentManager.beginTransaction()
                        .replace(container.id, newFragment, fragmentTag)
                        .commitNowAllowingStateLoss()
                }
            fragment.onDocumentLoaded = { pageCount ->
                onReady(fragment)
                onLoaded(pageCount)
            }
            fragment.onDocumentError = onError
            fragment.isToolboxVisible = false
            if (fragment.documentUri != uri) {
                fragment.documentUri = uri
            } else if (fragment.isDocumentReady) {
                onReady(fragment)
                onLoaded(0)
            }
        },
    )
}

// Public, no-argument fragment so FragmentManager can restore it after recreation.
class AppPdfViewerFragment : PdfViewerFragment() {
    var isDocumentReady: Boolean = false
        private set
    var onDocumentLoaded: ((Int) -> Unit)? = null
    var onDocumentError: ((String) -> Unit)? = null

    override fun onLoadDocumentSuccess(document: PdfDocument) {
        super.onLoadDocumentSuccess(document)
        isDocumentReady = true
        isToolboxVisible = false
        onDocumentLoaded?.invoke(document.pageCount)
    }

    override fun onLoadDocumentError(error: Throwable) {
        super.onLoadDocumentError(error)
        isDocumentReady = false
        onDocumentError?.invoke(error.message ?: "Unable to open PDF")
    }

    override fun onDestroyView() {
        isDocumentReady = false
        onDocumentLoaded = null
        onDocumentError = null
        super.onDestroyView()
    }
}

@Composable
private fun LegacyPdfPages(
    uri: Uri,
    currentPage: Int,
    onLoading: () -> Unit,
    onLoaded: (Int) -> Unit,
    onPageChanged: (Int, Int) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PdfRendererView(context).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
            }
        },
        update = { view ->
            if (view.tag == uri) return@AndroidView
            view.tag = uri
            view.statusListener = object : PdfRendererView.StatusCallBack {
                override fun onPdfLoadStart() {
                    onLoading()
                }

                override fun onError(error: Throwable) {
                    onError(error.message ?: "Unable to open PDF")
                }

                override fun onPageChanged(currentPage: Int, totalPage: Int) {
                    onPageChanged((currentPage - 1).coerceAtLeast(0), totalPage)
                }

                override fun onPdfRenderSuccess() {
                    val pageCount = runCatching { view.totalPageCount }.getOrDefault(0)
                    onLoaded(pageCount)
                    if (currentPage > 0) {
                        view.jumpToPage(currentPage, smoothScroll = false)
                    }
                }
            }
            view.zoomListener = object : PdfRendererView.ZoomListener {
                override fun onZoomChanged(isZoomedIn: Boolean, scale: Float) = Unit
            }
            view.loadPdf(uri, lifecycleOwner)
        },
    )
}

private fun PdfRendererView.loadPdf(uri: Uri, lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
    when (uri.scheme?.lowercase()) {
        "http", "https" -> initWithUrl(
            url = uri.toString(),
            lifecycleCoroutineScope = lifecycleOwner.lifecycleScope,
            lifecycle = lifecycleOwner.lifecycle,
            cacheStrategy = CacheStrategy.MAXIMIZE_PERFORMANCE,
        )
        "file" -> initWithFile(
            file = File(uri.path.orEmpty()),
            cacheStrategy = CacheStrategy.MAXIMIZE_PERFORMANCE,
        )
        else -> initWithUri(uri)
    }
}

@Composable
internal fun PdfViewerTopBar(
    title: String,
    pageLabel: String,
    onBack: () -> Unit,
    onSearch: (() -> Unit)?,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppToolbar(title = title, subtitle = pageLabel, onBack = onBack, modifier = modifier, titleMaxLines = 1) {
        if (onSearch != null) {
            PdfToolbarAction(label = stringResource(R.string.ui_find),
                description = stringResource(R.string.ui_search_in_pdf), icon = Icons.Outlined.Search, onClick = onSearch)
        }
        PdfToolbarAction(label = stringResource(R.string.ui_share_short),
            description = stringResource(R.string.ui_share), icon = Icons.Outlined.Share, onClick = onShare)
    }
}

@Composable
private fun PdfToolbarAction(
    label: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.semantics { contentDescription = description }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
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

sealed interface PdfSourceState {
    data object Loading : PdfSourceState
    data class Ready(val uri: Uri) : PdfSourceState
    data class Error(val message: String) : PdfSourceState
}

private fun preparePdfViewerUri(context: Context, uri: Uri): Uri {
    return when (uri.scheme?.lowercase()) {
        "http", "https" -> Uri.fromFile(copyRemotePdf(context, uri))
        "content", "file" -> uri
        else -> uri
    }
}

private fun copyRemotePdf(context: Context, uri: Uri): File {
    val dir = File(context.cacheDir, "pdf_viewer").apply { mkdirs() }
    val target = File(dir, "viewer-${uri.toString().hashCode()}.pdf")
    if (target.length() > 0L) return target

    val connection = (URL(uri.toString()).openConnection() as HttpURLConnection).apply {
        connectTimeout = 20_000
        readTimeout = 30_000
        requestMethod = "GET"
        setRequestProperty("User-Agent", "Mozilla/5.0 Android VoterList2026")
        instanceFollowRedirects = true
    }

    try {
        val code = connection.responseCode
        if (code !in 200..299) {
            throw IllegalStateException("Unable to download PDF: HTTP $code")
        }
        connection.inputStream.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        if (target.length() == 0L) {
            target.delete()
            throw IllegalStateException("PDF file is empty")
        }
        return target
    } catch (error: Throwable) {
        target.delete()
        throw error
    } finally {
        connection.disconnect()
    }
}
