package com.skillbridge.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.core.content.ContextCompat
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.LngLat
import com.skillbridge.app.ui.components.MapMarkerData
import com.skillbridge.app.ui.components.OpportunityCard
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SelectableChips
import com.skillbridge.app.ui.components.SkillBridgeMap
import com.skillbridge.app.ui.components.rememberOpportunities
import com.skillbridge.app.ui.theme.Line
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper
import com.skillbridge.app.utils.CityCoords
import com.skillbridge.app.utils.LocationUtils
import java.util.Locale

private const val MY_LOCATION = "My location"
private val RADIUS_OPTIONS = listOf("5 km", "10 km", "25 km", "50 km", "100 km")
private val QUICK_CITIES = listOf("Mumbai", "Pune", "Bengaluru", "Hyderabad", "Delhi", "Chennai", "Kolkata", "Ahmedabad")

private data class Nearby(val opp: Opportunity, val km: Double)

/**
 * Jobs around you on an OpenStreetMap, plus a distance-sorted list. Uses the device GPS, or a
 * city picked from the chips when GPS is unavailable (e.g. an emulator parked in California).
 * Remote jobs have no fixed place, so they are not pinned.
 */
@Composable
fun NearbyJobsScreen(
    onBack: () -> Unit,
    onOpenProject: (String) -> Unit,
    onViewMap: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val all by rememberOpportunities()

    var gps by remember { mutableStateOf<LngLat?>(null) }
    var gpsFailed by remember { mutableStateOf(false) }
    var city by remember { mutableStateOf<String?>(null) }
    var radius by remember { mutableStateOf("25 km") }
    var selectedId by remember { mutableStateOf<String?>(null) }

    fun fetchGps() {
        LocationUtils.getCurrentLocation(
            context,
            onSuccess = { lat, lon -> gps = LngLat(lon, lat); gpsFailed = false },
            onFailure = { gpsFailed = true }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted -> if (granted.values.any { it }) fetchGps() else gpsFailed = true }

    LaunchedEffect(Unit) {
        val has = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (has) fetchGps()
        else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    val cityPoint = city?.let { CityCoords.CITIES[it] }?.let { LngLat(it.second, it.first) }
    val center: LngLat? = cityPoint ?: gps
    val radiusKm = radius.removeSuffix(" km").toDouble()

    val nearby = remember(all, center, radiusKm) {
        if (center == null) emptyList()
        else all.mapNotNull { o ->
            if (o.remote) return@mapNotNull null // remote jobs only carry a placeholder base city
            val lat = o.latitude ?: return@mapNotNull null
            val lon = o.longitude ?: return@mapNotNull null
            Nearby(o, LocationUtils.calculateDistanceKm(center.lat, center.lng, lat, lon))
        }.filter { it.km <= radiusKm }.sortedBy { it.km }
    }
    val remoteCount = all.count { it.remote }
    val selected = nearby.firstOrNull { it.opp.id == selectedId }

    ScreenScaffold(title = "Nearby Jobs", onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Search around", style = MaterialTheme.typography.labelMedium, color = Mute)
                Spacer(Modifier.height(6.dp))
                SelectableChips(
                    options = listOf(MY_LOCATION) + QUICK_CITIES,
                    selected = setOf(city ?: MY_LOCATION),
                    onToggle = { picked ->
                        if (picked == MY_LOCATION) {
                            city = null
                            if (gps == null) permissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        } else city = picked
                        selectedId = null
                    }
                )
            }
            item {
                Text("Radius", style = MaterialTheme.typography.labelMedium, color = Mute)
                Spacer(Modifier.height(6.dp))
                SelectableChips(RADIUS_OPTIONS, setOf(radius), { radius = it })
            }
            item {
                val note = when {
                    center == null && gpsFailed -> "Couldn't get your location. Pick a city above to explore."
                    center == null -> "Finding your location…"
                    else -> "${nearby.size} job${if (nearby.size == 1) "" else "s"} within $radius" +
                        (if (city != null) " of $city" else " of you")
                }
                Text(note, style = MaterialTheme.typography.labelMedium, color = Mute)
            }
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .background(Paper, RoundedCornerShape(12.dp))
                        .border(1.dp, Line, RoundedCornerShape(12.dp))
                ) {
                    SkillBridgeMap(
                        modifier = Modifier.fillMaxSize(),
                        initialCenter = center ?: LngLat(78.9629, 20.5937),
                        initialZoom = if (center == null) 4.0 else 11.0,
                        markers = nearby.map {
                            MapMarkerData(
                                id = it.opp.id,
                                position = LngLat(it.opp.longitude!!, it.opp.latitude!!),
                                title = it.opp.title,
                                snippet = listOf(it.opp.company, it.opp.budget).filter { s -> s.isNotBlank() }.joinToString(" · ")
                            )
                        },
                        userLocation = if (city == null) gps else null,
                        radiusKm = if (center != null) radiusKm else null,
                        onMarkerClick = { selectedId = it }
                    )
                    selected?.let { n ->
                        Box(Modifier.align(Alignment.BottomCenter).padding(8.dp)) {
                            OpportunityCard(n.opp, distanceLabel(n.km), onViewMap = { onViewMap(n.opp.id) }) { onOpenProject(n.opp.id) }
                        }
                    }
                }
            }
            if (center != null && nearby.isEmpty()) {
                item {
                    EmptyState(
                        "No jobs within $radius. Try a bigger radius or another city." +
                            if (remoteCount > 0) " $remoteCount remote jobs are available in Explore." else ""
                    )
                }
            }
            items(nearby, key = { it.opp.id }) { n ->
                OpportunityCard(n.opp, distanceLabel(n.km), onViewMap = { onViewMap(n.opp.id) }) { onOpenProject(n.opp.id) }
            }
        }
    }
}

private fun distanceLabel(km: Double) = String.format(Locale.getDefault(), "📍 %.1f km away", km)
