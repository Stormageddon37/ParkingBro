package com.bresler.parkingbro.domain

import javax.inject.Inject

/** Decides whether a Bluetooth disconnect should trigger an auto-save. Time is injected via nowMs. */
class AutoSaveDecider(private val debounceMs: Long) {
    @Inject constructor() : this(10_000L)

    private var lastAcceptedMs: Long? = null

    fun shouldSave(disconnectedAddress: String, carAddresses: Set<String>, nowMs: Long): Boolean {
        if (disconnectedAddress !in carAddresses) return false
        val last = lastAcceptedMs
        if (last != null && nowMs - last < debounceMs) return false
        lastAcceptedMs = nowMs
        return true
    }
}
