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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

import com.skillbridge.app.ui.components.SkillBridgeMap

import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.Profile
import com.skillbridge.app.recommendation.RecommendationCache
import com.skillbridge.app.ui.components.Avatar
import com.skillbridge.app.ui.components.CandidateRecommendationCard
import com.skillbridge.app.ui.components.EmptyState
import com.skillbridge.app.ui.components.OutlinedCardBox
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SearchField
import com.skillbridge.app.ui.components.SelectableChips
import com.skillbridge.app.ui.components.SkillChips
import com.skillbridge.app.ui.components.SkillBridgeMap
import com.skillbridge.app.ui.components.MapMarkerData
import com.skillbridge.app.ui.components.rememberClientProjects
import com.skillbridge.app.ui.components.rememberStudentProfiles
import com.skillbridge.app.ui.theme.Line
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper
import com.skillbridge.app.utils.LocationUtils

import java.util.Locale
import com.maptiler.maptilersdk.map.LngLat

@Composable
fun ClientDiscoverScreen(
    onOpenStudentProfile: (String) -> Unit
) {

    val context = LocalContext.current
    val allStudents by rememberStudentProfiles()
    val clientProjects by rememberClientProjects()
    val clientUid = AuthManager.uid()

    var query by remember { mutableStateOf("") }
    var selectedProjectTitle by remember { mutableStateOf("All Freelancers") }
    val projectFilterOptions = remember(clientProjects) {
        listOf("All Freelancers") + clientProjects.take(4).map { it.title }
    }
    val selectedMatchProject = clientProjects.find { it.title == selectedProjectTitle }

    // ---------------------------------------------------------
    // GPS & Filtering State
    // ---------------------------------------------------------

    var distanceFilter by remember { mutableStateOf("Anywhere") }

    val distanceOptions = listOf(
        "Anywhere",
        "5 km",
        "10 km",
        "25 km",
        "50 km"
    )

    var clientLat by remember { mutableStateOf<Double?>(null) }
    var clientLon by remember { mutableStateOf<Double?>(null) }

    var isMapView by remember { mutableStateOf(false) }

    // ---------------------------------------------------------
    // Location Permission
    // ---------------------------------------------------------

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->

            if (permissions.values.any { it }) {

                LocationUtils.getCurrentLocation(
                    context,
                    onSuccess = { lat, lon ->
                        clientLat = lat
                        clientLon = lon
                    },
                    onFailure = { }
                )

            } else {
                distanceFilter = "Anywhere"
            }
        }
    )

    // ---------------------------------------------------------
    // Distance Filter
    // ---------------------------------------------------------

    fun onDistanceFilterChanged(newFilter: String) {

        distanceFilter = newFilter

        if (
            newFilter != "Anywhere" &&
            (clientLat == null || clientLon == null)
        ) {

            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // ---------------------------------------------------------
    // Filter Students
    // ---------------------------------------------------------

    val filteredBase = allStudents
        .filter { student ->

            val matchesQuery =
                query.isBlank() ||
                        student.name.contains(
                            query,
                            ignoreCase = true
                        ) ||
                        student.skills.any {
                            it.contains(
                                query,
                                ignoreCase = true
                            )
                        } ||
                        student.college.contains(
                            query,
                            ignoreCase = true
                        )

            var matchesDistance = true

            if (
                distanceFilter != "Anywhere" &&
                clientLat != null &&
                clientLon != null &&
                student.latitude != null &&
                student.longitude != null
            ) {

                val distance =
                    LocationUtils.calculateDistanceKm(
                        clientLat!!,
                        clientLon!!,
                        student.latitude,
                        student.longitude
                    )

                val maxDistance =
                    distanceFilter
                        .replace(" km", "")
                        .toDoubleOrNull()
                        ?: Double.MAX_VALUE

                matchesDistance = distance <= maxDistance

            } else if (distanceFilter != "Anywhere") {

                // Student without location is excluded
                matchesDistance = false
            }

            matchesQuery && matchesDistance

        }
        .sortedByDescending { it.createdAt }

    val candidateRecByUid = remember(selectedMatchProject, filteredBase, clientUid) {
        if (selectedMatchProject == null) emptyMap()
        else RecommendationCache.getOrComputeCandidateRecommendations(
            project = selectedMatchProject,
            studentProfiles = filteredBase,
            proposals = emptyList(),
            authenticatedClientUid = clientUid
        ).associateBy { it.candidate.uid }
    }

    val filtered = remember(filteredBase, selectedMatchProject, candidateRecByUid) {
        if (selectedMatchProject != null) {
            filteredBase.sortedByDescending { candidateRecByUid[it.uid]?.matchScorePercent ?: 0 }
        } else {
            filteredBase
        }
    }

    // ---------------------------------------------------------
    // Google Maps
    // ---------------------------------------------------------

    val defaultLocation = LngLat(
        72.8777,
        19.0760
    )

    val initialLocation =
        if (clientLat != null && clientLon != null) {

            LngLat(
                clientLon!!,
                clientLat!!
            )

        } else {

            filtered
                .firstOrNull {
                    it.latitude != null &&
                            it.longitude != null
                }
                ?.let {
                    LngLat(
                        it.longitude!!,
                        it.latitude!!
                    )
                }
                ?: defaultLocation
        }

    // ---------------------------------------------------------
    // Check Location Permission
    // ---------------------------------------------------------

    val hasLocationPermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    // ---------------------------------------------------------
    // Screen
    // ---------------------------------------------------------

    ScreenScaffold(
        title = "Discover Talent"
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(
                Modifier.height(12.dp)
            )

            // -------------------------------------------------
            // Search
            // -------------------------------------------------

            SearchField(
                query,
                { query = it },
                placeholder = "Search by name, skill, or college..."
            )

            if (clientProjects.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Rank by AI Match for Project",
                    style = MaterialTheme.typography.labelMedium,
                    color = Mute
                )
                Spacer(Modifier.height(6.dp))
                SelectableChips(
                    options = projectFilterOptions,
                    selected = setOf(selectedProjectTitle),
                    onToggle = { selectedProjectTitle = it }
                )
            }

            Spacer(
                Modifier.height(10.dp)
            )

            // -------------------------------------------------
            // Distance + Map/List Toggle
            // -------------------------------------------------

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "Distance",
                    style = MaterialTheme.typography.labelMedium,
                    color = Mute
                )

                TextButton(
                    onClick = {
                        isMapView = !isMapView
                    }
                ) {

                    Text(
                        if (isMapView)
                            "Show List"
                        else
                            "Show Map"
                    )
                }
            }

            // -------------------------------------------------
            // Distance Chips
            // -------------------------------------------------

            SelectableChips(
                options = distanceOptions,
                selected = setOf(distanceFilter),
                onToggle = {
                    onDistanceFilterChanged(it)
                }
            )

            Spacer(
                Modifier.height(10.dp)
            )

            if (distanceFilter != "Anywhere" && clientLat == null) {
                Text(
                    "Enable location to use distance filters.",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error
                )
            } else {
                val matchSuffix = if (selectedMatchProject != null) "  ·  ranked by AI match" else ""
                Text(
                    "${filtered.size} student${
                        if (filtered.size == 1)
                            ""
                        else
                            "s"
                    } found$matchSuffix",
                    style = MaterialTheme.typography.labelMedium,
                    color = Mute
                )
            }

            Spacer(
                Modifier.height(10.dp)
            )

            // =================================================
            // MAP VIEW
            // =================================================

            if (isMapView) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            Paper,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            Line,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    var selectedStudent by remember { mutableStateOf<Profile?>(null) }

                    SkillBridgeMap(
                        modifier = Modifier.fillMaxSize(),
                        initialCenter = initialLocation,
                        initialZoom = if (clientLat != null && clientLon != null) 12.0 else 10.0,
                        markers = filtered
                            .filter { it.latitude != null && it.longitude != null }
                            .map { student ->
                                val (approxLat, approxLon) = LocationUtils.approximateLocation(student.latitude!!, student.longitude!!)
                                MapMarkerData(
                                    id = student.uid,
                                    position = LngLat(approxLon, approxLat),
                                    title = student.name,
                                    snippet = listOfNotNull(student.degree, student.college)
                                        .filter { it.isNotBlank() }
                                        .joinToString(", ")
                                )
                            },
                        userLocation = if (clientLat != null && clientLon != null) LngLat(clientLon!!, clientLat!!) else null,
                        onMarkerClick = { id ->
                            selectedStudent = filtered.find { it.uid == id }
                        }
                    )

                    // -------------------------------------------------
                    // No Student Location Message
                    // -------------------------------------------------

                    if (
                        filtered.none {
                            it.latitude != null &&
                                    it.longitude != null
                        }
                    ) {

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(12.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 10.dp
                                )
                        ) {

                            Text(
                                "No student locations available",
                                style = MaterialTheme.typography.labelMedium,
                                color = Mute
                            )
                        }
                    }

                        if (clientLat == null) {
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
                        
                        selectedStudent?.let { student ->
                        var distanceText = ""
                        if (clientLat != null && clientLon != null && student.latitude != null && student.longitude != null) {
                            val dist = LocationUtils.calculateDistanceKm(clientLat!!, clientLon!!, student.latitude, student.longitude)
                            distanceText = String.format(Locale.getDefault(), "📍 %.1f km away", dist)
                        }
                        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)) {
                            StudentCard(student, distanceText) { onOpenStudentProfile(student.uid) }
                        }
                    }
                }

                Spacer(
                    Modifier.height(20.dp)
                )

            } else {

                // =================================================
                // LIST VIEW
                // =================================================

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    contentPadding =
                        PaddingValues(
                            bottom = 20.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    if (filtered.isEmpty()) {

                        item {

                            EmptyState(
                                "No students found matching your criteria."
                            )
                        }

                    } else {

                        items(
                            filtered,
                            key = { it.uid }
                        ) { student ->

                            val rec = candidateRecByUid[student.uid]
                            if (selectedMatchProject != null && rec != null) {
                                CandidateRecommendationCard(
                                    rec = rec,
                                    onViewProfile = { onOpenStudentProfile(student.uid) }
                                )
                            } else {
                                var distanceText = ""

                                if (
                                    clientLat != null &&
                                    clientLon != null &&
                                    student.latitude != null &&
                                    student.longitude != null
                                ) {

                                    val distance =
                                        LocationUtils.calculateDistanceKm(
                                            clientLat!!,
                                            clientLon!!,
                                            student.latitude,
                                            student.longitude
                                        )

                                    distanceText =
                                        String.format(
                                            Locale.getDefault(),
                                            "📍 %.1f km away",
                                            distance
                                        )
                                }

                                StudentCard(
                                    student = student,
                                    distanceText = distanceText
                                ) {

                                    onOpenStudentProfile(
                                        student.uid
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// Student Card
// =============================================================

@Composable
fun StudentCard(
    student: Profile,
    distanceText: String = "",
    onClick: () -> Unit
) {

    OutlinedCardBox(
        onClick = onClick
    ) {

        Column {

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Avatar(
                    student.name,
                    size = 48
                )

                Spacer(
                    Modifier.width(12.dp)
                )

                Column(
                    Modifier.weight(1f)
                ) {

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            student.name,
                            style =
                                MaterialTheme.typography.bodyLarge,
                            fontWeight =
                                FontWeight.SemiBold
                        )

                        if (distanceText.isNotBlank()) {

                            Text(
                                distanceText,
                                style =
                                    MaterialTheme.typography.labelMedium,
                                color =
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        listOfNotNull(
                            student.degree,
                            student.college
                        )
                            .filter {
                                it.isNotBlank()
                            }
                            .joinToString(", "),

                        style =
                            MaterialTheme.typography.labelMedium,

                        color = Mute,

                        maxLines = 1
                    )
                }
            }

            if (student.bio.isNotBlank()) {

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    student.bio,
                    style =
                        MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }

            if (student.skills.isNotEmpty()) {

                Spacer(
                    Modifier.height(8.dp)
                )

                SkillChips(
                    student.skills.take(4)
                )
            }
        }
    }
}