package com.iurispraecepta.herolog.logic.focus

import com.iurispraecepta.herolog.model.CharClass
import kotlin.random.Random

/**
 * Port das regras puras do sistema de "Morte Cognitiva" (infração de Terra Selvagem), auditado
 * a partir da fonte real de `useFocusSession.ts` (React) em 09/08/2026.
 *
 * IMPORTANTE — este arquivo NÃO inclui o gatilho (trigger). No React, o gatilho é
 * `document.visibilitychange` + `window.blur`/`focus` (conceito de aba/janela). **Decisão de
 * escopo aprovada por Bruno**: o gatilho não precisa de fidelidade estrita no Android — não
 * existe o conceito de aba. O equivalente adotado é o app indo para background
 * (`ON_STOP`/`ON_PAUSE` do lifecycle), a ser conectado num bloco posterior de wiring, não aqui.
 * Esta camada só decide "o que acontece quando a infração é confirmada", que mantém fidelidade
 * estrita com a fonte.
 *
 * NÃO INCLUÍDO NESTE ARQUIVO (fora de escopo deste bloco):
 * - O gatilho de lifecycle em si (bloco futuro de wiring).
 * - Aplicação dos resultados a um `CharacterState`/`GameState` real — as funções abaixo retornam
 *   dados puros (novo streak, novo combo, novo nível/gold/XP), quem aplica é a camada de wiring,
 *   seguindo o mesmo padrão de separação calcula/aplica já usado em
 *   `FocusRewardsLogic`/`FocusApplyLogic`.
 * - O countdown do grace period em si (é estado de UI/timer, não regra pura) — só a decisão de
 *   qual caminho seguir (`resolveWildernessInfraction`) e o resultado final de cada caminho.
 */

/** Duração do grace period antes da morte cognitiva ser confirmada, em segundos — valor fonte
 * real (`graceSecondsLeft` inicial em `useFocusSession.ts`). Não é regra pura testável por si
 * só, mas fica aqui como constante de referência única para quem for portar o countdown de UI. */
const val WILDERNESS_GRACE_PERIOD_SECONDS = 3

/** Chance de a classe Ranger salvar a streak na morte cognitiva — valor fonte real
 * (`saveChance = charClass === 'Ranger' ? 0.15 : 0` em `useFocusSession.ts`). */
private const val RANGER_STREAK_SAVE_CHANCE = 0.15

/** Penalidade de ouro ao reviver — valor fonte real (`goldPenalty = 50`). */
private const val RESPAWN_GOLD_PENALTY = 50

/**
 * O que acontece quando uma infração de Terra Selvagem é detectada (app foi para background
 * durante uma sessão com `isWildernessChecked = true`).
 *
 * Fonte real: título `DEATH-PROOF` equipado converte a infração numa pausa normal, sem
 * penalidade nenhuma — não entra no grace period. Qualquer outro caso inicia o grace period.
 */
enum class WildernessInfractionOutcome {
    CONVERTED_TO_PAUSE,
    GRACE_PERIOD_STARTED,
}

fun resolveWildernessInfraction(equippedTitleId: String?): WildernessInfractionOutcome =
    if (equippedTitleId == "DEATH-PROOF") {
        WildernessInfractionOutcome.CONVERTED_TO_PAUSE
    } else {
        WildernessInfractionOutcome.GRACE_PERIOD_STARTED
    }

/**
 * Resultado da resolução de morte cognitiva (grace period esgotado sem o jogador retornar).
 *
 * `streakSaved` reflete se a classe Ranger salvou a streak (só pode ser `true` quando
 * `previousStreak > 0`, já que não existe "salvar" uma streak que já era zero — nesse caso o
 * resultado é `streakSaved = false` mas `newStreak` já seria 0 mesmo antes, sem penalidade
 * "nova" de fato, exatamente como a fonte real: o `else if (prev.streak > 0)` do React só existe
 * pra decidir qual mensagem de log mostrar, não muda o valor numérico quando streak já é 0).
 */
data class CognitiveDeathResult(
    val newStreak: Int,
    val streakSaved: Boolean,
    val newCombo: Int,
)

/**
 * Resolve o que acontece com streak/combo quando a morte cognitiva se confirma. `random` é
 * injetável para determinismo em teste, mesmo padrão já usado em `FocusRewardsLogic.calculate()`.
 *
 * Fonte real (`triggerCognitiveDeath` em `useFocusSession.ts`):
 * ```
 * val roll = random.nextDouble()
 * val saveChance = if (charClass == CharClass.Ranger) 0.15 else 0.0
 * if (roll >= saveChance && previousStreak > 0) -> streak zerada
 * else if (previousStreak > 0) -> streak preservada (Ranger "Esquiva Rápida")
 * combo sempre zerado
 * ```
 */
fun resolveCognitiveDeath(
    charClass: CharClass,
    previousStreak: Int,
    random: Random = Random,
): CognitiveDeathResult {
    val saveChance = if (charClass == CharClass.Ranger) RANGER_STREAK_SAVE_CHANCE else 0.0
    val roll = random.nextDouble()

    val streakSaved = previousStreak > 0 && roll < saveChance
    val newStreak = if (previousStreak > 0 && !streakSaved) 0 else previousStreak

    return CognitiveDeathResult(
        newStreak = newStreak,
        streakSaved = streakSaved,
        newCombo = 0,
    )
}

/**
 * Resultado da penalidade de reviver após a morte cognitiva.
 */
data class RespawnResult(
    val newCombatLevel: Int,
    val newGold: Int,
    val newCombatXp: Int,
)

/**
 * Resolve a penalidade de `respawnHero()` — fonte real: -1 nível de combate (piso 1), -50 GP
 * (piso 0), XP de combate zerado.
 */
fun resolveRespawn(
    currentCombatLevel: Int,
    currentGold: Int,
): RespawnResult {
    val newCombatLevel = maxOf(1, currentCombatLevel - 1)
    val newGold = maxOf(0, currentGold - RESPAWN_GOLD_PENALTY)

    return RespawnResult(
        newCombatLevel = newCombatLevel,
        newGold = newGold,
        newCombatXp = 0,
    )
}
