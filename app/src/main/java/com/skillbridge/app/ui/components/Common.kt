package com.skillbridge.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.Color
import com.skillbridge.app.ui.theme.GlassWhite
import com.skillbridge.app.ui.theme.glass
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.skillbridge.app.utils.FileUtils
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.skillbridge.app.data.AppStatus
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.AccentSoft
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Line
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper
import com.skillbridge.app.ui.theme.Smoke
import com.skillbridge.app.ui.theme.StatusAccepted
import com.skillbridge.app.ui.theme.StatusApplied
import com.skillbridge.app.ui.theme.StatusRejected
import com.skillbridge.app.ui.theme.StatusReview
import com.skillbridge.app.ui.theme.StatusShortlisted

// ---- Text fields -----------------------------------------------------

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
        colors = glassFieldColors(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}

/** Translucent field fill + soft blue-grey outline; turns ink-black when focused. */
@Composable
private fun glassFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = GlassWhite.copy(alpha = 0.70f),
    unfocusedContainerColor = GlassWhite.copy(alpha = 0.50f),
    disabledContainerColor = GlassWhite.copy(alpha = 0.35f),
    focusedBorderColor = Ink,
    unfocusedBorderColor = Color(0xFFCFD9E1),
    cursorColor = Ink
)

// ---- Buttons --------------------------------------------------------

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) = FilledButton(text, onClick, modifier, enabled, loading, Ink)

@Composable
fun AccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) = FilledButton(text, onClick, modifier, enabled, loading, Accent)

@Composable
private fun FilledButton(
    text: String, onClick: () -> Unit, modifier: Modifier,
    enabled: Boolean, loading: Boolean, container: androidx.compose.ui.graphics.Color
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = Paper),
        modifier = modifier.fillMaxWidth().height(50.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(color = Paper, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Text(text, color = Paper, style = MaterialTheme.typography.labelLarge)
        }
    }
}

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
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = GlassWhite.copy(alpha = 0.75f)),
        border = BorderStroke(1.dp, Color(0x1F111111)),
        modifier = modifier.fillMaxWidth().height(50.dp)
    ) {
        Text(text, color = Ink, style = MaterialTheme.typography.labelLarge)
    }
}

// ---- Small text bits ---------------------------------------------

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

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = modifier)
}

@Composable
fun LabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Mute)
        Text(value.ifBlank { "-" }, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Mute)
    }
}

// ---- Cards / containers ----------------------------------------

@Composable
fun OutlinedCardBox(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    // Frosted glass card. `glass()` clips to the shape first so the ripple stays inside it.
    Box(
        if (onClick != null) modifier.fillMaxWidth().glass().clickable(onClick = onClick)
        else modifier.fillMaxWidth().glass()
    ) { Box(Modifier.padding(16.dp)) { content() } }
}

@Composable
fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Box(modifier.glass()) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value.toString(), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = Mute)
        }
    }
}

// ---- Chips / pills --------------------------------------------

@Composable
fun Pill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(GlassWhite.copy(alpha = 0.55f), RoundedCornerShape(999.dp))
            .border(1.dp, GlassWhite.copy(alpha = 0.9f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) { Text(text, style = MaterialTheme.typography.labelMedium, color = Ink) }
}

@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val c = when (status) {
        AppStatus.UNDER_REVIEW -> StatusReview
        AppStatus.SHORTLISTED -> StatusShortlisted
        AppStatus.ACCEPTED -> StatusAccepted
        AppStatus.REJECTED -> StatusRejected
        else -> StatusApplied
    }
    Box(
        modifier
            .background(c.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
            .border(1.dp, c.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(status, style = MaterialTheme.typography.labelMedium, color = c, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillChips(skills: List<String>, modifier: Modifier = Modifier) {
    if (skills.isEmpty()) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) { skills.forEach { Pill(it) } }
}

/** Multi- or single-select chip group. Used for categories and skills. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectableChips(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { opt ->
            val on = opt in selected
            Box(
                Modifier
                    .clickable { onToggle(opt) }
                    .background(if (on) Ink else GlassWhite.copy(alpha = 0.55f), RoundedCornerShape(999.dp))
                    .border(1.dp, if (on) Ink else GlassWhite.copy(alpha = 0.9f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    opt,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (on) Paper else Ink
                )
            }
        }
    }
}

// ---- Search -----------------------------------------------------

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search projects...",
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Mute) },
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = glassFieldColors(),
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    )
}

@Composable
fun Avatar(name: String, size: Int = 44, modifier: Modifier = Modifier, photo: String = "") {
    val initials = name.trim().split(" ").filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
    val bitmap = remember(photo) { if (photo.isBlank()) null else FileUtils.decodePhoto(photo)?.asImageBitmap() }
    Box(
        modifier
            .size(size.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(AccentSoft, RoundedCornerShape(999.dp))
            .border(1.dp, Line, RoundedCornerShape(999.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(bitmap, contentDescription = name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Text(initials, style = MaterialTheme.typography.labelLarge, color = Accent)
        }
    }
}
