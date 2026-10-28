package com.bresler.parkingbro.ui.main

import android.net.Uri

/** How many parking photos may be kept at once. */
const val MAX_IMAGES = 3

/** Immutable snapshot the main screen renders from. */
data class MainUiState(
    val savedLocation: String = "Unknown",
    val savedTimestamp: String = "Unknown",
    val images: List<Uri?> = List(MAX_IMAGES) { null },
) {
    val imageCount: Int get() = images.count { it != null }
    val displayText: String get() = "$savedLocation\n$savedTimestamp"
}

enum class ToastType { SUCCESS, ERROR, INFO }

/** One-shot effects the ViewModel asks the UI to perform. */
sealed interface UiEvent {
    data class ShowToast(val type: ToastType, val message: String) : UiEvent
    data class OpenMaps(val query: String) : UiEvent
    data class OpenImage(val uri: Uri) : UiEvent
    data class LaunchCamera(val uri: Uri) : UiEvent
    data object ShowDeleteDialog : UiEvent
}
