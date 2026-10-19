package com.samoondigital.yojnaplus.core.navigation

import android.net.Uri

/**
 * Single source of truth for navigation routes. Keeping routes as constants
 * (not magic strings spread across the app) makes the nav graph easy to audit.
 */
object Routes {
    const val HOME = "home"
    const val PDF = "pdf"
    const val DOWNLOADS = "downloads"
    const val PDF_VIEWER = "pdf_viewer"
    const val PDF_VIEWER_ROUTE = "$PDF_VIEWER/{uri}/{title}"

    fun pdfViewerRoute(uri: String, title: String): String =
        "$PDF_VIEWER/${Uri.encode(uri)}/${Uri.encode(title)}"
}
