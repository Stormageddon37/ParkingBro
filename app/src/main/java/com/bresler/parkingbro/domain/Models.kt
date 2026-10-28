package com.bresler.parkingbro.domain

/** A geographic coordinate. Stringified as "lat, lng" to match the stored format. */
data class LatLng(val latitude: Double, val longitude: Double) {
    override fun toString(): String = "$latitude, $longitude"
}

/** The last parking spot the user saved. Fields are null when nothing has been saved yet. */
data class SavedParking(val location: String?, val timestamp: String?)

/** Prefer a fresh fix; only consult (lazily) the last-known location when there is none. */
fun resolveLocation(fresh: LatLng?, lastKnown: () -> LatLng?): LatLng? = fresh ?: lastKnown()
