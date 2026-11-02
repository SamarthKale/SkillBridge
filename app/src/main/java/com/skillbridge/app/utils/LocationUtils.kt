package com.skillbridge.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        context: Context,
        onSuccess: (latitude: Double, longitude: Double) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        val cancellationTokenSource = CancellationTokenSource()

        // Ask for a FRESH high-accuracy fix (maxUpdateAge 0) instead of letting the provider hand
        // back a cached location from earlier, which can be somewhere else entirely.
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .setDurationMillis(15_000)
            .build()

        fusedLocationClient.getCurrentLocation(
            request,
            cancellationTokenSource.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                android.util.Log.d("SkillBridge GPS DEBUG", "locationReceived = true, latitude = ${location.latitude}, longitude = ${location.longitude}")
                onSuccess(location.latitude, location.longitude)
            } else {
                android.util.Log.e("SkillBridge GPS DEBUG", "locationReceived = false (location is null)")
                onFailure(Exception("Location is null"))
            }
        }.addOnFailureListener { exception ->
            android.util.Log.e("SkillBridge GPS DEBUG", "locationReceived = false (exception)", exception)
            onFailure(exception)
        }
    }

    /**
     * Human-readable place for a point ("Vashi, Navi Mumbai"), or null when it can't be resolved
     * (no network / no geocoder). Blocking: call off the main thread.
     */
    @Suppress("DEPRECATION")
    fun placeName(context: Context, lat: Double, lon: Double): String? = try {
        if (!android.location.Geocoder.isPresent()) null
        else android.location.Geocoder(context, java.util.Locale.getDefault())
            .getFromLocation(lat, lon, 1)?.firstOrNull()?.let { a ->
                listOfNotNull(a.subLocality ?: a.thoroughfare, a.locality ?: a.subAdminArea)
                    .distinct().joinToString(", ").ifBlank { a.adminArea }
            }
    } catch (_: Exception) {
        null
    }

    /**
     * Calculates distance between two points in kilometers using the Haversine formula.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Approximates location for privacy by rounding to 2 decimal places.
     * This provides accuracy to about 1.11 kilometers.
     */
    fun approximateLocation(lat: Double, lon: Double): Pair<Double, Double> {
        val approxLat = kotlin.math.round(lat * 100) / 100.0
        val approxLon = kotlin.math.round(lon * 100) / 100.0
        return Pair(approxLat, approxLon)
    }
}
