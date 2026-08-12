package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.logic.toSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterMappersTest {

    @Test
    fun toSummary_mapsAllRelevantFields() {
        val state = createInitialCharacterState().copy(
            charName = "Aethelgard",
            streak = 5,
            bestStreak = 10,
            combatLevel = 3,
            combatXP = 150,
            hp = 40,
            maxHp = 50,
            totalMinutes = 300,
            equippedTitle = "CHAMPION"
        )

        val summary = state.toSummary()

        assertEquals("Aethelgard", summary.charName)
        assertEquals(5, summary.streak)
        assertEquals(10, summary.bestStreak)
        assertEquals(3, summary.combatLevel)
        assertEquals(150, summary.combatXP)
        assertEquals(40, summary.hp)
        assertEquals(50, summary.maxHp)
        assertEquals(300, summary.totalMinutes)
        assertEquals("CHAMPION", summary.equippedTitle)
    }
}
