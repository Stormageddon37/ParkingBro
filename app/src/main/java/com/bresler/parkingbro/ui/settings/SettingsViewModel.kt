package com.bresler.parkingbro.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bresler.parkingbro.domain.AutoSaveController
import com.bresler.parkingbro.domain.AutoSaveSettings
import com.bresler.parkingbro.domain.BluetoothDeviceSource
import com.bresler.parkingbro.domain.BtDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DeviceRow(val address: String, val name: String, val connected: Boolean, val isCar: Boolean)

data class SettingsUiState(val enabled: Boolean = false, val devices: List<DeviceRow> = emptyList())

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: AutoSaveSettings,
    private val deviceSource: BluetoothDeviceSource,
    private val controller: AutoSaveController,
) : ViewModel() {

    private val bonded = MutableStateFlow<List<BtDevice>>(emptyList())

    val uiState: StateFlow<SettingsUiState> =
        combine(settings.enabled, settings.carAddresses, bonded) { enabled, cars, devices ->
            val rows = devices.map { DeviceRow(it.address, it.name, it.connected, it.address in cars) }
            val listed = devices.map { it.address }.toSet()
            // Tagged cars not currently bonded (BT off, permission missing, unpaired) stay visible/untaggable.
            SettingsUiState(enabled, rows + (cars - listed).map { DeviceRow(it, it, false, true) })
        }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    init { reloadDevices() }

    fun reloadDevices() {
        viewModelScope.launch { bonded.value = deviceSource.bondedDevices() }
    }

    fun onToggleEnabled(value: Boolean) {
        viewModelScope.launch {
            settings.setEnabled(value)
            apply(value, settings.carAddresses.first())
        }
    }

    fun onToggleCar(address: String) {
        viewModelScope.launch {
            val cars = settings.carAddresses.first()
            val updated = if (address in cars) cars - address else cars + address
            settings.setCarAddresses(updated)
            if (settings.enabled.first()) apply(true, updated)
        }
    }

    private fun apply(enabled: Boolean, cars: Set<String>) =
        if (enabled && cars.isNotEmpty()) controller.start() else controller.stop()
}
