package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.ui.focus.RaidMode
import com.iurispraecepta.herolog.ui.focus.lootChancePercentFrom
import com.iurispraecepta.herolog.ui.focus.raidModeFrom
import com.iurispraecepta.herolog.ui.focus.toLegacyFlags
import org.junit.Assert.assertEquals
import org.junit.Test

class RaidModeSectionTest {

    @Test
    fun raidModeFrom_convertsLegacyFlagsCorrectly() {
        assertEquals(RaidMode.PADRAO, raidModeFrom(isDungeonMode = false, isWildernessChecked = false))
        assertEquals(RaidMode.MASMORRA, raidModeFrom(isDungeonMode = true, isWildernessChecked = false))
        assertEquals(RaidMode.SELVAGEM, raidModeFrom(isDungeonMode = false, isWildernessChecked = true))
        // Dungeon takes precedence if both true
        assertEquals(RaidMode.MASMORRA, raidModeFrom(isDungeonMode = true, isWildernessChecked = true))
    }

    @Test
    fun toLegacyFlags_convertsModeToFlagsCorrectly() {
        assertEquals(Pair(false, false), RaidMode.PADRAO.toLegacyFlags())
        assertEquals(Pair(true, false), RaidMode.MASMORRA.toLegacyFlags())
        assertEquals(Pair(false, true), RaidMode.SELVAGEM.toLegacyFlags())
    }

    @Test
    fun raidMode_roundTrip_preservesMode() {
        RaidMode.values().forEach { mode ->
            val (dungeon, wilderness) = mode.toLegacyFlags()
            val reconstituted = raidModeFrom(dungeon, wilderness)
            assertEquals(mode, reconstituted)
        }
    }

    @Test
    fun lootChancePercentFrom_delegatesToLootConfig() {
        val shortSessionChance = lootChancePercentFrom(studiedMinutes = 25, isDungeon = false, equippedTitleId = null)
        assertEquals(25, shortSessionChance)

        val longSessionChance = lootChancePercentFrom(studiedMinutes = 90, isDungeon = false, equippedTitleId = null)
        assertEquals(70, longSessionChance)

        val dungeonSessionChance = lootChancePercentFrom(studiedMinutes = 30, isDungeon = true, equippedTitleId = null)
        assertEquals(40, dungeonSessionChance)
    }
}
