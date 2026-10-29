package com.bresler.parkingbro

import android.net.Uri
import app.cash.turbine.test
import com.bresler.parkingbro.domain.Clock
import com.bresler.parkingbro.domain.ImageStore
import com.bresler.parkingbro.domain.LatLng
import com.bresler.parkingbro.domain.LocationProvider
import com.bresler.parkingbro.domain.ParkingStorage
import com.bresler.parkingbro.domain.SavedParking
import com.bresler.parkingbro.ui.main.MainViewModel
import com.bresler.parkingbro.ui.main.ToastType
import com.bresler.parkingbro.ui.main.UiEvent
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeStorage : ParkingStorage {
    val savedFlow = MutableStateFlow(SavedParking(null, null))
    val dialogFlow = MutableStateFlow(true)
    val flushFlow = MutableStateFlow(true)
    override val saved: Flow<SavedParking> = savedFlow
    override suspend fun save(location: String, timestamp: String) {
        savedFlow.value = SavedParking(location, timestamp)
    }
    override val showDeleteDialog: Flow<Boolean> = dialogFlow
    override suspend fun setShowDeleteDialog(show: Boolean) { dialogFlow.value = show }
    override val flushAfterSave: Flow<Boolean> = flushFlow
    override suspend fun setFlushAfterSave(flush: Boolean) { flushFlow.value = flush }
    val imagesFlow = MutableStateFlow<List<String>>(emptyList())
    override val images: Flow<List<String>> = imagesFlow
    override suspend fun setImages(uris: List<String>) { imagesFlow.value = uris }
}

private class FakeLocation(var value: LatLng?) : LocationProvider {
    override fun lastKnownLocation(): LatLng? = value
    override suspend fun currentLocation(): LatLng? = value
}

private class FakeImageStore(private val uri: Uri) : ImageStore {
    override fun createCaptureUri(): Uri = uri
    override fun liveUris(stored: List<String>): List<Uri> = emptyList()
}

private class FixedClock : Clock {
    override fun now(): Long = 0L
}

class MainViewModelTest {

    private lateinit var storage: FakeStorage
    private lateinit var location: FakeLocation

    private fun viewModel(uri: Uri = mockk(relaxed = true)): MainViewModel {
        storage = FakeStorage()
        location = FakeLocation(LatLng(1.0, 2.0))
        return MainViewModel(storage, location, FakeImageStore(uri), FixedClock())
    }

    @Before fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun teardown() = Dispatchers.resetMain()

    @Test fun capturedImageLandsInFirstSlot() = runTest {
        val uri = mockk<Uri>(relaxed = true)
        val vm = viewModel(uri)
        vm.onCameraRequested()
        vm.onImageCaptured(success = true)
        assertEquals(uri, vm.uiState.value.images[0])
        assertEquals(1, vm.uiState.value.imageCount)
    }

    @Test fun capturedImageIsPersisted() = runTest {
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://img/1"
        val vm = viewModel(uri)
        vm.onCameraRequested()
        vm.onImageCaptured(success = true)
        assertEquals(listOf("content://img/1"), storage.imagesFlow.value)
    }

    @Test fun cancelledCaptureAddsNothing() = runTest {
        val vm = viewModel()
        vm.onCameraRequested()
        vm.onImageCaptured(success = false)
        assertEquals(0, vm.uiState.value.imageCount)
    }

    @Test fun removingCompactsNullsToEnd() = runTest {
        val vm = viewModel()
        repeat(2) { vm.onCameraRequested(); vm.onImageCaptured(true) }
        val second = vm.uiState.value.images[1]
        vm.onRemoveImage(0)
        assertEquals(second, vm.uiState.value.images[0])
        assertNull(vm.uiState.value.images[1])
        assertEquals(1, vm.uiState.value.imageCount)
    }

    @Test fun maxImagesEmitsError() = runTest {
        val vm = viewModel()
        repeat(3) { vm.onCameraRequested(); vm.onImageCaptured(true) }
        vm.onCameraRequested() // over the limit
        vm.events.test {
            val event = expectMostRecentItem()
            assertTrue(event is UiEvent.ShowToast && event.type == ToastType.ERROR)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(3, vm.uiState.value.imageCount)
    }

    @Test fun saveWritesLocationAndTimestamp() = runTest {
        val vm = viewModel()
        vm.onSaveRequested().join()
        assertEquals("1.0, 2.0", storage.savedFlow.value.location)
        assertTrue(storage.savedFlow.value.timestamp!!.matches(Regex("""\d{2}/\d{2}/\d{4} @ \d{2}:\d{2}:\d{2}""")))
    }

    @Test fun saveWithNoFixEmitsErrorAndStoresNothing() = runTest {
        val vm = viewModel()
        location.value = null
        vm.onSaveRequested().join()
        vm.events.test {
            val event = expectMostRecentItem()
            assertTrue(event is UiEvent.ShowToast && event.type == ToastType.ERROR)
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(storage.savedFlow.value.location)
    }

    @Test fun navigateDefaultsToZeroCoords() = runTest {
        val vm = viewModel()
        vm.onNavigateRequested().join()
        vm.events.test {
            val maps = expectMostRecentItem()
            assertTrue(maps is UiEvent.OpenMaps && maps.query == "0.0,0.0")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
