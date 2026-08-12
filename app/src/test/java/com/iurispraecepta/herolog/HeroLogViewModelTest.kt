package com.iurispraecepta.herolog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.ui.HeroLogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HeroLogViewModelTest {

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

    private fun createBaseState(): CharacterState = CharacterState(
        gold = 0,
        totalXP = 0,
        totalGoldEarned = 0,
        totalSessions = 0,
        totalMinutes = 0,
        combatLevel = 1,
        combatXP = 0,
        skills = emptyList(),
        history = emptyList(),
        inventory = emptyList(),
        streak = 0,
        bestStreak = 0,
        lastStudyDate = null,
        wildernessWins = 0,
        combo = 0,
        dungeonProgress = 0,
        isDungeonMode = false,
        dungeonSessions = 0,
        achievements = emptyList(),
        charName = "Hero",
        charClass = CharClass.Warrior,
        todayXP = 0,
        todayMinutes = 0,
        todayDate = "Wed Aug 11 2026",
        hasClaimedLogin = false,
        hp = 100,
        maxHp = 100,
        habits = emptyList(),
        dailies = emptyList(),
        todos = emptyList(),
        pomodoroSettings = PomodoroSettings(25, 5, 15, false, false)
    )

    @Test
    fun viewModel_loadsNullInitially_whenNoSavedState() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val viewModel = HeroLogViewModel(repository)

        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.characterState.value)
        db.close()
    }

    @Test
    fun viewModel_savesAndReloadsState_persistsAcrossNewViewModelInstance() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val viewModel = HeroLogViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = createBaseState().copy(charName = "Aethelgard", gold = 500)
        viewModel.saveCharacterState(state)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(state, viewModel.characterState.value)

        // Novo ViewModel, mesmo banco - prova persistência real, não só estado em memória do primeiro objeto
        val secondViewModel = HeroLogViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(state, secondViewModel.characterState.value)
        db.close()
    }
}
