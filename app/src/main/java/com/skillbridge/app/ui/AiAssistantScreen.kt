package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.recommendation.RecommendationCache
import com.skillbridge.app.ui.components.CandidateRecommendationCard
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.RecommendedProjectCard
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SelectableChips
import com.skillbridge.app.ui.components.rememberClientProjects
import com.skillbridge.app.ui.components.rememberMyApplications
import com.skillbridge.app.ui.components.rememberMyProfile
import com.skillbridge.app.ui.components.rememberOpportunities
import com.skillbridge.app.ui.components.rememberStudentProfiles
import com.skillbridge.app.ui.theme.Mute

/**
 * Role-aware match assistant. Students see their best-fitting opportunities; clients pick
 * one of their projects and see the best-fitting students. Uses the on-device rule-based
 * engine in `recommendation/` (no external AI service).
 */
@Composable
fun AiAssistantScreen(
    onBack: () -> Unit,
    onOpenProject: (String) -> Unit,
    onOpenStudentProfile: (String) -> Unit,
    onOpenProposal: (String) -> Unit
) {
    val profile by rememberMyProfile()

    ScreenScaffold(title = "AI Match Assistant", onBack = onBack) { padding ->
        when {
            profile == null ->
                Text("Loading...", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
            profile?.role == "client" -> ClientMatches(padding, onOpenStudentProfile, onOpenProposal)
            else -> StudentMatches(padding, onOpenProject)
        }
    }
}

@Composable
private fun StudentMatches(padding: PaddingValues, onOpenProject: (String) -> Unit) {
    val profile by rememberMyProfile()
    val apps by rememberMyApplications()
    val opps by rememberOpportunities()
    val recs = remember(profile, opps, apps) {
        RecommendationCache.getOrComputeStudentRecommendations(profile, opps, apps, excludeApplied = true).take(15)
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Opportunities ranked by how well they fit your skills, interests and location.",
                    style = MaterialTheme.typography.bodyMedium, color = Mute
                )
                recs.firstOrNull()?.limitedProfileNote?.let {
                    Text("ℹ️ $it", style = MaterialTheme.typography.bodySmall, color = Mute)
                }
            }
        }
        if (recs.isEmpty()) item { EmptyState("No new opportunities to recommend right now.") }
        items(recs, key = { it.opportunity.id }) { rec ->
            RecommendedProjectCard(rec, onViewProject = { onOpenProject(rec.opportunity.id) })
        }
    }
}

@Composable
private fun ClientMatches(
    padding: PaddingValues,
    onOpenStudentProfile: (String) -> Unit,
    onOpenProposal: (String) -> Unit
) {
    val projects by rememberClientProjects()
    val students by rememberStudentProfiles()
    val clientUid = AuthManager.uid()
    var selectedTitle by remember(projects) { mutableStateOf(projects.firstOrNull()?.title ?: "") }
    val selected = projects.find { it.title == selectedTitle } ?: projects.firstOrNull()
    val recs = remember(selected, students, clientUid) {
        if (selected == null) emptyList()
        else RecommendationCache
            .getOrComputeCandidateRecommendations(selected, students, emptyList(), clientUid).take(15)
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Students ranked by how well they fit the selected project.",
                    style = MaterialTheme.typography.bodyMedium, color = Mute
                )
                if (projects.isNotEmpty()) {
                    SelectableChips(
                        options = projects.map { it.title },
                        selected = setOf(selected?.title ?: ""),
                        onToggle = { selectedTitle = it }
                    )
                }
            }
        }
        if (projects.isEmpty()) item { EmptyState("Post a project to get student matches.") }
        else if (recs.isEmpty()) item { EmptyState("No eligible student profiles yet.") }
        items(recs, key = { it.candidate.uid }) { rec ->
            CandidateRecommendationCard(rec, onViewProfile = { onOpenStudentProfile(rec.candidate.uid) })
        }
    }
}
