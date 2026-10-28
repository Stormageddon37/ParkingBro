package com.bresler.parkingbro.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.bresler.parkingbro.domain.LatLng
import com.bresler.parkingbro.domain.LocationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Reads the last-known location from the system, trying GPS, then network, then
 * passive providers — the same fallback order the app has always used.
 */
class AndroidLocationProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : LocationProvider {

    override fun lastKnownLocation(): LatLng? {
        if (!hasLocationPermission()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val location = manager.lastKnownOrNull(LocationManager.GPS_PROVIDER)
            ?: manager.lastKnownOrNull(LocationManager.NETWORK_PROVIDER)
            ?: manager.lastKnownOrNull(LocationManager.PASSIVE_PROVIDER)
            ?: return null

        return LatLng(location.latitude, location.longitude)
    }

    private fun LocationManager.lastKnownOrNull(provider: String) =
        try {
            if (isProviderEnabled(provider)) getLastKnownLocation(provider) else null
        } catch (_: SecurityException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }
}
