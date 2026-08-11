package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.CombatLogic
import org.junit.Assert.assertEquals
import org.junit.Test

class CombatLogicTest {

    @Test
    fun requiredXpForCombatLevel_level1_returns100() {
        assertEquals(100, CombatLogic.requiredXpForCombatLevel(1))
    }

    @Test
    fun requiredXpForCombatLevel_level45_returns4500() {
        assertEquals(4500, CombatLogic.requiredXpForCombatLevel(45))
    }
}
