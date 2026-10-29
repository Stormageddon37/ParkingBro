package com.bresler.parkingbro.domain

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Supplies the device's current best-known location. Decouples the ViewModel from
 * Android's LocationManager so the save flow can be unit-tested with a fake.
 */
interface LocationProvider {
    /** Best last-known location, or null if unavailable or location permission is missing. */
    fun lastKnownLocation(): LatLng?

    /** A fresh fix if one arrives in time, else the last-known location, else null. */
    suspend fun currentLocation(): LatLng?
}

/**
 * Persists the saved parking spot and the two user preferences. Decouples the
 * ViewModel from the concrete storage (DataStore) behind it.
 */
interface ParkingStorage {
    val saved: Flow<SavedParking>
    suspend fun save(location: String, timestamp: String)

    /** Whether to show the "delete pictures?" dialog on save (default true). */
    val showDeleteDialog: Flow<Boolean>
    suspend fun setShowDeleteDialog(show: Boolean)

    /** Whether saving should also clear the captured images (default true). */
    val flushAfterSave: Flow<Boolean>
    suspend fun setFlushAfterSave(flush: Boolean)

    /** Persisted captured image URIs (non-null, in order) so they survive process death. */
    val images: Flow<List<String>>
    suspend fun setImages(uris: List<String>)
}

/**
 * Allocates a destination URI for a full-resolution camera capture. Decouples the
 * ViewModel from MediaStore/ContentResolver.
 */
interface ImageStore {
    /** Create a MediaStore image URI to capture into, or null if it could not be created. */
    fun createCaptureUri(): Uri?

    /** Parse persisted URI strings into live URIs, dropping any no longer readable. */
    fun liveUris(stored: List<String>): List<Uri>
}

/** Wall-clock time source, injected so timestamp formatting is deterministic in tests. */
interface Clock {
    fun now(): Long
}
