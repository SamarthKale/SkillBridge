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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.AppStatus
import com.skillbridge.app.data.Application
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.data.Repo
import com.skillbridge.app.recommendation.RecommendationCache
import com.skillbridge.app.ui.components.CandidateRecommendationCard
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.LabeledValue
import com.skillbridge.app.ui.components.OutlinedCardBox
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.SkillChips
import com.skillbridge.app.ui.components.rememberStudentProfiles
import com.skillbridge.app.ui.theme.Mute

@Composable
fun ClientProjectDetailsScreen(
    opportunityId: String,
    onBack: () -> Unit,
    onViewProposals: () -> Unit,
    onOpenStudentProfile: (String) -> Unit = {},
    onOpenProposal: (String) -> Unit = {}
) {
    var opp by remember { mutableStateOf<Opportunity?>(null) }
    var proposals by remember { mutableStateOf<List<Application>>(emptyList()) }
    val studentProfiles by rememberStudentProfiles()
    val clientUid = AuthManager.uid()

    LaunchedEffect(opportunityId) {
        Repo.getOpportunity(opportunityId) { opp = it }
    }

    DisposableEffect(opportunityId) {
        val reg = Repo.listenProposals(opportunityId) { proposals = it }
        onDispose { reg.remove() }
    }

    ScreenScaffold(title = "Project Details", onBack = onBack) { padding ->
        val o = opp
        if (o == null) {
            Text("Loading...", color = Mute, modifier = Modifier.padding(padding).padding(20.dp))
            return@ScreenScaffold
        }

        val recommendedCandidates = remember(o, studentProfiles, proposals, clientUid) {
            RecommendationCache.getOrComputeCandidateRecommendations(
                project = o,
                studentProfiles = studentProfiles,
                proposals = proposals,
                authenticatedClientUid = clientUid
            ).take(6)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(o.title, style = MaterialTheme.typography.titleLarge)
            
            OutlinedCardBox {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledValue("Budget", o.budget)
                    LabeledValue("Deadline", o.deadline.ifBlank { "Not specified" })
                    LabeledValue("Arrangement", o.workArrangement)
                    if (o.workArrangement != com.skillbridge.app.data.WorkArrangement.REMOTE) {
                        LabeledValue("Location", o.location.ifBlank { "On-site" })
                        LabeledValue("Max Distance", "${o.maxDistanceKm} km")
                        if (o.latitude != null && o.longitude != null) {
                            Text("📍 View on Map", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            
            SectionHeader("Description")
            Text(o.description, style = MaterialTheme.typography.bodyMedium)

            if (o.responsibilities.isNotBlank()) {
                SectionHeader("Responsibilities")
                Text(o.responsibilities, style = MaterialTheme.typography.bodyMedium)
            }

            if (o.requirements.isNotBlank()) {
                SectionHeader("Requirements")
                Text(o.requirements, style = MaterialTheme.typography.bodyMedium)
            }

            if (o.skills.isNotEmpty()) {
                SectionHeader("Skills Required")
                SkillChips(o.skills)
            }
            
            Spacer(Modifier.height(4.dp))
            PrimaryButton("View Proposals (${proposals.size})", onClick = onViewProposals)

            Spacer(Modifier.height(4.dp))
            SectionHeader("Recommended Candidates")
            Text(
                "Ranked against your project's skills, category, and requirements.",
                style = MaterialTheme.typography.bodySmall,
                color = Mute
            )

            if (recommendedCandidates.isEmpty()) {
                EmptyState("No eligible student candidates found yet.")
            } else {
                recommendedCandidates.forEach { rec ->
                    CandidateRecommendationCard(
                        rec = rec,
                        onViewProfile = { onOpenStudentProfile(rec.candidate.uid) },
                        onViewApplication = rec.applicationId?.let { appId -> { onOpenProposal(appId) } },
                        onShortlist = rec.applicationId?.let { appId ->
                            {
                                Repo.updateApplicationStatus(appId, AppStatus.SHORTLISTED) {
                                    RecommendationCache.invalidateAll()
                                }
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

