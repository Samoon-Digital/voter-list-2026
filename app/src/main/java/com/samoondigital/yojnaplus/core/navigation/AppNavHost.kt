package com.samoondigital.yojnaplus.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.samoondigital.yojnaplus.ads.AppOpenAdManager
import com.samoondigital.yojnaplus.core.ui.components.AdMobBannerAd
import com.samoondigital.yojnaplus.feature.chandigarh.ChandigarhScreen
import com.samoondigital.yojnaplus.feature.downloads.DownloadsScreen
import com.samoondigital.yojnaplus.feature.home.HomeScreen
import com.samoondigital.yojnaplus.feature.jharkhand.JharkhandScreen
import com.samoondigital.yojnaplus.feature.news.NewsScreen
import com.samoondigital.yojnaplus.feature.notifications.NotificationsScreen
import com.samoondigital.yojnaplus.feature.oldsir.OldSirScreen
import com.samoondigital.yojnaplus.feature.pdf.PdfScreen
import com.samoondigital.yojnaplus.feature.pdfviewer.PdfViewerScreen
import com.samoondigital.yojnaplus.feature.settings.SettingsScreen
import com.samoondigital.yojnaplus.feature.up2003.UpRollScreen
import com.samoondigital.yojnaplus.feature.webview.ChandigarhWebViewScreen
import com.samoondigital.yojnaplus.feature.webview.DadraNagarHaveliWebViewScreen
import com.samoondigital.yojnaplus.feature.webview.GujaratWebViewScreen
import com.samoondigital.yojnaplus.feature.webview.JammuKashmirWebViewScreen
import com.samoondigital.yojnaplus.feature.webview.KarnatakaWebViewScreen
import com.samoondigital.yojnaplus.feature.webview.UttarakhandWebViewScreen
import com.samoondigital.yojnaplus.feature.westbengal.WestBengalScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
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
        Box(Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onDownloadPdf = { navController.navigate(Routes.PDF) },
                        onOpenOldSir = { navController.navigate(Routes.OLD_SIR) },
                        onOpenDownloads = { navigateToDownloads() },
                        contentPadding = innerPadding,
                    )
                }
                composable(Routes.OLD_SIR) {
                    OldSirScreen(
                        onBack = { navController.popBackStack() },
                        onOpenUttarPradesh = { navController.navigate(Routes.UP_2003) },
                        onOpenJammuKashmir = { navController.navigate(Routes.JAMMU_KASHMIR_WEB) },
                        onOpenChandigarh = { navController.navigate(Routes.CHANDIGARH_2002) },
                        onOpenDadraNagarHaveli = { navController.navigate(Routes.DADRA_NAGAR_HAVELI_WEB) },
                        onOpenGujarat = { navController.navigate(Routes.GUJARAT_WEB) },
                        onOpenKarnataka = { navController.navigate(Routes.KARNATAKA_WEB) },
                        onOpenUttarakhand = { navController.navigate(Routes.UTTARAKHAND_WEB) },
                        onOpenJharkhand = { navController.navigate(Routes.JHARKHAND_2003) },
                        onOpenWestBengal = { navController.navigate(Routes.WEST_BENGAL_2002) },
                        onOpenDownloads = { navigateToDownloads() },
                        onOpenPdf = { uri, title ->
                            navController.navigate(Routes.pdfViewerRoute(uri, title))
                        },
                    )
                }
                composable(Routes.UP_2003) {
                    UpRollScreen(
                        onBack = { navController.popBackStack() },
                        onOpenDownloads = { navigateToDownloads() },
                        onOpenPdf = { uri, title ->
                            navController.navigate(Routes.pdfViewerRoute(uri, title))
                        },
                    )
                }
                composable(Routes.JHARKHAND_2003) {
                    JharkhandScreen(
                        onBack = { navController.popBackStack() },
                        onOpenDownloads = { navigateToDownloads() },
                        onOpenPdf = { uri, title ->
                            navController.navigate(Routes.pdfViewerRoute(uri, title))
                        },
                    )
                }
                composable(Routes.WEST_BENGAL_2002) {
                    WestBengalScreen(
                        onBack = { navController.popBackStack() },
                        onOpenDownloads = { navigateToDownloads() },
                        onOpenPdf = { uri, title ->
                            navController.navigate(Routes.pdfViewerRoute(uri, title))
                        },
                    )
                }
                composable(Routes.JAMMU_KASHMIR_WEB) {
                    JammuKashmirWebViewScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.CHANDIGARH_WEB) {
                    ChandigarhWebViewScreen(
                        onBack = { navController.popBackStack() },
                        onOpenPdf = { uri, title ->
                            navController.navigate(Routes.pdfViewerRoute(uri, title))
                        },
                    )
                }
                composable(Routes.CHANDIGARH_2002) {
                    ChandigarhScreen(
                        onBack = { navController.popBackStack() },
                        onOpenDownloads = { navigateToDownloads() },
                        onOpenPdf = { uri, title ->
                            navController.navigate(Routes.pdfViewerRoute(uri, title))
                        },
                    )
                }
                composable(Routes.DADRA_NAGAR_HAVELI_WEB) {
                    DadraNagarHaveliWebViewScreen(
                        onBack = { navController.popBackStack() },
                        onOpenPdf = { uri, title ->
                            navController.navigate(Routes.pdfViewerRoute(uri, title))
                        },
                    )
                }
                composable(Routes.GUJARAT_WEB) {
                    GujaratWebViewScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.KARNATAKA_WEB) {
                    KarnatakaWebViewScreen(
                        onBack = { navController.popBackStack() },
                        onOpenPdf = { uri, title ->
                            navController.navigate(Routes.pdfViewerRoute(uri, title))
                        },
                    )
                }
                composable(Routes.UTTARAKHAND_WEB) {
                    UttarakhandWebViewScreen(
                        onBack = { navController.popBackStack() },
                        onOpenPdf = { uri, title ->
                            navController.navigate(Routes.pdfViewerRoute(uri, title))
                        },
                    )
                }
                composable(Routes.NEWS) { NewsScreen(contentPadding = innerPadding) }
                composable(Routes.NOTIFICATIONS) { NotificationsScreen(contentPadding = innerPadding) }
                composable(Routes.SETTINGS) { SettingsScreen(contentPadding = innerPadding) }
                composable(Routes.PDF) {
                    PdfScreen(
                        onBack = { navController.popBackStack() },
                        onDownloadsComplete = { navigateToDownloads() },
                    )
                }
                composable(Routes.DOWNLOADS) {
                    DownloadsScreen(
                        onBack = { navigateToHome() },
                        onOpenPdf = { uri, title ->
                            navController.navigate(Routes.pdfViewerRoute(uri, title))
                        },
                    )
                }
                composable(Routes.PDF_VIEWER_ROUTE) {
                    PdfViewerScreen(
                        onBack = { navigateToDownloads() },
                    )
                }
            }
            if (currentRoute != null && currentRoute != Routes.HOME) {
                BottomRouteBanner()
            }
        }
    }
}

@Composable
private fun BoxScope.BottomRouteBanner() {
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    AdMobBannerAd(
        placementKey = "route-bottom-banner",
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = -navigationBarHeight)
            .fillMaxWidth(),
    )
}




