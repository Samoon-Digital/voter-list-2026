package com.samoondigital.yojnaplus.feature.pdfviewer

import com.samoondigital.yojnaplus.core.ui.components.stableStatusBarsPadding
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.pdf.view.search.PdfSearchView
import androidx.pdf.viewer.fragment.PdfViewerFragment
import com.rajat.pdfviewer.PdfRendererView
import com.rajat.pdfviewer.util.CacheStrategy
import com.samoondigital.yojnaplus.core.ui.components.AdMobBannerAd
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
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val useAndroidXPdfViewer = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    var androidXPdfFragment by remember(state.uri) { mutableStateOf<PdfViewerFragment?>(null) }
    var isAndroidXPdfSearchActive by remember(state.uri) { mutableStateOf(false) }
    val sourceState by produceState<PdfSourceState>(PdfSourceState.Loading, state.uri) {
        value = PdfSourceState.Loading
        value = runCatching { PdfSourceState.Ready(preparePdfViewerUri(context, state.uri.toUri())) }
            .getOrElse { error -> PdfSourceState.Error(error.message ?: "Unable to open PDF") }
    }

    LaunchedEffect(sourceState) {
        when (val source = sourceState) {
            PdfSourceState.Loading -> viewModel.setLoading()
            is PdfSourceState.Ready -> viewModel.onLoaded(pageCount = 0)
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
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LegacyPdfPages(
                        uri = source.uri,
                        currentPage = state.currentPage,
                        onLoading = viewModel::setLoading,
                        onLoaded = viewModel::onLoaded,
                        onPageChanged = viewModel::onPageChanged,
                        onError = viewModel::onError,
                        modifier = Modifier.fillMaxSize(),
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
            pageLabel = state.pageLabel,
            onBack = onBack,
            onSearch = if (useAndroidXPdfViewer) {
                {
                    isAndroidXPdfSearchActive = true
                    runCatching { androidXPdfFragment?.isTextSearchActive = true }
                }
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

        PdfViewerBottomBanner()

        if (useAndroidXPdfViewer) {
            AndroidXPdfSearchBarHost(
                fragment = androidXPdfFragment,
                isActive = isAndroidXPdfSearchActive,
                onClosed = { isAndroidXPdfSearchActive = false },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .stableStatusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun AndroidXPdfSearchBarHost(
    fragment: PdfViewerFragment?,
    isActive: Boolean,
    onClosed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isActive) 52.dp else 0.dp),
        factory = { context -> FrameLayout(context) },
        update = { host ->
            if (!isActive) {
                host.visibility = View.GONE
                host.getChildAt(0)?.visibility = View.GONE
                return@AndroidView
            }
            val searchView = fragment?.searchViewOrNull()
            host.visibility = if (searchView != null) View.VISIBLE else View.GONE
            if (searchView == null) {
                host.removeAllViews()
                return@AndroidView
            }
            if (searchView.parent !== host) {
                (searchView.parent as? ViewGroup)?.removeView(searchView)
                host.removeAllViews()
                host.addView(
                    searchView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER,
                    ),
                )
            }
            searchView.visibility = View.VISIBLE
            searchView.closeButton.setOnClickListener {
                fragment.isTextSearchActive = false
                onClosed()
            }
        },
    )
}

@Composable
private fun BoxScope.PdfViewerBottomBanner() {
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    AdMobBannerAd(
        placementKey = "pdf-viewer-bottom-banner",
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = -navigationBarHeight)
            .fillMaxWidth(),
    )
}

@Composable
private fun AndroidXPdfPages(
    uri: Uri,
    onReady: (PdfViewerFragment) -> Unit,
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
    val containerId = remember(uri) { View.generateViewId() }
    val fragmentTag = remember(uri) { "androidx-pdf-viewer-${uri.hashCode()}" }

    DisposableEffect(fragmentManager, fragmentTag) {
        onDispose {
            val fragment = fragmentManager.findFragmentByTag(fragmentTag) ?: return@onDispose
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
    }

    AndroidView(
        modifier = modifier.padding(top = 78.dp),
        factory = { viewContext ->
            FrameLayout(viewContext).apply { id = containerId }
        },
        update = { container ->
            val fragment = (fragmentManager.findFragmentByTag(fragmentTag) as? PdfViewerFragment)
                ?: PdfViewerFragment().also { newFragment ->
                    fragmentManager.beginTransaction()
                        .replace(container.id, newFragment, fragmentTag)
                        .commitNowAllowingStateLoss()
                }
            if (fragment.documentUri != uri) {
                fragment.documentUri = uri
            }
            onReady(fragment)
        },
    )
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
        modifier = modifier.padding(top = 78.dp),
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
private fun PdfViewerTopBar(
    title: String,
    pageLabel: String,
    onBack: () -> Unit,
    onSearch: (() -> Unit)?,
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
                    .stableStatusBarsPadding()
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
                if (onSearch != null) {
                    IconButton(onClick = onSearch) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search text",
                            tint = Color.White,
                        )
                    }
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

private fun PdfViewerFragment.searchViewOrNull(): PdfSearchView? =
    runCatching {
        PdfViewerFragment::class.java
            .getDeclaredMethod("getPdfSearchView")
            .apply { isAccessible = true }
            .invoke(this) as? PdfSearchView
    }.getOrNull()

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
private val PdfViewerPurpleDark = Color(0xFF2B137F)
private val PdfViewerPurple = Color(0xFF4B2DBF)
