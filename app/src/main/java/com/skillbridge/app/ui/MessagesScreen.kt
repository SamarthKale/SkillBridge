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
import com.skillbridge.app.data.Message
import com.skillbridge.app.ui.components.rememberClientProjects
import com.skillbridge.app.ui.components.rememberMyProfile
import com.skillbridge.app.ui.components.rememberMyApplications
import com.skillbridge.app.ui.theme.Mute

@Composable
fun MessagesScreen(onOpenConversation: (String) -> Unit) {
    val uid = AuthManager.uid()
    val profile by rememberMyProfile()
    val isClient = profile?.role == "client"

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    DisposableEffect(uid, isClient) {
        val reg = if (isClient) {
            Repo.listenClientThreads(uid) { 
                messages = it 
                loading = false
            }
        } else {
            Repo.listenStudentThreads(uid) {
                messages = it
                loading = false
            }
        }
        onDispose { reg.remove() }
    }

    ScreenScaffold(title = "Messages") { padding ->
        if (loading) {
            Text("Loading...", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
        } else if (messages.isEmpty()) {
            EmptyState("No conversations yet.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Group messages by application ID and only show the latest message for each thread
                val uniqueThreads = messages.groupBy { it.applicationId }.map { it.value.first() }
                
                items(uniqueThreads, key = { it.applicationId }) { msg ->
                    ConversationRow(msg, isClient) { onOpenConversation(msg.applicationId) }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(msg: Message, isClient: Boolean, onClick: () -> Unit) {
    val displayName = if (isClient) "Student" else msg.clientName // Hardcoded Student name for prototype as we don't have it in the message object
    val snippet = (if (msg.fromClient == isClient) "You: " else "") + msg.text

    OutlinedCardBox(onClick = onClick) {
        Row(Modifier.fillMaxWidth()) {
            Avatar(displayName)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(displayName, style = MaterialTheme.typography.bodyLarge)
                Text(msg.projectTitle, style = MaterialTheme.typography.labelMedium, color = Mute,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(snippet, style = MaterialTheme.typography.bodyMedium, color = Mute,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
