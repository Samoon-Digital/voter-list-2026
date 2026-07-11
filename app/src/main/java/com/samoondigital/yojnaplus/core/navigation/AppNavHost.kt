package com.samoondigital.yojnaplus.core.navigation

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samoondigital.yojnaplus.ads.AdManager
import com.samoondigital.yojnaplus.feature.downloads.DownloadsScreen
import com.samoondigital.yojnaplus.feature.home.HomeScreen
import com.samoondigital.yojnaplus.feature.news.NewsScreen
import com.samoondigital.yojnaplus.feature.notifications.NotificationsScreen
import com.samoondigital.yojnaplus.feature.pdf.PdfScreen
import com.samoondigital.yojnaplus.feature.pdfviewer.PdfViewerScreen
import com.samoondigital.yojnaplus.feature.settings.SettingsScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val activity = LocalContext.current as? Activity

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onDownloadPdf = { navController.navigate(Routes.PDF) },
                    onOpenDownloads = {
                        navController.navigate(Routes.DOWNLOADS) {
                            launchSingleTop = true
                        }
                    },
                    contentPadding = innerPadding,
                )
            }
            composable(Routes.NEWS) { NewsScreen(contentPadding = innerPadding) }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen(contentPadding = innerPadding) }
            composable(Routes.SETTINGS) { SettingsScreen(contentPadding = innerPadding) }
            composable(Routes.PDF) {
                PdfScreen(
                    onBack = { navController.popBackStack() },
                    onDownloadsComplete = {
                        navController.navigate(Routes.DOWNLOADS) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Routes.DOWNLOADS) {
                DownloadsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPdf = { uri, title ->
                        val open = { navController.navigate(Routes.pdfViewerRoute(uri, title)) }
                        activity?.let { AdManager.showInterstitial(it, open) } ?: open()
                    },
                )
            }
            composable(Routes.PDF_VIEWER_ROUTE) {
                PdfViewerScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
