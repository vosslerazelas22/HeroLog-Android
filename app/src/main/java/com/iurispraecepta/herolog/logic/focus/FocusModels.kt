package com.iurispraecepta.herolog.logic.focus

import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.Rarity
import kotlinx.serialization.Serializable

@Serializable
data class FocusSessionConfig(
    val selectedSkillIdx: Int,
    val isWildernessChecked: Boolean,
    val isDungeonMode: Boolean,
    val dungeonSessions: Int
)

enum class AmbientEventType { XP, GOLD, INSTANT }
// NOTA: 'INSTANT' existe no React (rolls array em triggerRandomAmbientEncounterScheduler) mas
// NÃO é tratado em completeFocusQuest — só 'xp' e 'gold' afetam multiplicadores lá. Replicando
// fielmente essa lacuna da fonte, não é omissão do port.

@Serializable
data class UsedEquipmentCharge(val index: Int, val remainingCharges: Int)

@Serializable
data class DroppedTitle(val id: String, val name: String, val emoji: String)

@Serializable
data class LootItem(
    val name: String,
    val emoji: String,
    val desc: String,
    val buff: BuffType,
    val price: Int,
    val isEquipment: Boolean,
    val charges: Int,
    val maxCharges: Int,
    val rarity: Rarity
)

@Serializable
data class FocusRewardsCalculation(
    val skillIdx: Int,
    val skillName: String,
    val xpEarned: Int,
    val goldEarned: Int,
    val durationMins: Int,
    val dungeonClearGoldBonus: Int,
    val hasUsedDoubleLoot: Boolean,
    val hasUsedFocusElixir: Boolean,
    val hasUsedRuneFortune: Boolean,
    val hasUsedCrystalClarity: Boolean,
    val usedEquipmentIndicesAndCharges: List<UsedEquipmentCharge>,
    val lootedItems: List<LootItem>,
    val droppedTitle: DroppedTitle?,
    val isWildernessChecked: Boolean,
    val isDungeonMode: Boolean,
    val comboBonusPercent: Int
)