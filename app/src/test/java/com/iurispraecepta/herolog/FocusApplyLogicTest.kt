package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.focus.FocusApplyLogic
import com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation
import com.iurispraecepta.herolog.logic.focus.LootItem
import com.iurispraecepta.herolog.logic.focus.UsedEquipmentCharge
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.model.Skill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class FocusApplyLogicTest {

    @Test
    fun apply_xpAndGoldApplied_skillLeveledUp() {
        val state = createBaseState().copy(
            skills = listOf(Skill(id = "sk_1", name = "Estudos", level = 1, xp = 0))
        )
        val calc = createBaseCalc().copy(
            skillIdx = 0,
            skillName = "Estudos",
            xpEarned = 150,
            goldEarned = 100,
            durationMins = 25
        )

        val nextState = FocusApplyLogic.apply(state, calc, editedNotes = "Notas da sessão", selectedTag = "Kotlin", referenceDate = Date(1700000000000L))

        assertEquals(200, nextState.gold) // 100 + 100
        assertEquals(300, nextState.totalGoldEarned) // 200 + 100
        assertEquals(6, nextState.totalSessions) // 5 + 1
        assertEquals(145, nextState.totalMinutes) // 120 + 25

        // Level 1 requires 80 XP -> level becomes 2 with 70 XP remaining
        assertEquals(2, nextState.skills[0].level)
        assertEquals(70, nextState.skills[0].xp)
    }

    @Test
    fun apply_inventoryConsumablesUsed_removed() {
        val consumableItem = InventoryItem("inv_1", "Focus Elixir", "🧪", BuffType.FocusElixir, 100, "XP Boost")
        val state = createBaseState().copy(
            inventory = listOf(consumableItem)
        )
        val calc = createBaseCalc().copy(hasUsedFocusElixir = true)

        val nextState = FocusApplyLogic.apply(state, calc, editedNotes = "", selectedTag = null)

        assertTrue(nextState.inventory.none { it.buff == BuffType.FocusElixir })
    }

    @Test
    fun apply_equipmentChargesDecremented_orExpired() {
        val itemSlot0 = InventoryItem("eq_0", "Coruja Pixelada", "🦉", BuffType.PixelOwl, 250, "Owl", isEquipment = true, charges = 2, maxCharges = 8)
        val itemSlot1 = InventoryItem("eq_1", "Broche de Ouro", "🏅", BuffType.GoldBrooch, 90, "Brooch", isEquipment = true, charges = 1, maxCharges = 5)
        val state = createBaseState().copy(
            equippedEquipment = listOf(itemSlot0, itemSlot1, null)
        )
        val calc = createBaseCalc().copy(
            usedEquipmentIndicesAndCharges = listOf(
                UsedEquipmentCharge(index = 0, remainingCharges = 1),
                UsedEquipmentCharge(index = 1, remainingCharges = 0)
            )
        )

        val nextState = FocusApplyLogic.apply(state, calc, editedNotes = "", selectedTag = null)

        assertEquals(1, nextState.equippedEquipment?.get(0)?.charges)
        assertNull(nextState.equippedEquipment?.get(1)) // expired because charges reached 0
    }

    @Test
    fun apply_lootedItemsAndHistoryAdded() {
        val state = createBaseState()
        val loot = LootItem("Grimório", "📚", "Grimório desc", BuffType.SilverGrimoire, 80, true, 5, 5, Rarity.Comum)
        val calc = createBaseCalc().copy(
            lootedItems = listOf(loot),
            xpEarned = 50,
            goldEarned = 75,
            durationMins = 25
        )

        val nextState = FocusApplyLogic.apply(state, calc, editedNotes = "Minhas anotações", selectedTag = "Kotlin")

        assertEquals(1, nextState.inventory.size)
        assertEquals("Grimório", nextState.inventory[0].name)
        assertEquals(1, nextState.history.size)
        assertEquals("Minhas anotações", nextState.history[0].notes)
        assertEquals("Kotlin", nextState.history[0].subskillTag)
        assertEquals(25, nextState.history[0].duration)
    }

    @Test
    fun apply_achievementsCheckedAndUnlocked() {
        val state = createBaseState().copy(
            totalSessions = 0,
            achievements = emptyList()
        )
        val calc = createBaseCalc()

        val nextState = FocusApplyLogic.apply(state, calc, editedNotes = "", selectedTag = null)

        // totalSessions becomes 1 -> unlocks "first_quest"
        assertTrue(nextState.achievements.contains("first_quest"))
    }

    @Test
    fun apply_unlocksAchievementBeyondOriginalFourInReact() {
        val state = createBaseState().copy(
            totalXP = 10000,
            achievements = emptyList()
        )
        val calc = createBaseCalc().copy(xpEarned = 50)

        val nextState = FocusApplyLogic.apply(state, calc, editedNotes = "", selectedTag = null)

        assertTrue(nextState.achievements.contains("xp_10000"))
    }

    @Test
    fun apply_survivedWilderness_unlocksAchievementAutomatically() {
        val state = createBaseState().copy(
            wildernessWins = 0,
            achievements = emptyList()
        )
        val calc = createBaseCalc().copy(isWildernessChecked = true)

        val nextState = FocusApplyLogic.apply(state, calc, editedNotes = "", selectedTag = null)

        assertTrue(nextState.achievements.contains("survive_wilderness"))
    }

    @Test
    fun apply_incrementsStreakOnNewDay_keepsStreakOnSameDay() {
        val refDate = Date(1700000000000L)
        val sameDayString = QuestLogic.toDateStringJs(refDate)

        // Case A: lastStudyDate is different -> streak incremented
        val stateNewDay = createBaseState().copy(streak = 3, lastStudyDate = "01/01/2020")
        val calc = createBaseCalc()
        val nextStateNewDay = FocusApplyLogic.apply(stateNewDay, calc, editedNotes = "", selectedTag = null, referenceDate = refDate)
        assertEquals(4, nextStateNewDay.streak)

        // Case B: lastStudyDate is same day -> streak kept same
        val stateSameDay = createBaseState().copy(streak = 3, lastStudyDate = sameDayString)
        val nextStateSameDay = FocusApplyLogic.apply(stateSameDay, calc, editedNotes = "", selectedTag = null, referenceDate = refDate)
        assertEquals(3, nextStateSameDay.streak)
    }

    @Test
    fun apply_historyEntryDateMatchesExpectedFormat() {
        val state = createBaseState()
        val calc = createBaseCalc()

        val nextState = FocusApplyLogic.apply(state, calc, editedNotes = "teste", selectedTag = null, referenceDate = Date())

        val dateString = nextState.history[0].date
        assertTrue(dateString.matches(Regex("""\d{2}/\d{2}/\d{4}, \d{2}:\d{2}:\d{2}""")))
    }

    @Test
    fun apply_incrementsTotalXp() {
        val state = createBaseState().copy(totalXP = 500)
        val calc = createBaseCalc().copy(xpEarned = 50)

        val nextState = FocusApplyLogic.apply(state, calc, editedNotes = "", selectedTag = null)

        assertEquals(550, nextState.totalXP)
    }

    @Test
    fun apply_updatesLastDungeonClearedTime_whenDungeonBonusApplied() {
        val refDate = Date(1700000000000L)
        val state = createBaseState().copy(lastDungeonClearedTime = 0L)
        val calc = createBaseCalc().copy(dungeonClearGoldBonus = 2500)

        val nextState = FocusApplyLogic.apply(state, calc, editedNotes = "", selectedTag = null, referenceDate = refDate)

        assertEquals(refDate.time, nextState.lastDungeonClearedTime)
    }

    @Test
    fun apply_keepsLastDungeonClearedTime_whenNoDungeonBonus() {
        val previousClearTime = 1650000000000L
        val state = createBaseState().copy(lastDungeonClearedTime = previousClearTime)
        val calc = createBaseCalc().copy(dungeonClearGoldBonus = 0)

        val nextState = FocusApplyLogic.apply(state, calc, editedNotes = "", selectedTag = null, referenceDate = Date(1700000000000L))

        assertEquals(previousClearTime, nextState.lastDungeonClearedTime)
    }

    private fun createBaseState(): CharacterState {
        return CharacterState(
            gold = 100,
            totalXP = 500,
            totalGoldEarned = 200,
            totalSessions = 5,
            totalMinutes = 120,
            combatLevel = 1,
            combatXP = 0,
            skills = listOf(Skill(id = "sk_prog", name = "Programação", level = 1, xp = 0)),
            history = emptyList(),
            inventory = emptyList(),
            streak = 1,
            bestStreak = 2,
            lastStudyDate = "05/08/2026",
            wildernessWins = 0,
            combo = 0,
            dungeonProgress = 0,
            isDungeonMode = false,
            dungeonSessions = 0,
            achievements = emptyList(),
            charName = "Hero",
            charClass = CharClass.Ranger,
            todayXP = 0,
            todayMinutes = 0,
            todayDate = "Wed Aug 05 2026",
            hasClaimedLogin = false,
            hp = 100,
            maxHp = 100,
            habits = emptyList(),
            dailies = emptyList(),
            todos = emptyList(),
            pomodoroSettings = PomodoroSettings(25, 5, 15, false, false)
        )
    }

    private fun createBaseCalc(): FocusRewardsCalculation {
        return FocusRewardsCalculation(
            skillIdx = 0,
            skillName = "Programação",
            xpEarned = 50,
            goldEarned = 75,
            durationMins = 25,
            dungeonClearGoldBonus = 0,
            hasUsedDoubleLoot = false,
            hasUsedFocusElixir = false,
            hasUsedRuneFortune = false,
            hasUsedCrystalClarity = false,
            usedEquipmentIndicesAndCharges = emptyList(),
            lootedItems = emptyList(),
            droppedTitle = null,
            isWildernessChecked = false,
            isDungeonMode = false,
            comboBonusPercent = 0
        )
    }
}
