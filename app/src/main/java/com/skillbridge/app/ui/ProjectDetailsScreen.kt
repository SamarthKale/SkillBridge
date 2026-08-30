package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.skillbridge.app.ui.components.LabeledValue
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.SkillChips
import com.skillbridge.app.ui.components.formatDate
import com.skillbridge.app.ui.theme.Mute

@Composable
fun ProjectDetailsScreen(
    opportunityId: String,
    onBack: () -> Unit,
    onApply: () -> Unit
) {
    var opp by remember { mutableStateOf<Opportunity?>(null) }
    var loading by remember { mutableStateOf(true) }
    var alreadyApplied by remember { mutableStateOf(false) }

    LaunchedEffect(opportunityId) {
        Repo.getOpportunity(opportunityId) { opp = it; loading = false }
        Repo.getApplication(Repo.applicationId(AuthManager.uid(), opportunityId)) { alreadyApplied = it != null }
    }

    ScreenScaffold(title = "Project Details", onBack = onBack) { padding ->
        val o = opp
        when {
            loading -> Text("Loading...", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
            o == null -> Text("Project not found.", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(o.title, style = MaterialTheme.typography.titleLarge)
                Text(o.company, style = MaterialTheme.typography.bodyMedium, color = Mute)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    LabeledValue("Budget", o.budget, Modifier.weight(1f))
                    LabeledValue("Deadline", o.deadline, Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    LabeledValue("Location", if (o.remote) "Remote" else o.location.ifBlank { "On-site" }, Modifier.weight(1f))
                    LabeledValue("Experience", o.experienceRequired, Modifier.weight(1f))
                }

                Spacer(Modifier.height(2.dp))
                SectionHeader("Required Skills")
                SkillChips(o.skills)

                Section("Description", o.description)
                Section("Responsibilities", o.responsibilities)
                Section("Requirements", o.requirements)

                Text("Posted ${formatDate(o.postedAt)}", style = MaterialTheme.typography.labelMedium, color = Mute)

                Spacer(Modifier.height(4.dp))
                PrimaryButton(
                    text = if (alreadyApplied) "Already Applied" else "Apply for Project",
                    enabled = !alreadyApplied,
                    onClick = onApply
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    if (body.isBlank()) return
    Column {
        SectionHeader(title)
        Spacer(Modifier.height(4.dp))
        Text(body.replace(" | ", "\n• ").let { if (it.contains("\n•")) "• $it" else it },
            style = MaterialTheme.typography.bodyMedium)
    }
}
