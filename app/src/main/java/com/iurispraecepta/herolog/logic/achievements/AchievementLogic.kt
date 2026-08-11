package com.iurispraecepta.herolog.logic.achievements

import com.iurispraecepta.herolog.model.CharacterState

object AchievementLogic {
    fun isUnlocked(achievement: Achievement, state: CharacterState): Boolean {
        return state.achievements.contains(achievement.id) || achievement.check(state)
    }
}