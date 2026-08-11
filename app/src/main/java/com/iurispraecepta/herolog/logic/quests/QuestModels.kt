package com.iurispraecepta.herolog.logic.quests

import com.iurispraecepta.herolog.model.CharacterState

data class QuestDef(
    val id: String,
    val name: String,
    val desc: String,
    val summary: String? = null,
    val target: Int,
    val rewardGold: Int,
    val rewardXp: Int,
    val getProgress: (CharacterState) -> Int
)