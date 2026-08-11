package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.focus.FocusRewardsLogic
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.model.Skill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FocusRewardsLogicTest {

    @Test
    fun calculate_baseRewards_25min() {
        val state = createBaseState()
        val config = FocusSessionConfig(selectedSkillIdx = 0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0)
        // Pass random that yields > 0.95 to avoid random drops interfering
        val noDropRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextDouble(): Double = 0.99
        }

        val result = FocusRewardsLogic.calculate(state, config, studiedMinutes = 25, random = noDropRandom)

        assertEquals(0, result.skillIdx)
        assertEquals("Programação", result.skillName)
        assertEquals(50, result.xpEarned) // 25 * 2 * 1.0
        assertEquals(75, result.goldEarned) // 25 * 3 * 1.0
        assertEquals(25, result.durationMins)
        assertEquals(0, result.dungeonClearGoldBonus)
    }

    @Test
    fun calculate_charClassBuffs() {
        val noDropRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextDouble(): Double = 0.99
        }
        val config = FocusSessionConfig(selectedSkillIdx = 0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0)

        val mageState = createBaseState().copy(charClass = CharClass.Mage)
        val mageResult = FocusRewardsLogic.calculate(mageState, config, studiedMinutes = 25, random = noDropRandom)
        assertEquals(60, mageResult.xpEarned) // 50 * 1.20

        val warriorState = createBaseState().copy(charClass = CharClass.Warrior)
        val warriorResult = FocusRewardsLogic.calculate(warriorState, config, studiedMinutes = 25, random = noDropRandom)
        assertEquals(90, warriorResult.goldEarned) // 75 * 1.20
    }

    @Test
    fun calculate_comboBoost_cappedAt50Percent() {
        val noDropRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextDouble(): Double = 0.99
        }
        val config = FocusSessionConfig(selectedSkillIdx = 0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0)

        val comboState = createBaseState().copy(combo = 12)
        val result = FocusRewardsLogic.calculate(comboState, config, studiedMinutes = 25, random = noDropRandom)

        assertEquals(50, result.comboBonusPercent)
        assertEquals(75, result.xpEarned) // 50 * (1.0 + 0.50)
    }

    @Test
    fun calculate_wildernessAndDungeonMode_multipliers() {
        val noDropRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextDouble(): Double = 0.99
        }
        val config = FocusSessionConfig(selectedSkillIdx = 0, isWildernessChecked = true, isDungeonMode = true, dungeonSessions = 3)

        val result = FocusRewardsLogic.calculate(createBaseState(), config, studiedMinutes = 25, random = noDropRandom)

        // baseXP = 50, xpMult = 1.0 + 0.25 (wilderness) + 0.50 (dungeon) = 1.75 -> 87
        // baseGold = 75, goldMult = 1.0 + 0.25 (wilderness) = 1.25 -> 93
        assertEquals(87, result.xpEarned)
        assertEquals(93, result.goldEarned)
        assertEquals(2500, result.dungeonClearGoldBonus) // dungeonSessions + 1 = 4 >= 4
    }

    @Test
    fun calculate_consumables_doubleLoot_crystalClarity() {
        val noDropRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextDouble(): Double = 0.99
        }
        val config = FocusSessionConfig(selectedSkillIdx = 0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0)

        val stateWithConsumables = createBaseState().copy(
            inventory = listOf(
                InventoryItem("inv_1", "Double Loot", "💰", BuffType.DoubleLoot, 100, "2x Gold"),
                InventoryItem("inv_2", "Crystal Clarity", "🔮", BuffType.CrystalClarity, 100, "2x XP")
            )
        )

        val result = FocusRewardsLogic.calculate(stateWithConsumables, config, studiedMinutes = 25, random = noDropRandom)

        assertTrue(result.hasUsedDoubleLoot)
        assertTrue(result.hasUsedCrystalClarity)
        assertEquals(100, result.xpEarned) // 50 * 1.0 * 2.0
        assertEquals(150, result.goldEarned) // 75 * 1.0 * 2.0
    }

    @Test
    fun calculate_equipmentChargeUsed_andXpBonus() {
        val noDropRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextDouble(): Double = 0.99
        }
        val config = FocusSessionConfig(selectedSkillIdx = 0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0)

        val pixelOwl = InventoryItem(
            id = "owl_1", name = "Coruja Pixelada", emoji = "🦉", buff = BuffType.PixelOwl,
            price = 250, desc = "Owl", isEquipment = true, charges = 8, maxCharges = 8
        )
        val stateWithEquipment = createBaseState().copy(equippedEquipment = listOf(pixelOwl, null, null))

        val result = FocusRewardsLogic.calculate(stateWithEquipment, config, studiedMinutes = 25, random = noDropRandom)

        assertEquals(1, result.usedEquipmentIndicesAndCharges.size)
        assertEquals(0, result.usedEquipmentIndicesAndCharges[0].index)
        assertEquals(7, result.usedEquipmentIndicesAndCharges[0].remainingCharges)
        assertEquals(52, result.xpEarned) // 50 * (1.0 + 0.05)
    }

    @Test
    fun calculate_deterministicLootDrop_viaInjectedRandom() {
        val alwaysDropRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextDouble(): Double = 0.0
        }
        val config = FocusSessionConfig(selectedSkillIdx = 0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0)

        val result = FocusRewardsLogic.calculate(createBaseState(), config, studiedMinutes = 25, random = alwaysDropRandom)

        assertTrue(result.lootedItems.isNotEmpty())
        assertNotNull(result.droppedTitle)
    }

    @Test
    fun calculate_thunderstruckTitle_goldBonusUnconditional_xpBonusRequiresWilderness() {
        val noDropRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextDouble(): Double = 0.99
        }
        val state = createBaseState().copy(equippedTitle = "THUNDERSTRUCK")

        // Case A: Wilderness is false -> Gold gets +10% unconditionally, XP gets +0% title bonus
        val configNoWilderness = FocusSessionConfig(selectedSkillIdx = 0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0)
        val resultA = FocusRewardsLogic.calculate(state, configNoWilderness, studiedMinutes = 25, random = noDropRandom)
        assertEquals(50, resultA.xpEarned)   // 50 * 1.0
        assertEquals(82, resultA.goldEarned) // floor(75 * 1.10) = 82

        // Case B: Wilderness is true -> Gold gets +10% title + 25% wilderness = 1.35, XP gets +25% title + 25% wilderness = 1.50
        val configWilderness = FocusSessionConfig(selectedSkillIdx = 0, isWildernessChecked = true, isDungeonMode = false, dungeonSessions = 0)
        val resultB = FocusRewardsLogic.calculate(state, configWilderness, studiedMinutes = 25, random = noDropRandom)
        assertEquals(75, resultB.xpEarned)    // floor(50 * 1.50) = 75
        assertEquals(101, resultB.goldEarned) // floor(75 * 1.35) = 101
    }

    @Test
    fun calculate_dungeonBonus_notAppliedBeforeFourthSession() {
        val noDropRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextDouble(): Double = 0.99
        }
        val config = FocusSessionConfig(selectedSkillIdx = 0, isWildernessChecked = false, isDungeonMode = true, dungeonSessions = 2)

        val result = FocusRewardsLogic.calculate(createBaseState(), config, studiedMinutes = 25, random = noDropRandom)

        assertEquals(0, result.dungeonClearGoldBonus) // dungeonSessions + 1 = 3 < 4
    }

    @Test
    fun calculate_forcedNoLootRoll_emptyLandedLoots() {
        val noDropRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextDouble(): Double = 0.99
        }
        val config = FocusSessionConfig(selectedSkillIdx = 0, isWildernessChecked = false, isDungeonMode = false, dungeonSessions = 0)

        val result = FocusRewardsLogic.calculate(createBaseState(), config, studiedMinutes = 25, random = noDropRandom)

        assertTrue(result.lootedItems.isEmpty())
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
}
