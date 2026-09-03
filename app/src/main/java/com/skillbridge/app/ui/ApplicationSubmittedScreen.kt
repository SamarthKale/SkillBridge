package com.skillbridge.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.SecondaryButton
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.AccentSoft
import com.skillbridge.app.ui.theme.Mute

@Composable
fun ApplicationSubmittedScreen(
    onViewApplications: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(72.dp)
                .background(AccentSoft, RoundedCornerShape(999.dp))
                .border(1.dp, Accent, RoundedCornerShape(999.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.Check, contentDescription = null, tint = Accent) }

        Text(
            "Application submitted successfully!",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "The client will review your application. You can track its status any time.",
            style = MaterialTheme.typography.bodyMedium,
            color = Mute,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton("View My Applications", onClick = onViewApplications)
        Spacer(Modifier.height(10.dp))
        SecondaryButton("Back to Home", onClick = onHome)
    }
}
