package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import com.skillbridge.app.data.AppStatus
import com.skillbridge.app.data.Application
import com.skillbridge.app.data.Repo
import com.skillbridge.app.ui.components.AccentButton
import com.skillbridge.app.ui.components.LabeledValue
import com.skillbridge.app.ui.components.OutlinedCardBox
import com.skillbridge.app.ui.components.PipelineStepper
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.StatusChip
import com.skillbridge.app.ui.components.formatDate
import com.skillbridge.app.ui.theme.Mute

@Composable
fun ApplicationStatusScreen(
    applicationId: String,
    onBack: () -> Unit,
    onMessage: () -> Unit
) {
    var app by remember { mutableStateOf<Application?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(applicationId) {
        Repo.getApplication(applicationId) { app = it; loading = false }
    }

    ScreenScaffold(title = "Application Status", onBack = onBack) { padding ->
        val a = app
        when {
            loading -> Text("Loading...", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
            a == null -> Text("Application not found.", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(a.projectTitle, style = MaterialTheme.typography.titleLarge)
                Text(a.company, style = MaterialTheme.typography.bodyMedium, color = Mute)

                OutlinedCardBox {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Text("Current status", style = MaterialTheme.typography.labelMedium, color = Mute)
                            Spacer(Modifier.height(4.dp))
                            StatusChip(a.status)
                        }
                        LabeledValue("Budget", a.budget)
                        LabeledValue("Applied on", formatDate(a.appliedAt))
                    }
                }

                SectionHeader("Progress")
                PipelineStepper(a.status)

                SectionHeader("Your introduction")
                Text(a.intro.ifBlank { "-" }, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(4.dp))
                if (AppStatus.messagingUnlocked(a.status)) {
                    AccentButton("Message Client", onClick = onMessage)
                } else {
                    Text(
                        "Messaging opens once you are shortlisted.",
                        style = MaterialTheme.typography.labelMedium, color = Mute
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
