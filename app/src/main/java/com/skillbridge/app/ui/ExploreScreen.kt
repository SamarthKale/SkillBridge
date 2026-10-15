package com.skillbridge.app.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.maptiler.maptilersdk.map.LngLat
import com.skillbridge.app.data.Category
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.recommendation.RecommendationCache
import com.skillbridge.app.ui.components.*
import com.skillbridge.app.ui.theme.Line
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper
import com.skillbridge.app.utils.LocationUtils
import java.util.Locale

private fun budgetvalue(s: String): Int = s.filter { it.isDigit() }.toIntOrNull() ?: 0

@Composable
fun ExploreScreen(onOpenProject: (String) -> Unit) {
    val context = LocalContext.current
    val all by rememberOpportunities()
    val profile by rememberMyProfile()
    val apps by rememberMyApplications()

    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    var budgetBand by remember { mutableStateOf("Any budget") }
    val budgetBands = listOf("Any budget", "Under 10k", "10k - 20k", "Over 20k")

    var sortMode by remember { mutableStateOf("Newest first") }
    val sortOptions = listOf("Newest first", "Recommended For You")

    // GPS & Filtering State
    var distanceFilter by remember { mutableStateOf("Anywhere") }
    val distanceOptions = listOf("Anywhere", "5 km", "10 km", "25 km", "50 km")
    var studentLat by remember { mutableStateOf<Double?>(null) }
    var studentLon by remember { mutableStateOf<Double?>(null) }
    var isMapView by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.values.any { it }) {
                LocationUtils.getCurrentLocation(context, onSuccess = { lat, lon ->
                    studentLat = lat
                    studentLon = lon
                }, onFailure = { })
            } else {
                distanceFilter = "Anywhere"
            }
        }
    )

    fun onDistanceFilterChanged(newFilter: String) {
        distanceFilter = newFilter
        if (newFilter != "Anywhere" && (studentLat == null || studentLon == null)) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val filteredBase = run {
        val base = all.filter { o ->
            val matchesQuery = query.isBlank() ||
                o.title.contains(query, true) ||
                o.company.contains(query, true) ||
                o.skills.any { it.contains(query, true) }

            val matchesCategory = category == null || o.category == category

            val matchesBudget = when (budgetBand) {
                "Under 10k" -> budgetvalue(o.budget) < 10000
                "10k - 20k" -> budgetvalue(o.budget) in 10000..20000
                "Over 20k" -> budgetvalue(o.budget) > 20000
                else -> true
            }

            val matchesDistance = when {
                distanceFilter == "Anywhere" -> true
                // Distance filter active, student location known, project has coordinates
                studentLat != null && studentLon != null && o.latitude != null && o.longitude != null -> {
                    val dist = LocationUtils.calculateDistanceKm(studentLat!!, studentLon!!, o.latitude, o.longitude)
                    val maxDist = distanceFilter.replace(" km", "").toDoubleOrNull() ?: Double.MAX_VALUE
                    dist <= maxDist
                }
                // Distance filter active but project has no coordinates → exclude from distance-filtered view
                o.latitude == null || o.longitude == null -> false
                // Distance filter active, project has coordinates but student location unknown → include (can't filter)
                else -> true
            }

            matchesQuery && matchesCategory && matchesBudget && matchesDistance
        }.sortedByDescending { it.postedAt }

        // Diagnostic logging
        val withCoords = base.count { it.latitude != null && it.longitude != null }
        android.util.Log.d("SkillBridgeMapDebug", "Projects read from SQLite / Firestore: ${all.size}")
        android.util.Log.d("SkillBridgeMapDebug", "Projects passed to ExploreScreen map after filters: ${base.size}")
        android.util.Log.d("SkillBridgeMapDebug", "Projects with valid coordinates (will become markers): $withCoords")

        base
    }

    val recommendationsById = remember(profile, filteredBase, apps) {
        RecommendationCache.getOrComputeStudentRecommendations(
            profile = profile,
            opportunities = filteredBase,
            applications = apps,
            excludeApplied = false
        ).associateBy { it.opportunity.id }
    }

    val filtered = remember(filteredBase, sortMode, recommendationsById) {
        if (sortMode == "Recommended For You") {
            filteredBase.sortedByDescending { recommendationsById[it.id]?.matchScorePercent ?: 0 }
        } else {
            filteredBase
        }
    }

    // MapTiler Setup
    // Use an intelligent center:
    // 1. Current user location if available
    // 2. First filtered project's location
    // 3. Fallback to Mumbai (19.0760, 72.8777) but DO NOT drop a red marker there!
    val defaultLocation = LngLat(72.8777, 19.0760) // LngLat is (Longitude, Latitude)
    
    val initialLocation = filtered.firstOrNull { it.latitude != null && it.longitude != null }?.let {
        LngLat(it.longitude!!, it.latitude!!)
    } ?: if (studentLat != null && studentLon != null) {
        LngLat(studentLon!!, studentLat!!)
    } else {
        defaultLocation
    }

    ScreenScaffold(title = "Explore Projects") { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SearchField(query, { query = it }, placeholder = "Search projects, skills, companies...")
            }
            item {
                Text("Sort & Match", style = MaterialTheme.typography.labelMedium, color = Mute)
                Spacer(Modifier.height(6.dp))
                SelectableChips(
                    options = sortOptions,
                    selected = setOf(sortMode),
                    onToggle = { sortMode = it }
                )
            }
            item {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = Mute)
                Spacer(Modifier.height(6.dp))
                SelectableChips(
                    options = Category.ALL,
                    selected = category?.let { setOf(it) } ?: emptySet(),
                    onToggle = { c -> category = if (category == c) null else c }
                )
            }
            item {
                Text("Budget", style = MaterialTheme.typography.labelMedium, color = Mute)
                Spacer(Modifier.height(6.dp))
                SelectableChips(
                    options = budgetBands,
                    selected = setOf(budgetBand),
                    onToggle = { budgetBand = it }
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Distance", style = MaterialTheme.typography.labelMedium, color = Mute)
                    TextButton(onClick = { isMapView = !isMapView }) {
                        Text(if (isMapView) "Show List" else "Show Map")
                    }
                }
                SelectableChips(
                    options = distanceOptions,
                    selected = setOf(distanceFilter),
                    onToggle = { onDistanceFilterChanged(it) }
                )
            }
            
            item {
                if (distanceFilter != "Anywhere" && studentLat == null) {
                    Text(
                        "Enable location to use distance filters.",
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error
                    )
                } else {
                    val sortSuffix = if (sortMode == "Recommended For You") "ranked by AI match" else "newest first"
                    Text(
                        "${filtered.size} project${if (filtered.size == 1) "" else "s"}  ·  $sortSuffix",
                        style = MaterialTheme.typography.labelMedium, color = Mute
                    )
                }
            }
            
            if (isMapView) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .background(Paper, RoundedCornerShape(12.dp))
                            .border(1.dp, Line, RoundedCornerShape(12.dp))
                    ) {
                        var selectedProject by remember { mutableStateOf<Opportunity?>(null) }

                        // Automatically select the active project on the map so its details are visible
                        LaunchedEffect(filtered) {
                            if (selectedProject == null || filtered.none { it.id == selectedProject?.id }) {
                                selectedProject = filtered.firstOrNull { it.latitude != null && it.longitude != null }
                            }
                        }

                        SkillBridgeMap(
                            modifier = Modifier.fillMaxSize(),
                            initialCenter = initialLocation,
                            initialZoom = if (studentLat != null && studentLon != null) 12.0 else 10.0,
                            markers = filtered
                                .filter { it.latitude != null && it.longitude != null }
                                .map { opp ->
                                    MapMarkerData(
                                        id = opp.id,
                                        position = LngLat(opp.longitude!!, opp.latitude!!),
                                        title = opp.title,
                                        snippet = listOfNotNull(opp.company, if (opp.remote) "Remote" else opp.workArrangement).filter { it.isNotBlank() }.joinToString(" - ")
                                    )
                                },
                            userLocation = if (studentLat != null && studentLon != null) LngLat(studentLon!!, studentLat!!) else null,
                            onMarkerClick = { id ->
                                selectedProject = filtered.find { it.id == id }
                            }
                        )

                        if (studentLat == null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Location unavailable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Mute
                                )
                            }
                        }

                        if (filtered.none { it.latitude != null && it.longitude != null }) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(12.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    "No project locations available",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Mute
                                )
                            }
                        }

                        selectedProject?.let { opp ->
                            var distanceText = ""
                            if (studentLat != null && studentLon != null && opp.latitude != null && opp.longitude != null) {
                                val dist = LocationUtils.calculateDistanceKm(studentLat!!, studentLon!!, opp.latitude, opp.longitude)
                                distanceText = String.format(Locale.getDefault(), "📍 %.1f km away", dist)
                            }
                            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)) {
                                OpportunityCard(opp, distanceText) { onOpenProject(opp.id) }
                            }
                        }
                    }
                }
            } else {
                if (filtered.isEmpty()) {
                    item { EmptyState("No projects match your filters.") }
                } else {
                    items(filtered, key = { it.id }) { opp -> 
                        val rec = recommendationsById[opp.id]
                        if (sortMode == "Recommended For You" && rec != null) {
                            RecommendedProjectCard(
                                rec = rec,
                                onViewProject = { onOpenProject(opp.id) }
                            )
                        } else {
                            var distanceText = ""
                            if (studentLat != null && studentLon != null && opp.latitude != null && opp.longitude != null) {
                                val dist = LocationUtils.calculateDistanceKm(studentLat!!, studentLon!!, opp.latitude, opp.longitude)
                                distanceText = String.format(Locale.getDefault(), "📍 %.1f km away", dist)
                            }
                            OpportunityCard(opp, distanceText) { onOpenProject(opp.id) }
                        }
                    }
                }
            }
        }
    }
}
