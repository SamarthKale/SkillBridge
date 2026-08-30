package com.skillbridge.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// --- Monochrome palette: black / white / gray only -----------------------
val Ink = Color(0xFF111111)      // near-black, primary
val Paper = Color(0xFFFFFFFF)    // white background
val Smoke = Color(0xFFF4F4F4)    // very light gray fill
val Line = Color(0xFFE0E0E0)     // border gray
val Mute = Color(0xFF666666)     // secondary text gray

private val MonoColors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    secondary = Ink,
    onSecondary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Smoke,
    onSurfaceVariant = Mute,
    outline = Line,
    error = Color(0xFFB00020),
    onError = Paper
)

private val MonoTypography = Typography(
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Ink),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Ink),
    bodyLarge = TextStyle(fontSize = 15.sp, color = Ink),
    bodyMedium = TextStyle(fontSize = 14.sp, color = Ink),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, color = Mute)
)

@Composable
fun SkillBridgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MonoColors,
        typography = MonoTypography,
        content = content
    )
}
