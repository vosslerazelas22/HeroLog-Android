package com.iurispraecepta.herolog.util

object LevelCalculator {
    fun calculateExampleThreshold(level: Int): Int {
        if (level <= 0) return 0
        return level * 100
    }
}
