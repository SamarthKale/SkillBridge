package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.Counts
import com.skillbridge.app.ui.components.ProjectRow
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SecondaryButton
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.StatCard
import com.skillbridge.app.ui.components.rememberProjects
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper

@Composable
fun HomeScreen(
    onOpenProjects: () -> Unit,
    onOpenProfile: () -> Unit,
    onAddProject: () -> Unit,
    onOpenProject: (String) -> Unit
) {
    val state = rememberProjects()
    val counts = Counts.from(state.projects)
    val recent = state.projects.take(5)
    val error = state.error

    ScreenScaffold(
        title = "Home",
        actions = {
            IconButton(onClick = onOpenProfile) {
                Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Ink)
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProject, containerColor = Ink, contentColor = Paper) {
                Icon(Icons.Filled.Add, contentDescription = "Add Project")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Welcome, ${AuthManager.displayName()}", style = MaterialTheme.typography.titleLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Total", counts.total, Modifier.weight(1f))
                StatCard("Active", counts.active, Modifier.weight(1f))
                StatCard("Completed", counts.completed, Modifier.weight(1f))
            }

            SectionHeader("Recent Projects")

            when {
                error != null -> Text(
                    error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                state.loading -> Text(
                    "Loading…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute
                )
                recent.isEmpty() -> Text(
                    "No projects yet. Tap + to add your first one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute
                )
                else -> recent.forEach { project ->
                    ProjectRow(project = project, onClick = { onOpenProject(project.id) })
                }
            }

            SecondaryButton(text = "View All Projects", onClick = onOpenProjects)
        }
    }
}
