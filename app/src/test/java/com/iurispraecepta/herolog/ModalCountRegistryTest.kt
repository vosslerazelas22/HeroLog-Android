package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.ui.components.ModalCountRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ModalCountRegistryTest {

    @Before
    fun setup() {
        while (ModalCountRegistry.isAnyModalOpen) {
            ModalCountRegistry.decrement()
        }
    }

    @Test
    fun testModalCountIncrementAndDecrement() {
        assertFalse(ModalCountRegistry.isAnyModalOpen)

        ModalCountRegistry.increment()
        assertTrue(ModalCountRegistry.isAnyModalOpen)

        ModalCountRegistry.increment()
        assertTrue(ModalCountRegistry.isAnyModalOpen)

        ModalCountRegistry.decrement()
        assertTrue(ModalCountRegistry.isAnyModalOpen)

        ModalCountRegistry.decrement()
        assertFalse(ModalCountRegistry.isAnyModalOpen)
    }

    @Test
    fun testModalCountDoesNotGoBelowZero() {
        assertFalse(ModalCountRegistry.isAnyModalOpen)

        ModalCountRegistry.decrement()
        assertFalse(ModalCountRegistry.isAnyModalOpen)

        ModalCountRegistry.increment()
        assertTrue(ModalCountRegistry.isAnyModalOpen)

        ModalCountRegistry.decrement()
        assertFalse(ModalCountRegistry.isAnyModalOpen)
    }
}
