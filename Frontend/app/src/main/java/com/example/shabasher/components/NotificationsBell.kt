package com.example.shabasher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shabasher.notifications.NotificationCenter

@Composable
fun NotificationsBell(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unread by NotificationCenter.unreadCountFlow().collectAsState(initial = 0)

    Box(modifier = modifier) {
        IconButton(onClick = onClick) {
            Icon(Icons.Default.Notifications, contentDescription = "Уведомления")
        }
        if (unread > 0) {
            val text = if (unread > 99) "99+" else unread.toString()
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                    .background(colorScheme.primary, RoundedCornerShape(10.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
