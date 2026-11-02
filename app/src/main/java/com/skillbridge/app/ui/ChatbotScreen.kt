package com.skillbridge.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.skillbridge.app.chatbot.ChatbotEngine
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.rememberMyProfile
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.AccentSoft
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Line
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper
import com.skillbridge.app.ui.theme.Smoke
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class ChatLine(
    val fromUser: Boolean,
    val text: String,
    val action: String? = null,
    val followUps: List<String> = emptyList()
)

/** Where a reply's `action` hint can take the user. Only actions that make sense for the role are shown. */
private fun actionLabel(action: String, isClient: Boolean): String? = when (action) {
    "nearby" -> if (isClient) null else "Open Nearby Jobs"
    "explore" -> if (isClient) null else "Open Explore"
    "applications" -> if (isClient) null else "Open My Applications"
    "messages" -> if (isClient) null else "Open Messages"
    "matches" -> "Open AI Match Assistant"
    "profile" -> "Open Profile"
    "home" -> if (isClient) null else "Go Home"
    "post" -> if (isClient) "Post a project" else null
    "discover" -> if (isClient) "Discover students" else null
    "projects" -> if (isClient) "My projects" else null
    else -> null
}

/**
 * "Bridgey" - an in-app helper chat. Answers come from `assets/chatbot/answers.json` via
 * [ChatbotEngine]; there is no AI service behind it.
 */
@Composable
fun ChatbotScreen(
    onBack: () -> Unit,
    onNavigate: (action: String) -> Unit
) {
    val context = LocalContext.current
    val engine = remember { ChatbotEngine.get(context) }
    val profile by rememberMyProfile()
    val isClient = profile?.role == "client"
    val firstName = profile?.name?.trim()?.substringBefore(' ').orEmpty()

    val chat = remember { mutableStateListOf<ChatLine>() }
    var draft by remember { mutableStateOf("") }
    var typing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Greet once, after the profile (and so the user's role and name) is known.
    LaunchedEffect(profile != null, firstName) {
        if (profile != null && chat.size <= 1) {   // re-greet if the name arrives after the profile
            chat.clear()
            chat.add(ChatLine(false, engine.welcome(firstName)))
        }
    }
    LaunchedEffect(chat.size, typing) {
        val last = chat.size + (if (typing) 1 else 0)
        if (last > 0) listState.animateScrollToItem(last)   // +1 for the suggestions header item
    }

    fun send(raw: String) {
        val text = raw.trim()
        if (text.isEmpty() || typing) return
        chat.add(ChatLine(true, text))
        draft = ""
        typing = true
        scope.launch {
            delay(450L + (text.length * 8L).coerceAtMost(700L))   // a little "thinking" feels natural
            val r = engine.reply(text, isClient, firstName)
            typing = false
            chat.add(ChatLine(false, r.text, r.action, r.followUps))
        }
    }

    ScreenScaffold(title = engine.botName, onBack = onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chat.size) { i ->
                    val line = chat[i]
                    Column {
                        Bubble(line.fromUser, line.text)
                        if (!line.fromUser && i == chat.lastIndex) {
                            val label = line.action?.let { actionLabel(it, isClient) }
                            if (label != null || line.followUps.isNotEmpty()) {
                                Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (label != null) Chip(label, primary = true) { onNavigate(line.action!!) }
                                    line.followUps.forEach { f -> Chip(f) { send(f) } }
                                }
                            }
                        }
                    }
                }
                item(key = "hint") {
                    if (chat.size <= 1) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Try asking", style = MaterialTheme.typography.labelMedium, color = Mute)
                            engine.suggestions(isClient).forEach { s -> Chip(s) { send(s) } }
                        }
                    }
                }
                if (typing) item(key = "typing") { Bubble(false, "Bridgey is typing…", muted = true) }
            }

            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Ask me anything…", color = Mute) },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { send(draft) })
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { send(draft) }, enabled = draft.isNotBlank() && !typing) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Accent)
                }
            }
        }
    }
}

@Composable
private fun Bubble(fromUser: Boolean, text: String, muted: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start) {
        Box(
            Modifier
                .widthIn(max = 300.dp)
                .background(if (fromUser) Accent else Smoke, RoundedCornerShape(14.dp))
                .border(1.dp, if (fromUser) Accent else Line, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (fromUser) Paper else if (muted) Mute else Ink
            )
        }
    }
}

@Composable
private fun Chip(text: String, primary: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier
            .clickable(onClick = onClick)
            .background(if (primary) Accent else AccentSoft, RoundedCornerShape(999.dp))
            .border(1.dp, Accent.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (primary) Paper else Accent
        )
    }
}
