package com.skillbridge.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
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

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
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
