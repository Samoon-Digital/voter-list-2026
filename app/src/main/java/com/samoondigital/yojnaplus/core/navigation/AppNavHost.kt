package com.samoondigital.yojnaplus.core.navigation

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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samoondigital.yojnaplus.feature.home.HomeScreen
import com.samoondigital.yojnaplus.feature.news.NewsScreen
import com.samoondigital.yojnaplus.feature.notifications.NotificationsScreen
import com.samoondigital.yojnaplus.feature.pdf.PdfScreen
import com.samoondigital.yojnaplus.feature.results.VoterResultsScreen
import com.samoondigital.yojnaplus.feature.search.ElectoralSearchScreen
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
                    onStartSearch = { navController.navigate(Routes.ELECTORAL_SEARCH) },
                    onDownloadPdf = { navController.navigate(Routes.PDF) },
                    contentPadding = innerPadding,
                )
            }
            composable(Routes.NEWS) { NewsScreen(contentPadding = innerPadding) }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen(contentPadding = innerPadding) }
            composable(Routes.SETTINGS) { SettingsScreen(contentPadding = innerPadding) }
            composable(Routes.ELECTORAL_SEARCH) {
                ElectoralSearchScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToResults = { searchType, query ->
                        navController.navigate(Routes.voterResults(searchType, query))
                    },
                )
            }
            composable(
                route = Routes.VOTER_RESULTS,
                arguments = listOf(
                    navArgument("searchType") { type = NavType.StringType; defaultValue = "" },
                    navArgument("query") { type = NavType.StringType; defaultValue = "" },
                ),
            ) {
                VoterResultsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.PDF) { PdfScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
