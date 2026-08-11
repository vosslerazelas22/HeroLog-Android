package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.quests.QuestCatalog
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.HistoryEntry
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.model.RepeatInterval
import com.iurispraecepta.herolog.model.Skill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestCatalogTest {

    @Test
    fun dailyQuestCatalogCount_is20() {
        assertEquals(20, QuestCatalog.DAILY_QUEST_CATALOG.size)
    }

    @Test
    fun guildQuestCatalogCount_is3() {
        assertEquals(3, QuestCatalog.GUILD_QUESTS.size)
    }

    @Test
    fun allQuestIds_areUnique() {
        val dailyIds = QuestCatalog.DAILY_QUEST_CATALOG.map { it.id }
        val guildIds = QuestCatalog.GUILD_QUESTS.map { it.id }
        val allIds = dailyIds + guildIds

        assertEquals(allIds.size, allIds.distinct().size)
    }

    @Test
    fun spotCheck_getProgress_dailyFirstTorch() {
        val quest = QuestCatalog.DAILY_QUEST_CATALOG.first { it.id == "daily_first_torch" }
        val today = QuestLogic.todayLocalStr()
        val stateWithHistory = createBaseState().copy(
            history = listOf(
                HistoryEntry(
                    id = "h1",
                    skillName = "Foco",
                    date = "$today 10:00",
                    duration = 25,
                    xp = 50,
                    gold = 10,
                    notes = "",
                    wilderness = false
                )
            )
        )
        val progress = quest.getProgress(stateWithHistory)
        assertEquals(1, progress)
    }

    @Test
    fun spotCheck_getProgress_dailyRite25() {
        val quest = QuestCatalog.DAILY_QUEST_CATALOG.first { it.id == "daily_rite_25" }
        val state = createBaseState().copy(todayMinutes = 30)
        val progress = quest.getProgress(state)
        assertEquals(25, progress)
    }

    @Test
    fun spotCheck_getProgress_guild1() {
        val quest = QuestCatalog.GUILD_QUESTS.first { it.id == "guild_1" }
        val state = createBaseState().copy(combatLevel = 7)
        val progress = quest.getProgress(state)
        assertEquals(5, progress)
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
