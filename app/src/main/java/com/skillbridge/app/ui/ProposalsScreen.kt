package com.skillbridge.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.AppStatus
import com.skillbridge.app.data.Application
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.data.Repo
import com.skillbridge.app.recommendation.CandidateRecommendation
import com.skillbridge.app.recommendation.RecommendationCache
import com.skillbridge.app.ui.components.AiMatchBadge
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.OutlinedCardBox
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.StatusChip
import com.skillbridge.app.ui.components.formatDate
import com.skillbridge.app.ui.components.rememberStudentProfiles
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper
import com.skillbridge.app.ui.theme.Smoke

@Composable
fun ProposalsScreen(
    projectId: String,
    onBack: () -> Unit,
    onOpenProposal: (String) -> Unit
) {
    var proposals by remember { mutableStateOf<List<Application>>(emptyList()) }
    var project by remember { mutableStateOf<Opportunity?>(null) }
    var loading by remember { mutableStateOf(true) }
    val studentProfiles by rememberStudentProfiles()
    val clientUid = AuthManager.uid()

    LaunchedEffect(projectId) {
        Repo.getOpportunity(projectId) { project = it }
    }

    DisposableEffect(projectId) {
        val reg = Repo.listenProposals(projectId) { 
            proposals = it 
            loading = false 
        }
        onDispose { reg.remove() }
    }

    val matchByStudentUid = remember(project, studentProfiles, proposals, clientUid) {
        val p = project ?: return@remember emptyMap()
        RecommendationCache.getOrComputeCandidateRecommendations(
            project = p,
            studentProfiles = studentProfiles,
            proposals = proposals,
            authenticatedClientUid = clientUid
        ).associateBy { it.candidate.uid }
    }

    val rankedProposals = remember(proposals, matchByStudentUid) {
        proposals.sortedByDescending { matchByStudentUid[it.studentUid]?.matchScorePercent ?: 0 }
    }

    ScreenScaffold(title = "Proposals", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (loading) {
                item { Text("Loading...", color = Mute) }
            } else if (rankedProposals.isEmpty()) {
                item { EmptyState("No proposals received yet.") }
            } else {
                item {
                    Text(
                        "${rankedProposals.size} proposal${if (rankedProposals.size == 1) "" else "s"}  ·  ranked by AI match",
                        style = MaterialTheme.typography.labelMedium, color = Mute
                    )
                }
                items(rankedProposals, key = { it.id }) { p ->
                    val rec = matchByStudentUid[p.studentUid]
                    ProposalCard(
                        app = p,
                        rec = rec,
                        onShortlist = if (p.status == AppStatus.APPLIED || p.status == AppStatus.UNDER_REVIEW) {
                            {
                                Repo.updateApplicationStatus(p.id, AppStatus.SHORTLISTED) {
                                    RecommendationCache.invalidateAll()
                                }
                            }
                        } else null,
                        onClick = { onOpenProposal(p.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProposalCard(
    app: Application,
    rec: CandidateRecommendation?,
    onShortlist: (() -> Unit)?,
    onClick: () -> Unit
) {
    OutlinedCardBox(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(app.studentName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
                if (rec != null) {
                    AiMatchBadge(scorePercent = rec.matchScorePercent)
                    Spacer(Modifier.width(6.dp))
                }
                StatusChip(app.status)
            }
            Text(app.intro, style = MaterialTheme.typography.bodyMedium, maxLines = 2, color = Mute)

            if (rec != null && rec.reasons.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Smoke, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    rec.reasons.take(2).forEach { reason ->
                        Text("• $reason", style = MaterialTheme.typography.bodySmall, color = Ink)
                    }
                    if (rec.missingSkills.isNotEmpty()) {
                        Text(
                            "Missing skills: ${rec.missingSkills.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Mute
                        )
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(app.budget, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Applied ${formatDate(app.appliedAt)}", style = MaterialTheme.typography.labelMedium, color = Mute)
                    if (onShortlist != null) {
                        Box(
                            modifier = Modifier
                                .clickable(onClick = onShortlist)
                                .background(Ink, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Shortlist ✓", style = MaterialTheme.typography.labelSmall, color = Paper)
                        }
                    }
                }
            }
        }
    }
}

