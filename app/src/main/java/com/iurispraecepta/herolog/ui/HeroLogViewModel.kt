package com.iurispraecepta.herolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.logic.EquipTitleResult
import com.iurispraecepta.herolog.logic.InventoryLogic
import com.iurispraecepta.herolog.logic.TitleLogic
import com.iurispraecepta.herolog.logic.focus.FocusRewardsLogic
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.FocusSessionState
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.InventoryItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

class HeroLogViewModel(
    private val repository: CharacterRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val _characterState = MutableStateFlow<CharacterState?>(null)
    val characterState: StateFlow<CharacterState?> = _characterState.asStateFlow()

    private val _focusSessionState = MutableStateFlow(FocusSessionState())
    val focusSessionState: StateFlow<FocusSessionState> = _focusSessionState.asStateFlow()

    private var focusTickJob: Job? = null
    private var focusEndTimeMillis: Long = 0L

    init {
        viewModelScope.launch {
            val existing = repository.getCharacterState()
            if (existing != null) {
                _characterState.value = existing
            } else {
                val initial = createInitialCharacterState()
                repository.saveCharacterState(initial)
                _characterState.value = initial
            }
        }
    }

    fun saveCharacterState(state: CharacterState) {
        viewModelScope.launch {
            repository.saveCharacterState(state)
            _characterState.value = state
        }
    }

    fun unequipItem(slotIdx: Int) {
        val current = _characterState.value ?: return
        val result = InventoryLogic.unequipItem(current.inventory, current.equippedEquipment, slotIdx)
        saveCharacterState(current.copy(inventory = result.inventory, equippedEquipment = result.equippedEquipment))
    }

    fun equipItem(item: InventoryItem, slotIdx: Int) {
        val current = _characterState.value ?: return
        val result = InventoryLogic.equipItem(current.inventory, current.equippedEquipment, item, slotIdx)
        saveCharacterState(current.copy(inventory = result.inventory, equippedEquipment = result.equippedEquipment))
    }

    // Bug corrigido em relacao ao scaffolding anterior do MainActivity: o gold precisa ser
    // incrementado com o sellPrice (fonte real confirmada: hook useInventory.ts, funcao
    // sellItem, gold: prev.gold + sellingPrice). A versao anterior so logava o preco sem
    // aplicar ao estado.
    fun sellItem(item: InventoryItem) {
        val current = _characterState.value ?: return
        val (updatedInventory, sellPrice) = InventoryLogic.sellItem(current.inventory, item)
        saveCharacterState(current.copy(inventory = updatedInventory, gold = current.gold + sellPrice))
    }

    fun discardItem(item: InventoryItem) {
        val current = _characterState.value ?: return
        val updatedInventory = InventoryLogic.discardItem(current.inventory, item)
        saveCharacterState(current.copy(inventory = updatedInventory))
    }

    fun equipTitle(titleId: String?) {
        val current = _characterState.value ?: return
        when (val result = TitleLogic.equipTitle(current.ownedTitles, titleId)) {
            is EquipTitleResult.Success -> saveCharacterState(current.copy(equippedTitle = result.equippedTitle))
            EquipTitleResult.NotOwned -> { /* no-op: mesma regra da fonte, titulo nao possuido nao equipa */ }
        }
    }

    fun startSession(config: FocusSessionConfig, durationMinutes: Int) {
        if (_focusSessionState.value.isRunning) return
        focusTickJob?.cancel()

        val totalSeconds = durationMinutes * 60
        focusEndTimeMillis = clock() + totalSeconds * 1000L

        _focusSessionState.value = FocusSessionState(
            isRunning = true,
            isPaused = false,
            isFocusCompleted = false,
            timeLeft = totalSeconds,
            totalSeconds = totalSeconds,
            pauseCount = 0,
            config = config,
            durationMinutes = durationMinutes,
            pendingRewardsCalculation = null
        )

        viewModelScope.launch {
            focusSessionRepository.saveSession(
                PersistedFocusSession(
                    config = config,
                    durationMinutes = durationMinutes,
                    endTimeMillis = focusEndTimeMillis,
                    pendingCalculation = null
                )
            )
        }

        startFocusTickJob()
    }

    fun togglePauseQuest() {
        val current = _focusSessionState.value
        if (!current.isRunning) return

        if (!current.isPaused) {
            // Pausando
            focusTickJob?.cancel()
            _focusSessionState.value = current.copy(
                isPaused = true,
                pauseCount = current.pauseCount + 1
            )
            viewModelScope.launch {
                focusSessionRepository.clearSession()
            }
        } else {
            // Retomando
            focusEndTimeMillis = clock() + current.timeLeft * 1000L
            _focusSessionState.value = current.copy(isPaused = false)
            val config = current.config
            val durationMinutes = current.durationMinutes
            if (config != null) {
                viewModelScope.launch {
                    focusSessionRepository.saveSession(
                        PersistedFocusSession(
                            config = config,
                            durationMinutes = durationMinutes,
                            endTimeMillis = focusEndTimeMillis,
                            pendingCalculation = null
                        )
                    )
                }
            }
            startFocusTickJob()
        }
    }

    fun cancelSession() {
        focusTickJob?.cancel()
        _focusSessionState.value = FocusSessionState()
        viewModelScope.launch {
            focusSessionRepository.clearSession()
        }
    }

    private fun startFocusTickJob() {
        focusTickJob?.cancel()
        focusTickJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val remaining = max(0, ((focusEndTimeMillis - clock()) / 1000.0).roundToInt())
                _focusSessionState.value = _focusSessionState.value.copy(timeLeft = remaining)
                if (remaining <= 0) {
                    onFocusSessionCompleted()
                    return@launch
                }
            }
        }
    }

    private fun onFocusSessionCompleted() {
        val current = _focusSessionState.value
        val config = current.config ?: return
        val durationMins = current.durationMinutes
        val charState = _characterState.value ?: return

        val calc = FocusRewardsLogic.calculate(
            state = charState,
            config = config,
            studiedMinutes = durationMins
        )

        _focusSessionState.value = current.copy(
            isRunning = false,
            isPaused = false,
            isFocusCompleted = true,
            timeLeft = 0,
            pendingRewardsCalculation = calc
        )

        viewModelScope.launch {
            focusSessionRepository.saveSession(
                PersistedFocusSession(
                    config = config,
                    durationMinutes = durationMins,
                    endTimeMillis = focusEndTimeMillis,
                    pendingCalculation = calc
                )
            )
        }
    }
}
