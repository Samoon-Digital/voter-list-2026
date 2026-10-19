package com.samoondigital.yojnaplus.core.navigation

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.samoondigital.yojnaplus.ads.AppOpenAdManager
import com.samoondigital.yojnaplus.core.ui.components.AdMobBannerAd
import com.samoondigital.yojnaplus.core.ui.components.PurpleHeaderStatusBarEffect
import com.samoondigital.yojnaplus.core.ui.components.rememberLargeAdaptiveBannerHeight
import com.samoondigital.yojnaplus.feature.downloads.DownloadsScreen
import com.samoondigital.yojnaplus.feature.home.HomeScreen
import com.samoondigital.yojnaplus.feature.pdf.PdfScreen
import com.samoondigital.yojnaplus.feature.pdfviewer.PdfViewerScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    PurpleHeaderStatusBarEffect()
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            AppOpenAdManager.setHomeScreenVisible(route == Routes.HOME)
        }
    }
    fun navigateToHome() {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.HOME) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun navigateToDownloads() {
        navController.navigate(Routes.DOWNLOADS) {
            popUpTo(Routes.HOME) { inclusive = false }
            launchSingleTop = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val showBottomBanner = currentRoute != null && shouldShowRouteBottomBanner(currentRoute)
            val adaptiveBannerHeight = rememberLargeAdaptiveBannerHeight(maxWidth)
            var routeBannerHeight by remember(currentRoute, adaptiveBannerHeight) {
                mutableStateOf(adaptiveBannerHeight)
            }
            val bottomBannerHeight = if (showBottomBanner) routeBannerHeight else 0.dp
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onDownloadPdf = { navController.navigate(Routes.PDF) },
                        onOpenDownloads = { navigateToDownloads() },
                        contentPadding = innerPadding,
                    )
                }
                composable(Routes.PDF) {
                    PdfScreen(
                        onBack = { navController.popBackStack() },
                        onDownloadsComplete = { navigateToDownloads() },
                        bottomBannerHeight = bottomBannerHeight,
                    )
                }
                composable(Routes.DOWNLOADS) {
                    DownloadsScreen(
                        onBack = { navigateToHome() },
                        onDownloadList = { navController.navigate(Routes.PDF) },
                        onOpenPdf = { uri, title ->
                            navController.navigate(Routes.pdfViewerRoute(uri, title))
                        },
                        bottomBannerHeight = bottomBannerHeight,
                    )
                }
                composable(Routes.PDF_VIEWER_ROUTE) {
                    PdfViewerScreen(
                        onBack = { navigateToDownloads() },
                    )
                }
            }
            if (showBottomBanner) {
                BottomRouteBanner(
                    bannerHeight = bottomBannerHeight,
                    onHeightChanged = { routeBannerHeight = it },
                )
            }
        }
    }
}

private fun shouldShowRouteBottomBanner(route: String): Boolean =
    route == Routes.PDF || route == Routes.DOWNLOADS

@Composable
private fun BoxScope.BottomRouteBanner(
    bannerHeight: Dp,
    onHeightChanged: (Dp) -> Unit,
) {
    AdMobBannerAd(
        placementKey = "route-bottom-banner",
        onHeightChanged = onHeightChanged,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .height(bannerHeight)
            .fillMaxWidth(),
    )
}
