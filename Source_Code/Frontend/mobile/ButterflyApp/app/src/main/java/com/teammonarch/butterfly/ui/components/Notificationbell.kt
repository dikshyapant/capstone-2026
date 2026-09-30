package com.teammonarch.butterfly.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun NotificationBell(
    onClick: () -> Unit,
    count: Int = 0
) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (count > 0) Badge { Text(count.toString()) }
            }
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
        }
    }
}