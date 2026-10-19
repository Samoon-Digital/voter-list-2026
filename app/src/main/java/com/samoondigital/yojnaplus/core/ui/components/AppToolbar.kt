package com.samoondigital.yojnaplus.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.samoondigital.yojnaplus.R

/** Wraps at larger font sizes while keeping status-bar insets consistent across screens. */
@Composable
fun AppToolbar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    titleMaxLines: Int = 2,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth().testTag("app-toolbar"),
    ) {
        Column {
            Row(
                modifier = Modifier.stableStatusBarsPadding().heightIn(min = 56.dp)
                    .padding(start = if (onBack == null) 16.dp else 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.ui_back), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                if (onBack == null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.HowToVote, contentDescription = null, modifier = Modifier.size(22.dp))
                        }
                    }
                }
                Column(Modifier.weight(1f).padding(start = if (onBack == null) 10.dp else 0.dp,
                    end = 8.dp, top = 6.dp, bottom = 6.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, maxLines = titleMaxLines, overflow = TextOverflow.Ellipsis)
                    if (!subtitle.isNullOrBlank()) {
                        Text(subtitle, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                actions()
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
