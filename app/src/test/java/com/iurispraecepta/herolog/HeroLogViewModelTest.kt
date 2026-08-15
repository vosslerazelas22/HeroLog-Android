package com.iurispraecepta.herolog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
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
import org.junit.Assert.assertTrue
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
    fun viewModel_createsAndPersistsInitialState_whenNoneExists() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.characterState.value
        assertEquals(200, state?.gold)
        assertEquals("Aventureiro do Foco", state?.charName)

        // Confirma que persistiu de verdade - novo ViewModel no mesmo banco nao recria, so recarrega
        val secondViewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(state, secondViewModel.characterState.value)

        db.close()
    }

    @Test
    fun viewModel_unequipItem_movesItemFromEquipmentToInventory() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val equippedItem = com.iurispraecepta.herolog.model.InventoryItem(
            "eq1", "Espada", "⚔️", com.iurispraecepta.herolog.model.BuffType.UnwaveringSword, 100, "desc", isEquipment = true
        )
        val stateWithEquipment = createBaseState().copy(equippedEquipment = listOf(equippedItem, null, null))
        viewModel.saveCharacterState(stateWithEquipment)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.unequipItem(0)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.characterState.value
        assertNull(result?.equippedEquipment?.get(0))
        assertTrue(result?.inventory?.contains(equippedItem) == true)

        db.close()
    }

    @Test
    fun viewModel_sellItem_addsGoldToState_regressionForPreviousLoggingOnlyBug() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val item = com.iurispraecepta.herolog.model.InventoryItem(
            "sell1", "Relíquia", "🔮", com.iurispraecepta.herolog.model.BuffType.ArcaneRelic, 100, "desc", isEquipment = false
        )
        val stateWithItem = createBaseState().copy(gold = 100, inventory = listOf(item))
        viewModel.saveCharacterState(stateWithItem)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.sellItem(item)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.characterState.value
        assertEquals(150, result?.gold) // 100 + 50 (nao-equipamento vende por 50 fixo)
        assertTrue(result?.inventory?.isEmpty() == true)

        db.close()
    }

    @Test
    fun viewModel_savesAndReloadsState_persistsAcrossNewViewModelInstance() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = createBaseState().copy(charName = "Aethelgard", gold = 500)
        viewModel.saveCharacterState(state)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(state, viewModel.characterState.value)

        // Novo ViewModel, mesmo banco - prova persistência real, não só estado em memória do primeiro objeto
        val secondViewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(state, secondViewModel.characterState.value)
        db.close()
    }
}
