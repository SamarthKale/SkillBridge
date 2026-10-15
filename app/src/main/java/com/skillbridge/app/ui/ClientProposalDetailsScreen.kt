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
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SecondaryButton
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.StatusChip
import com.skillbridge.app.ui.components.formatDate
import com.skillbridge.app.ui.theme.Mute

@Composable
fun ClientProposalDetailsScreen(
    applicationId: String,
    onBack: () -> Unit,
    onOpenStudentProfile: (String) -> Unit,
    onMessage: () -> Unit
) {
    var app by remember { mutableStateOf<Application?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(applicationId) {
        Repo.getApplication(applicationId) { 
            app = it 
            loading = false 
        }
    }

    ScreenScaffold(title = "Review Proposal", onBack = onBack) { padding ->
        val a = app
        if (loading) {
            Text("Loading...", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
        } else if (a == null) {
            Text("Proposal not found.", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(a.studentName, style = MaterialTheme.typography.titleLarge)
                
                OutlinedCardBox {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Text("Current status", style = MaterialTheme.typography.labelMedium, color = Mute)
                            Spacer(Modifier.height(4.dp))
                            StatusChip(a.status)
                        }
                        LabeledValue("Budget Requested", a.budget)
                        LabeledValue("Applied on", formatDate(a.appliedAt))
                    }
                }

                SecondaryButton("View Student Profile", onClick = { onOpenStudentProfile(a.studentUid) })

                SectionHeader("Introduction")
                Text(a.intro.ifBlank { "-" }, style = MaterialTheme.typography.bodyMedium)

                SectionHeader("Resume")
                Text(a.resumeUrl.ifBlank { "No resume provided." }, style = MaterialTheme.typography.bodyMedium, color = Mute)

                Spacer(Modifier.height(4.dp))

                // Actions based on current status
                when (a.status) {
                    AppStatus.APPLIED, AppStatus.UNDER_REVIEW -> {
                        PrimaryButton(
                            text = "Shortlist Candidate",
                            onClick = {
                                Repo.updateApplicationStatus(a.id, AppStatus.SHORTLISTED) { success ->
                                    if (success) app = a.copy(status = AppStatus.SHORTLISTED)
                                }
                            }
                        )
                        SecondaryButton(
                            text = "Reject",
                            onClick = {
                                Repo.updateApplicationStatus(a.id, AppStatus.REJECTED) { success ->
                                    if (success) app = a.copy(status = AppStatus.REJECTED)
                                }
                            }
                        )
                    }
                    AppStatus.SHORTLISTED -> {
                        AccentButton("Message Candidate", onClick = onMessage)
                        Spacer(Modifier.height(8.dp))
                        PrimaryButton(
                            text = "Accept Proposal & Hire",
                            onClick = {
                                Repo.updateApplicationStatus(a.id, AppStatus.ACCEPTED) { success ->
                                    if (success) app = a.copy(status = AppStatus.ACCEPTED)
                                }
                            }
                        )
                    }
                    AppStatus.ACCEPTED -> {
                        AccentButton("Message", onClick = onMessage)
                    }
                    AppStatus.REJECTED -> {
                        Text("This proposal was rejected.", style = MaterialTheme.typography.labelMedium, color = Mute)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
