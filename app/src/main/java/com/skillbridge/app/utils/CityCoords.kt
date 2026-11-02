package com.skillbridge.app.utils

/**
 * Offline city -> (lat, lon) lookup so opportunities that only carry a free-text `location`
 * (e.g. "Bengaluru", "Hybrid - Mumbai") can still be shown on the map and in "nearby" lists.
 */
object CityCoords {

    val CITIES: Map<String, Pair<Double, Double>> = linkedMapOf(
        "Mumbai" to (19.0760 to 72.8777),
        "Delhi" to (28.6139 to 77.2090),
        "Bengaluru" to (12.9716 to 77.5946),
        "Hyderabad" to (17.3850 to 78.4867),
        "Chennai" to (13.0827 to 80.2707),
        "Kolkata" to (22.5726 to 88.3639),
        "Pune" to (18.5204 to 73.8567),
        "Ahmedabad" to (23.0225 to 72.5714),
        "Jaipur" to (26.9124 to 75.7873),
        "Lucknow" to (26.8467 to 80.9462),
        "Chandigarh" to (30.7333 to 76.7794),
        "Indore" to (22.7196 to 75.8577),
        "Kochi" to (9.9312 to 76.2673),
        "Nagpur" to (21.1458 to 79.0882),
        "Gurugram" to (28.4595 to 77.0266),
        "Noida" to (28.5355 to 77.3910),
        "Coimbatore" to (11.0168 to 76.9558),
        "Bhopal" to (23.2599 to 77.4126),
        "Surat" to (21.1702 to 72.8311),
        "Visakhapatnam" to (17.6868 to 83.2185),
    )

    private val ALIASES = mapOf(
        "bangalore" to "Bengaluru",
        "bengaluru" to "Bengaluru",
        "bombay" to "Mumbai",
        "delhi ncr" to "Delhi",
        "new delhi" to "Delhi",
        "ncr" to "Delhi",
        "gurgaon" to "Gurugram",
        "calcutta" to "Kolkata",
        "madras" to "Chennai",
        "cochin" to "Kochi",
        "vizag" to "Visakhapatnam",
    )

    /** Finds a known city inside free text like "Hybrid - Mumbai". Null when none matches. */
    fun lookup(location: String): Pair<Double, Double>? {
        val text = location.lowercase()
        if (text.isBlank()) return null
        ALIASES.entries.firstOrNull { text.contains(it.key) }?.let { return CITIES[it.value] }
        return CITIES.entries.firstOrNull { text.contains(it.key.lowercase()) }?.value
    }

    /**
     * Same as [lookup] but nudges the point by up to ~2 km, deterministically per [seed],
     * so several jobs in one city don't render as a single stacked pin.
     */
    fun lookupSpread(location: String, seed: String): Pair<Double, Double>? {
        val base = lookup(location) ?: return null
        return nudge(base, seed)
    }

    /**
     * For jobs whose location text names no known city (e.g. "Remote", blank): picks one of
     * [CITIES] deterministically from [seed] (the job id) and nudges it. Same job -> same place
     * every time, different jobs spread over different cities.
     */
    fun fallbackSpread(seed: String): Pair<Double, Double> {
        val cities = CITIES.values.toList()
        val base = cities[(seed.hashCode() and 0x7FFFFFFF) % cities.size]
        return nudge(base, seed)
    }

    /** Name of the closest known city to a point, for labels like "Base: Pune". */
    fun nearestCity(lat: Double, lon: Double): String =
        CITIES.minByOrNull { (_, p) ->
            val dLat = p.first - lat
            val dLon = p.second - lon
            dLat * dLat + dLon * dLon
        }?.key ?: ""

    private fun nudge(base: Pair<Double, Double>, seed: String): Pair<Double, Double> {
        val h = seed.hashCode()
        val dLat = ((h and 0xFFFF) / 65535.0 - 0.5) * 0.04
        val dLon = (((h ushr 16) and 0xFFFF) / 65535.0 - 0.5) * 0.04
        return (base.first + dLat) to (base.second + dLon)
    }
}
