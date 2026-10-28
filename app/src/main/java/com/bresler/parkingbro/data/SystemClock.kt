package com.bresler.parkingbro.data

import com.bresler.parkingbro.domain.Clock
import javax.inject.Inject

class SystemClock @Inject constructor() : Clock {
    override fun now(): Long = System.currentTimeMillis()
}
