package com.skillbridge.app.ui.components

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.skillbridge.app.R
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.glass
import kotlinx.coroutines.delay

/**
 * Plays the bundled explainer video (`res/raw/skillbridge_explainer_voice.mp4`) with the platform
 * [MediaPlayer] on a [TextureView] — no extra library. A TextureView is a normal view, so it clips,
 * scrolls and layers like the rest of the screen. Controls are drawn INSIDE the frame (the old
 * floating `MediaController` popup stayed pinned to the screen and ended up over the bottom bar).
 */
private class VideoPlayerState(private val context: Context) {
    private var player: MediaPlayer? = null
    private var surface: Surface? = null

    var prepared by mutableStateOf(false)
    var playing by mutableStateOf(false)
    var positionMs by mutableIntStateOf(0)
    var durationMs by mutableIntStateOf(0)
    var aspect by mutableFloatStateOf(16f / 9f) // until the real size is known

    fun attach(texture: SurfaceTexture) {
        release()
        val s = Surface(texture)
        surface = s
        player = MediaPlayer().apply {
            setDataSource(context, Uri.parse("android.resource://${context.packageName}/${R.raw.skillbridge_explainer_voice}"))
            setSurface(s)
            setOnPreparedListener { mp ->
                prepared = true
                durationMs = mp.duration
                if (mp.videoWidth > 0 && mp.videoHeight > 0) {
                    aspect = (mp.videoWidth.toFloat() / mp.videoHeight).coerceIn(0.56f, 2f)
                }
                mp.seekTo(1, MediaPlayer.SEEK_CLOSEST) // paints the first frame as a poster
            }
            setOnCompletionListener { playing = false; positionMs = durationMs }
            prepareAsync()
        }
    }

    fun toggle() {
        val mp = player ?: return
        if (!prepared) return
        if (playing) {
            mp.pause(); playing = false
        } else {
            if (positionMs >= durationMs - 300) { mp.seekTo(0); positionMs = 0 }
            mp.start(); playing = true
        }
    }

    fun pause() {
        if (playing) { player?.pause(); playing = false }
    }

    fun seekToFraction(f: Float) {
        if (!prepared || durationMs <= 0) return
        val target = (f.coerceIn(0f, 1f) * durationMs).toInt()
        player?.seekTo(target.toLong(), MediaPlayer.SEEK_CLOSEST)
        positionMs = target
    }

    fun poll() {
        val mp = player ?: return
        if (prepared) positionMs = mp.currentPosition
    }

    fun release() {
        try { player?.release() } catch (_: Exception) { }
        player = null
        surface?.release()
        surface = null
        prepared = false
        playing = false
    }
}

/** "About SkillBridge" block for the Profile screen: a short blurb plus the explainer video. */
@Composable
fun AboutSkillBridge(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val vp = remember { VideoPlayerState(context) }

    DisposableEffect(vp) { onDispose { vp.release() } }

    // Pause when the app goes to the background so audio never plays unseen.
    val owner = context as? LifecycleOwner
    DisposableEffect(owner, vp) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_PAUSE) vp.pause() }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    LaunchedEffect(vp.playing) {
        while (vp.playing) { vp.poll(); delay(250) }
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader("About SkillBridge")
        Text(
            "SkillBridge connects college students with freelance projects: build a profile, " +
                "apply with an intro, track every application, and chat with the client once you're " +
                "shortlisted. Watch the walkthrough below.",
            style = MaterialTheme.typography.bodyMedium,
            color = Mute
        )

        Box(
            Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(20.dp))
                .padding(6.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(vp.aspect)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.matchParentSize(),
                    factory = { ctx ->
                        TextureView(ctx).apply {
                            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) = vp.attach(st)
                                override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) = Unit
                                override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                                    vp.release(); return true
                                }
                                override fun onSurfaceTextureUpdated(st: SurfaceTexture) = Unit
                            }
                        }
                    }
                )

                // Tap anywhere on the video to play / pause.
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { vp.toggle() }
                )

                if (!vp.playing) PlayBadge(Modifier.align(Alignment.Center))

                VideoControlsBar(
                    vp = vp,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun PlayBadge(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.2.dp, Color.White.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(22.dp)) {
            val p = Path().apply {
                moveTo(size.width * 0.18f, size.height * 0.05f)
                lineTo(size.width * 0.95f, size.height * 0.5f)
                lineTo(size.width * 0.18f, size.height * 0.95f)
                close()
            }
            drawPath(p, Color.White)
        }
    }
}

/** Small play/pause button, elapsed time, a tappable/draggable progress track, and total time. */
@Composable
private fun VideoControlsBar(vp: VideoPlayerState, modifier: Modifier = Modifier) {
    var trackWidthPx by remember { mutableIntStateOf(1) }
    val fraction = if (vp.durationMs > 0) (vp.positionMs.toFloat() / vp.durationMs).coerceIn(0f, 1f) else 0f

    Row(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
            .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { vp.toggle() },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(14.dp)) {
                if (vp.playing) {
                    val w = size.width * 0.3f
                    drawRoundRect(Color.White, Offset(size.width * 0.08f, 0f), Size(w, size.height), CornerRadius(2f, 2f))
                    drawRoundRect(Color.White, Offset(size.width * 0.62f, 0f), Size(w, size.height), CornerRadius(2f, 2f))
                } else {
                    drawPath(
                        Path().apply {
                            moveTo(size.width * 0.1f, 0f)
                            lineTo(size.width, size.height * 0.5f)
                            lineTo(size.width * 0.1f, size.height)
                            close()
                        },
                        Color.White
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(formatTime(vp.positionMs), color = Color.White, fontSize = 11.sp)
        Spacer(Modifier.width(8.dp))

        // Track: tap to seek.
        Box(
            Modifier
                .weight(1f)
                .height(24.dp)
                .onSizeChanged { trackWidthPx = it.width.coerceAtLeast(1) }
                .pointerInput(vp) {
                    detectTapGestures { offset -> vp.seekToFraction(offset.x / trackWidthPx) }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.35f)))
            Box(Modifier.fillMaxWidth(fraction).height(3.dp).clip(CircleShape).background(Color.White))
        }

        Spacer(Modifier.width(8.dp))
        Text(formatTime(vp.durationMs), color = Color.White, fontSize = 11.sp)
    }
}

private fun formatTime(ms: Int): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}
