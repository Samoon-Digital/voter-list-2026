package com.samoondigital.yojnaplus.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.samoondigital.yojnaplus.ads.AppOpenAdManager
import com.samoondigital.yojnaplus.feature.downloads.DownloadsScreen
import com.samoondigital.yojnaplus.feature.home.HomeScreen
import com.samoondigital.yojnaplus.feature.pdf.PdfScreen
import com.samoondigital.yojnaplus.feature.pdfviewer.PdfViewerScreen

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
                )
            }
            composable(Routes.DOWNLOADS) {
                DownloadsScreen(
                    onBack = { navigateToHome() },
                    onDownloadList = { navController.navigate(Routes.PDF) },
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
    }
}
