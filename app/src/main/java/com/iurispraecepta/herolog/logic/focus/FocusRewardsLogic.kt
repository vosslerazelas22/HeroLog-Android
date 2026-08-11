package com.iurispraecepta.herolog.logic.focus

import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.Rarity
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

object FocusRewardsLogic {

    fun calculate(
        state: CharacterState,
        config: FocusSessionConfig,
        studiedMinutes: Int,
        activeEventType: AmbientEventType? = null,
        random: Random = Random.Default
    ): FocusRewardsCalculation {
        val skillIdx = config.selectedSkillIdx
        val skillName = state.skills.getOrNull(skillIdx)?.name ?: "Código Sagrado"

        val baseXP = studiedMinutes * 2
        val baseGold = studiedMinutes * 3

        var xpMultiplier = 1.0
        var goldMultiplier = 1.0

        val eqTitleId = state.equippedTitle
        var titleXpAdd = 0.0
        var titleGoldAdd = 0.0

        // Perks de título — replicando a cadeia de `if` independentes do React 1:1. Como todos
        // testam o MESMO eqTitleId por igualdade, no máximo um bloco pode ser verdadeiro por vez
        // (exceto THUNDERSTRUCK, que tem uma parte condicional e outra incondicional — ver nota).
        if (eqTitleId == "LEGEND_GP") titleXpAdd += 0.05
        if (eqTitleId == "INFERNO") titleXpAdd += 0.08
        if (eqTitleId == "STARBOUND") { titleXpAdd += 0.10; titleGoldAdd += 0.08 }
        if (eqTitleId == "DRAGONBORN") titleXpAdd += 0.15
        if (eqTitleId == "VOIDWALKER") titleXpAdd += 0.20
        if (eqTitleId == "THE_WATCHER") { titleXpAdd += 0.25; titleGoldAdd += 0.15 }
        if (eqTitleId == "TRANSCENDENT") { titleXpAdd += 0.30; titleGoldAdd += 0.20 }
        if (eqTitleId == "THE_ETERNAL_SCHOLAR") { titleXpAdd += 0.50; titleGoldAdd += 0.30 }

        if (eqTitleId == "IRON_WILL") titleXpAdd += 0.05
        if (eqTitleId == "DIAMOND_MIND") { titleXpAdd += 0.08; titleGoldAdd += 0.05 }
        if (eqTitleId == "THE_CENTURY") { titleXpAdd += 0.12; titleGoldAdd += 0.08 }
        if (eqTitleId == "A_FULL_YEAR") { titleXpAdd += 0.25; titleGoldAdd += 0.15 }
        if (eqTitleId == "CENTURION") titleXpAdd += 0.05
        if (eqTitleId == "THE_OBSESSED") { titleXpAdd += 0.15; titleGoldAdd += 0.10 }
        if (eqTitleId == "IMMORTAL_SCHOLAR") { titleXpAdd += 0.25; titleGoldAdd += 0.20 }
        if (eqTitleId == "DEATH-PROOF" && config.isWildernessChecked) titleXpAdd += 0.25
        if (eqTitleId == "DUNGEON_LORD" && config.isDungeonMode) titleXpAdd += 0.15
        if (eqTitleId == "RAID_VETERAN" && config.isDungeonMode) { titleXpAdd += 0.25; titleGoldAdd += 0.20 }
        if (eqTitleId == "LEGEND_ACH") titleXpAdd += 0.10
        if (eqTitleId == "PANTHEON") titleXpAdd += 0.20
        if (eqTitleId == "MANIAC") titleXpAdd += 0.10
        if (eqTitleId == "IN_THE_ZONE") titleXpAdd += 0.05
        if (eqTitleId == "MARATHONER" && studiedMinutes >= 60) titleXpAdd += 0.15
        if (eqTitleId == "XP_GOD") { titleXpAdd += 0.20; titleGoldAdd += 0.10 }
        if (eqTitleId == "NOCTURNAL") titleXpAdd += 0.15
        if (eqTitleId == "ASCENDED") titleXpAdd += 0.10

        if (eqTitleId == "BLESSED") { titleXpAdd += 0.08; titleGoldAdd += 0.10 }
        if (eqTitleId == "SHADOW") titleXpAdd += 0.10
        if (eqTitleId == "THE_FORSAKEN" && config.isWildernessChecked) titleXpAdd += 0.15
        if (eqTitleId == "CELESTIAL") { titleXpAdd += 0.20; titleGoldAdd += 0.15 }
        if (eqTitleId == "THUNDERSTRUCK") {
            // NOTA: goldAdd é INCONDICIONAL aqui, só o xpAdd depende de wilderness — confirmado
            // contra a fonte real, não é erro de digitação.
            if (config.isWildernessChecked) titleXpAdd += 0.25
            titleGoldAdd += 0.10
        }
        if (eqTitleId == "HAUNTED") { titleXpAdd += 0.10; titleGoldAdd += 0.20 }
        if (eqTitleId == "BLOOD_FORGED" && config.isDungeonMode) { titleXpAdd += 0.20; titleGoldAdd += 0.20 }

        xpMultiplier += titleXpAdd
        goldMultiplier += titleGoldAdd

        if (state.charClass == CharClass.Mage) xpMultiplier += 0.20
        if (state.charClass == CharClass.Warrior) goldMultiplier += 0.20

        val comboBoost = min(state.combo * 0.05, 0.50)
        xpMultiplier += comboBoost

        if (activeEventType == AmbientEventType.XP) xpMultiplier += 0.25
        if (activeEventType == AmbientEventType.GOLD) goldMultiplier += 0.50

        if (config.isWildernessChecked) {
            xpMultiplier += 0.25
            goldMultiplier += 0.25
        }
        if (config.isDungeonMode) {
            xpMultiplier += 0.50
        }

        val hasUsedDoubleLoot = state.inventory.any { it.buff == BuffType.DoubleLoot }
        if (hasUsedDoubleLoot) goldMultiplier *= 2.0

        val hasUsedFocusElixir = state.inventory.any { it.buff == BuffType.FocusElixir }
        if (hasUsedFocusElixir) xpMultiplier += 0.20

        val hasUsedRuneFortune = state.inventory.any { it.buff == BuffType.RuneFortune }
        if (hasUsedRuneFortune) goldMultiplier *= 2.0

        val hasUsedCrystalClarity = state.inventory.any { it.buff == BuffType.CrystalClarity }
        if (hasUsedCrystalClarity) xpMultiplier *= 2.0

        val equipped = state.equippedEquipment ?: listOf(null, null, null)
        val usedEquipment = mutableListOf<UsedEquipmentCharge>()

        equipped.forEachIndexed { index, item ->
            if (item != null) {
                var activated = false
                when (item.buff) {
                    BuffType.PixelOwl -> { xpMultiplier += 0.05; activated = true }
                    BuffType.DragonQuill -> if (studiedMinutes >= 45) { xpMultiplier += 0.08; activated = true }
                    BuffType.CrystalBall -> { xpMultiplier += 0.10; activated = true }
                    BuffType.AncientTome -> if (studiedMinutes >= 60) { xpMultiplier += 0.15; activated = true }
                    BuffType.SilverGrimoire -> { xpMultiplier += 0.01; activated = true }
                    BuffType.AncientScroll -> { goldMultiplier += 0.01; activated = true }
                    BuffType.CelestinePotion -> if (studiedMinutes >= 30) { xpMultiplier += 0.02; activated = true }
                    BuffType.StarPowder -> if (studiedMinutes >= 30) { goldMultiplier += 0.02; activated = true }
                    BuffType.GoldBrooch -> { xpMultiplier += 0.01; goldMultiplier += 0.01; activated = true }
                    BuffType.ChaosGrimoire -> { xpMultiplier += 0.02; activated = true }
                    BuffType.UnwaveringSword -> { goldMultiplier += 0.02; activated = true }
                    BuffType.SacredChalice -> { xpMultiplier += 0.01; goldMultiplier += 0.01; activated = true }
                    BuffType.ArcaneRelic -> if (studiedMinutes >= 45) { xpMultiplier += 0.03; activated = true }
                    BuffType.RunicStone -> if (studiedMinutes >= 45) { goldMultiplier += 0.03; activated = true }
                    else -> {}
                }
                if (activated) {
                    val currentCharges = item.charges ?: item.maxCharges ?: 5
                    usedEquipment.add(UsedEquipmentCharge(index, currentCharges - 1))
                }
            }
        }

        val finalXP = floor(baseXP * xpMultiplier).toInt()
        val finalGold = floor(baseGold * goldMultiplier).toInt()

        val lootChanceInfo = LootConfig.calculateLootChance(studiedMinutes, config.isDungeonMode, eqTitleId)
        val landedLoots = mutableListOf<LootItem>()
        val rollCount = if (config.isDungeonMode) 4 else 1

        repeat(rollCount) {
            if (random.nextDouble() < lootChanceInfo.finalChance) {
                val chosenRarity = if (config.isDungeonMode) {
                    if (random.nextDouble() < 0.40) Rarity.Especial else Rarity.Comum
                } else {
                    if (random.nextDouble() < 0.12) Rarity.Especial else Rarity.Comum
                }
                val candidates = LootTable.LOOT_TABLE.filter { it.rarity == chosenRarity }
                if (candidates.isNotEmpty()) {
                    landedLoots.add(candidates[floor(random.nextDouble() * candidates.size).toInt()])
                }
            }
        }

        val baseTitleChance = if (config.isDungeonMode) 0.05 else if (studiedMinutes >= 50) 0.03 else 0.01
        var titleDropMultiplier = 1.0
        if (eqTitleId == "VOIDWALKER") titleDropMultiplier += 0.50
        if (eqTitleId == "NOCTURNAL") titleDropMultiplier += 0.30
        if (eqTitleId == "SHADOW") titleDropMultiplier += 0.75
        val finalTitleDropChance = baseTitleChance * titleDropMultiplier

        var droppedTitle: DroppedTitle? = null
        if (random.nextDouble() < finalTitleDropChance) {
            data class PoolTitle(val id: String, val name: String, val emoji: String)
            val dropTitlesPool = listOf(
                PoolTitle("BLESSED", "BLESSED", "🌸"),
                PoolTitle("SHADOW", "SHADOW", "🌑"),
                PoolTitle("THE_FORSAKEN", "THE FORSAKEN", "🔮"),
                PoolTitle("CELESTIAL", "CELESTIAL", "✨"),
                PoolTitle("THUNDERSTRUCK", "THUNDERSTRUCK", "⚡"),
                PoolTitle("HAUNTED", "HAUNTED", "👻"),
                PoolTitle("BLOOD_FORGED", "BLOOD-FORGED", "🩸")
            )
            val currentOwned = state.ownedTitles ?: emptyList()
            val filteredPool = dropTitlesPool.filter { t ->
                if (currentOwned.contains(t.id)) return@filter false
                when (t.id) {
                    "THUNDERSTRUCK", "HAUNTED", "THE_FORSAKEN" -> config.isWildernessChecked
                    "BLOOD_FORGED" -> config.isDungeonMode
                    else -> true
                }
            }
            if (filteredPool.isNotEmpty()) {
                val picked = filteredPool[floor(random.nextDouble() * filteredPool.size).toInt()]
                droppedTitle = DroppedTitle(picked.id, picked.name, picked.emoji)
            }
        }

        val dungeonClearGoldBonus = if (config.isDungeonMode && config.dungeonSessions + 1 >= 4) 2500 else 0

        return FocusRewardsCalculation(
            skillIdx = skillIdx,
            skillName = skillName,
            xpEarned = finalXP,
            goldEarned = finalGold,
            durationMins = studiedMinutes,
            dungeonClearGoldBonus = dungeonClearGoldBonus,
            hasUsedDoubleLoot = hasUsedDoubleLoot,
            hasUsedFocusElixir = hasUsedFocusElixir,
            hasUsedRuneFortune = hasUsedRuneFortune,
            hasUsedCrystalClarity = hasUsedCrystalClarity,
            usedEquipmentIndicesAndCharges = usedEquipment,
            lootedItems = landedLoots,
            droppedTitle = droppedTitle,
            isWildernessChecked = config.isWildernessChecked,
            isDungeonMode = config.isDungeonMode,
            comboBonusPercent = (comboBoost * 100).roundToInt()
        )
    }
}