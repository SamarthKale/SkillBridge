package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.data.Repo
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.OutlinedCardBox
import com.skillbridge.app.ui.components.Pill
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SkillChips
import com.skillbridge.app.ui.components.formatDate
import com.skillbridge.app.ui.theme.Mute

@Composable
fun ClientProjectsScreen(
    onOpenProject: (String) -> Unit
) {
    val uid = AuthManager.uid()
    var projects by remember { mutableStateOf<List<Opportunity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    DisposableEffect(uid) {
        val reg = Repo.listenClientProjects(uid, onChange = {
            projects = it
            loading = false
        })
        onDispose { reg.remove() }
    }

    ScreenScaffold(title = "My Projects") { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (loading) {
                item { Text("Loading...", color = Mute) }
            } else if (projects.isEmpty()) {
                item { EmptyState("You haven't posted any projects yet.") }
            } else {
                item {
                    Text(
                        "${projects.size} active project${if (projects.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelMedium, color = Mute
                    )
                }
                items(projects, key = { it.id }) { opp ->
                    ClientOpportunityCard(opp) { onOpenProject(opp.id) }
                }
            }
        }
    }
}

@Composable
private fun ClientOpportunityCard(opp: Opportunity, onClick: () -> Unit) {
    OutlinedCardBox(onClick = onClick) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(opp.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(opp.budget, style = MaterialTheme.typography.bodyMedium)
                }
                Text("Posted ${formatDate(opp.postedAt)}", style = MaterialTheme.typography.labelMedium, color = Mute)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(opp.category)
            }
        }
    }
}
