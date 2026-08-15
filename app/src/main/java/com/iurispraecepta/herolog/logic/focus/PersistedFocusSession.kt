package com.iurispraecepta.herolog.logic.focus

import kotlinx.serialization.Serializable

/**
 * Persisted state of an active or pending focus session.
 *
 * Semântica:
 * - `pendingCalculation == null` = sessão em andamento
 * - `pendingCalculation != null` = cálculo já feito uma vez, congelado, aguardando confirmação futura.
 */
@Serializable
data class PersistedFocusSession(
    val config: FocusSessionConfig,
    val durationMinutes: Int,
    val endTimeMillis: Long,
    val pendingCalculation: FocusRewardsCalculation? = null
)
