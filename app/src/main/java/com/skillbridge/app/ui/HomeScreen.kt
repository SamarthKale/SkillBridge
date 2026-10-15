package com.skillbridge.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skillbridge.app.data.AppStatus
import com.skillbridge.app.data.Category
import com.skillbridge.app.recommendation.RecommendationCache
import com.skillbridge.app.ui.components.AiAssistantBanner
import com.skillbridge.app.ui.components.ApplicationCard
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.RecommendedProjectCard
import com.skillbridge.app.ui.components.SearchField
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.SelectableChips
import com.skillbridge.app.ui.components.StatCard
import com.skillbridge.app.ui.components.rememberMyApplications
import com.skillbridge.app.ui.components.rememberMyProfile
import com.skillbridge.app.ui.components.rememberNotifications
import com.skillbridge.app.ui.components.rememberOpportunities
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.AccentSoft
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Line
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper

@Composable
fun HomeScreen(
    onOpenExplore: () -> Unit,
    onOpenProject: (String) -> Unit,
    onOpenApplications: () -> Unit,
    onOpenApplication: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAiAssistant: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val profile by rememberMyProfile()
    val apps by rememberMyApplications()
    val opps by rememberOpportunities()
    val notes by rememberNotifications()
    val unread = notes.count { !it.read }

    val firstName = (profile?.name ?: "there").trim().split(" ").firstOrNull().takeUnless { it.isNullOrBlank() } ?: "there"
    val allRecommendations = remember(profile, opps, apps) {
        RecommendationCache.getOrComputeStudentRecommendations(
            profile = profile,
            opportunities = opps,
            applications = apps,
            excludeApplied = false
        )
    }
    val recommended = allRecommendations.take(5)
    val limitedNote = recommended.firstOrNull()?.limitedProfileNote
    val recent = apps.take(3)

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
                Text("Hello, $firstName 👋", style = MaterialTheme.typography.titleLarge)
                Text("Find your next opportunity.", style = MaterialTheme.typography.bodyMedium, color = Mute)
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

        SearchField(value = "", onValueChange = {}, enabled = false, onClick = onOpenExplore)

        SelectableChips(
            options = Category.ALL,
            selected = emptySet(),
            onToggle = { onOpenExplore() }
        )

        AiAssistantBanner(
            isClient = false,
            onClick = onOpenAiAssistant
        )

        Spacer(Modifier.height(2.dp))
        SectionHeader("My Progress")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Applied", apps.size, Modifier.weight(1f))
            StatCard("Review", apps.count { it.status == AppStatus.UNDER_REVIEW }, Modifier.weight(1f))
            StatCard("Shortlisted", apps.count { it.status == AppStatus.SHORTLISTED }, Modifier.weight(1f))
            StatCard("Accepted", apps.count { it.status == AppStatus.ACCEPTED }, Modifier.weight(1f))
        }

        Spacer(Modifier.height(2.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SectionHeader("Recent Applications")
            Text("View all", style = MaterialTheme.typography.labelMedium, color = Accent,
                modifier = Modifier.clickable(onClick = onOpenApplications).padding(4.dp))
        }
        if (recent.isEmpty()) {
            EmptyState("You haven't applied to anything yet.")
        } else {
            recent.forEach { ApplicationCard(it) { onOpenApplication(it.id) } }
        }

        Spacer(Modifier.height(2.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader("Recommended For You")
            Text(
                "Explore all",
                style = MaterialTheme.typography.labelMedium,
                color = Accent,
                modifier = Modifier.clickable(onClick = onOpenExplore).padding(4.dp)
            )
        }

        if (!limitedNote.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentSoft, RoundedCornerShape(10.dp))
                    .border(1.dp, Line, RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenProfile)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ℹ️ $limitedNote",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Update Profile →",
                    style = MaterialTheme.typography.labelMedium,
                    color = Accent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (recommended.isEmpty()) {
            EmptyState("No opportunities available right now.")
        } else {
            recommended.forEach { rec ->
                RecommendedProjectCard(
                    rec = rec,
                    onViewProject = { onOpenProject(rec.opportunity.id) }
                )
            }
        }
    }
}

