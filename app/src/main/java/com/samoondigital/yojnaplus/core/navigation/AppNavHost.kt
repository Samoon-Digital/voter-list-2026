package com.samoondigital.yojnaplus.core.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val topLevelRoutes = TopLevelDestination.entries.map { it.route }
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar, enter = fadeIn(), exit = fadeOut()) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { dest ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onDownloadPdf = { navController.navigate(Routes.PDF) },
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
                    onOpenPdf = { uri, title -> navController.navigate(Routes.pdfViewerRoute(uri, title)) },
                )
            }
            composable(Routes.PDF_VIEWER_ROUTE) { entry ->
                PdfViewerScreen(
                    title = Uri.decode(entry.arguments?.getString("title").orEmpty()),
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
