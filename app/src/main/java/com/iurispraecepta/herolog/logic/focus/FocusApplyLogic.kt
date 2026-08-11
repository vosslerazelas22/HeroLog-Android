package com.iurispraecepta.herolog.logic.focus

import com.iurispraecepta.herolog.logic.CombatLogic
import com.iurispraecepta.herolog.logic.SkillLogic
import com.iurispraecepta.herolog.logic.achievements.AchievementCatalog
import com.iurispraecepta.herolog.logic.achievements.AchievementLogic
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.HistoryEntry
import com.iurispraecepta.herolog.model.InventoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object FocusApplyLogic {

    fun apply(
        state: CharacterState,
        calc: FocusRewardsCalculation,
        editedNotes: String,
        selectedTag: String?,
        referenceDate: Date = Date()
    ): CharacterState {
        val totalGoldGained = calc.goldEarned + calc.dungeonClearGoldBonus

        // 1. Skills — level up com bônus de prestígio por título
        val updatedSkills = state.skills.mapIndexed { idx, sk ->
            if (idx == calc.skillIdx) {
                val eqTitleId = state.equippedTitle
                val prestigeGrowth = when (eqTitleId) {
                    "DRAGONBORN" -> 0.25 * 1.25
                    "PANTHEON" -> 0.25 * 1.30
                    "THE_ETERNAL_SCHOLAR", "ASCENDED" -> 0.25 * 1.50
                    else -> 0.25
                }
                val prestigeBonus = 1 + (sk.prestige ?: 0) * prestigeGrowth
                val finalXPApplied = (calc.xpEarned * prestigeBonus).roundToInt()

                var updatedXP = sk.xp + finalXPApplied
                var currentLevel = sk.level
                var xpRequired = SkillLogic.requiredXpForLevel(currentLevel)
                while (updatedXP >= xpRequired) {
                    updatedXP -= xpRequired
                    currentLevel += 1
                    xpRequired = SkillLogic.requiredXpForLevel(currentLevel)
                }
                sk.copy(level = currentLevel, xp = updatedXP)
            } else sk
        }

        // 2. Consumir itens de uso único (um de cada, se usado)
        val updatedInv = state.inventory.toMutableList()
        if (calc.hasUsedDoubleLoot) updatedInv.indexOfFirst { it.buff == com.iurispraecepta.herolog.model.BuffType.DoubleLoot }.let { if (it >= 0) updatedInv.removeAt(it) }
        if (calc.hasUsedFocusElixir) updatedInv.indexOfFirst { it.buff == com.iurispraecepta.herolog.model.BuffType.FocusElixir }.let { if (it >= 0) updatedInv.removeAt(it) }
        if (calc.hasUsedRuneFortune) updatedInv.indexOfFirst { it.buff == com.iurispraecepta.herolog.model.BuffType.RuneFortune }.let { if (it >= 0) updatedInv.removeAt(it) }
        if (calc.hasUsedCrystalClarity) updatedInv.indexOfFirst { it.buff == com.iurispraecepta.herolog.model.BuffType.CrystalClarity }.let { if (it >= 0) updatedInv.removeAt(it) }

        // 3. Decrementar/expirar cargas de equipamento usado
        val updatedEquip = (state.equippedEquipment ?: listOf(null, null, null)).toMutableList()
        calc.usedEquipmentIndicesAndCharges.forEach { (index, charges) ->
            val item = updatedEquip.getOrNull(index)
            if (item != null) {
                updatedEquip[index] = if (charges <= 0) null else item.copy(charges = charges)
            }
        }

        // 4. Adicionar loot ao inventário
        calc.lootedItems.forEach { loot ->
            updatedInv.add(
                InventoryItem(
                    id = java.util.UUID.randomUUID().toString(),
                    name = loot.name, emoji = loot.emoji, buff = loot.buff, price = loot.price,
                    desc = loot.desc, isEquipment = loot.isEquipment, charges = loot.charges,
                    maxCharges = loot.maxCharges, rarity = loot.rarity
                )
            )
        }

        // 5. Progressão de Combat Level
        var combatXPApplied = state.combatXP + (calc.xpEarned * 0.4).toInt()
        var currentCombatLevel = state.combatLevel
        var combatXPRequirement = CombatLogic.requiredXpForCombatLevel(currentCombatLevel)
        while (combatXPApplied >= combatXPRequirement) {
            combatXPApplied -= combatXPRequirement
            currentCombatLevel += 1
            combatXPRequirement = CombatLogic.requiredXpForCombatLevel(currentCombatLevel)
        }

        // 6. Streak — reaproveita o mesmo formato de data (inglês) já usado no LCG das quests
        val todayString = QuestLogic.toDateStringJs(referenceDate)
        val newStreak = if (state.lastStudyDate != todayString) state.streak + 1 else state.streak
        val newBestStreak = maxOf(newStreak, state.bestStreak)

        // 7. HistoryEntry — formato confirmado por auditoria: data + hora, pt-BR
        val historyObj = HistoryEntry(
            id = java.util.UUID.randomUUID().toString(),
            skillName = calc.skillName,
            date = SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.forLanguageTag("pt-BR")).format(referenceDate),
            duration = calc.durationMins,
            xp = calc.xpEarned,
            gold = totalGoldGained,
            notes = editedNotes.trim(),
            subskillTag = selectedTag,
            wilderness = calc.isWildernessChecked,
            aiChronicle = null
        )

        val nextOwnedTitles = (state.ownedTitles ?: emptyList()).toMutableList()
        if (calc.droppedTitle != null && !nextOwnedTitles.contains(calc.droppedTitle.id)) {
            nextOwnedTitles.add(calc.droppedTitle.id)
        }

        // 8. Candidato de próximo estado (tudo exceto achievements resolvido)
        val candidateState = state.copy(
            gold = state.gold + totalGoldGained,
            totalGoldEarned = state.totalGoldEarned + totalGoldGained,
            totalXP = state.totalXP + calc.xpEarned,
            totalSessions = state.totalSessions + 1,
            totalMinutes = state.totalMinutes + calc.durationMins,
            combatLevel = currentCombatLevel,
            combatXP = combatXPApplied,
            skills = updatedSkills,
            inventory = updatedInv,
            equippedEquipment = updatedEquip,
            history = listOf(historyObj) + state.history,
            streak = newStreak,
            bestStreak = newBestStreak,
            lastStudyDate = todayString,
            wildernessWins = state.wildernessWins + if (calc.isWildernessChecked) 1 else 0,
            combo = state.combo + 1,
            todayMinutes = state.todayMinutes + calc.durationMins,
            todayXP = state.todayXP + calc.xpEarned,
            ownedTitles = nextOwnedTitles,
            lastDungeonClearedTime = if (calc.dungeonClearGoldBonus > 0) referenceDate.time else state.lastDungeonClearedTime
        )

        // 9. MELHORIA CONSCIENTE (decisão registrada no PARIDADE.md): usa o catálogo real de 8
        // conquistas + AchievementLogic.isUnlocked contra o estado candidato já atualizado, em
        // vez da lista inline de 4 ids do React (testAchievements). Cobre as 8, não só 4, e
        // resolve survive_wilderness automaticamente via wildernessWins já incrementado acima —
        // sem precisar do tratamento especial separado que o React tinha.
        val unlockedIds = state.achievements.toMutableList()
        AchievementCatalog.ACHIEVEMENTS_LIST.forEach { ach ->
            if (!unlockedIds.contains(ach.id) && AchievementLogic.isUnlocked(ach, candidateState)) {
                unlockedIds.add(ach.id)
            }
        }

        return candidateState.copy(achievements = unlockedIds)
    }
}