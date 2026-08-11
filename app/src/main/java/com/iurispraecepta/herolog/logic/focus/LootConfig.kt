package com.iurispraecepta.herolog.logic.focus

object LootConfig {
    const val BASE_LOOT_CHANCE_SHORT = 0.25
    const val BASE_LOOT_CHANCE_MEDIUM = 0.45
    const val BASE_LOOT_CHANCE_LONG = 0.70
    const val BASE_LOOT_CHANCE_DUNGEON = 0.40
    const val MAX_LOOT_CHANCE_CAP = 0.95

    val TITLE_LOOT_MULTIPLIERS: Map<String, Double> = mapOf(
        "TRANSCENDENT" to 1.00,
        "CELESTIAL" to 1.00,
        "SHADOW" to 0.75,
        "VOIDWALKER" to 0.50,
        "IMMORTAL_SCHOLAR" to 0.50,
        "NOCTURNAL" to 0.30
    )

    data class LootChanceResult(val baseChance: Double, val lootRateMultiplier: Double, val finalChance: Double)

    fun calculateLootChance(studiedMinutes: Int, isDungeon: Boolean, equippedTitleId: String?): LootChanceResult {
        var baseChance = BASE_LOOT_CHANCE_SHORT
        if (isDungeon) {
            baseChance = BASE_LOOT_CHANCE_DUNGEON
        } else if (studiedMinutes >= 90) {
            baseChance = BASE_LOOT_CHANCE_LONG
        } else if (studiedMinutes >= 50) {
            baseChance = BASE_LOOT_CHANCE_MEDIUM
        }
        val bonus = equippedTitleId?.let { TITLE_LOOT_MULTIPLIERS[it] } ?: 0.0
        val lootRateMultiplier = 1.0 + bonus
        val finalChance = minOf(MAX_LOOT_CHANCE_CAP, baseChance * lootRateMultiplier)
        return LootChanceResult(baseChance, lootRateMultiplier, finalChance)
    }
}