package com.iurispraecepta.herolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.model.CharacterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeroLogViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _characterState = MutableStateFlow<CharacterState?>(null)
    val characterState: StateFlow<CharacterState?> = _characterState.asStateFlow()

    init {
        viewModelScope.launch {
            _characterState.value = repository.getCharacterState()
        }
    }

    fun saveCharacterState(state: CharacterState) {
        viewModelScope.launch {
            repository.saveCharacterState(state)
            _characterState.value = state
        }
    }
}
