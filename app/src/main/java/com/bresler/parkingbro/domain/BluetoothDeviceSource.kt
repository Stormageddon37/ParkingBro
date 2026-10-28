package com.bresler.parkingbro.domain

/** A paired Bluetooth device; [connected] is its live connection state. */
data class BtDevice(val address: String, val name: String, val connected: Boolean)

/** Lists the user's paired Bluetooth devices. Never throws; empty when unavailable. */
interface BluetoothDeviceSource {
    suspend fun bondedDevices(): List<BtDevice>
}
