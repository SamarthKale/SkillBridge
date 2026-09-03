package com.skillbridge.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.Repo
import com.skillbridge.app.navigation.Routes
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.AccentSoft
import com.skillbridge.app.ui.theme.Mute
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onResolved: (String) -> Unit) {
    LaunchedEffect(Unit) {
        delay(1400)
        if (!AuthManager.isLoggedIn()) {
            onResolved(Routes.LOGIN)
        } else {
            Repo.getProfile(AuthManager.uid()) { profile ->
                onResolved(if (profile != null) Routes.HOME else Routes.PROFILE_SETUP)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(84.dp)
                .background(AccentSoft, RoundedCornerShape(20.dp))
                .border(1.dp, Accent, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("SB", style = MaterialTheme.typography.titleLarge, color = Accent)
        }
        Text(
            "SkillBridge",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            "Connecting student talent with real opportunities.",
            style = MaterialTheme.typography.bodyMedium,
            color = Mute,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
