package com.iurispraecepta.herolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iurispraecepta.herolog.data.createInitialCharacterState
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.logic.EquipTitleResult
import com.iurispraecepta.herolog.logic.InventoryLogic
import com.iurispraecepta.herolog.logic.TitleLogic
import com.iurispraecepta.herolog.logic.SkillLogic
import com.iurispraecepta.herolog.logic.SkillOperationResult
import com.iurispraecepta.herolog.logic.SkillError
import com.iurispraecepta.herolog.logic.DeleteSkillEligibility
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.logic.focus.FocusApplyLogic
import com.iurispraecepta.herolog.logic.focus.FocusRewardsLogic
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.FocusSessionState
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import com.iurispraecepta.herolog.logic.focus.WILDERNESS_GRACE_PERIOD_SECONDS
import com.iurispraecepta.herolog.logic.focus.WildernessInfractionOutcome
import com.iurispraecepta.herolog.logic.focus.resolveWildernessInfraction
import com.iurispraecepta.herolog.logic.focus.resolveCognitiveDeath
import com.iurispraecepta.herolog.logic.focus.resolveRespawn
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.InventoryItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.ceil
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

    private val _dungeonSessionsProgress = MutableStateFlow(0)
    val dungeonSessionsProgress: StateFlow<Int> = _dungeonSessionsProgress.asStateFlow()

    private var focusTickJob: Job? = null
    private var focusEndTimeMillis: Long = 0L
    private var graceTickJob: Job? = null
    private var graceEndTimeMillis: Long = 0L

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
            recoverFocusSession()
        }
    }

    private suspend fun recoverFocusSession() {
        val persisted = focusSessionRepository.getSession() ?: return

        if (persisted.pendingCalculation != null) {
            // Estado 3: já calculated, só recarrega, NUNCA recalcula.
            _focusSessionState.value = FocusSessionState(
                isRunning = false,
                isPaused = false,
                isFocusCompleted = true,
                timeLeft = 0,
                totalSeconds = persisted.durationMinutes * 60,
                config = persisted.config,
                durationMinutes = persisted.durationMinutes,
                pendingRewardsCalculation = persisted.pendingCalculation
            )
            return
        }

        val remaining = max(0, ((persisted.endTimeMillis - clock()) / 1000.0).roundToInt())

        if (remaining <= 0) {
            // Estado 2: expirou enquanto o app estava fechado. Calcula UMA VEZ agora.
            focusEndTimeMillis = persisted.endTimeMillis
            _focusSessionState.value = FocusSessionState(
                isRunning = false,
                isPaused = false,
                isFocusCompleted = false,
                timeLeft = 0,
                totalSeconds = persisted.durationMinutes * 60,
                config = persisted.config,
                durationMinutes = persisted.durationMinutes
            )
            onFocusSessionCompleted() // já persiste o resultado calculado (Bloco 31)
        } else {
            // Estado 1: ainda em andamento. Retoma o timer normalmente.
            focusEndTimeMillis = persisted.endTimeMillis
            _focusSessionState.value = FocusSessionState(
                isRunning = true,
                isPaused = false,
                isFocusCompleted = false,
                timeLeft = remaining,
                totalSeconds = persisted.durationMinutes * 60,
                pauseCount = 0,
                config = persisted.config,
                durationMinutes = persisted.durationMinutes
            )
            startFocusTickJob()
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

    fun addCustomSkill(nameInput: String, emoji: String): SkillOperationResult {
        val current = _characterState.value ?: return SkillOperationResult.Error(SkillError.InvalidIndex)
        val result = SkillLogic.addCustomSkill(current.skills, nameInput, emoji)
        if (result is SkillOperationResult.Success) {
            saveCharacterState(current.copy(skills = result.newSkills))
        }
        return result
    }

    fun addTagToSkill(skillIdx: Int, newTag: String) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(skills = SkillLogic.addTagToSkill(current.skills, skillIdx, newTag)))
    }

    fun removeTagFromSkill(skillIdx: Int, tagIdx: Int) {
        val current = _characterState.value ?: return
        saveCharacterState(current.copy(skills = SkillLogic.removeTagFromSkill(current.skills, skillIdx, tagIdx)))
    }

    fun renameSkill(idx: Int, newName: String): SkillOperationResult {
        val current = _characterState.value ?: return SkillOperationResult.Error(SkillError.InvalidIndex)
        val result = SkillLogic.renameSkill(current.skills, idx, newName)
        if (result is SkillOperationResult.Success) {
            saveCharacterState(current.copy(skills = result.newSkills))
        }
        return result
    }

    fun deleteSkill(idx: Int): DeleteSkillEligibility {
        val current = _characterState.value ?: return DeleteSkillEligibility.Blocked
        val isFocusSessionRunning = _focusSessionState.value.isRunning
        val eligibility = SkillLogic.canDeleteSkill(current.skills, isFocusSessionRunning)
        if (eligibility == DeleteSkillEligibility.Eligible) {
            saveCharacterState(current.copy(skills = SkillLogic.deleteSkillAt(current.skills, idx)))
        }
        return eligibility
    }

    fun prestigeSkill(idx: Int) {
        val current = _characterState.value ?: return
        val skill = current.skills.getOrNull(idx) ?: return
        if (SkillLogic.isPrestigeEligible(skill)) {
            val updated = SkillLogic.applyPrestige(skill)
            saveCharacterState(current.copy(skills = current.skills.toMutableList().apply { this[idx] = updated }))
        }
    }

    fun startSession(config: FocusSessionConfig, durationMinutes: Int) {
        if (_focusSessionState.value.isRunning) return
        focusTickJob?.cancel()
        graceTickJob?.cancel()

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
        graceTickJob?.cancel()
        _focusSessionState.value = FocusSessionState()
        viewModelScope.launch {
            focusSessionRepository.clearSession()
        }
    }

    fun onAppBackgrounded() {
        val current = _focusSessionState.value
        if (!current.isRunning || current.isPaused || current.config?.isWildernessChecked != true ||
            current.isGraceActive || current.isPlayerDead) return

        val equippedTitleId = _characterState.value?.equippedTitle
        when (resolveWildernessInfraction(equippedTitleId)) {
            WildernessInfractionOutcome.CONVERTED_TO_PAUSE -> {
                if (!_focusSessionState.value.isPaused) {
                    togglePauseQuest()
                }
            }
            WildernessInfractionOutcome.GRACE_PERIOD_STARTED -> startGracePeriod()
        }
    }

    private fun startGracePeriod() {
        graceEndTimeMillis = clock() + WILDERNESS_GRACE_PERIOD_SECONDS * 1000L
        _focusSessionState.value = _focusSessionState.value.copy(
            isGraceActive = true,
            graceSecondsLeft = WILDERNESS_GRACE_PERIOD_SECONDS
        )
        graceTickJob?.cancel()
        graceTickJob = viewModelScope.launch {
            while (isActive) {
                val remainingMs = graceEndTimeMillis - clock()
                val remainingSec = ceil(remainingMs / 1000.0).toInt().coerceAtLeast(0)
                _focusSessionState.value = _focusSessionState.value.copy(graceSecondsLeft = remainingSec)
                if (remainingMs <= 0) {
                    triggerCognitiveDeath()
                    break
                }
                delay(250)
            }
        }
    }

    private fun triggerCognitiveDeath() {
        graceTickJob?.cancel()
        val charState = _characterState.value ?: return
        val result = resolveCognitiveDeath(charState.charClass, charState.streak)
        saveCharacterState(charState.copy(streak = result.newStreak, combo = result.newCombo))
        focusTickJob?.cancel() // cancelar também o job de contagem da sessão em si
        _focusSessionState.value = _focusSessionState.value.copy(
            isGraceActive = false,
            isPlayerDead = true,
            isRunning = false
        )
        viewModelScope.launch { focusSessionRepository.clearSession() }
    }

    fun onAppForegrounded() {
        if (!_focusSessionState.value.isGraceActive) return
        returnToFocusFromGrace()
    }

    fun returnToFocusFromGrace() {
        graceTickJob?.cancel()
        _focusSessionState.value = _focusSessionState.value.copy(
            isGraceActive = false,
            graceSecondsLeft = WILDERNESS_GRACE_PERIOD_SECONDS
        )
    }

    fun respawnHero() {
        val charState = _characterState.value ?: return
        val result = resolveRespawn(charState.combatLevel, charState.gold)
        saveCharacterState(
            charState.copy(
                combatLevel = result.newCombatLevel,
                gold = result.newGold,
                combatXP = result.newCombatXp
            )
        )
        _focusSessionState.value = FocusSessionState()
    }

    fun confirmFocusSession(editedNotes: String, selectedTag: String) {
        val current = _focusSessionState.value
        val calc = current.pendingRewardsCalculation ?: return
        val charState = _characterState.value ?: return
        val config = current.config

        val newState = FocusApplyLogic.apply(
            state = charState,
            calc = calc,
            editedNotes = editedNotes,
            selectedTag = selectedTag.ifEmpty { null },
            referenceDate = Date(clock())
        )
        saveCharacterState(newState)

        if (config?.isDungeonMode == true) {
            val nextSessions = config.dungeonSessions + 1
            _dungeonSessionsProgress.value = if (nextSessions >= 4) 0 else nextSessions
        }

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
