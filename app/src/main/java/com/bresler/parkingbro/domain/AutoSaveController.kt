package com.bresler.parkingbro.domain

/** Starts/stops the background auto-save listener. */
interface AutoSaveController {
    fun start()
    fun stop()
}
