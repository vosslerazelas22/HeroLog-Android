package com.iurispraecepta.herolog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
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
class FocusSessionConfirmTest {

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
    fun confirmFocusSession_withPendingCalculation_appliesChangesResetsFocusStateAndClearsRepository() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val initialChar = viewModel.characterState.value!!

        // Inicia e conclui naturalmente a sessão (10 minutos)
        viewModel.startSession(defaultConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()

        val completedFocusState = viewModel.focusSessionState.value
        assertTrue(completedFocusState.isFocusCompleted)
        assertNotNull(completedFocusState.pendingRewardsCalculation)
        assertNotNull(focusRepository.getSession())

        // Confirma a sessão
        viewModel.confirmFocusSession(editedNotes = "Nota de teste", selectedTag = "Kotlin")
        testDispatcher.scheduler.runCurrent()

        // 1. CharacterState atualizado e persistido
        val updatedChar = viewModel.characterState.value!!
        val persistedChar = repository.getCharacterState()!!
        assertTrue(updatedChar.gold > initialChar.gold)
        assertTrue(updatedChar.totalXP > initialChar.totalXP)
        assertEquals(updatedChar, persistedChar)
        assertEquals(1, updatedChar.history.size)
        assertEquals("Nota de teste", updatedChar.history.first().notes)
        assertEquals("Kotlin", updatedChar.history.first().subskillTag)

        // 2. FocusSessionState resetado para o padrão
        assertEquals(FocusSessionState(), viewModel.focusSessionState.value)

        // 3. FocusSessionRepository limpo
        assertNull(focusRepository.getSession())

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun confirmFocusSession_withoutPendingCalculation_isNoOp() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val initialChar = viewModel.characterState.value!!
        val initialFocus = viewModel.focusSessionState.value

        // Inicia sessão mas não conclui
        viewModel.startSession(defaultConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        // Tenta confirmar sem pendingCalculation
        viewModel.confirmFocusSession(editedNotes = "Notas", selectedTag = "")
        testDispatcher.scheduler.runCurrent()

        // Estado do personagem e estado da sessão não mudaram pela confirmação
        val finalChar = viewModel.characterState.value!!
        assertEquals(initialChar.gold, finalChar.gold)
        assertEquals(initialChar.totalXP, finalChar.totalXP)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun confirmFocusSession_dungeonMode2Sessions_incrementsDungeonSessionsProgressTo3() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val dungeonConfig = FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 2
        )

        viewModel.startSession(dungeonConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.confirmFocusSession(editedNotes = "", selectedTag = "")
        testDispatcher.scheduler.runCurrent()

        assertEquals(3, viewModel.dungeonSessionsProgress.value)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun confirmFocusSession_dungeonMode3Sessions_resetsDungeonSessionsProgressTo0() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(
            repository,
            focusRepository,
            clock = { testDispatcher.scheduler.currentTime }
        )
        testDispatcher.scheduler.runCurrent()

        val dungeonConfig = FocusSessionConfig(
            selectedSkillIdx = 0,
            isWildernessChecked = false,
            isDungeonMode = true,
            dungeonSessions = 3
        )

        viewModel.startSession(dungeonConfig, durationMinutes = 10)
        testDispatcher.scheduler.runCurrent()

        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.confirmFocusSession(editedNotes = "", selectedTag = "")
        testDispatcher.scheduler.runCurrent()

        // 3 + 1 = 4 -> reseta para 0
        assertEquals(0, viewModel.dungeonSessionsProgress.value)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun confirmFocusSession_nonDungeonMode_doesNotChangeDungeonSessionsProgress() = runTest {
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

        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()

        viewModel.confirmFocusSession(editedNotes = "", selectedTag = "")
        testDispatcher.scheduler.runCurrent()

        assertEquals(0, viewModel.dungeonSessionsProgress.value)

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }
}
