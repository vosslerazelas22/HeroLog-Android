package com.iurispraecepta.herolog.data

import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.PomodoroSettings
import java.util.Date

/**
 * Estado inicial de um personagem novo - port de INITIAL_STATE (src/hooks/useGameState.ts).
 *
 * DIVERGENCIA CONSCIENTE (decisao aprovada por Bruno, 11/08/2026): o INITIAL_STATE real do
 * React inclui skills/habits/dailies/todos de exemplo com dados PESSOAIS de Bruno (tags de
 * concurso, nome de animal de estimacao, livro especifico) - nao portados aqui de proposito.
 * O personagem novo no Android nasce com essas 4 listas vazias.
 */
fun createInitialCharacterState(referenceDate: Date = Date()): CharacterState {
    return CharacterState(
        gold = 200,
        totalXP = 0,
        totalGoldEarned = 200,
        totalSessions = 0,
        totalMinutes = 0,
        combatLevel = 1,
        combatXP = 0,
        skills = emptyList(),
        history = emptyList(),
        inventory = emptyList(),
        streak = 0,
        bestStreak = 0,
        lastStudyDate = null,
        wildernessWins = 0,
        combo = 0,
        dungeonProgress = 0,
        isDungeonMode = false,
        dungeonSessions = 0,
        achievements = emptyList(),
        charName = "Aventureiro do Foco",
        charClass = CharClass.Mage,
        todayXP = 0,
        todayMinutes = 0,
        todayDate = QuestLogic.toDateStringJs(referenceDate),
        hasClaimedLogin = false,
        hp = 50,
        maxHp = 50,
        habits = emptyList(),
        dailies = emptyList(),
        todos = emptyList(),
        equippedTitle = null,
        ownedTitles = emptyList(),
        equippedEquipment = listOf(null, null, null),
        pomodoroSettings = PomodoroSettings(
            focusDuration = 25,
            shortBreakDuration = 5,
            longBreakDuration = 15,
            autoStartBreak = false,
            autoStartFocus = false
        ),
        lastDungeonClearedTime = 0L
    )
}
