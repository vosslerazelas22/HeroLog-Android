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
import com.iurispraecepta.herolog.logic.SkillOperationResult
import com.iurispraecepta.herolog.logic.SkillError
import com.iurispraecepta.herolog.logic.DeleteSkillEligibility
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

    @Test
    fun viewModel_addCustomSkill_success_persistsSkills() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val baseState = createBaseState().copy(skills = emptyList())
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.addCustomSkill("Android Dev", "🤖")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result is SkillOperationResult.Success)
        val finalSkills = viewModel.characterState.value?.skills ?: emptyList()
        assertEquals(1, finalSkills.size)
        assertEquals("Android Dev", finalSkills[0].name)
        assertEquals("🤖", finalSkills[0].emoji)

        db.close()
    }

    @Test
    fun viewModel_addCustomSkill_validationError_doesNotPersist() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val baseState = createBaseState().copy(skills = listOf(
            com.iurispraecepta.herolog.model.Skill(id = "s1", name = "Android Dev", level = 1, xp = 0, emoji = "🤖")
        ))
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Duplicate
        val duplicateResult = viewModel.addCustomSkill("Android Dev", "🤖")
        assertTrue(duplicateResult is SkillOperationResult.Error)
        assertEquals(SkillError.DuplicateName, (duplicateResult as SkillOperationResult.Error).reason)

        // 2. Empty/Blank
        val blankResult = viewModel.addCustomSkill("   ", "🤖")
        assertTrue(blankResult is SkillOperationResult.Error)
        assertEquals(SkillError.BlankName, (blankResult as SkillOperationResult.Error).reason)

        testDispatcher.scheduler.advanceUntilIdle()
        // verify skills hasn't changed from original size 1
        assertEquals(1, viewModel.characterState.value?.skills?.size)

        db.close()
    }

    @Test
    fun viewModel_deleteSkill_blockedDuringActiveFocusSession() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.runCurrent()

        val skills = listOf(
            com.iurispraecepta.herolog.model.Skill(id = "s1", name = "Android Dev", level = 1, xp = 0, emoji = "🤖"),
            com.iurispraecepta.herolog.model.Skill(id = "s2", name = "Kotlin", level = 1, xp = 0, emoji = "☕")
        )
        val baseState = createBaseState().copy(skills = skills)
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.runCurrent()

        // Simulate focus session running
        val config = com.iurispraecepta.herolog.logic.focus.FocusSessionConfig(0, false, false, 0)
        viewModel.startSession(config, 25)
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.focusSessionState.value.isRunning)

        // Try to delete skill while running
        val eligibility = viewModel.deleteSkill(0)
        testDispatcher.scheduler.runCurrent()

        assertEquals(DeleteSkillEligibility.Blocked, eligibility)
        assertEquals(2, viewModel.characterState.value?.skills?.size) // Still 2

        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()
        db.close()
    }

    @Test
    fun viewModel_deleteSkill_eligibleAndDeletesSuccessfully() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val skills = listOf(
            com.iurispraecepta.herolog.model.Skill(id = "s1", name = "Android Dev", level = 1, xp = 0, emoji = "🤖"),
            com.iurispraecepta.herolog.model.Skill(id = "s2", name = "Kotlin", level = 1, xp = 0, emoji = "☕")
        )
        val baseState = createBaseState().copy(skills = skills)
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.advanceUntilIdle()

        val eligibility = viewModel.deleteSkill(0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DeleteSkillEligibility.Eligible, eligibility)
        val finalSkills = viewModel.characterState.value?.skills ?: emptyList()
        assertEquals(1, finalSkills.size)
        assertEquals("Kotlin", finalSkills[0].name)

        db.close()
    }

    @Test
    fun viewModel_renameAndTagOperations_persistsCorrectly() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val skills = listOf(
            com.iurispraecepta.herolog.model.Skill(id = "s1", name = "Android Dev", level = 1, xp = 0, emoji = "🤖", tags = listOf("OriginalTag"))
        )
        val baseState = createBaseState().copy(skills = skills)
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Rename Skill
        val renameResult = viewModel.renameSkill(0, "Modern Android")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(renameResult is SkillOperationResult.Success)
        assertEquals("Modern Android", viewModel.characterState.value?.skills?.get(0)?.name)

        // 2. Add Tag
        viewModel.addTagToSkill(0, "Compose")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf("OriginalTag", "Compose"), viewModel.characterState.value?.skills?.get(0)?.tags)

        // 3. Remove Tag
        viewModel.removeTagFromSkill(0, 0) // removes OriginalTag
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf("Compose"), viewModel.characterState.value?.skills?.get(0)?.tags)

        db.close()
    }

    @Test
    fun viewModel_prestigeSkill_resetsXpAndLevel_increasesPrestigeCounter() = runTest {
        val db = createInMemoryDatabase()
        val repository = CharacterRepository(db.characterStateDao())
        val focusRepository = FocusSessionRepository(db.activeFocusSessionDao())
        val viewModel = HeroLogViewModel(repository, focusRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val maxedSkill = com.iurispraecepta.herolog.model.Skill(id = "s1", name = "Android Dev", level = 99, xp = 500, emoji = "🤖", prestige = 2)
        val baseState = createBaseState().copy(skills = listOf(maxedSkill))
        viewModel.saveCharacterState(baseState)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.prestigeSkill(0)
        testDispatcher.scheduler.advanceUntilIdle()

        val upgraded = viewModel.characterState.value?.skills?.get(0)
        assertEquals(1, upgraded?.level)
        assertEquals(0, upgraded?.xp)
        assertEquals(3, upgraded?.prestige)

        db.close()
    }
}
