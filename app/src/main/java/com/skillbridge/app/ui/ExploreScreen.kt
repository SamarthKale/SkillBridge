package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.skillbridge.app.data.Category
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.OpportunityCard
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SearchField
import com.skillbridge.app.ui.components.SelectableChips
import com.skillbridge.app.ui.components.rememberOpportunities
import com.skillbridge.app.ui.theme.Mute

private fun budgetvalue(s: String): Int = s.filter { it.isDigit() }.toIntOrNull() ?: 0

@Composable
fun ExploreScreen(onOpenProject: (String) -> Unit) {
    val all by rememberOpportunities()

    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    var budgetBand by remember { mutableStateOf("Any budget") }
    val budgetBands = listOf("Any budget", "Under 10k", "10k - 20k", "Over 20k")

    val filtered = all.filter { o ->
        (query.isBlank() ||
            o.title.contains(query, true) ||
            o.company.contains(query, true) ||
            o.skills.any { it.contains(query, true) }) &&
        (category == null || o.category == category) &&
        when (budgetBand) {
            "Under 10k" -> budgetvalue(o.budget) < 10000
            "10k - 20k" -> budgetvalue(o.budget) in 10000..20000
            "Over 20k" -> budgetvalue(o.budget) > 20000
            else -> true
        }
    }.sortedByDescending { it.postedAt }

    ScreenScaffold(title = "Explore Projects") { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SearchField(query, { query = it }, placeholder = "Search projects, skills, companies...")
            }
            item {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = Mute)
                Spacer(Modifier.height(6.dp))
                SelectableChips(
                    options = Category.ALL,
                    selected = category?.let { setOf(it) } ?: emptySet(),
                    onToggle = { c -> category = if (category == c) null else c }
                )
            }
            item {
                Text("Budget", style = MaterialTheme.typography.labelMedium, color = Mute)
                Spacer(Modifier.height(6.dp))
                SelectableChips(
                    options = budgetBands,
                    selected = setOf(budgetBand),
                    onToggle = { budgetBand = it }
                )
            }
            item {
                Text(
                    "${filtered.size} project${if (filtered.size == 1) "" else "s"}  ·  newest first",
                    style = MaterialTheme.typography.labelMedium, color = Mute
                )
            }
            if (filtered.isEmpty()) {
                item { EmptyState("No projects match your filters.") }
            } else {
                items(filtered, key = { it.id }) { OpportunityCard(it) { onOpenProject(it.id) } }
            }
        }
    }
}
