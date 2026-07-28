package com.samoondigital.yojnaplus.core.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
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
    const val PDF = "pdf"
    const val OLD_SIR = "old_sir"
    const val UP_2003 = "up_2003"
    const val JAMMU_KASHMIR_WEB = "jammu_kashmir_web"
    const val CHANDIGARH_WEB = "chandigarh_web"
    const val CHANDIGARH_2002 = "chandigarh_2002"
    const val DADRA_NAGAR_HAVELI_WEB = "dadra_nagar_haveli_web"
    const val DOWNLOADS = "downloads"
    const val PDF_VIEWER = "pdf_viewer"
    const val PDF_VIEWER_ROUTE = "$PDF_VIEWER/{uri}/{title}"
    const val NEWS = "news"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"

    fun pdfViewerRoute(uri: String, title: String): String =
        "$PDF_VIEWER/${Uri.encode(uri)}/${Uri.encode(title)}"
}

/** Items shown in the bottom navigation bar. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, "Home", Icons.Outlined.Home),
    NEWS(Routes.NEWS, "News", Icons.AutoMirrored.Outlined.Article),
    NOTIFICATIONS(Routes.NOTIFICATIONS, "Alerts", Icons.Outlined.Notifications),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Outlined.Settings),
}
