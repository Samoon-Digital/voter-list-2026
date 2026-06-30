package com.samoondigital.yojnaplus.feature.news

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.samoondigital.yojnaplus.core.ui.components.PlaceholderContent

@Composable
fun NewsScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    PlaceholderContent(
        icon = Icons.Outlined.Article,
        title = "News & Updates",
        subtitle = "Latest election notices and announcements will appear here.",
        modifier = modifier.fillMaxSize(),
    )
}
