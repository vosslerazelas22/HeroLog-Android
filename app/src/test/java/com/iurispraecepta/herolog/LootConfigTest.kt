package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.focus.LootConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class LootConfigTest {

    @Test
    fun calculateLootChance_shortSession_noTitle() {
        val result = LootConfig.calculateLootChance(studiedMinutes = 25, isDungeon = false, equippedTitleId = null)
        assertEquals(0.25, result.baseChance, 0.001)
        assertEquals(1.0, result.lootRateMultiplier, 0.001)
        assertEquals(0.25, result.finalChance, 0.001)
    }

    @Test
    fun calculateLootChance_mediumSession() {
        val result = LootConfig.calculateLootChance(studiedMinutes = 50, isDungeon = false, equippedTitleId = null)
        assertEquals(0.45, result.baseChance, 0.001)
        assertEquals(0.45, result.finalChance, 0.001)
    }

    @Test
    fun calculateLootChance_longSession() {
        val result = LootConfig.calculateLootChance(studiedMinutes = 90, isDungeon = false, equippedTitleId = null)
        assertEquals(0.70, result.baseChance, 0.001)
        assertEquals(0.70, result.finalChance, 0.001)
    }

    @Test
    fun calculateLootChance_dungeonSession() {
        val result = LootConfig.calculateLootChance(studiedMinutes = 30, isDungeon = true, equippedTitleId = null)
        assertEquals(0.40, result.baseChance, 0.001)
        assertEquals(0.40, result.finalChance, 0.001)
    }

    @Test
    fun calculateLootChance_titleMultiplier_and_capAt95Percent() {
        val result = LootConfig.calculateLootChance(studiedMinutes = 90, isDungeon = false, equippedTitleId = "TRANSCENDENT")
        assertEquals(0.70, result.baseChance, 0.001)
        assertEquals(2.0, result.lootRateMultiplier, 0.001)
        // 0.70 * 2.0 = 1.40, capped at 0.95
        assertEquals(0.95, result.finalChance, 0.001)
    }
}
