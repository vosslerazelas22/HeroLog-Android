package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.focus.CognitiveDeathResult
import com.iurispraecepta.herolog.logic.focus.RespawnResult
import com.iurispraecepta.herolog.logic.focus.WildernessInfractionOutcome
import com.iurispraecepta.herolog.logic.focus.resolveCognitiveDeath
import com.iurispraecepta.herolog.logic.focus.resolveRespawn
import com.iurispraecepta.herolog.logic.focus.resolveWildernessInfraction
import com.iurispraecepta.herolog.model.CharClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CognitiveDeathLogicTest {

    private fun fixedRandom(value: Double) = object : Random() {
        override fun nextDouble(): Double = value
        override fun nextBits(bitCount: Int): Int = 0
    }

    @Test
    fun resolveWildernessInfraction_deathProofTitle_returnsConvertedToPause() {
        val outcome = resolveWildernessInfraction("DEATH-PROOF")
        assertEquals(WildernessInfractionOutcome.CONVERTED_TO_PAUSE, outcome)
    }

    @Test
    fun resolveWildernessInfraction_otherOrNullTitle_returnsGracePeriodStarted() {
        assertEquals(WildernessInfractionOutcome.GRACE_PERIOD_STARTED, resolveWildernessInfraction(null))
        assertEquals(WildernessInfractionOutcome.GRACE_PERIOD_STARTED, resolveWildernessInfraction("SOME_TITLE"))
        assertEquals(WildernessInfractionOutcome.GRACE_PERIOD_STARTED, resolveWildernessInfraction(""))
    }

    @Test
    fun resolveCognitiveDeath_rangerRollUnder15Percent_savesStreak() {
        val result = resolveCognitiveDeath(
            charClass = CharClass.Ranger,
            previousStreak = 10,
            random = fixedRandom(0.10)
        )
        assertTrue(result.streakSaved)
        assertEquals(10, result.newStreak)
        assertEquals(0, result.newCombo)
    }

    @Test
    fun resolveCognitiveDeath_rangerRoll15PercentOrHigher_losesStreak() {
        val result = resolveCognitiveDeath(
            charClass = CharClass.Ranger,
            previousStreak = 10,
            random = fixedRandom(0.15)
        )
        assertFalse(result.streakSaved)
        assertEquals(0, result.newStreak)
        assertEquals(0, result.newCombo)
    }

    @Test
    fun resolveCognitiveDeath_nonRangerClass_alwaysLosesStreakRegardlessOfRoll() {
        CharClass.values().filter { it != CharClass.Ranger }.forEach { nonRangerClass ->
            val result = resolveCognitiveDeath(
                charClass = nonRangerClass,
                previousStreak = 10,
                random = fixedRandom(0.01)
            )
            assertFalse(result.streakSaved)
            assertEquals(0, result.newStreak)
            assertEquals(0, result.newCombo)
        }
    }

    @Test
    fun resolveCognitiveDeath_zeroPreviousStreak_alwaysReturnsZeroStreakAndNotSaved() {
        CharClass.values().forEach { charClass ->
            val result = resolveCognitiveDeath(
                charClass = charClass,
                previousStreak = 0,
                random = fixedRandom(0.01)
            )
            assertFalse(result.streakSaved)
            assertEquals(0, result.newStreak)
            assertEquals(0, result.newCombo)
        }
    }

    @Test
    fun resolveRespawn_decreasesCombatLevelByOne_andFloorIsOne() {
        val normalResult = resolveRespawn(currentCombatLevel = 5, currentGold = 100)
        assertEquals(4, normalResult.newCombatLevel)

        val floorResult = resolveRespawn(currentCombatLevel = 1, currentGold = 100)
        assertEquals(1, floorResult.newCombatLevel)
    }

    @Test
    fun resolveRespawn_decreasesGoldBy50_andFloorIsZero() {
        val normalResult = resolveRespawn(currentCombatLevel = 5, currentGold = 100)
        assertEquals(50, normalResult.newGold)

        val lowGoldResult = resolveRespawn(currentCombatLevel = 5, currentGold = 30)
        assertEquals(0, lowGoldResult.newGold)

        val zeroGoldResult = resolveRespawn(currentCombatLevel = 5, currentGold = 0)
        assertEquals(0, zeroGoldResult.newGold)
    }

    @Test
    fun resolveRespawn_alwaysResetsCombatXpToZero() {
        val result = resolveRespawn(currentCombatLevel = 5, currentGold = 100)
        assertEquals(0, result.newCombatXp)
    }
}
