package com.skillbridge.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.AppNotification
import com.skillbridge.app.data.Application
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.data.Profile
import com.skillbridge.app.data.Repo

/**
 * Small helpers that attach a Firestore snapshot listener for the lifetime of a screen
 * and expose the result as Compose state. One per data need the tabs share.
 */

@Composable
fun rememberMyApplications(): State<List<Application>> {
    val uid = AuthManager.uid()
    val state = remember { mutableStateOf<List<Application>>(emptyList()) }
    DisposableEffect(uid) {
        if (uid.isEmpty()) return@DisposableEffect onDispose { }
        val reg = Repo.listenMyApplications(uid, onChange = { state.value = it })
        onDispose { reg.remove() }
    }
    return state
}

@Composable
fun rememberMyProfile(): State<Profile?> {
    val uid = AuthManager.uid()
    val state = remember { mutableStateOf<Profile?>(null) }
    DisposableEffect(uid) {
        if (uid.isEmpty()) return@DisposableEffect onDispose { }
        val reg = Repo.listenProfile(uid, onChange = { state.value = it })
        onDispose { reg.remove() }
    }
    return state
}

@Composable
fun rememberClientProjects(): State<List<Opportunity>> {
    val uid = AuthManager.uid()
    val state = remember { mutableStateOf<List<Opportunity>>(emptyList()) }
    DisposableEffect(uid) {
        if (uid.isEmpty()) return@DisposableEffect onDispose { }
        val reg = Repo.listenClientProjects(uid, onChange = { state.value = it })
        onDispose { reg.remove() }
    }
    return state
}

@Composable
fun rememberOpportunities(): State<List<Opportunity>> {
    val state = remember { mutableStateOf<List<Opportunity>>(emptyList()) }
    DisposableEffect(Unit) {
        val reg = Repo.listenOpportunities(onChange = { state.value = it })
        onDispose { reg.remove() }
    }
    return state
}

@Composable
fun rememberStudentProfiles(): State<List<Profile>> {
    val state = remember { mutableStateOf<List<Profile>>(emptyList()) }
    DisposableEffect(Unit) {
        val reg = Repo.listenStudentProfiles(onChange = { state.value = it })
        onDispose { reg.remove() }
    }
    return state
}

@Composable
fun rememberNotifications(): State<List<AppNotification>> {
    val uid = AuthManager.uid()
    val state = remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    DisposableEffect(uid) {
        if (uid.isEmpty()) return@DisposableEffect onDispose { }
        val reg = Repo.listenNotifications(uid, onChange = { state.value = it })
        onDispose { reg.remove() }
    }
    return state
}
