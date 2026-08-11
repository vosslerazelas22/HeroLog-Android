package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.focus.formatDungeonCooldown
import com.iurispraecepta.herolog.logic.focus.resolveDungeonCooldownRemainingMs
import org.junit.Assert.assertEquals
import org.junit.Test

class DungeonCooldownLogicTest {

    @Test
    fun resolveDungeonCooldownRemainingMs_elapsedLessThanTwoHours_returnsRemainingTime() {
        val nowMs = 10_000_000_000L
        val oneHourAgoMs = nowMs - (1 * 60 * 60 * 1000L)
        val expectedRemaining = 1 * 60 * 60 * 1000L // 1 hour remaining

        val remaining = resolveDungeonCooldownRemainingMs(lastClearedAtMs = oneHourAgoMs, nowMs = nowMs)
        assertEquals(expectedRemaining, remaining)
    }

    @Test
    fun resolveDungeonCooldownRemainingMs_elapsedExactlyTwoHours_returnsZero() {
        val nowMs = 10_000_000_000L
        val exactlyTwoHoursAgoMs = nowMs - (2 * 60 * 60 * 1000L)

        val remaining = resolveDungeonCooldownRemainingMs(lastClearedAtMs = exactlyTwoHoursAgoMs, nowMs = nowMs)
        assertEquals(0L, remaining)
    }

    @Test
    fun resolveDungeonCooldownRemainingMs_elapsedGreaterThanTwoHours_returnsZero() {
        val nowMs = 10_000_000_000L
        val threeHoursAgoMs = nowMs - (3 * 60 * 60 * 1000L)

        val remaining = resolveDungeonCooldownRemainingMs(lastClearedAtMs = threeHoursAgoMs, nowMs = nowMs)
        assertEquals(0L, remaining)
    }

    @Test
    fun resolveDungeonCooldownRemainingMs_lastClearedAtZero_farInFuture_returnsZero() {
        val nowMs = 1700000000000L

        val remaining = resolveDungeonCooldownRemainingMs(lastClearedAtMs = 0L, nowMs = nowMs)
        assertEquals(0L, remaining)
    }

    @Test
    fun formatDungeonCooldown_zeroMs_returnsZeros() {
        assertEquals("00:00:00", formatDungeonCooldown(0L))
    }

    @Test
    fun formatDungeonCooldown_ninetySeconds_returnsOneMinThirtySecs() {
        assertEquals("00:01:30", formatDungeonCooldown(90_000L))
    }

    @Test
    fun formatDungeonCooldown_exactHourMinSec_returnsFormattedString() {
        assertEquals("01:01:01", formatDungeonCooldown(3_661_000L))
    }

    @Test
    fun formatDungeonCooldown_negativeMs_returnsZerosWithoutException() {
        assertEquals("00:00:00", formatDungeonCooldown(-5000L))
    }
}
