package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.util.LevelCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class LevelCalculatorTest {
    @Test
    fun calculateExampleThreshold_returnsCorrectValues() {
        assertEquals(0, LevelCalculator.calculateExampleThreshold(0))
        assertEquals(100, LevelCalculator.calculateExampleThreshold(1))
        assertEquals(500, LevelCalculator.calculateExampleThreshold(5))
    }
}
