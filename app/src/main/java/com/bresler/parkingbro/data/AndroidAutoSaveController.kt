package com.bresler.parkingbro.data

import android.content.Context
import com.bresler.parkingbro.domain.AutoSaveController
import com.bresler.parkingbro.service.AutoSaveService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidAutoSaveController @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AutoSaveController {
    override fun start() = AutoSaveService.start(context)
    override fun stop() = AutoSaveService.stop(context)
    override fun reArm() {
        AutoSaveService.stop(context)
        AutoSaveService.start(context)
    }
}
