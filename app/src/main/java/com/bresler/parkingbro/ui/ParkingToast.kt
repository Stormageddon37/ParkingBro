package com.bresler.parkingbro.ui

import android.content.Context
import android.widget.Toast
import com.bresler.parkingbro.ui.main.ToastType
import es.dmoral.toasty.Toasty

/** Thin bridge to Toasty, kept so the toasts look exactly as before. */
object ParkingToast {
    fun show(context: Context, type: ToastType, message: String) {
        val toast = when (type) {
            ToastType.SUCCESS -> Toasty.success(context, message, Toast.LENGTH_SHORT, true)
            ToastType.ERROR -> Toasty.error(context, message, Toast.LENGTH_SHORT, true)
            ToastType.INFO -> Toasty.info(context, message, Toast.LENGTH_SHORT, true)
        }
        toast.show()
    }
}
