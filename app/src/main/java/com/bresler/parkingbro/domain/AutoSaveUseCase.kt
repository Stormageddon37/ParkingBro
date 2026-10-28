package com.bresler.parkingbro.domain

import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** The one place the app formats a parking timestamp. */
fun formatParkingTimestamp(epochMs: Long): String =
    SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss", Locale.getDefault()).format(Date(epochMs))

sealed interface AutoSaveResult {
    data object Saved : AutoSaveResult
    data object NoLocation : AutoSaveResult
}

/** Captures a fresh location and persists it as the parking spot, honoring flush-after-save. */
class AutoSaveUseCase @Inject constructor(
    private val location: LocationProvider,
    private val storage: ParkingStorage,
    private val clock: Clock,
) {
    suspend fun run(): AutoSaveResult {
        val fix = location.currentLocation() ?: return AutoSaveResult.NoLocation
        storage.save(fix.toString(), formatParkingTimestamp(clock.now()))
        if (storage.flushAfterSave.first()) storage.setImages(emptyList())
        return AutoSaveResult.Saved
    }
}
