package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.model.CharClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class HeroLogInitialStateTest {

    @Test
    fun createInitialCharacterState_matchesReactInitialStateValues() {
        val state = createInitialCharacterState(Date(1700000000000L))

        assertEquals(200, state.gold)
        assertEquals(200, state.totalGoldEarned)
        assertEquals(1, state.combatLevel)
        assertEquals("Aventureiro do Foco", state.charName)
        assertEquals(CharClass.Mage, state.charClass)
        assertEquals(50, state.hp)
        assertEquals(50, state.maxHp)
        assertEquals(listOf(null, null, null), state.equippedEquipment)
        assertEquals(25, state.pomodoroSettings.focusDuration)
    }

    @Test
    fun createInitialCharacterState_omitsPersonalExampleData() {
        val state = createInitialCharacterState()

        assertTrue(state.skills.isEmpty())
        assertTrue(state.habits.isEmpty())
        assertTrue(state.dailies.isEmpty())
        assertTrue(state.todos.isEmpty())
    }
}
