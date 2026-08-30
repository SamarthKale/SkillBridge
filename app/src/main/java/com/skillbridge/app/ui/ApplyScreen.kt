package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.data.Profile
import com.skillbridge.app.data.Repo
import com.skillbridge.app.ui.components.AppTextField
import com.skillbridge.app.ui.components.ErrorText
import com.skillbridge.app.ui.components.LabeledValue
import com.skillbridge.app.ui.components.OutlinedCardBox
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SecondaryButton
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.SkillChips
import com.skillbridge.app.ui.theme.Mute

@Composable
fun ApplyScreen(
    opportunityId: String,
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    val uid = AuthManager.uid()
    var opp by remember { mutableStateOf<Opportunity?>(null) }
    var profile by remember { mutableStateOf<Profile?>(null) }
    var intro by remember { mutableStateOf("") }
    var resumeUrl by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    LaunchedEffect(opportunityId) {
        Repo.getOpportunity(opportunityId) { opp = it }
        Repo.getProfile(uid) { p -> profile = p; resumeUrl = p?.resumeUrl ?: "" }
    }

    ScreenScaffold(title = "Apply", onBack = onBack) { padding ->
        val o = opp
        if (o == null) {
            Text("Loading...", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
            return@ScreenScaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedCardBox {
                Column {
                    Text(o.title, style = MaterialTheme.typography.bodyLarge)
                    Text("${o.company}  ·  ${o.budget}", style = MaterialTheme.typography.labelMedium, color = Mute)
                }
            }

            SectionHeader("Your Profile")
            OutlinedCardBox {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledValue("Name", profile?.name ?: AuthManager.displayName())
                    Column {
                        Text("Skills", style = MaterialTheme.typography.labelMedium, color = Mute)
                        Spacer(Modifier.height(4.dp))
                        SkillChips(profile?.skills ?: emptyList())
                    }
                    LabeledValue(
                        "Education",
                        listOfNotNull(profile?.degree, profile?.college).filter { it.isNotBlank() }.joinToString(", ")
                    )
                }
            }

            SectionHeader("Introduction")
            AppTextField(
                intro, { intro = it; error = null },
                "Tell the client why you are suitable for this project...",
                singleLine = false, minLines = 4
            )

            SectionHeader("Resume")
            if (resumeUrl.isNotBlank()) {
                Text("Current: $resumeUrl", style = MaterialTheme.typography.labelMedium, color = Mute)
            }
            AppTextField(resumeUrl, { resumeUrl = it }, "Resume link")

            ErrorText(error)

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton("Cancel", onClick = onBack, modifier = Modifier.weight(1f))
                PrimaryButton(
                    "Submit Application",
                    loading = submitting,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (intro.isBlank()) { error = "Please add a short introduction."; return@PrimaryButton }
                        submitting = true
                        Repo.submitApplication(
                            o, uid, profile?.name ?: AuthManager.displayName(), intro.trim(), resumeUrl.trim()
                        ) { ok ->
                            submitting = false
                            if (ok) onSubmitted() else error = "You have already applied to this project."
                        }
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
