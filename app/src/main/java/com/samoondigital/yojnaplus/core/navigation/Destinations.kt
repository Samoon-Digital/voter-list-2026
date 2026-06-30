package com.samoondigital.yojnaplus.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Single source of truth for navigation routes. Keeping routes as constants
 * (not magic strings spread across the app) makes the nav graph easy to audit.
 */
object Routes {
    const val HOME = "home"
    const val ELECTORAL_SEARCH = "electoral_search"
    const val VOTER_RESULTS = "voter_results?searchType={searchType}&query={query}"
    const val PDF = "pdf"
    const val NEWS = "news"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"

    fun voterResults(searchType: String, query: String) =
        "voter_results?searchType=$searchType&query=${android.net.Uri.encode(query)}"
}

/** Items shown in the bottom navigation bar. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, "Home", Icons.Outlined.Home),
    NEWS(Routes.NEWS, "News", Icons.Outlined.Article),
    NOTIFICATIONS(Routes.NOTIFICATIONS, "Alerts", Icons.Outlined.Notifications),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Outlined.Settings),
}
