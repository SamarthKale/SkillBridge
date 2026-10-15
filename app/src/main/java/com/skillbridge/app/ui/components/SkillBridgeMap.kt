package com.skillbridge.app.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.maptiler.maptilersdk.annotations.MTMarker
import com.maptiler.maptilersdk.annotations.MTTextPopup
import com.maptiler.maptilersdk.map.LngLat
import com.maptiler.maptilersdk.map.MTMapOptions
import com.maptiler.maptilersdk.map.MTMapViewController
import com.maptiler.maptilersdk.map.MTMapView
import com.maptiler.maptilersdk.map.MTMapViewDelegate
import com.maptiler.maptilersdk.map.style.MTMapReferenceStyle
import com.maptiler.maptilersdk.map.types.MTData
import com.maptiler.maptilersdk.events.MTEvent

private const val MAP_TAG = "SkillBridgeMapDebug"

data class MapMarkerData(
    val id: String,
    val position: LngLat,
    val title: String,
    val snippet: String
)

@Composable
fun SkillBridgeMap(
    modifier: Modifier = Modifier,
    initialCenter: LngLat = LngLat(0.0, 0.0),
    initialZoom: Double = 1.0,
    markers: List<MapMarkerData> = emptyList(),
    userLocation: LngLat? = null,
    onMarkerClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val controller = remember { MTMapViewController(context) }
    val options = remember { MTMapOptions() }

    // isMapReady becomes true once ON_READY fires and the JS map is fully initialised.
    var isMapReady by remember { mutableStateOf(false) }

    // Keep a stable reference to the current markers list so the tap handler always sees
    // the latest data without needing to be recreated on every recomposition.
    val currentMarkersRef = remember { mutableStateOf<List<MapMarkerData>>(emptyList()) }
    currentMarkersRef.value = markers

    // Register the delegate once per controller lifetime. The delegate stays attached for
    // the full lifetime of this composable — it is not re-registered on recomposition.
    LaunchedEffect(controller) {
        controller.delegate = object : MTMapViewDelegate {
            override fun onMapViewInitialized() {
                Log.d(MAP_TAG, "onMapViewInitialized fired — map is ready")
                isMapReady = true
                controller.setCenter(initialCenter)
                controller.setZoom(initialZoom)
            }

            override fun onEventTriggered(event: MTEvent, data: MTData?) {
                Log.d(MAP_TAG, "Event triggered: $event, id=${data?.id}, coord=${data?.coordinate}")
                if (event == MTEvent.ON_TAP || event == MTEvent.ON_POPUP_OPEN) {
                    val rawId = data?.id ?: ""
                    val cleanId = rawId.removePrefix("proj_").removePrefix("pop_proj_").removePrefix("pop_")

                    // 1. Match by id
                    val matchedById = currentMarkersRef.value.find { 
                        it.id == cleanId || 
                        "proj_${it.id.replace(Regex("[^a-zA-Z0-9_]"), "_")}" == rawId ||
                        "pop_proj_${it.id.replace(Regex("[^a-zA-Z0-9_]"), "_")}" == rawId
                    }
                    if (matchedById != null) {
                        Log.d(MAP_TAG, "Marker clicked by ID: ${matchedById.title} (${matchedById.id})")
                        onMarkerClick?.invoke(matchedById.id)
                        return
                    }

                    // 2. Match by proximity to tap coordinate (tap near pin)
                    data?.coordinate?.let { tapCoord ->
                        val nearest = currentMarkersRef.value.minByOrNull { m ->
                            com.skillbridge.app.utils.LocationUtils.calculateDistanceKm(
                                tapCoord.lat, tapCoord.lng, m.position.lat, m.position.lng
                            )
                        }
                        if (nearest != null) {
                            val dist = com.skillbridge.app.utils.LocationUtils.calculateDistanceKm(
                                tapCoord.lat, tapCoord.lng, nearest.position.lat, nearest.position.lng
                            )
                            if (dist <= 3.0) { // within reasonable click radius
                                Log.d(MAP_TAG, "Marker tapped by proximity ($dist km): ${nearest.title} (${nearest.id})")
                                onMarkerClick?.invoke(nearest.id)
                                return
                            }
                        }
                    }

                    // 3. If there is only one marker on screen, tap on map selects it
                    if (currentMarkersRef.value.size == 1) {
                        val single = currentMarkersRef.value.first()
                        Log.d(MAP_TAG, "Single marker auto-selected on map tap: ${single.title}")
                        onMarkerClick?.invoke(single.id)
                    }
                }
            }
        }
    }

    // Track currently displayed project markers so we can remove stale ones on update.
    var activeMarkers by remember { mutableStateOf<List<MTMarker>>(emptyList()) }

    // Track the user-location marker separately so it never interferes with project markers.
    var userLocationMarker by remember { mutableStateOf<MTMarker?>(null) }

    // Re-render project markers whenever the map becomes ready OR the marker list changes.
    LaunchedEffect(isMapReady, markers) {
        if (!isMapReady) return@LaunchedEffect

        val style = controller.style
        if (style == null) {
            Log.w(MAP_TAG, "isMapReady=true but controller.style is null — markers deferred")
            return@LaunchedEffect
        }

        Log.d(MAP_TAG, "Projects received by SkillBridgeMap: ${markers.size}")
        Log.d(MAP_TAG, "Markers attempted: ${markers.size}")

        // Remove stale project markers.
        activeMarkers.forEach { style.removeMarker(it) }

        val newMarkers = mutableListOf<MTMarker>()
        var successCount = 0

        markers.forEach { markerData ->
            try {
                // Guaranteed valid JS identifier (starts with letter, alphanumeric/underscore only)
                val safeMarkerId = "proj_${markerData.id.replace(Regex("[^a-zA-Z0-9_]"), "_")}"
                val marker = MTMarker(
                    identifier = safeMarkerId,
                    _coordinates = markerData.position
                )
                // Project markers are bold, unmistakable RED pins
                marker.color = android.graphics.Color.RED

                if (markerData.title.isNotEmpty() || markerData.snippet.isNotEmpty()) {
                    val popupText = buildString {
                        if (markerData.title.isNotEmpty()) appendLine("💼 ${markerData.title}")
                        if (markerData.snippet.isNotEmpty()) append(markerData.snippet)
                    }
                    val safePopupId = "pop_$safeMarkerId"
                    marker.popup = MTTextPopup(identifier = safePopupId, _coordinates = markerData.position).apply {
                        text = popupText
                    }
                }

                style.addMarker(marker)
                newMarkers.add(marker)
                successCount++

                Log.d(MAP_TAG, "Marker added: '${markerData.title}' at " +
                    "lng=${markerData.position.lng}, lat=${markerData.position.lat}")
            } catch (e: Exception) {
                Log.e(MAP_TAG, "Failed to add marker for '${markerData.title}': ${e.message}")
            }
        }

        activeMarkers = newMarkers
        Log.d(MAP_TAG, "Markers successfully created: $successCount / ${markers.size}")
    }

    // Separately manage the user-location marker.
    // Avoid dropping a duplicate pin directly on top of a project location.
    LaunchedEffect(isMapReady, userLocation, markers) {
        if (!isMapReady) return@LaunchedEffect

        val style = controller.style ?: return@LaunchedEffect

        if (userLocation != null) {
            // Check if user location is right on top of a project (< 50 meters away)
            val isOverlappingProject = markers.any { m ->
                com.skillbridge.app.utils.LocationUtils.calculateDistanceKm(
                    userLocation.lat, userLocation.lng, m.position.lat, m.position.lng
                ) < 0.05
            }

            if (isOverlappingProject) {
                // Do not draw an overlapping marker over the project pin
                userLocationMarker?.let {
                    style.removeMarker(it)
                    userLocationMarker = null
                }
                Log.d(MAP_TAG, "User location overlaps project marker — omitting duplicate pin so project remains prominent")
            } else {
                userLocationMarker?.let { style.removeMarker(it) }
                val marker = MTMarker(
                    identifier = "user_gps_location",
                    _coordinates = userLocation
                )
                // Distinct BLUE pin for current device GPS to clearly differentiate from red project pins
                marker.color = android.graphics.Color.BLUE
                style.addMarker(marker)
                userLocationMarker = marker
                Log.d(MAP_TAG, "User location marker placed at lng=${userLocation.lng}, lat=${userLocation.lat}")
            }
        } else {
            userLocationMarker?.let {
                style.removeMarker(it)
                userLocationMarker = null
                Log.d(MAP_TAG, "User location marker removed (location unavailable)")
            }
        }
    }

    // Re-centre the map when the initial centre changes (e.g. user grants GPS permission).
    LaunchedEffect(initialCenter) {
        if (isMapReady) {
            controller.setCenter(initialCenter)
            controller.setZoom(initialZoom)
        }
    }

    // Clean up the controller when this composable leaves the composition.
    DisposableEffect(controller) {
        onDispose {
            Log.d(MAP_TAG, "SkillBridgeMap disposed — cleaning up controller")
        }
    }

    MTMapView(
        referenceStyle = MTMapReferenceStyle.STREETS,
        options = options,
        controller = controller,
        modifier = modifier.fillMaxSize()
    )
}
