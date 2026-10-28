package com.bresler.parkingbro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import es.dmoral.toasty.Toasty

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Matches the original: don't queue toasts, replace the current one.
        Toasty.Config.getInstance().allowQueue(false).apply()
    }
}
