package com.samoondigital.yojnaplus.feature.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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

private const val JammuKashmirUrl = "https://ceo.jk.gov.in/namesearch/"
private val WebPurple = Color(0xFF3522A8)
private val WebPurpleDark = Color(0xFF20106F)
private val WebPurpleBright = Color(0xFF7C5CFF)
private val WebSurface = Color(0xFFFCFCFF)
private val WebMuted = Color(0xFF686A8D)

@Composable
fun JammuKashmirWebViewScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var pageTitle by remember { mutableStateOf("Jammu & Kashmir") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleBack() {
        val currentWebView = webView
        if (currentWebView?.canGoBack() == true) {
            currentWebView.goBack()
        } else {
            onBack()
        }
    }

    BackHandler(onBack = ::handleBack)

    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
            webView = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WebTopBar(
                title = pageTitle.ifBlank { "Jammu & Kashmir" },
                progress = progress,
                isLoading = isLoading,
                canGoBack = canGoBack,
                onBack = ::handleBack,
                onRefresh = {
                    errorMessage = null
                    webView?.reload()
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WebSurface),
        ) {
            JammuKashmirWebView(
                onWebViewReady = { webView = it },
                onPageStarted = {
                    isLoading = true
                    errorMessage = null
                    canGoBack = webView?.canGoBack() == true
                },
                onPageFinished = { view, title ->
                    isLoading = false
                    canGoBack = view.canGoBack()
                    pageTitle = title?.takeIf { it.isNotBlank() } ?: "Jammu & Kashmir"
                },
                onProgressChanged = {
                    progress = it
                    isLoading = it in 1..99
                },
                onOpenExternal = { url ->
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                },
                onError = {
                    isLoading = false
                    errorMessage = it
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp),
            )

            errorMessage?.let { message ->
                WebErrorState(
                    message = message,
                    onRetry = {
                        errorMessage = null
                        webView?.loadUrl(JammuKashmirUrl)
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun JammuKashmirWebView(
    onWebViewReady: (WebView) -> Unit,
    onPageStarted: () -> Unit,
    onPageFinished: (WebView, String?) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onOpenExternal: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            runCatching {
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    setBackgroundColor(android.graphics.Color.WHITE)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        settings.safeBrowsingEnabled = true
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    }
                    CookieManager.getInstance().setAcceptCookie(true)
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                            onPageStarted()
                        }

                        override fun onPageFinished(view: WebView, url: String?) {
                            onPageFinished(view, view.title)
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest,
                        ): Boolean {
                            val url = request.url.toString()
                            return if (url.startsWith("http://") || url.startsWith("https://")) {
                                false
                            } else {
                                onOpenExternal(url)
                                true
                            }
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError,
                        ) {
                            if (request.isForMainFrame) {
                                onError(error.description?.toString() ?: "Unable to load page")
                            }
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            onProgressChanged(newProgress)
                        }
                    }
                    onWebViewReady(this)
                    loadUrl(JammuKashmirUrl)
                }
            }.getOrElse { error: Throwable ->
                FrameLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    post { onError(error.message ?: "Android System WebView is not available") }
                }
            }
        },
    )
}
@Composable
private fun WebTopBar(
    title: String,
    progress: Int,
    isLoading: Boolean,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(WebPurpleDark, WebPurple, Color(0xFF2E1B98)),
                    start = Offset.Zero,
                    end = Offset(950f, 360f),
                ),
            ),
    ) {
        HeaderArtwork(modifier = Modifier.matchParentSize())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 18.dp, top = 10.dp, end = 18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = WebPurpleDark,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp),
            ) {
                Text(
                    text = "Jammu & Kashmir",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.76f),
                    modifier = Modifier.padding(top = 1.dp),
                )
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .padding(top = 9.dp)
                            .fillMaxWidth(0.86f)
                            .height(3.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.28f),
                    )
                } else {
                    Text(
                        text = if (canGoBack) "Back opens previous page" else "Official voter search",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.70f),
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.size(48.dp),
            ) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        if (isLoading) Icons.Outlined.Language else Icons.Outlined.Refresh,
                        contentDescription = "Refresh",
                        tint = WebPurpleDark,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val wave = Path().apply {
            moveTo(0f, h * 0.64f)
            cubicTo(w * 0.10f, h * 0.46f, w * 0.15f, h * 0.78f, w * 0.28f, h * 0.58f)
            cubicTo(w * 0.42f, h * 0.36f, w * 0.50f, h * 0.78f, w * 0.66f, h * 0.62f)
            cubicTo(w * 0.78f, h * 0.50f, w * 0.88f, h * 0.80f, w, h * 0.56f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(wave, Color(0xFF5B49D7).copy(alpha = 0.30f))

        val map = Path().apply {
            moveTo(w * 0.68f, h * 0.20f)
            cubicTo(w * 0.72f, h * 0.12f, w * 0.76f, h * 0.18f, w * 0.76f, h * 0.26f)
            cubicTo(w * 0.83f, h * 0.25f, w * 0.89f, h * 0.35f, w * 0.86f, h * 0.44f)
            cubicTo(w * 0.91f, h * 0.50f, w * 0.84f, h * 0.55f, w * 0.78f, h * 0.51f)
            cubicTo(w * 0.74f, h * 0.58f, w * 0.66f, h * 0.53f, w * 0.70f, h * 0.45f)
            cubicTo(w * 0.63f, h * 0.39f, w * 0.67f, h * 0.30f, w * 0.68f, h * 0.20f)
            close()
        }
        drawPath(map, Color.White.copy(alpha = 0.13f))
    }
}

@Composable
private fun WebErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE3E2F5)),
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (message.isBlank()) {
                CircularProgressIndicator(color = WebPurple)
            } else {
                Text(
                    text = "Unable to load page",
                    style = MaterialTheme.typography.titleMedium,
                    color = WebPurpleDark,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WebMuted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
