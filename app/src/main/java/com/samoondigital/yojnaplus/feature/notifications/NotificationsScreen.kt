package com.samoondigital.yojnaplus.feature.notifications

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.samoondigital.yojnaplus.core.ui.components.PlaceholderContent

@Composable
fun NotificationsScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    PlaceholderContent(
        icon = Icons.Outlined.NotificationsNone,
        title = "No notifications yet",
        subtitle = "You'll be notified about voter list updates and important alerts here.",
        modifier = modifier.fillMaxSize(),
    )
}
