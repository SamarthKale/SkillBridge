package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skillbridge.app.ui.components.ProjectRow
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.rememberProjects
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper

@Composable
fun ProjectsScreen(
    onBack: () -> Unit,
    onAddProject: () -> Unit,
    onOpenProject: (String) -> Unit
) {
    val state = rememberProjects()

    ScreenScaffold(
        title = "Projects",
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProject, containerColor = Ink, contentColor = Paper) {
                Icon(Icons.Filled.Add, contentDescription = "Add Project")
            }
        }
    ) { padding ->
        val message = when {
            state.error != null -> state.error
            state.loading -> "Loading…"
            state.projects.isEmpty() -> "No projects yet.\nTap + to add one."
            else -> null
        }

        if (message != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(message, style = MaterialTheme.typography.bodyMedium, color = Mute)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.projects, key = { it.id }) { project ->
                    ProjectRow(project = project, onClick = { onOpenProject(project.id) })
                }
            }
        }
    }
}
