package com.iurispraecepta.herolog.logic

import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.CharacterSummary

fun CharacterState.toSummary(): CharacterSummary {
    return CharacterSummary(
        charName = charName,
        charClass = charClass,
        equippedTitle = equippedTitle,
        streak = streak,
        bestStreak = bestStreak,
        totalMinutes = totalMinutes,
        combatLevel = combatLevel,
        combatXP = combatXP,
        hp = hp,
        maxHp = maxHp
    )
}
