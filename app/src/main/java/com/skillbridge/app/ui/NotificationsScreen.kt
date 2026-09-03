package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skillbridge.app.data.Repo
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.OutlinedCardBox
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.rememberNotifications
import com.skillbridge.app.ui.components.relativeTime
import com.skillbridge.app.ui.theme.Mute

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val notes by rememberNotifications()

    LaunchedEffect(notes.size) {
        notes.filter { !it.read }.forEach { Repo.markNotificationRead(it.id) }
    }

    ScreenScaffold(title = "Notifications", onBack = onBack) { padding ->
        if (notes.isEmpty()) {
            EmptyState("No notifications yet.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes, key = { it.id }) { n ->
                    OutlinedCardBox {
                        Column {
                            Text(n.text, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(2.dp))
                            Text(relativeTime(n.createdAt), style = MaterialTheme.typography.labelMedium, color = Mute)
                        }
                    }
                }
            }
        }
    }
}
