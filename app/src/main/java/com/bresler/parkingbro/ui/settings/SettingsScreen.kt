package com.bresler.parkingbro.ui.settings

import androidx.compose.foundation.background
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
            LazyColumn(Modifier.fillMaxWidth()) {
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
