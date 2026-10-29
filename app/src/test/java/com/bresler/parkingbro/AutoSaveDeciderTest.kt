package com.bresler.parkingbro

import com.bresler.parkingbro.domain.AutoSaveDecider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoSaveDeciderTest {
    private val cars = setOf("AA:BB")

    @Test
    fun nonCarAddress_returnsFalse() {
        assertFalse(AutoSaveDecider().shouldSave("CC:DD", cars, 0L))
    }

    @Test
    fun carAddress_returnsTrue() {
        assertTrue(AutoSaveDecider().shouldSave("AA:BB", cars, 0L))
    }

    @Test
    fun secondCarEventWithin10s_returnsFalse() {
        val d = AutoSaveDecider()
        assertTrue(d.shouldSave("AA:BB", cars, 1_000L))
        assertFalse(d.shouldSave("AA:BB", cars, 10_999L))
    }

    @Test
    fun carEventAfter10s_returnsTrue() {
        val d = AutoSaveDecider()
        assertTrue(d.shouldSave("AA:BB", cars, 1_000L))
        assertTrue(d.shouldSave("AA:BB", cars, 11_000L))
    }

    @Test
    fun nonCarEventDoesNotStartWindow() {
        val d = AutoSaveDecider()
        assertFalse(d.shouldSave("CC:DD", cars, 1_000L))
        assertTrue(d.shouldSave("AA:BB", cars, 2_000L))
    }
}
