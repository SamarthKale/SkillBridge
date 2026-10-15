package com.skillbridge.app.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot

private const val MAPPER_TAG = "SkillBridgeMapDebug"

/**
 * Returns true when both values are finite and within valid geographic ranges.
 * Zero is explicitly allowed — it is a valid coordinate.
 * Valid ranges: lat -90..+90, lon -180..+180.
 */
fun isValidCoordinatePair(lat: Double?, lon: Double?): Boolean {
    if (lat == null || lon == null) return false
    if (!lat.isFinite() || !lon.isFinite()) return false
    return lat in -90.0..90.0 && lon in -180.0..180.0
}

fun DocumentSnapshot.toOpportunitySafe(): Opportunity? {
    val obj = try {
        this.toObject(Opportunity::class.java)
    } catch (e: Exception) {
        Log.w(MAPPER_TAG, "toObject failed for doc ${this.id}: ${e.message}")
        null
    }

    if (obj == null) {
        Log.w(MAPPER_TAG, "toObject returned null for doc ${this.id}")
        return null
    }

    // Explicitly read raw latitude/longitude in case toObject() silently lost them
    // (e.g. type stored as String in Firestore, or Firestore SDK type-mismatch).
    val latRaw = this.get("latitude")
    val lonRaw = this.get("longitude")

    val safeLat: Double? = when {
        latRaw is Number -> latRaw.toDouble()
        latRaw is String -> {
            val parsed = latRaw.toDoubleOrNull()
            if (parsed == null) Log.w(MAPPER_TAG, "Doc ${this.id}: latitude '$latRaw' could not be parsed as Double")
            parsed
        }
        latRaw == null -> null
        else -> {
            Log.w(MAPPER_TAG, "Doc ${this.id}: latitude has unexpected type ${latRaw::class.java.simpleName} (value=$latRaw)")
            null
        }
    }

    val safeLon: Double? = when {
        lonRaw is Number -> lonRaw.toDouble()
        lonRaw is String -> {
            val parsed = lonRaw.toDoubleOrNull()
            if (parsed == null) Log.w(MAPPER_TAG, "Doc ${this.id}: longitude '$lonRaw' could not be parsed as Double")
            parsed
        }
        lonRaw == null -> null
        else -> {
            Log.w(MAPPER_TAG, "Doc ${this.id}: longitude has unexpected type ${lonRaw::class.java.simpleName} (value=$lonRaw)")
            null
        }
    }

    // Validate range — reject non-finite or out-of-range values
    val validatedLat = safeLat?.takeIf { it.isFinite() && it in -90.0..90.0 }
    val validatedLon = safeLon?.takeIf { it.isFinite() && it in -180.0..180.0 }

    if (safeLat != null && validatedLat == null)
        Log.w(MAPPER_TAG, "Doc ${this.id}: latitude $safeLat is out of range [-90, 90] — discarding")
    if (safeLon != null && validatedLon == null)
        Log.w(MAPPER_TAG, "Doc ${this.id}: longitude $safeLon is out of range [-180, 180] — discarding")

    Log.d(MAPPER_TAG, "Mapped doc ${this.id} '${obj.title}': " +
        "latRaw=$latRaw (${latRaw?.let { it::class.java.simpleName } ?: "null"}), " +
        "lonRaw=$lonRaw (${lonRaw?.let { it::class.java.simpleName } ?: "null"}), " +
        "validLat=$validatedLat, validLon=$validatedLon, " +
        "remote=${obj.remote}, workArrangement=${obj.workArrangement}")

    return obj.copy(latitude = validatedLat, longitude = validatedLon)
}

fun DocumentSnapshot.toProfileSafe(): Profile? {
    val obj = try {
        this.toObject(Profile::class.java)
    } catch (e: Exception) {
        null
    }

    if (obj == null) return null

    val latRaw = this.get("latitude")
    val lonRaw = this.get("longitude")
    val safeLat = (latRaw as? Number)?.toDouble() ?: (latRaw as? String)?.toDoubleOrNull()
    val safeLon = (lonRaw as? Number)?.toDouble() ?: (lonRaw as? String)?.toDoubleOrNull()

    val validatedLat = safeLat?.takeIf { it.isFinite() && it in -90.0..90.0 }
    val validatedLon = safeLon?.takeIf { it.isFinite() && it in -180.0..180.0 }

    return obj.copy(latitude = validatedLat, longitude = validatedLon)
}
