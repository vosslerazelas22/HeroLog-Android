package com.iurispraecepta.herolog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.FocusSessionState
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import com.iurispraecepta.herolog.ui.HeroLogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusSessionRecoveryTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createInMemoryDatabase(): HeroLogDatabase {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return Room.inMemoryDatabaseBuilder(context, HeroLogDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
    }

    private val defaultConfig = FocusSessionConfig(
        selectedSkillIdx = 0,
        isWildernessChecked = false,
        isDungeonMode = false,
        dungeonSessions = 0
    )

    @Test
    fun recoverFocusSession_whenNoSessionPersisted_initializesWithDefaultState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())

        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.focusSessionState.value
        assertEquals(FocusSessionState(), state)
        assertFalse(state.isRunning)
        assertFalse(state.isFocusCompleted)
        assertNull(state.pendingRewardsCalculation)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun recoverFocusSession_state1_inProgress_resumesTimerWithCorrectRemainingTime() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())

        // Primeiro ViewModel inicia a sessão e simula fechamento abrupto
        val viewModel1 = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        viewModel1.startSession(defaultConfig, durationMinutes = 20) // 1200s
        testDispatcher.scheduler.runCurrent()

        // Avança 300 segundos (5 min) sem cancelar a sessão no VM1 (simula fechamento abrupto)
        testDispatcher.scheduler.advanceTimeBy(300_000L)
        testDispatcher.scheduler.runCurrent()

        // Segundo ViewModel reabre o app no mesmo banco com tempo avançado
        val viewModel2 = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val state2 = viewModel2.focusSessionState.value
        assertTrue(state2.isRunning)
        assertFalse(state2.isPaused)
        assertFalse(state2.isFocusCompleted)
        assertEquals(900, state2.timeLeft) // 1200 - 300 = 900s restantes
        assertEquals(20, state2.durationMinutes)
        assertEquals(defaultConfig, state2.config)

        viewModel2.cancelSession()
        viewModel1.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun recoverFocusSession_state2_expiredWithoutCalculation_calculatesOnceAndDoesNotApplyToCharacterState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())

        val viewModel1 = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val initialCharState = viewModel1.characterState.value!!

        viewModel1.startSession(defaultConfig, durationMinutes = 10) // 600s
        testDispatcher.scheduler.runCurrent()

        // Avança o tempo além do tempo limite (700s > 600s) ANTES de criar o segundo ViewModel
        testDispatcher.scheduler.advanceTimeBy(700_000L)
        testDispatcher.scheduler.runCurrent()

        // Segundo ViewModel reabre o app
        val viewModel2 = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val state2 = viewModel2.focusSessionState.value
        assertFalse(state2.isRunning)
        assertFalse(state2.isPaused)
        assertTrue(state2.isFocusCompleted)
        assertEquals(0, state2.timeLeft)
        assertNotNull(state2.pendingRewardsCalculation)

        // Prova de que NÃO aplicou automaticamente as recompensas ao personagem
        val finalCharState = viewModel2.characterState.value!!
        assertEquals(initialCharState.gold, finalCharState.gold)
        assertEquals(initialCharState.totalXP, finalCharState.totalXP)
        assertEquals(initialCharState.inventory, finalCharState.inventory)

        // Confirma que pendingCalculation foi persistido pelo onFocusSessionCompleted() chamado na recuperação
        val persisted = focusRepository.getSession()
        assertNotNull(persisted)
        assertNotNull(persisted?.pendingCalculation)
        assertEquals(state2.pendingRewardsCalculation, persisted?.pendingCalculation)

        viewModel2.cancelSession()
        viewModel1.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun recoverFocusSession_state3_alreadyCalculated_reloadsPersistedCalculationWithoutRecalculating() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())

        // Monta diretamente a sessão persistida com um valor não-natural de teste
        val dummyCalc = FocusRewardsCalculation(
            skillIdx = 0,
            skillName = "Arcane Logic",
            xpEarned = 999999,
            goldEarned = 888888,
            durationMins = 15,
            dungeonClearGoldBonus = 0,
            hasUsedDoubleLoot = false,
            hasUsedFocusElixir = false,
            hasUsedRuneFortune = false,
            hasUsedCrystalClarity = false,
            usedEquipmentIndicesAndCharges = emptyList(),
            lootedItems = emptyList(),
            droppedTitle = null,
            isWildernessChecked = false,
            isDungeonMode = false,
            comboBonusPercent = 0
        )
        val persistedSession = PersistedFocusSession(
            config = defaultConfig,
            durationMinutes = 15,
            endTimeMillis = testDispatcher.scheduler.currentTime - 1000L,
            pendingCalculation = dummyCalc
        )
        focusRepository.saveSession(persistedSession)

        // ViewModel inicializa lendo esse estado persistido
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.focusSessionState.value
        assertFalse(state.isRunning)
        assertFalse(state.isPaused)
        assertTrue(state.isFocusCompleted)
        assertEquals(0, state.timeLeft)
        assertNotNull(state.pendingRewardsCalculation)
        assertEquals(dummyCalc, state.pendingRewardsCalculation)
        assertEquals(999999, state.pendingRewardsCalculation?.xpEarned)
        assertEquals(888888, state.pendingRewardsCalculation?.goldEarned)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }
}
