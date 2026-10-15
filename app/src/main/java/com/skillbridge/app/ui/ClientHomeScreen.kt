package com.skillbridge.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.recommendation.RecommendationCache
import com.skillbridge.app.ui.components.AccentButton
import com.skillbridge.app.ui.components.AiAssistantBanner
import com.skillbridge.app.ui.components.CandidateRecommendationCard
import com.skillbridge.app.ui.components.OpportunityCard
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.SearchField
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.SelectableChips
import com.skillbridge.app.ui.components.StatCard
import com.skillbridge.app.ui.components.rememberClientProjects
import com.skillbridge.app.ui.components.rememberMyProfile
import com.skillbridge.app.ui.components.rememberNotifications
import com.skillbridge.app.ui.components.rememberStudentProfiles
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ClientHomeScreen(
    onOpenDiscover: () -> Unit,
    onPostProject: () -> Unit,
    onOpenProject: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAiAssistant: () -> Unit = {},
    onOpenStudentProfile: (String) -> Unit = {}
) {
    val profile by rememberMyProfile()
    val notes by rememberNotifications()
    val projects by rememberClientProjects()
    val studentProfiles by rememberStudentProfiles()
    val clientUid = AuthManager.uid()
    
    val unread = notes.count { !it.read }
    val activeCount = projects.size

    val name = profile?.name ?: "Client"
    val companyName = profile?.companyName?.ifBlank { null }

    var selectedProjectTitle by remember(projects) {
        mutableStateOf(projects.firstOrNull()?.title ?: "")
    }
    val selectedProject = projects.find { it.title == selectedProjectTitle } ?: projects.firstOrNull()

    val topCandidates = remember(selectedProject, studentProfiles, clientUid) {
        if (selectedProject == null) emptyList()
        else RecommendationCache.getOrComputeCandidateRecommendations(
            project = selectedProject,
            studentProfiles = studentProfiles,
            proposals = emptyList(),
            authenticatedClientUid = clientUid
        ).take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Welcome back, $name", style = MaterialTheme.typography.titleLarge)
                if (companyName != null) {
                    Text(companyName, style = MaterialTheme.typography.bodyMedium, color = Mute)
                }
            }
            Box {
                IconButton(onClick = onOpenNotifications) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Ink)
                }
                if (unread > 0) {
                    Box(
                        Modifier
                            .size(9.dp)
                            .align(Alignment.TopEnd)
                            .background(Accent, RoundedCornerShape(999.dp))
                            .border(1.dp, Paper, RoundedCornerShape(999.dp))
                    )
                }
            }
        }

        SearchField(
            value = "", 
            onValueChange = {}, 
            enabled = false, 
            onClick = onOpenDiscover,
            placeholder = "Search for student freelancers..."
        )

        AiAssistantBanner(
            isClient = true,
            onClick = onOpenAiAssistant
        )

        Spacer(Modifier.height(4.dp))
        AccentButton("Post a New Project", onClick = onPostProject)

        Spacer(Modifier.height(4.dp))
        SectionHeader("Overview")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Active", activeCount, Modifier.weight(1f))
            StatCard("Talent Pool", studentProfiles.size, Modifier.weight(1f))
            StatCard("Top Matches", topCandidates.count { it.matchScorePercent >= 65 }, Modifier.weight(1f))
        }

        Spacer(Modifier.height(4.dp))
        SectionHeader("Your Projects")
        if (projects.isEmpty()) {
            EmptyState("You haven't posted any projects yet. Click 'Post a New Project' to get started.")
        } else {
            projects.take(3).forEach { opp ->
                OpportunityCard(opp) { onOpenProject(opp.id) }
            }
        }

        if (projects.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            SectionHeader("Recommended Candidates")
            if (projects.size > 1) {
                Text(
                    "Matching candidates for project:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Mute
                )
                SelectableChips(
                    options = projects.take(4).map { it.title },
                    selected = setOfNotNull(selectedProject?.title),
                    onToggle = { selectedProjectTitle = it }
                )
            }
            if (topCandidates.isEmpty()) {
                EmptyState("No eligible student profiles available yet.")
            } else {
                topCandidates.forEach { rec ->
                    CandidateRecommendationCard(
                        rec = rec,
                        onViewProfile = { onOpenStudentProfile(rec.candidate.uid) }
                    )
                }
            }
        }
    }
}

