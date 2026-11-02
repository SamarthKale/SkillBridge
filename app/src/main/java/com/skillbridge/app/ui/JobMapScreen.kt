package com.skillbridge.app.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.data.Repo
import com.skillbridge.app.ui.components.LngLat
import com.skillbridge.app.ui.components.MapMarkerData
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SkillBridgeMap
import com.skillbridge.app.ui.theme.Line
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.utils.CityCoords
import java.util.Locale

/** Where to show a job: its own city, or a "Company base" city for remote jobs. */
fun Opportunity.placeLabel(): String {
    val lat = latitude ?: return location
    val lon = longitude ?: return location
    val city = CityCoords.nearestCity(lat, lon)
    return when {
        remote || location.isBlank() || location.equals("Remote", true) -> "Company base: $city"
        else -> location
    }
}

/** Full-screen OpenStreetMap showing one job's location, with a hand-off to a maps app. */
@Composable
fun JobMapScreen(opportunityId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var opp by remember { mutableStateOf<Opportunity?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(opportunityId) {
        Repo.getOpportunity(opportunityId) { opp = it; loading = false }
    }

    ScreenScaffold(title = "Job Location", onBack = onBack) { padding ->
        val o = opp
        val lat = o?.latitude
        val lon = o?.longitude
        if (o == null || lat == null || lon == null) {
            Text(
                if (loading) "Loading..." else "This job has no location.",
                color = Mute, modifier = Modifier.padding(padding).padding(20.dp)
            )
            return@ScreenScaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f).fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Line, RoundedCornerShape(12.dp))
            ) {
                SkillBridgeMap(
                    modifier = Modifier.fillMaxSize(),
                    initialCenter = LngLat(lon, lat),
                    initialZoom = 13.0,
                    markers = listOf(
                        MapMarkerData(o.id, LngLat(lon, lat), o.title, o.company)
                    )
                )
            }
            Text(o.title, style = MaterialTheme.typography.titleMedium)
            Text("${o.company} · ${o.placeLabel()}", style = MaterialTheme.typography.bodyMedium, color = Mute)
            PrimaryButton("Open in Maps app", onClick = {
                val coords = String.format(Locale.US, "%.5f,%.5f", lat, lon)
                val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$coords?q=$coords(${Uri.encode(o.title)})"))
                try {
                    context.startActivity(geo)
                } catch (_: ActivityNotFoundException) {
                    // No maps app installed: fall back to the browser.
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.openstreetmap.org/?mlat=${lat}&mlon=${lon}#map=14/${lat}/${lon}")))
                    } catch (_: ActivityNotFoundException) { }
                }
            })
        }
    }
}
