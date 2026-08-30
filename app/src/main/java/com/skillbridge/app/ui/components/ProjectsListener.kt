package com.skillbridge.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.Project
import com.skillbridge.app.data.ProjectRepository

/** What a screen needs to know about the current user's project list. */
data class ProjectsState(
    val projects: List<Project> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

/**
 * Attaches a Firestore snapshot listener for the signed-in user's projects and keeps it
 * alive while the screen is on screen. The returned state updates live on any change.
 */
@Composable
fun rememberProjects(): ProjectsState {
    var state by remember { mutableStateOf(ProjectsState()) }
    val uid = AuthManager.uid()

    DisposableEffect(uid) {
        if (uid.isEmpty()) {
            state = ProjectsState(loading = false)
            return@DisposableEffect onDispose { }
        }
        val registration = ProjectRepository.listenForProjects(
            userId = uid,
            onChange = { state = ProjectsState(projects = it, loading = false, error = null) },
            onError = { state = state.copy(loading = false, error = it) }
        )
        onDispose { registration.remove() }
    }

    return state
}
