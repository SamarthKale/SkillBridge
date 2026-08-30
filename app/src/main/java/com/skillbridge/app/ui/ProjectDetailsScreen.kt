package com.skillbridge.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skillbridge.app.data.Project
import com.skillbridge.app.data.ProjectRepository
import com.skillbridge.app.data.Status
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SecondaryButton
import com.skillbridge.app.ui.theme.Line
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper

@Composable
fun ProjectDetailsScreen(
    projectId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit
) {
    var project by remember { mutableStateOf<Project?>(null) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun reload() {
        ProjectRepository.getProject(projectId) { p ->
            project = p
            loading = false
        }
    }

    LaunchedEffect(projectId) { reload() }

    ScreenScaffold(title = "Project Details", onBack = onBack) { padding ->
        val p = project
        when {
            loading -> Text(
                "Loading…",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute,
                modifier = Modifier.padding(padding).padding(20.dp)
            )

            p == null -> Text(
                "Project not found.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute,
                modifier = Modifier.padding(padding).padding(20.dp)
            )

            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Paper),
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailField("Project name", p.projectName)
                        DetailField("Client name", p.clientName.ifBlank { "-" })
                        DetailField("Description", p.description.ifBlank { "-" })
                        DetailField("Budget", p.budget.ifBlank { "-" })
                        DetailField("Due date", p.dueDate.ifBlank { "-" })
                        DetailField("Status", p.status)
                    }
                }

                actionError?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }

                if (p.status != Status.COMPLETED) {
                    PrimaryButton(
                        text = "Mark Completed",
                        loading = busy,
                        onClick = {
                            busy = true
                            actionError = null
                            ProjectRepository.setStatus(projectId, Status.COMPLETED) { ok ->
                                busy = false
                                if (ok) reload() else actionError = "Could not update status."
                            }
                        }
                    )
                }
                SecondaryButton(text = "Edit", onClick = { onEdit(projectId) })
                SecondaryButton(text = "Delete", onClick = { showDeleteDialog = true })
                Spacer(Modifier.height(4.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete project?") },
            text = { Text("This project will be permanently removed from Firestore.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    busy = true
                    actionError = null
                    ProjectRepository.deleteProject(projectId) { ok ->
                        busy = false
                        if (ok) onDeleted() else actionError = "Could not delete the project."
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Mute)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
