package com.skillbridge.app.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.skillbridge.app.R
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.glass

/**
 * "About SkillBridge" block for the Profile screen: a short blurb plus the bundled explainer
 * video (`res/raw/skillbridge_explainer_voice.mp4`) played with the platform [VideoView], so no
 * extra library is needed. It does not auto-play (the video has a voice-over): tap it to show
 * the play/seek controls. It pauses when the app is backgrounded.
 */
@Composable
fun AboutSkillBridge(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Until the video reports its real size, assume landscape 16:9.
    var aspect by remember { mutableStateOf(16f / 9f) }
    var player by remember { mutableStateOf<VideoView?>(null) }

    // Pause when the screen goes to the background so audio never plays unseen.
    val owner = context as? LifecycleOwner
    DisposableEffect(owner, player) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) player?.pause()
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
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

        Column(
            Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(20.dp))
                .padding(6.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .aspectRatio(aspect),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val controller = MediaController(ctx)
                        controller.setAnchorView(this)
                        setMediaController(controller)
                        setVideoURI(Uri.parse("android.resource://${ctx.packageName}/${R.raw.skillbridge_explainer_voice}"))
                        setOnPreparedListener { mp ->
                            if (mp.videoWidth > 0 && mp.videoHeight > 0) {
                                aspect = (mp.videoWidth.toFloat() / mp.videoHeight).coerceIn(0.56f, 2f)
                            }
                            mp.setLooping(false)
                            seekTo(1) // paints the first frame as a poster without starting playback
                        }
                        player = this
                    }
                },
                onRelease = {
                    it.stopPlayback()
                    player = null
                }
            )
        }
    }
}
