package com.bresler.parkingbro.ui.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bresler.parkingbro.domain.Clock
import com.bresler.parkingbro.domain.ImageStore
import com.bresler.parkingbro.domain.LocationProvider
import com.bresler.parkingbro.domain.ParkingStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Owns the main screen's state and the save/navigate/camera logic. Depends only on
 * the domain interfaces, so every path here is unit-testable with fakes.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val storage: ParkingStorage,
    private val location: LocationProvider,
    private val imageStore: ImageStore,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var pendingCaptureUri: Uri? = null

    init {
        viewModelScope.launch {
            storage.saved.collect { saved ->
                _uiState.update {
                    it.copy(
                        savedLocation = saved.location ?: "Unknown",
                        savedTimestamp = saved.timestamp ?: "Unknown",
                    )
                }
            }
        }
        // Restore persisted images, dropping any whose photo was deleted meanwhile.
        viewModelScope.launch {
            val stored = storage.images.first()
            val live = withContext(Dispatchers.IO) { imageStore.liveUris(stored) }
            // Don't clobber anything captured while this load was in flight.
            _uiState.update { state ->
                if (state.images.any { it != null }) state else state.copy(images = toSlots(live))
            }
            if (live.size != stored.size) storage.setImages(live.map(Uri::toString))
        }
    }

    // --- Save ---

    /** Call once location permission is granted. Shows the delete dialog first when warranted. */
    fun onSaveRequested() = viewModelScope.launch {
        if (_uiState.value.imageCount > 0 && storage.showDeleteDialog.first()) {
            _events.send(UiEvent.ShowDeleteDialog)
        } else {
            performSave(storage.flushAfterSave.first())
        }
    }

    /** Result of the "delete pictures?" dialog. [flush] clears images; [remember] hides the dialog next time. */
    fun onDeleteDialogResult(flush: Boolean, remember: Boolean) = viewModelScope.launch {
        storage.setShowDeleteDialog(!remember)
        storage.setFlushAfterSave(flush)
        performSave(flush)
    }

    private suspend fun performSave(flush: Boolean) {
        val fix = location.currentLocation()
        if (fix == null) {
            _events.send(UiEvent.ShowToast(ToastType.ERROR, "Could not get location"))
        } else {
            storage.save(fix.toString(), formatTimestamp())
            _events.send(UiEvent.ShowToast(ToastType.SUCCESS, "Location saved!"))
        }
        if (flush) clearImages()
    }

    private fun formatTimestamp(): String =
        SimpleDateFormat("dd/MM/yyyy @ HH:mm:ss", Locale.getDefault()).format(Date(clock.now()))

    // --- Navigate ---

    fun onNavigateRequested() = viewModelScope.launch {
        val query = storage.saved.first().location ?: "0.0,0.0"
        _events.send(UiEvent.ShowToast(ToastType.INFO, "Launching navigation app!"))
        _events.send(UiEvent.OpenMaps(query))
    }

    // --- Camera / images ---

    /** Call once camera permission is granted. */
    fun onCameraRequested() {
        if (_uiState.value.imageCount >= MAX_IMAGES) {
            emit(UiEvent.ShowToast(ToastType.ERROR, "Maximum images reached!"))
            return
        }
        val uri = imageStore.createCaptureUri()
        if (uri == null) {
            emit(UiEvent.ShowToast(ToastType.ERROR, "Could not open camera"))
            return
        }
        pendingCaptureUri = uri
        emit(UiEvent.LaunchCamera(uri))
    }

    fun onImageCaptured(success: Boolean) {
        val uri = pendingCaptureUri
        pendingCaptureUri = null
        if (!success || uri == null) return
        val slot = _uiState.value.imageCount
        if (slot >= MAX_IMAGES) return
        _uiState.update { state ->
            state.copy(images = state.images.toMutableList().also { it[slot] = uri })
        }
        persistImages()
        emit(UiEvent.ShowToast(ToastType.SUCCESS, "Saved image!"))
    }

    fun onImageClicked(index: Int) {
        val uri = _uiState.value.images.getOrNull(index) ?: return
        emit(UiEvent.OpenImage(uri))
    }

    fun onRemoveImage(index: Int) {
        if (_uiState.value.images.getOrNull(index) == null) {
            emit(UiEvent.ShowToast(ToastType.ERROR, "No image here!"))
            return
        }
        emit(UiEvent.ShowToast(ToastType.SUCCESS, "Removed image!"))
        _uiState.update { state ->
            val list = state.images.toMutableList()
            list[index] = null
            pushNullsToEnd(list)
            state.copy(images = list)
        }
        persistImages()
    }

    private fun clearImages() {
        _uiState.update { it.copy(images = List(MAX_IMAGES) { null }) }
        persistImages()
    }

    private fun toSlots(uris: List<Uri>): List<Uri?> = List(MAX_IMAGES) { uris.getOrNull(it) }

    private fun persistImages() {
        val uris = _uiState.value.images.filterNotNull().map(Uri::toString)
        viewModelScope.launch { storage.setImages(uris) }
    }

    private fun emit(event: UiEvent) {
        _events.trySend(event)
    }

    /** Stable compaction: keep non-null entries in order at the front, nulls at the end. */
    private fun pushNullsToEnd(array: MutableList<Uri?>) {
        var j = 0
        for (i in array.indices) {
            if (array[i] != null) {
                val tmp = array[j]; array[j] = array[i]; array[i] = tmp; j++
            }
        }
    }
}
