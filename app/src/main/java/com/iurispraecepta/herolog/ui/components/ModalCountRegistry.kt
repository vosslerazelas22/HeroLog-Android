package com.iurispraecepta.herolog.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

object ModalCountRegistry {
    private var openModalCount by mutableIntStateOf(0)

    val isAnyModalOpen: Boolean
        get() = openModalCount > 0

    fun increment() {
        openModalCount++
    }

    fun decrement() {
        openModalCount = (openModalCount - 1).coerceAtLeast(0)
    }
}
