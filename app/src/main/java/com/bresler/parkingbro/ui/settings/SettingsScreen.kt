package com.bresler.parkingbro.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bresler.parkingbro.ui.theme.AppBlack
import com.bresler.parkingbro.ui.theme.AppWhite
import com.bresler.parkingbro.ui.theme.DarkBackground
import com.bresler.parkingbro.ui.theme.Green

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxWidth().background(AppBlack).statusBarsPadding()) {}
            TopAppBar(
                title = { Text("Auto-save settings", color = AppWhite, letterSpacing = 0.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Green, titleContentColor = AppWhite),
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
            LazyColumn(Modifier.fillMaxSize()) {
                item {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Save parking when my car disconnects",
                    color = AppWhite, fontSize = 18.sp, letterSpacing = 0.sp,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = state.enabled,
                    onCheckedChange = viewModel::onToggleEnabled,
                    colors = SwitchDefaults.colors(checkedTrackColor = Green),
                )
            }
                    PermissionRows(onBluetoothGranted = viewModel::reloadDevices)
            Text(
                "Select your car's Bluetooth device(s)",
                color = AppWhite, fontSize = 14.sp, letterSpacing = 0.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (state.devices.isEmpty()) {
                Text(
                    "No paired devices. Turn on Bluetooth and pair your car.",
                    color = AppWhite, letterSpacing = 0.sp,
                    modifier = Modifier.padding(16.dp),
                )
            }
                }
                items(state.devices, key = { it.address }) { d ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(d.name, color = AppWhite, fontSize = 16.sp, letterSpacing = 0.sp)
                            if (d.connected) {
                                Text("Connected", color = Green, fontSize = 12.sp, letterSpacing = 0.sp)
                            }
                        }
                        Checkbox(
                            checked = d.isCar,
                            onCheckedChange = { viewModel.onToggleCar(d.address) },
                            colors = CheckboxDefaults.colors(checkedColor = Green, uncheckedColor = AppWhite),
                        )
                    }
                }
            }
        }
    }
}

private fun Context.granted(p: String) =
    ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED

@Composable
private fun StatusRow(label: String, ok: Boolean, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label + if (ok) ": OK" else ": needed",
            color = if (ok) Green else AppWhite, fontSize = 14.sp, letterSpacing = 0.sp,
            modifier = Modifier.weight(1f),
        )
        if (!ok) TextButton(onClick = onClick) { Text(action, color = Green, letterSpacing = 0.sp) }
    }
}

@Composable
private fun PermissionRows(onBluetoothGranted: () -> Unit) {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) } // bump to recompose status after any grant/return from settings
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++; onBluetoothGranted() }
    val refresh = { tick++ }
    val btLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        refresh(); if (it) onBluetoothGranted()
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh() }
    val locLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh() }
    val bgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh() }
    @Suppress("UNUSED_EXPRESSION") tick

    Column {
        if (Build.VERSION.SDK_INT >= 31) {
            StatusRow("Bluetooth", ctx.granted(Manifest.permission.BLUETOOTH_CONNECT), "Allow") {
                btLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (Build.VERSION.SDK_INT >= 33) {
            StatusRow("Notifications", ctx.granted(Manifest.permission.POST_NOTIFICATIONS), "Allow") {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        val fine = ctx.granted(Manifest.permission.ACCESS_FINE_LOCATION)
        StatusRow("Location", fine, "Allow") { locLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
        if (fine && Build.VERSION.SDK_INT >= 29) {
            // API 30+: system routes the user to Settings; choose "Allow all the time".
            StatusRow("Location all the time", ctx.granted(Manifest.permission.ACCESS_BACKGROUND_LOCATION), "Allow all the time") {
                bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
        val ignoring = ctx.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(ctx.packageName)
        StatusRow("Battery unrestricted", ignoring, "Allow") {
            runCatching {
                ctx.startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + ctx.packageName)),
                )
            }
        }
        StatusRow("Auto-launch (OxygenOS)", false, "Open app settings") {
            // Best effort: no OEM-private intents.
            runCatching {
                ctx.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + ctx.packageName)),
                )
            }
        }
        Text(
            "Enable \"Auto-launch\" / \"Allow background activity\" for this app.",
            color = AppWhite, fontSize = 12.sp, letterSpacing = 0.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}
