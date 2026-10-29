package com.bresler.parkingbro.domain

import kotlinx.coroutines.flow.Flow

/** Auto-save-parking settings: feature toggle and the tagged "car" Bluetooth addresses. */
interface AutoSaveSettings {
    val enabled: Flow<Boolean>
    suspend fun setEnabled(value: Boolean)

    val carAddresses: Flow<Set<String>>
    suspend fun setCarAddresses(value: Set<String>)
}
