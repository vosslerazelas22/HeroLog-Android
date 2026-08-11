package com.iurispraecepta.herolog.logic.achievements

import com.iurispraecepta.herolog.model.CharacterState

data class Achievement(
    val id: String,
    val name: String,
    val desc: String,
    val icon: String,
    val check: (CharacterState) -> Boolean
)