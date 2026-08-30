package com.skillbridge.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import com.skillbridge.app.data.Project
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Line
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper
import com.skillbridge.app.ui.theme.Smoke

/** Standard single-line text field used across every form. */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}

/** Solid black primary action button. Shows a spinner while [loading]. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Paper),
        modifier = modifier.fillMaxWidth().height(48.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(color = Paper, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Text(text, color = Paper, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Outlined secondary button (e.g. "Create Account" on the login screen). */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Ink),
        modifier = modifier.fillMaxWidth().height(48.dp)
    ) {
        Text(text, color = Ink)
    }
}

/** Small red error line shown under forms. */
@Composable
fun ErrorText(message: String?, modifier: Modifier = Modifier) {
    if (!message.isNullOrBlank()) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
        )
    }
}

/** Plain section title, e.g. "Recent Projects". */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
    )
}

/** One dashboard stat inside a minimal bordered card. */
@Composable
fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Paper),
        border = BorderStroke(1.dp, Line),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value.toString(), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = Mute)
        }
    }
}

/** A tappable project row: name + client on the left, status + due date on the right. */
@Composable
fun ProjectRow(project: Project, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Paper),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(project.projectName, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Text(
                    project.clientName.ifBlank { "No client" },
                    style = MaterialTheme.typography.labelMedium,
                    color = Mute
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(project.status)
                if (project.dueDate.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Due ${project.dueDate}", style = MaterialTheme.typography.labelMedium, color = Mute)
                }
            }
        }
    }
}

/** Grayscale status label. */
@Composable
fun StatusChip(status: String) {
    Box(
        modifier = Modifier
            .background(Smoke, RoundedCornerShape(4.dp))
            .border(1.dp, Line, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(status, style = MaterialTheme.typography.labelMedium, color = Ink)
    }
}

/** Three-way status selector used in the Add / Edit form. */
@Composable
fun StatusPicker(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    Column {
        Text("Status", style = MaterialTheme.typography.labelMedium, color = Mute)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val isOn = option == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(selected = isOn, onClick = { onSelected(option) })
                        .background(if (isOn) Ink else Paper, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isOn) Ink else Line, RoundedCornerShape(8.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        option,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isOn) Paper else Ink
                    )
                }
            }
        }
    }
}
