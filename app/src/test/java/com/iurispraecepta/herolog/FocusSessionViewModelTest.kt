package com.iurispraecepta.herolog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.FocusSessionState
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
        var fakeNow = 1000000L
        val viewModel = HeroLogViewModel(repository, clock = { fakeNow })
        testDispatcher.scheduler.advanceUntilIdle()

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

        db.close()
    }

    @Test
    fun sessionNaturalCompletion_calculatesPendingRewards_andDoesNotAlterCharacterState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        var fakeNow = 1000000L
        val viewModel = HeroLogViewModel(repository, clock = { fakeNow })
        testDispatcher.scheduler.advanceUntilIdle()

        val initialCharState = viewModel.characterState.value!!

        viewModel.startSession(defaultConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        repeat(600) {
            fakeNow += 1000L
            testDispatcher.scheduler.advanceTimeBy(1000L)
            testDispatcher.scheduler.runCurrent()
        }

        val focusState = viewModel.focusSessionState.value
        assertFalse(focusState.isRunning)
        assertFalse(focusState.isPaused)
        assertTrue(focusState.isFocusCompleted)
        assertEquals(0, focusState.timeLeft)

        assertNotNull(focusState.pendingRewardsCalculation)
        assertEquals(0, focusState.pendingRewardsCalculation?.skillIdx)
        assertEquals(10, focusState.pendingRewardsCalculation?.durationMins)

        val finalCharState = viewModel.characterState.value!!
        assertEquals(initialCharState.gold, finalCharState.gold)
        assertEquals(initialCharState.totalXP, finalCharState.totalXP)
        assertEquals(initialCharState.inventory, finalCharState.inventory)

        db.close()
    }

    @Test
    fun pauseSession_freezesTimeLeft_evenIfClockAdvances() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        var fakeNow = 1000000L
        val viewModel = HeroLogViewModel(repository, clock = { fakeNow })
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.startSession(defaultConfig, durationMinutes = 10) // 600s
        testDispatcher.scheduler.runCurrent()

        repeat(100) {
            fakeNow += 1000L
            testDispatcher.scheduler.advanceTimeBy(1000L)
            testDispatcher.scheduler.runCurrent()
        }

        assertEquals(500, viewModel.focusSessionState.value.timeLeft)

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()

        val pausedState = viewModel.focusSessionState.value
        assertTrue(pausedState.isRunning)
        assertTrue(pausedState.isPaused)
        assertEquals(1, pausedState.pauseCount)
        assertEquals(500, pausedState.timeLeft)

        repeat(200) {
            fakeNow += 1000L
            testDispatcher.scheduler.advanceTimeBy(1000L)
            testDispatcher.scheduler.runCurrent()
        }

        assertEquals(500, viewModel.focusSessionState.value.timeLeft)

        db.close()
    }

    @Test
    fun resumeSessionAfterPause_completesCorrectlyForRemainingTime() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        var fakeNow = 1000000L
        val viewModel = HeroLogViewModel(repository, clock = { fakeNow })
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.startSession(defaultConfig, durationMinutes = 5) // 300s
        testDispatcher.scheduler.runCurrent()

        repeat(100) {
            fakeNow += 1000L
            testDispatcher.scheduler.advanceTimeBy(1000L)
            testDispatcher.scheduler.runCurrent()
        }
        assertEquals(200, viewModel.focusSessionState.value.timeLeft)

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, viewModel.focusSessionState.value.pauseCount)

        fakeNow += 500000L
        testDispatcher.scheduler.advanceTimeBy(500000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()

        assertFalse(viewModel.focusSessionState.value.isPaused)
        assertEquals(1, viewModel.focusSessionState.value.pauseCount)

        repeat(200) {
            fakeNow += 1000L
            testDispatcher.scheduler.advanceTimeBy(1000L)
            testDispatcher.scheduler.runCurrent()
        }

        val finalState = viewModel.focusSessionState.value
        assertTrue(finalState.isFocusCompleted)
        assertFalse(finalState.isRunning)
        assertNotNull(finalState.pendingRewardsCalculation)

        db.close()
    }

    @Test
    fun pauseCount_incrementsOnlyOnPauseNotOnResume() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        var fakeNow = 1000000L
        val viewModel = HeroLogViewModel(repository, clock = { fakeNow })
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.startSession(defaultConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()
        assertEquals(0, viewModel.focusSessionState.value.pauseCount)

        viewModel.togglePauseQuest()
        assertEquals(1, viewModel.focusSessionState.value.pauseCount)

        viewModel.togglePauseQuest()
        assertEquals(1, viewModel.focusSessionState.value.pauseCount)

        viewModel.togglePauseQuest()
        assertEquals(2, viewModel.focusSessionState.value.pauseCount)

        db.close()
    }

    @Test
    fun cancelSession_resetsToDefaultFocusSessionState_andDoesNotAlterCharacterState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        var fakeNow = 1000000L
        val viewModel = HeroLogViewModel(repository, clock = { fakeNow })
        testDispatcher.scheduler.advanceUntilIdle()

        val initialCharState = viewModel.characterState.value!!

        viewModel.startSession(defaultConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        repeat(50) {
            fakeNow += 1000L
            testDispatcher.scheduler.advanceTimeBy(1000L)
            testDispatcher.scheduler.runCurrent()
        }

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
        val viewModel = HeroLogViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialFocusState = viewModel.focusSessionState.value
        assertFalse(initialFocusState.isRunning)

        viewModel.togglePauseQuest()
        testDispatcher.scheduler.runCurrent()

        assertEquals(initialFocusState, viewModel.focusSessionState.value)

        db.close()
    }
}
