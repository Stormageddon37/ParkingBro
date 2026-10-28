package com.bresler.parkingbro.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.bresler.parkingbro.domain.BluetoothDeviceSource
import com.bresler.parkingbro.domain.BtDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Lists bonded devices and marks those currently connected over A2DP or HEADSET.
 * Missing BLUETOOTH_CONNECT permission degrades to `connected = false` (or empty).
 */
@SuppressLint("MissingPermission")
class AndroidBluetoothDeviceSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : BluetoothDeviceSource {

    override suspend fun bondedDevices(): List<BtDevice> {
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
            ?.adapter
        val bonded = try {
            if (adapter == null || !adapter.isEnabled) return emptyList()
            adapter.bondedDevices.orEmpty().map { it.address to (it.name ?: it.address) }
        } catch (_: SecurityException) {
            return emptyList()
        }
        val connected = connectedAddresses(adapter)
        return bonded.map { (address, name) -> BtDevice(address, name, address in connected) }
    }

    private suspend fun connectedAddresses(adapter: BluetoothAdapter): Set<String> =
        listOf(BluetoothProfile.A2DP, BluetoothProfile.HEADSET)
            .flatMap { connectedVia(adapter, it) }
            .toSet()

    private suspend fun connectedVia(adapter: BluetoothAdapter, profile: Int): List<String> {
        val result = CompletableDeferred<BluetoothProfile>()
        val listener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(p: Int, proxy: BluetoothProfile) {
                result.complete(proxy)
            }

            override fun onServiceDisconnected(p: Int) = Unit
        }
        return try {
            if (!adapter.getProfileProxy(context, listener, profile)) return emptyList()
            val proxy = withTimeoutOrNull(PROXY_TIMEOUT_MS) { result.await() } ?: return emptyList()
            try {
                proxy.connectedDevices.map { it.address }
            } finally {
                adapter.closeProfileProxy(profile, proxy)
            }
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    private companion object {
        const val PROXY_TIMEOUT_MS = 1500L
    }
}
