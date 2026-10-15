package com.skillbridge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skillbridge.app.data.AppStatus
import com.skillbridge.app.data.Application
import com.skillbridge.app.data.Message
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.AccentSoft
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Line
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper
import com.skillbridge.app.ui.theme.Smoke
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(millis: Long): String =
    if (millis <= 0) "-" else SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(millis))

fun relativeTime(millis: Long): String {
    if (millis <= 0) return ""
    val diff = System.currentTimeMillis() - millis
    val h = diff / 3_600_000
    return when {
        h < 1 -> "just now"
        h < 24 -> "${h}h ago"
        h < 24 * 7 -> "${h / 24}d ago"
        else -> formatDate(millis)
    }
}

@Composable
fun OpportunityCard(opp: Opportunity, distanceText: String = "", onClick: () -> Unit) {
    OutlinedCardBox(onClick = onClick) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(opp.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(opp.company, style = MaterialTheme.typography.labelMedium, color = Mute)
                }
                Text(opp.budget, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(if (opp.workArrangement == com.skillbridge.app.data.WorkArrangement.REMOTE) "Remote" else opp.location.ifBlank { "On-site" })
                Spacer(Modifier.width(6.dp))
                Pill(opp.category)
            }
            if (distanceText.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(distanceText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (opp.skills.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                SkillChips(opp.skills.take(3))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Posted ${relativeTime(opp.postedAt)}",
                style = MaterialTheme.typography.labelMedium, color = Mute
            )
        }
    }
}

@Composable
fun ApplicationCard(app: Application, onClick: () -> Unit) {
    OutlinedCardBox(onClick = onClick) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(app.projectTitle, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(app.company, style = MaterialTheme.typography.labelMedium, color = Mute)
                }
                StatusChip(app.status)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(app.budget, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Applied ${formatDate(app.appliedAt)}", style = MaterialTheme.typography.labelMedium, color = Mute)
            }
        }
    }
}

@Composable
fun MessageBubble(msg: Message, isClientUser: Boolean = false) {
    val mine = if (isClientUser) msg.fromClient else !msg.fromClient
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            Modifier
                .widthIn(max = 280.dp)
                .background(if (mine) Accent else Smoke, RoundedCornerShape(14.dp))
                .border(1.dp, if (mine) Accent else Line, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                msg.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (mine) Paper else Ink
            )
        }
    }
}

/** Horizontal Applied -> Under Review -> Shortlisted -> Accepted stepper. */
@Composable
fun PipelineStepper(status: String, modifier: Modifier = Modifier) {
    val steps = AppStatus.PIPELINE
    val rejected = status == AppStatus.REJECTED
    val currentIdx = if (rejected) 0 else steps.indexOf(status).coerceAtLeast(0)
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            steps.forEachIndexed { i, _ ->
                val done = !rejected && i <= currentIdx
                Box(
                    Modifier
                        .size(18.dp)
                        .background(if (done) Accent else Paper, RoundedCornerShape(999.dp))
                        .border(1.dp, if (done) Accent else Line, RoundedCornerShape(999.dp))
                )
                if (i < steps.lastIndex) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (!rejected && i < currentIdx) Accent else Line)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            steps.forEach {
                Text(it, style = MaterialTheme.typography.labelMedium, color = Mute)
            }
        }
        if (rejected) {
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .background(AccentSoft, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    "This application was not selected.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
