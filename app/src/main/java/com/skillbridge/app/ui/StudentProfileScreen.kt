package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skillbridge.app.data.Profile
import com.skillbridge.app.data.Repo
import com.skillbridge.app.ui.components.Avatar
import com.skillbridge.app.ui.components.LabeledValue
import com.skillbridge.app.ui.components.OutlinedCardBox
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.SkillChips
import com.skillbridge.app.ui.theme.Mute

@Composable
fun StudentProfileScreen(
    studentUid: String,
    onBack: () -> Unit
) {
    var profile by remember { mutableStateOf<Profile?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(studentUid) {
        Repo.getProfile(studentUid) { p ->
            profile = p
            loading = false
        }
    }

    ScreenScaffold(title = "Student Profile", onBack = onBack) { padding ->
        val p = profile
        if (loading) {
            Text("Loading...", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
        } else if (p == null) {
            Text("Profile not found.", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(p.name, size = 60)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(p.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            listOfNotNull(
                                p.role.replaceFirstChar { it.uppercase() },
                                p.branch.ifBlank { null }
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.labelMedium, color = Mute
                        )
                    }
                }

                SectionHeader("Skills")
                SkillChips(p.skills)

                SectionHeader("Education")
                OutlinedCardBox {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabeledValue("Degree", p.degree)
                        LabeledValue("College", p.college)
                        LabeledValue("Branch", p.branch)
                        LabeledValue("Graduation Year", p.graduationYear.takeIf { it > 0 }?.toString() ?: "-")
                    }
                }

                SectionHeader("Experience")
                Text(p.experience.ifBlank { "-" }, style = MaterialTheme.typography.bodyMedium)

                SectionHeader("About")
                Text(p.bio.ifBlank { "-" }, style = MaterialTheme.typography.bodyMedium)

                SectionHeader("Resume")
                Text(p.resumeUrl.ifBlank { "No resume link added." },
                    style = MaterialTheme.typography.bodyMedium, color = Mute)

                Spacer(Modifier.height(8.dp))
                PrimaryButton("Invite to Project (Coming Soon)", onClick = { /* TODO */ })
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
