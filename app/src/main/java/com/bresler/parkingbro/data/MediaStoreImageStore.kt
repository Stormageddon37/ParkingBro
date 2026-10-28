package com.bresler.parkingbro.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.bresler.parkingbro.domain.ImageStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Inserts a new image row into MediaStore and hands back its URI to capture into. */
class MediaStoreImageStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ImageStore {

    override fun createCaptureUri(): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "")
            put(MediaStore.Images.Media.DESCRIPTION, "")
        }
        return context.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    override fun liveUris(stored: List<String>): List<Uri> =
        stored.map(Uri::parse).filter(::isReadable)

    /** True if the photo behind the URI still exists and can be opened. */
    private fun isReadable(uri: Uri): Boolean = try {
        context.contentResolver.openInputStream(uri)?.use { true } ?: false
    } catch (_: Exception) {
        false
    }
}
