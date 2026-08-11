package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.quests.QuestCatalog
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.HistoryEntry
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.model.Skill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class QuestLogicTest {

    @Test
    fun mandatoryTest_rotateDailyQuests_aug4_2026() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val aug4Date = dateFormat.parse("2026-08-04")!!

        val quests = QuestLogic.rotateDailyQuests(QuestCatalog.DAILY_QUEST_CATALOG, 3, aug4Date)
        val questIds = quests.map { it.id }

        val expectedIds = listOf(
            "daily_wilderness_step",
            "daily_arcane_polish",
            "daily_triad_of_knowledge"
        )

        assertEquals(expectedIds, questIds)
    }

    @Test
    fun rotateDailyQuests_isDeterministic() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val aug4Date = dateFormat.parse("2026-08-04")!!

        val result1 = QuestLogic.rotateDailyQuests(QuestCatalog.DAILY_QUEST_CATALOG, 3, aug4Date).map { it.id }
        val result2 = QuestLogic.rotateDailyQuests(QuestCatalog.DAILY_QUEST_CATALOG, 3, aug4Date).map { it.id }

        assertEquals(result1, result2)
    }

    @Test
    fun todayLocalStr_formatsPtBr() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val testDate = dateFormat.parse("2026-08-05")!!

        val formatted = QuestLogic.todayLocalStr(testDate)
        assertEquals("05/08/2026", formatted)
    }

    @Test
    fun toDateStringJs_formatsJsDateString() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val testDate = dateFormat.parse("2026-08-04")!!

        val formatted = QuestLogic.toDateStringJs(testDate)
        assertEquals("Tue Aug 04 2026", formatted)
    }

    @Test
    fun weakestSkill_breaksTieWithFirst() {
        val state = createBaseState().copy(
            skills = listOf(
                Skill(name = "Algoritmos", level = 5, xp = 100),
                Skill(name = "Kotlin", level = 3, xp = 50),
                Skill(name = "Design", level = 3, xp = 80),
                Skill(name = "Arquitetura", level = 10, xp = 200)
            )
        )

        val weakest = QuestLogic.weakestSkill(state)
        assertEquals("Kotlin", weakest)
    }

    @Test
    fun todaySessions_filtersByTodayDate() {
        val state = createBaseState().copy(
            history = listOf(
                HistoryEntry("1", "Kotlin", "05/08/2026 09:00", 25, 50, 10, "", false),
                HistoryEntry("2", "Design", "04/08/2026 14:00", 30, 60, 15, "", false)
            )
        )

        val sessions = QuestLogic.todaySessions(state, "05/08/2026")
        assertEquals(1, sessions.size)
        assertEquals("Kotlin", sessions[0].skillName)
    }

    @Test
    fun todaySkillNames_returnsDistinctNames() {
        val state = createBaseState().copy(
            history = listOf(
                HistoryEntry("1", "Kotlin", "05/08/2026 09:00", 25, 50, 10, "", false),
                HistoryEntry("2", "Kotlin", "05/08/2026 11:00", 25, 50, 10, "", false),
                HistoryEntry("3", "Design", "05/08/2026 14:00", 30, 60, 15, "", false)
            )
        )

        val names = QuestLogic.todaySkillNames(state, "05/08/2026")
        assertEquals(listOf("Kotlin", "Design"), names)
    }

    @Test
    fun isQuestClaimed_supportsNonDailyDailyAndLegacy() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val aug4Date = dateFormat.parse("2026-08-04")!!
        val todayJs = "Tue Aug 04 2026"

        // Non-daily claim
        val stateGuild = createBaseState().copy(
            achievements = listOf("claimed_guild_1")
        )
        assertTrue(QuestLogic.isQuestClaimed(stateGuild, "guild_1", aug4Date))
        assertFalse(QuestLogic.isQuestClaimed(stateGuild, "guild_2", aug4Date))

        // Daily claim with date
        val claimId = QuestLogic.getQuestClaimId("daily_first_torch", aug4Date)
        val stateDaily = createBaseState().copy(
            achievements = listOf(claimId)
        )
        assertTrue(QuestLogic.isQuestClaimed(stateDaily, "daily_first_torch", aug4Date))

        // Legacy claim fallback
        val stateLegacy = createBaseState().copy(
            todayDate = todayJs,
            achievements = listOf("claimed_daily_first_torch")
        )
        assertTrue(QuestLogic.isQuestClaimed(stateLegacy, "daily_first_torch", aug4Date))
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
            skills = emptyList(),
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
            charClass = CharClass.Warrior,
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
