package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.Project
import com.skillbridge.app.data.ProjectRepository
import com.skillbridge.app.data.Status
import com.skillbridge.app.ui.components.AppTextField
import com.skillbridge.app.ui.components.ErrorText
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.StatusPicker

@Composable
fun AddProjectScreen(
    projectId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val isEdit = !projectId.isNullOrEmpty()

    var loaded by remember { mutableStateOf(!isEdit) }
    var original by remember { mutableStateOf<Project?>(null) }

    var name by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(Status.PENDING) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    // Load the existing project once when editing.
    LaunchedEffect(projectId) {
        if (isEdit) {
            ProjectRepository.getProject(projectId!!) { p ->
                if (p != null) {
                    original = p
                    name = p.projectName
                    client = p.clientName
                    description = p.description
                    budget = p.budget
                    dueDate = p.dueDate
                    status = p.status
                } else {
                    error = "Could not load this project."
                }
                loaded = true
            }
        }
    }

    fun save() {
        if (name.isBlank() || client.isBlank()) {
            error = "Project name and client name are required."
            return
        }
        saving = true
        val uid = AuthManager.uid()
        val base = original ?: Project()
        val edited = base.copy(
            projectName = name.trim(),
            clientName = client.trim(),
            description = description.trim(),
            budget = budget.trim(),
            dueDate = dueDate.trim(),
            status = status
        )
        val onResult: (Boolean) -> Unit = { ok ->
            if (ok) {
                onSaved()
            } else {
                saving = false
                error = "Could not save the project. Please try again."
            }
        }
        if (isEdit) {
            ProjectRepository.updateProject(edited.copy(id = projectId!!, userId = uid), onResult)
        } else {
            ProjectRepository.addProject(edited, uid, onResult)
        }
    }

    ScreenScaffold(title = if (isEdit) "Edit Project" else "Add Project", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppTextField(name, { name = it; error = null }, "Project Name")
            AppTextField(client, { client = it }, "Client Name")
            AppTextField(description, { description = it }, "Description", singleLine = false)
            AppTextField(budget, { budget = it }, "Budget", keyboardType = KeyboardType.Number)
            AppTextField(dueDate, { dueDate = it }, "Due Date (YYYY-MM-DD)")

            StatusPicker(selected = status, options = Status.ALL, onSelected = { status = it })

            ErrorText(error)

            PrimaryButton(
                text = "Save Project",
                loading = saving,
                enabled = loaded,
                onClick = { save() }
            )
        }
    }
}
