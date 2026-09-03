package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skillbridge.app.ui.components.ApplicationCard
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.rememberMyApplications

@Composable
fun ApplicationsScreen(onOpenApplication: (String) -> Unit) {
    val apps by rememberMyApplications()

    ScreenScaffold(title = "My Applications") { padding ->
        if (apps.isEmpty()) {
            EmptyState("You haven't applied to any projects yet.\nBrowse Explore to get started.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(apps, key = { it.id }) { app ->
                    ApplicationCard(app) { onOpenApplication(app.id) }
                }
            }
        }
    }
}
