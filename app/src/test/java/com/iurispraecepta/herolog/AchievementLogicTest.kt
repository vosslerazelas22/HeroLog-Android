package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.achievements.AchievementCatalog
import com.iurispraecepta.herolog.logic.achievements.AchievementLogic
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.PomodoroSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementLogicTest {

    @Test
    fun isUnlocked_trueWhenCheckPasses_evenIfNotInAchievementsArray() {
        val firstQuest = AchievementCatalog.ACHIEVEMENTS_LIST.first { it.id == "first_quest" }
        val state = createBaseState().copy(
            achievements = emptyList(),
            totalSessions = 5
        )

        assertTrue(AchievementLogic.isUnlocked(firstQuest, state))
    }

    @Test
    fun isUnlocked_trueWhenIdInAchievementsArray_evenIfCheckWouldFail() {
        val firstQuest = AchievementCatalog.ACHIEVEMENTS_LIST.first { it.id == "first_quest" }
        val state = createBaseState().copy(
            achievements = listOf("first_quest"),
            totalSessions = 0
        )

        assertTrue(AchievementLogic.isUnlocked(firstQuest, state))
    }

    @Test
    fun isUnlocked_falseWhenNeitherConditionMet() {
        val firstQuest = AchievementCatalog.ACHIEVEMENTS_LIST.first { it.id == "first_quest" }
        val state = createBaseState().copy(
            achievements = emptyList(),
            totalSessions = 0
        )

        assertFalse(AchievementLogic.isUnlocked(firstQuest, state))
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
