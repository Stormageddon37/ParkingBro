package com.bresler.parkingbro.domain

/** Starts/stops the background auto-save listener. */
interface AutoSaveController {
    fun start()
    fun stop()

    /**
     * Stop then start the service so it gets a fresh, foreground-attributed start.
     * After a reboot the service is started from the background and Android 14+ denies it
     * location access; re-arming it while the app is in the foreground restores that access.
     */
    fun reArm()
}
