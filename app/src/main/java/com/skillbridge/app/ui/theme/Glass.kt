package com.skillbridge.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * "Minimal glass" look: a soft, colourful sky backdrop with clearly translucent white
 * surfaces, a bright white edge and a soft shadow. The backdrop deliberately has distinct
 * colour blooms (blue, pink, lavender, cloud-white) so you can SEE them through the cards —
 * that is what reads as frosted glass. (Real backdrop blur would need a library.)
 */

// Backdrop gradient stops (top -> bottom). SkyTop / SkyBottom are also the status / navigation
// bar colours in res/values*/themes.xml — keep them in sync.
val SkyTop = Color(0xFFBBD4E8)
val SkyMid = Color(0xFFD3E2EE)
val SkyBottom = Color(0xFFEEF3F8)

/** Base colour for every frosted surface; callers pick the alpha. */
val GlassWhite = Color(0xFFFFFFFF)

private val GlassShadow = Color(0xFF28486F)

/** Full-screen gradient with soft colour blooms, drawn once behind the whole app. */
@Composable
fun GlassBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyTop, SkyMid, SkyBottom)))
            .drawBehind {
                fun bloom(cx: Float, cy: Float, r: Float, color: Color) {
                    val c = Offset(size.width * cx, size.height * cy)
                    val radius = size.width * r
                    drawCircle(
                        brush = Brush.radialGradient(listOf(color, Color.Transparent), c, radius),
                        radius = radius,
                        center = c
                    )
                }
                // Blooms stay clear of the top edge so the status bar colour matches SkyTop.
                bloom(0.05f, 0.28f, 0.75f, Color(0x887FB2E6)) // sky blue, left
                bloom(0.98f, 0.46f, 0.70f, Color(0x77F3B3C7)) // soft pink, right
                bloom(0.10f, 0.74f, 0.75f, Color(0x669F93F0)) // lavender, lower left
                bloom(0.92f, 0.16f, 0.50f, Color(0x99FFFFFF)) // cloud white, upper right
                bloom(0.80f, 0.92f, 0.65f, Color(0x55F8C8A8)) // peach, bottom right
            }
    )
}

/**
 * Frosted surface: soft shadow, translucent white fill (brighter at the top, clearer at the
 * bottom so the backdrop shows through), and a bright white hairline edge.
 * Apply BEFORE `.clickable` so the ripple is clipped to [shape].
 */
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(22.dp),
    fill: Float = 0.40f,
    elevation: Dp = 12.dp
): Modifier = this
    .outerShadow(shape, blur = elevation * 1.6f, dy = elevation * 0.5f, color = GlassShadow.copy(alpha = 0.20f))
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = (fill + 0.16f).coerceAtMost(1f)),
                Color.White.copy(alpha = (fill - 0.10f).coerceAtLeast(0.05f))
            )
        ),
        shape
    )
    .border(
        1.2.dp,
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.45f))),
        shape
    )

/**
 * Soft drop shadow drawn ONLY outside [shape]. The platform elevation shadow bleeds through
 * translucent fills (it showed up as pale rectangles inside the cards), so we clip the
 * shape out before drawing a blurred shadow layer. (No shadow on API 26-27, where Canvas
 * ignores shadow layers on paths — the glass edge still looks fine without it.)
 */
private fun Modifier.outerShadow(shape: Shape, blur: Dp, dy: Dp, color: Color): Modifier = drawBehind {
    val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawBehind)) }
    clipPath(path, ClipOp.Difference) {
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color.toArgb()
                setShadowLayer(blur.toPx(), 0f, dy.toPx(), color.toArgb())
            }
            canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
        }
    }
}

/** Flat, shadow-less variant for panels that sit inside a glass card. */
fun Modifier.glassInset(shape: Shape = RoundedCornerShape(14.dp), fill: Float = 0.38f): Modifier = this
    .clip(shape)
    .background(Color.White.copy(alpha = fill), shape)
    .border(1.dp, Color.White.copy(alpha = 0.85f), shape)
