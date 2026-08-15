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
class FocusSessionViewModelTest {

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
    fun startSession_setsIsRunningTrueAndCorrectTimeLeftAndTotalSeconds() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.startSession(defaultConfig, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.focusSessionState.value
        assertTrue(state.isRunning)
        assertFalse(state.isPaused)
        assertFalse(state.isFocusCompleted)
        assertEquals(1500, state.totalSeconds) // 25 * 60
        assertEquals(1500, state.timeLeft)
        assertEquals(25, state.durationMinutes)
        assertEquals(defaultConfig, state.config)
        assertNull(state.pendingRewardsCalculation)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun sessionNaturalCompletion_calculatesPendingRewards_andDoesNotAlterCharacterState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val initialCharState = viewModel.characterState.value!!

        viewModel.startSession(defaultConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        // Avança 10 minutos (600 segundos = 600.000 ms)
        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()

        val focusState = viewModel.focusSessionState.value
        assertFalse(focusState.isRunning)
        assertFalse(focusState.isPaused)
        assertTrue(focusState.isFocusCompleted)
        assertEquals(0, focusState.timeLeft)

        assertNotNull(focusState.pendingRewardsCalculation)

        val finalCharState = viewModel.characterState.value!!
        assertEquals(initialCharState.gold, finalCharState.gold)
        assertEquals(initialCharState.totalXP, finalCharState.totalXP)
        assertEquals(initialCharState.inventory, finalCharState.inventory)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun pauseSession_freezesTimeLeft_evenIfClockAdvances() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.startSession(defaultConfig, durationMinutes = 10) // 600s
        testDispatcher.scheduler.runCurrent()

        // Avança 100 segundos
        testDispatcher.scheduler.advanceTimeBy(100_000L)
        testDispatcher.scheduler.runCurrent()

        assertEquals(500, viewModel.focusSessionState.value.timeLeft)

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()

        val pausedState = viewModel.focusSessionState.value
        assertTrue(pausedState.isRunning)
        assertTrue(pausedState.isPaused)
        assertEquals(1, pausedState.pauseCount)
        assertEquals(500, pausedState.timeLeft)

        // Avança 200 segundos enquanto pausado
        testDispatcher.scheduler.advanceTimeBy(200_000L)
        testDispatcher.scheduler.runCurrent()

        assertEquals(500, viewModel.focusSessionState.value.timeLeft)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun resumeSessionAfterPause_completesCorrectlyForRemainingTime() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.startSession(defaultConfig, durationMinutes = 5) // 300s
        testDispatcher.scheduler.runCurrent()

        // Avança 100s
        testDispatcher.scheduler.advanceTimeBy(100_000L)
        testDispatcher.scheduler.runCurrent()
        assertEquals(200, viewModel.focusSessionState.value.timeLeft)

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, viewModel.focusSessionState.value.pauseCount)

        // Pausa por 500s
        testDispatcher.scheduler.advanceTimeBy(500_000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.togglePauseQuest() // Resume
        testDispatcher.scheduler.runCurrent()

        assertFalse(viewModel.focusSessionState.value.isPaused)
        assertEquals(1, viewModel.focusSessionState.value.pauseCount)

        // Avança os 200s restantes
        testDispatcher.scheduler.advanceTimeBy(200_000L)
        testDispatcher.scheduler.runCurrent()

        val finalState = viewModel.focusSessionState.value
        assertTrue(finalState.isFocusCompleted)
        assertFalse(finalState.isRunning)
        assertNotNull(finalState.pendingRewardsCalculation)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun pauseCount_incrementsOnlyOnPauseNotOnResume() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.startSession(defaultConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()
        assertEquals(0, viewModel.focusSessionState.value.pauseCount)

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, viewModel.focusSessionState.value.pauseCount)

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, viewModel.focusSessionState.value.pauseCount)

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()
        assertEquals(2, viewModel.focusSessionState.value.pauseCount)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun cancelSession_resetsToDefaultFocusSessionState_andDoesNotAlterCharacterState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val initialCharState = viewModel.characterState.value!!

        viewModel.startSession(defaultConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        testDispatcher.scheduler.advanceTimeBy(50_000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.focusSessionState.value
        assertEquals(FocusSessionState(), state)
        assertNull(state.pendingRewardsCalculation)

        val finalCharState = viewModel.characterState.value!!
        assertEquals(initialCharState, finalCharState)

        db.close()
    }

    @Test
    fun togglePauseQuest_whenNotRunning_isNoOp() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val initialFocusState = viewModel.focusSessionState.value
        assertFalse(initialFocusState.isRunning)

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()

        assertEquals(initialFocusState, viewModel.focusSessionState.value)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun startSession_persistsSessionDirectlyInRepository() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val startTime = testDispatcher.scheduler.currentTime
        viewModel.startSession(defaultConfig, durationMinutes = 25)
        testDispatcher.scheduler.runCurrent()

        val persisted = focusRepository.getSession()
        assertNotNull(persisted)
        assertEquals(defaultConfig, persisted?.config)
        assertEquals(25, persisted?.durationMinutes)
        assertEquals(startTime + 25 * 60 * 1000L, persisted?.endTimeMillis)
        assertNull(persisted?.pendingCalculation)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun pauseSession_clearsSessionInRepository() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.startSession(defaultConfig, durationMinutes = 20)
        testDispatcher.scheduler.runCurrent()

        assertNotNull(focusRepository.getSession())

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()

        val persisted = focusRepository.getSession()
        assertNull(persisted)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun resumeSession_persistsSessionAgainWithRecalculatedEndTime() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val startTime = testDispatcher.scheduler.currentTime
        viewModel.startSession(defaultConfig, durationMinutes = 10) // 600s
        testDispatcher.scheduler.runCurrent()

        val originalPersisted = focusRepository.getSession()
        assertNotNull(originalPersisted)
        val originalEndTime = originalPersisted!!.endTimeMillis
        assertEquals(startTime + 600_000L, originalEndTime)

        // Avança 100s e pausa
        testDispatcher.scheduler.advanceTimeBy(100_000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()
        assertNull(focusRepository.getSession())

        // Passa 500s em pausa e retoma
        testDispatcher.scheduler.advanceTimeBy(500_000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()

        val resumedPersisted = focusRepository.getSession()
        assertNotNull(resumedPersisted)
        assertEquals(defaultConfig, resumedPersisted?.config)
        assertEquals(10, resumedPersisted?.durationMinutes)
        assertNull(resumedPersisted?.pendingCalculation)
        assertTrue(resumedPersisted!!.endTimeMillis != originalEndTime)
        // Novo end time deve ser o tempo atual (startTime + 100s + 500s) + 500s restantes
        assertEquals(startTime + 600_000L + 500_000L, resumedPersisted.endTimeMillis)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun cancelSession_clearsSessionInRepository() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.startSession(defaultConfig, durationMinutes = 15)
        testDispatcher.scheduler.runCurrent()

        assertNotNull(focusRepository.getSession())

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()

        val persisted = focusRepository.getSession()
        assertNull(persisted)

        db.close()
    }

    @Test
    fun sessionNaturalCompletion_persistsSessionWithPendingCalculation() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.startSession(defaultConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        // Avança 10 minutos (600s)
        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()

        val persisted = focusRepository.getSession()
        assertNotNull(persisted)
        assertNotNull(persisted?.pendingCalculation)

        val memoryState = viewModel.focusSessionState.value
        assertEquals(memoryState.pendingRewardsCalculation, persisted?.pendingCalculation)
        assertEquals(0, persisted?.pendingCalculation?.skillIdx)
        assertEquals(10, persisted?.pendingCalculation?.durationMins)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }
}
