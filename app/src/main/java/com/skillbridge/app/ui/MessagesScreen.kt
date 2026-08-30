package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.AppStatus
import com.skillbridge.app.data.Application
import com.skillbridge.app.data.Repo
import com.skillbridge.app.ui.components.Avatar
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.OutlinedCardBox
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.rememberMyApplications
import com.skillbridge.app.ui.theme.Mute

@Composable
fun MessagesScreen(onOpenConversation: (String) -> Unit) {
    val apps by rememberMyApplications()
    val threads = apps.filter { AppStatus.messagingUnlocked(it.status) }

    ScreenScaffold(title = "Messages") { padding ->
        if (threads.isEmpty()) {
            EmptyState("No conversations yet.\nMessaging opens when an application is shortlisted or accepted.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(threads, key = { it.id }) { app ->
                    ConversationRow(app) { onOpenConversation(app.id) }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(app: Application, onClick: () -> Unit) {
    val uid = AuthManager.uid()
    var snippet by remember { mutableStateOf("Tap to open the conversation") }
    DisposableEffect(app.id) {
        val reg = Repo.listenThread(uid, app.id) { msgs ->
            msgs.lastOrNull()?.let {
                snippet = (if (it.fromClient) "" else "You: ") + it.text
            }
        }
        onDispose { reg.remove() }
    }
    OutlinedCardBox(onClick = onClick) {
        Row(Modifier.fillMaxWidth()) {
            Avatar(app.company)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(app.company, style = MaterialTheme.typography.bodyLarge)
                Text(app.projectTitle, style = MaterialTheme.typography.labelMedium, color = Mute,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(snippet, style = MaterialTheme.typography.bodyMedium, color = Mute,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
