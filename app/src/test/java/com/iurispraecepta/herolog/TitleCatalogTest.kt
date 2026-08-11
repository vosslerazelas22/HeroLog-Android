package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.TITLE_CATALOG
import com.iurispraecepta.herolog.data.TitleCategory
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.PomodoroSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TitleCatalogTest {

    private fun blankState(overrides: CharacterState.() -> CharacterState = { this }) =
        CharacterState(
            gold = 0, totalXP = 0, totalGoldEarned = 0, totalSessions = 0, totalMinutes = 0,
            combatLevel = 1, combatXP = 0, skills = emptyList(), history = emptyList(),
            inventory = emptyList(), streak = 0, bestStreak = 0, lastStudyDate = null,
            wildernessWins = 0, combo = 0, dungeonProgress = 0, isDungeonMode = false,
            dungeonSessions = 0, achievements = emptyList(), charName = "Teste",
            charClass = CharClass.Warrior, todayXP = 0, todayMinutes = 0, todayDate = "2026-01-01",
            hasClaimedLogin = false, hp = 100, maxHp = 100, habits = emptyList(),
            dailies = emptyList(), todos = emptyList(),
            pomodoroSettings = PomodoroSettings(25, 5, 15, true, false)
        ).overrides()

    @Test
    fun catalog_hasCorrectSizeAndCategoryCounts() {
        assertEquals(47, TITLE_CATALOG.size)
        assertEquals(8, TITLE_CATALOG.count { it.category == TitleCategory.Common })
        assertEquals(6, TITLE_CATALOG.count { it.category == TitleCategory.Rare })
        assertEquals(5, TITLE_CATALOG.count { it.category == TitleCategory.Epic })
        assertEquals(3, TITLE_CATALOG.count { it.category == TitleCategory.Legendary })
        assertEquals(18, TITLE_CATALOG.count { it.category == TitleCategory.Achievement })
        assertEquals(7, TITLE_CATALOG.count { it.category == TitleCategory.Drop })
    }

    @Test
    fun catalog_hasNoDuplicateIds() {
        val ids = TITLE_CATALOG.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun spotCheck_pricesMatchSourceExactly() {
        val byId = TITLE_CATALOG.associateBy { it.id }
        assertEquals(150, byId.getValue("APPRENTICE").price)
        assertEquals(200, byId.getValue("SCHOLAR").price)
        assertEquals(1500, byId.getValue("CHAMPION").price)
        assertEquals(10000, byId.getValue("LEGEND_GP").price)
        assertEquals(100000, byId.getValue("THE_WATCHER").price)
        assertEquals(500000, byId.getValue("THE_ETERNAL_SCHOLAR").price)
    }

    @Test
    fun spotCheck_checkUnlocked_centurion_requiresTotalSessions100() {
        val centurion = TITLE_CATALOG.first { it.id == "CENTURION" }
        val below = blankState { copy(totalSessions = 99) }
        val at = blankState { copy(totalSessions = 100) }
        assertTrue(centurion.checkUnlocked?.invoke(below) == false)
        assertTrue(centurion.checkUnlocked?.invoke(at) == true)
    }

    @Test
    fun spotCheck_checkUnlocked_xpGod_requires5MillionTotalXp() {
        val xpGod = TITLE_CATALOG.first { it.id == "XP_GOD" }
        val below = blankState { copy(totalXP = 4_999_999) }
        val at = blankState { copy(totalXP = 5_000_000) }
        assertTrue(xpGod.checkUnlocked?.invoke(below) == false)
        assertTrue(xpGod.checkUnlocked?.invoke(at) == true)
    }

    @Test
    fun spotCheck_checkUnlocked_ascended_sumsPrestigeAcrossSkills() {
        val ascended = TITLE_CATALOG.first { it.id == "ASCENDED" }
        val notEnough = blankState {
            copy(skills = listOf(
                com.iurispraecepta.herolog.model.Skill(name = "A", level = 99, xp = 0, prestige = 5),
                com.iurispraecepta.herolog.model.Skill(name = "B", level = 99, xp = 0, prestige = 4)
            ))
        }
        val enough = blankState {
            copy(skills = listOf(
                com.iurispraecepta.herolog.model.Skill(name = "A", level = 99, xp = 0, prestige = 5),
                com.iurispraecepta.herolog.model.Skill(name = "B", level = 99, xp = 0, prestige = 5)
            ))
        }
        assertTrue(ascended.checkUnlocked?.invoke(notEnough) == false)
        assertTrue(ascended.checkUnlocked?.invoke(enough) == true)
    }
}
