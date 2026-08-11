package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.achievements.AchievementCatalog
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.PomodoroSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementCatalogTest {

    @Test
    fun achievementCatalogCount_is8() {
        assertEquals(8, AchievementCatalog.ACHIEVEMENTS_LIST.size)
    }

    @Test
    fun allAchievementIds_areUnique() {
        val ids = AchievementCatalog.ACHIEVEMENTS_LIST.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun spotCheck_check_firstQuest_requiresAtLeast1Session() {
        val firstQuest = AchievementCatalog.ACHIEVEMENTS_LIST.first { it.id == "first_quest" }
        val state0 = createBaseState().copy(totalSessions = 0)
        val state1 = createBaseState().copy(totalSessions = 1)

        assertFalse(firstQuest.check(state0))
        assertTrue(firstQuest.check(state1))
    }

    @Test
    fun spotCheck_check_streak7_requiresBestStreak7() {
        val streak7 = AchievementCatalog.ACHIEVEMENTS_LIST.first { it.id == "streak_7" }
        val state6 = createBaseState().copy(bestStreak = 6)
        val state7 = createBaseState().copy(bestStreak = 7)

        assertFalse(streak7.check(state6))
        assertTrue(streak7.check(state7))
    }

    @Test
    fun spotCheck_check_xp10000_requiresTotalXp10000() {
        val xp10000 = AchievementCatalog.ACHIEVEMENTS_LIST.first { it.id == "xp_10000" }
        val state9999 = createBaseState().copy(totalXP = 9999)
        val state10000 = createBaseState().copy(totalXP = 10000)

        assertFalse(xp10000.check(state9999))
        assertTrue(xp10000.check(state10000))
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
