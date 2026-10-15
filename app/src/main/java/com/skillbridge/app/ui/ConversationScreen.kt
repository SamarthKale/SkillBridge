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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.Application
import com.skillbridge.app.data.Message
import com.skillbridge.app.data.Repo
import com.skillbridge.app.ui.components.MessageBubble
import com.skillbridge.app.ui.components.rememberMyProfile
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.Mute

@Composable
fun ConversationScreen(applicationId: String, onBack: () -> Unit) {
    val uid = AuthManager.uid()
    var app by remember { mutableStateOf<Application?>(null) }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val profile by rememberMyProfile()
    val isClient = profile?.role == "client"

    LaunchedEffect(applicationId) { Repo.getApplication(applicationId) { app = it } }
    DisposableEffect(applicationId) {
        val reg = Repo.listenThread(applicationId) { messages = it }
        onDispose { reg.remove() }
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    ScreenScaffold(title = app?.company ?: "Conversation", onBack = onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Text(
                            "Say hello to ${app?.company ?: "the client"}.",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = Mute,
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                items(messages, key = { it.id }) { MessageBubble(it, isClient) }
            }

            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Message", color = Mute) },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions.Default
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val a = app ?: return@IconButton
                        val text = draft.trim()
                        if (text.isEmpty()) return@IconButton
                        draft = ""
                        Repo.sendMessage(a, uid, text, isClient) { }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Accent)
                }
            }
        }
    }
}
