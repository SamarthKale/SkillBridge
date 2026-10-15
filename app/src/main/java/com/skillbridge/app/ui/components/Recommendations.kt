package com.skillbridge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skillbridge.app.recommendation.CandidateRecommendation
import com.skillbridge.app.recommendation.StudentRecommendation
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.AccentSoft
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Smoke

@Composable
fun AiMatchBadge(scorePercent: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(AccentSoft, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            "$scorePercent% match",
            style = MaterialTheme.typography.labelMedium,
            color = Accent,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AiAssistantBanner(isClient: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(AccentSoft, RoundedCornerShape(12.dp))
            .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "AI Match Assistant",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Accent
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (isClient) "See the best student matches for your projects"
                else "See the opportunities that fit your skills best",
                style = MaterialTheme.typography.bodySmall,
                color = Ink
            )
        }
        Text(">", style = MaterialTheme.typography.titleLarge, color = Accent)
    }
}

@Composable
private fun ReasonsBox(reasons: List<String>, missing: List<String>) {
    if (reasons.isEmpty() && missing.isEmpty()) return
    Column(
        Modifier
            .fillMaxWidth()
            .background(Smoke, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        reasons.take(2).forEach {
            Text("• $it", style = MaterialTheme.typography.bodySmall, color = Ink)
        }
        if (missing.isNotEmpty()) {
            Text(
                "Missing skills: ${missing.joinToString(", ")}",
                style = MaterialTheme.typography.labelSmall,
                color = Mute
            )
        }
    }
}

@Composable
fun RecommendedProjectCard(rec: StudentRecommendation, onViewProject: () -> Unit) {
    val opp = rec.opportunity
    OutlinedCardBox(onClick = onViewProject) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(opp.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(opp.company, style = MaterialTheme.typography.labelMedium, color = Mute)
                }
                Spacer(Modifier.width(8.dp))
                AiMatchBadge(rec.matchScorePercent)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(opp.category)
                Spacer(Modifier.width(6.dp))
                Text(opp.budget, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            ReasonsBox(rec.reasons, rec.missingSkills)
        }
    }
}

@Composable
fun CandidateRecommendationCard(
    rec: CandidateRecommendation,
    onViewProfile: () -> Unit,
    onViewApplication: (() -> Unit)? = null,
    onShortlist: (() -> Unit)? = null
) {
    val c = rec.candidate
    OutlinedCardBox(onClick = onViewProfile) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Avatar(c.name.ifBlank { "?" }, size = 40)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    val sub = listOf(c.degree, c.college).filter { it.isNotBlank() }.joinToString(" · ")
                    if (sub.isNotBlank()) {
                        Text(sub, style = MaterialTheme.typography.labelMedium, color = Mute)
                    }
                }
                Spacer(Modifier.width(8.dp))
                AiMatchBadge(rec.matchScorePercent)
            }
            if (c.skills.isNotEmpty()) SkillChips(c.skills.take(4))
            ReasonsBox(rec.reasons, rec.missingSkills)
            if (onViewApplication != null || onShortlist != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onViewApplication != null) {
                        SecondaryButton("View application", onViewApplication, Modifier.weight(1f))
                    }
                    if (onShortlist != null) {
                        AccentButton("Shortlist", onShortlist, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
