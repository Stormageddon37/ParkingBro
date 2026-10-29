package com.bresler.parkingbro

import com.bresler.parkingbro.domain.AutoSaveResult
import com.bresler.parkingbro.domain.AutoSaveUseCase
import com.bresler.parkingbro.domain.Clock
import com.bresler.parkingbro.domain.LatLng
import com.bresler.parkingbro.domain.LocationProvider
import com.bresler.parkingbro.domain.ParkingStorage
import com.bresler.parkingbro.domain.SavedParking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class AsStorage(flush: Boolean) : ParkingStorage {
    val savedFlow = MutableStateFlow(SavedParking(null, null))
    val flushFlow = MutableStateFlow(flush)
    val imagesFlow = MutableStateFlow(listOf("a", "b"))
    override val saved: Flow<SavedParking> = savedFlow
    override suspend fun save(location: String, timestamp: String) {
        savedFlow.value = SavedParking(location, timestamp)
    }
    override val showDeleteDialog: Flow<Boolean> = MutableStateFlow(true)
    override suspend fun setShowDeleteDialog(show: Boolean) {}
    override val flushAfterSave: Flow<Boolean> = flushFlow
    override suspend fun setFlushAfterSave(flush: Boolean) {}
    override val images: Flow<List<String>> = imagesFlow
    override suspend fun setImages(uris: List<String>) { imagesFlow.value = uris }
}

private class AsLocation(private val value: LatLng?) : LocationProvider {
    override fun lastKnownLocation(): LatLng? = value
    override suspend fun currentLocation(): LatLng? = value
}

private object AsClock : Clock { override fun now(): Long = 0L }

class AutoSaveUseCaseTest {
    @Test fun savesAndFlushesImagesWhenFlushEnabled() = runTest {
        val s = AsStorage(flush = true)
        val r = AutoSaveUseCase(AsLocation(LatLng(1.0, 2.0)), s, AsClock).run()
        assertEquals(AutoSaveResult.Saved, r)
        val saved = s.savedFlow.value
        assertEquals("1.0, 2.0", saved.location)
        assertTrue(Regex("""\d{2}/\d{2}/\d{4} @ \d{2}:\d{2}:\d{2}""").matches(saved.timestamp!!))
        assertEquals(emptyList<String>(), s.imagesFlow.value)
    }

    @Test fun keepsImagesWhenFlushDisabled() = runTest {
        val s = AsStorage(flush = false)
        assertEquals(AutoSaveResult.Saved, AutoSaveUseCase(AsLocation(LatLng(1.0, 2.0)), s, AsClock).run())
        assertEquals(listOf("a", "b"), s.imagesFlow.value)
    }

    @Test fun noLocationSavesNothing() = runTest {
        val s = AsStorage(flush = true)
        assertEquals(AutoSaveResult.NoLocation, AutoSaveUseCase(AsLocation(null), s, AsClock).run())
        assertNull(s.savedFlow.value.location)
    }
}
