package com.bresler.parkingbro

import com.bresler.parkingbro.domain.LatLng
import com.bresler.parkingbro.domain.resolveLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocationFallbackTest {
    private val fresh = LatLng(1.0, 2.0)
    private val last = LatLng(3.0, 4.0)

    @Test
    fun freshWins() {
        assertEquals(fresh, resolveLocation(fresh) { error("must not be called") })
    }

    @Test
    fun fallsBackToLastKnown() {
        assertEquals(last, resolveLocation(null) { last })
    }

    @Test
    fun bothNullIsNull() {
        assertNull(resolveLocation(null) { null })
    }
}
