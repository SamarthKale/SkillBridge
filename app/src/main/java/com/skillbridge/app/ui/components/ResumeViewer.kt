package com.skillbridge.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.skillbridge.app.data.Repo
import com.skillbridge.app.data.ResumeFile
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.utils.FileUtils

/**
 * Shows the resume file a user uploaded (if any) with an Open button. Renders nothing when the
 * user has not uploaded one, so callers can keep their "no resume link" text alongside it.
 * [reloadKey] re-fetches (e.g. after the owner uploads or removes a file).
 */
@Composable
fun ResumeViewer(uid: String, reloadKey: Any? = null) {
    val context = LocalContext.current
    var resume by remember(uid, reloadKey) { mutableStateOf<ResumeFile?>(null) }
    var failed by remember(uid, reloadKey) { mutableStateOf(false) }

    LaunchedEffect(uid, reloadKey) { Repo.getResume(uid) { resume = it } }

    val r = resume ?: return
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("📄 ${r.fileName}", style = MaterialTheme.typography.bodyMedium)
                Text(FileUtils.formatSize(r.sizeBytes), style = MaterialTheme.typography.labelMedium, color = Mute)
            }
            TextButton(onClick = { failed = !FileUtils.openResume(context, r) }) { Text("Open") }
        }
        if (failed) {
            Text(
                "No app on this device can open this file.",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error
            )
        }
    }
}
