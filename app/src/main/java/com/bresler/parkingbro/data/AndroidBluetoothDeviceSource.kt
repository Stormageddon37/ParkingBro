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
import kotlinx.coroutines.CancellationException
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
        val lock = Any()
        var proxy: BluetoothProfile? = null
        var abandoned = false
        val listener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(p: Int, connectedProxy: BluetoothProfile) {
                synchronized(lock) {
                    if (abandoned) {
                        // Connected after we gave up (timeout/cancel): release the binding.
                        runCatching { adapter.closeProfileProxy(profile, connectedProxy) }
                    } else {
                        proxy = connectedProxy
                        result.complete(connectedProxy)
                    }
                }
            }

            override fun onServiceDisconnected(p: Int) = Unit
        }
        return try {
            if (!adapter.getProfileProxy(context, listener, profile)) return emptyList()
            val connected = withTimeoutOrNull(PROXY_TIMEOUT_MS) { result.await() }
                ?: return emptyList()
            connected.connectedDevices.map { it.address }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        } finally {
            synchronized(lock) {
                abandoned = true
                proxy?.let { runCatching { adapter.closeProfileProxy(profile, it) } }
            }
        }
    }

    private companion object {
        const val PROXY_TIMEOUT_MS = 1500L
    }
}
