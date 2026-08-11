package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.dao.CharacterStateDao
import com.iurispraecepta.herolog.data.entity.CharacterStateEntity
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.ChecklistItem
import com.iurispraecepta.herolog.model.Daily
import com.iurispraecepta.herolog.model.Difficulty
import com.iurispraecepta.herolog.model.Habit
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.model.RepeatInterval
import com.iurispraecepta.herolog.model.Todo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FakeCharacterStateDao : CharacterStateDao {
    private var currentEntity: CharacterStateEntity? = null

    override suspend fun getState(): CharacterStateEntity? = currentEntity

    override suspend fun saveState(entity: CharacterStateEntity) {
        currentEntity = entity
    }

    override suspend fun clearState() {
        currentEntity = null
    }
}

class CharacterRepositoryTest {

    @Test
    fun repository_saves_and_retrieves_characterState_correctly() = runTest {
        val dao = FakeCharacterStateDao()
        val repository = CharacterRepository(dao)

        assertNull(repository.getCharacterState())

        val sampleItem = InventoryItem(
            id = "item_1",
            name = "Elixir da Foco",
            emoji = "🧪",
            buff = BuffType.FocusElixir,
            price = 50,
            desc = "Aumenta o ganho de XP",
            isEquipment = true,
            charges = 3,
            maxCharges = 3,
            rarity = Rarity.Especial
        )

        val sampleState = CharacterState(
            gold = 150,
            totalXP = 1200,
            totalGoldEarned = 500,
            totalSessions = 10,
            totalMinutes = 250,
            combatLevel = 3,
            combatXP = 450,
            skills = emptyList(),
            history = emptyList(),
            inventory = listOf(sampleItem),
            streak = 5,
            bestStreak = 7,
            lastStudyDate = "2026-07-29",
            wildernessWins = 2,
            combo = 1,
            dungeonProgress = 4,
            isDungeonMode = false,
            dungeonSessions = 2,
            achievements = listOf("first_blood"),
            charName = "Arthur",
            charClass = CharClass.Warrior,
            todayXP = 100,
            todayMinutes = 50,
            todayDate = "2026-07-29",
            hasClaimedLogin = true,
            hp = 100,
            maxHp = 100,
            habits = listOf(
                Habit(
                    id = "h1",
                    title = "Beber água",
                    notes = "2L por dia",
                    up = true,
                    down = false,
                    difficulty = Difficulty.Easy,
                    upCount = 5,
                    downCount = 0,
                    streak = 3,
                    tags = listOf("saude")
                )
            ),
            dailies = listOf(
                Daily(
                    id = "d1",
                    title = "Estudar Kotlin",
                    notes = "Bloco de treino",
                    difficulty = Difficulty.Medium,
                    completed = false,
                    streak = 2,
                    repeats = RepeatInterval.Daily,
                    every = 1,
                    tags = listOf("dev"),
                    checklist = listOf(ChecklistItem("c1", "Ler docs", true))
                )
            ),
            todos = listOf(
                Todo(
                    id = "t1",
                    title = "Configurar Room",
                    notes = "Bloco 1",
                    difficulty = Difficulty.Hard,
                    completed = true,
                    tags = listOf("herolog"),
                    checklist = emptyList()
                )
            ),
            equippedTitle = "The Novice",
            ownedTitles = listOf("The Novice"),
            equippedEquipment = listOf(sampleItem, null),
            pomodoroSettings = PomodoroSettings(
                focusDuration = 25,
                shortBreakDuration = 5,
                longBreakDuration = 15,
                autoStartBreak = true,
                autoStartFocus = false
            )
        )

        repository.saveCharacterState(sampleState)

        val retrievedState = repository.getCharacterState()
        assertNotNull(retrievedState)
        assertEquals(sampleState, retrievedState)

        repository.clearCharacterState()
        assertNull(repository.getCharacterState())
    }
}
