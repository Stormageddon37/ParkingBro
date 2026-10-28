package com.bresler.parkingbro

import com.bresler.parkingbro.domain.AutoSaveController
import com.bresler.parkingbro.domain.AutoSaveSettings
import com.bresler.parkingbro.domain.BluetoothDeviceSource
import com.bresler.parkingbro.domain.BtDevice
import com.bresler.parkingbro.ui.settings.DeviceRow
import com.bresler.parkingbro.ui.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private class FakeSettings : AutoSaveSettings {
    val enabledFlow = MutableStateFlow(false)
    val carsFlow = MutableStateFlow<Set<String>>(emptySet())
    override val enabled: Flow<Boolean> = enabledFlow
    override suspend fun setEnabled(value: Boolean) { enabledFlow.value = value }
    override val carAddresses: Flow<Set<String>> = carsFlow
    override suspend fun setCarAddresses(value: Set<String>) { carsFlow.value = value }
}

private class FakeController : AutoSaveController {
    val calls = mutableListOf<String>()
    override fun start() { calls += "start" }
    override fun stop() { calls += "stop" }
}

private class FakeDevices(val list: List<BtDevice>) : BluetoothDeviceSource {
    override suspend fun bondedDevices() = list
}

class SettingsViewModelTest {
    private val settings = FakeSettings()
    private val controller = FakeController()
    private val devices = listOf(BtDevice("AA", "Car", true), BtDevice("BB", "Buds", false))

    private fun vm() = SettingsViewModel(settings, FakeDevices(devices), controller)

    @Before fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun teardown() = Dispatchers.resetMain()

    @Test fun deviceList_reflectsTaggedSet() = runTest {
        settings.carsFlow.value = setOf("BB")
        assertEquals(
            listOf(DeviceRow("AA", "Car", true, false), DeviceRow("BB", "Buds", false, true)),
            vm().uiState.value.devices,
        )
    }

    @Test fun taggedCarNotBonded_stillListed_andUntaggable() = runTest {
        settings.carsFlow.value = setOf("ZZ")
        val vm = vm()
        assertEquals(DeviceRow("ZZ", "ZZ", false, true), vm.uiState.value.devices.last())
        vm.onToggleCar("ZZ")
        assertEquals(emptySet<String>(), settings.carsFlow.value)
        assertEquals(devices.map { it.address }, vm.uiState.value.devices.map { it.address })
    }

    @Test fun toggleCar_addsThenRemoves() = runTest {
        val vm = vm()
        vm.onToggleCar("AA")
        assertEquals(setOf("AA"), settings.carsFlow.value)
        assertEquals(true, vm.uiState.value.devices.first().isCar)
        vm.onToggleCar("AA")
        assertEquals(emptySet<String>(), settings.carsFlow.value)
    }

    @Test fun enableWithCar_starts() = runTest {
        settings.carsFlow.value = setOf("AA")
        val vm = vm()
        vm.onToggleEnabled(true)
        assertEquals(true, vm.uiState.value.enabled)
        assertEquals(listOf("start"), controller.calls)
    }

    @Test fun enableWithoutCars_doesNotStart() = runTest {
        vm().onToggleEnabled(true)
        assertEquals(listOf("stop"), controller.calls)
    }

    @Test fun disable_stops() = runTest {
        settings.carsFlow.value = setOf("AA")
        vm().onToggleEnabled(false)
        assertEquals(listOf("stop"), controller.calls)
    }

    @Test fun tagFirstCarWhileEnabled_starts_untagLast_stops() = runTest {
        settings.enabledFlow.value = true
        val vm = vm()
        vm.onToggleCar("AA")
        vm.onToggleCar("AA")
        assertEquals(listOf("start", "stop"), controller.calls)
    }

    @Test fun tagCarWhileDisabled_doesNothing() = runTest {
        vm().onToggleCar("AA")
        assertEquals(emptyList<String>(), controller.calls)
    }
}
