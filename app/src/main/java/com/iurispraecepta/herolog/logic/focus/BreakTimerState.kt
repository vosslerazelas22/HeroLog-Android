package com.iurispraecepta.herolog.logic.focus

data class BreakTimerState(
    val isBreakPrep: Boolean = false,
    val isBreakActive: Boolean = false,
    val selectedBreakMins: Int = 5,
    val secondsLeft: Int = 0,
    val totalSeconds: Int = 0
)
