package com.iurispraecepta.herolog.logic.focus

data class FocusSessionState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFocusCompleted: Boolean = false,
    val timeLeft: Int = 0,          // segundos restantes
    val totalSeconds: Int = 0,      // duração total configurada, em segundos
    val pauseCount: Int = 0,
    val config: FocusSessionConfig? = null,
    val durationMinutes: Int = 0,   // duração configurada em minutos — usada como "studiedMinutes"
    val pendingRewardsCalculation: FocusRewardsCalculation? = null,
    val isGraceActive: Boolean = false,
    val graceSecondsLeft: Int = WILDERNESS_GRACE_PERIOD_SECONDS,
    val isPlayerDead: Boolean = false
)
