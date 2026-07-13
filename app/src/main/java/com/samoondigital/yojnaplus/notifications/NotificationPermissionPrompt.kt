package com.samoondigital.yojnaplus.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun NotificationPermissionPrompt() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        NotificationPermissionSession.markPromptHandled()
        Log.d("NotificationPermission", "permission-result granted=$granted")
        if (granted) {
            AppNotificationManager.createNotificationChannel(context)
        }
    }

    LaunchedEffect(Unit) {
        showDialog = NotificationPermissionSession.shouldShowPrompt(context)
        Log.d("NotificationPermission", "prompt-check show=$showDialog")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                NotificationPermissionSession.markPromptHandled()
                showDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            title = { Text(text = "Stay Updated") },
            text = {
                Text(
                    text = "Get notified about new voter lists, election updates, and important announcements.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        NotificationPermissionSession.markPromptHandled()
                        showDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                ) {
                    Text(text = "Allow Notifications")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        NotificationPermissionSession.markPromptHandled()
                        showDialog = false
                    },
                ) {
                    Text(text = "Maybe Later")
                }
            },
        )
    }
}

private object NotificationPermissionSession {
    private var promptHandledThisSession = false

    fun shouldShowPrompt(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        if (promptHandledThisSession) return false
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED
    }

    fun markPromptHandled() {
        promptHandledThisSession = true
    }
}
