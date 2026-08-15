package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.data.dao.ActiveFocusSessionDao
import com.iurispraecepta.herolog.data.entity.ActiveFocusSessionEntity
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.logic.focus.DroppedTitle
import com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.logic.focus.LootItem
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import com.iurispraecepta.herolog.logic.focus.UsedEquipmentCharge
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.Rarity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FakeActiveFocusSessionDao : ActiveFocusSessionDao {
    private var currentEntity: ActiveFocusSessionEntity? = null

    override suspend fun getSession(): ActiveFocusSessionEntity? = currentEntity

    override suspend fun saveSession(entity: ActiveFocusSessionEntity) {
        currentEntity = entity
    }

    override suspend fun clearSession() {
        currentEntity = null
    }
}

class FocusSessionRepositoryTest {

    @Test
    fun getSession_whenNothingSaved_returnsNullWithoutException() = runTest {
        val dao = FakeActiveFocusSessionDao()
        val repository = FocusSessionRepository(dao)

        val result = repository.getSession()
        assertNull(result)
    }

    @Test
    fun saveAndGetSession_withPendingCalculationNull_returnsEqualObject() = runTest {
        val dao = FakeActiveFocusSessionDao()
        val repository = FocusSessionRepository(dao)

        val session = PersistedFocusSession(
            config = FocusSessionConfig(
                selectedSkillIdx = 0,
                isWildernessChecked = false,
                isDungeonMode = false,
                dungeonSessions = 0
            ),
            durationMinutes = 25,
            endTimeMillis = 1700000000000L,
            pendingCalculation = null
        )

        repository.saveSession(session)

        val retrieved = repository.getSession()
        assertNotNull(retrieved)
        assertEquals(session, retrieved)
    }

    @Test
    fun saveAndGetSession_withPendingCalculationNonNull_returnsEqualObjectIncludingNestedStructures() = runTest {
        val dao = FakeActiveFocusSessionDao()
        val repository = FocusSessionRepository(dao)

        val loot = LootItem(
            name = "Runa da Fortuna",
            emoji = "🔮",
            desc = "Aumenta o ganho de ouro",
            buff = BuffType.RuneFortune,
            price = 75,
            isEquipment = true,
            charges = 2,
            maxCharges = 2,
            rarity = Rarity.Especial
        )

        val calculation = FocusRewardsCalculation(
            skillIdx = 1,
            skillName = "Programação",
            xpEarned = 150,
            goldEarned = 80,
            durationMins = 30,
            dungeonClearGoldBonus = 20,
            hasUsedDoubleLoot = true,
            hasUsedFocusElixir = false,
            hasUsedRuneFortune = true,
            hasUsedCrystalClarity = false,
            usedEquipmentIndicesAndCharges = listOf(
                UsedEquipmentCharge(index = 0, remainingCharges = 1)
            ),
            lootedItems = listOf(loot),
            droppedTitle = DroppedTitle(id = "code_master", name = "Mestre do Código", emoji = "💻"),
            isWildernessChecked = true,
            isDungeonMode = true,
            comboBonusPercent = 10
        )

        val session = PersistedFocusSession(
            config = FocusSessionConfig(
                selectedSkillIdx = 1,
                isWildernessChecked = true,
                isDungeonMode = true,
                dungeonSessions = 3
            ),
            durationMinutes = 30,
            endTimeMillis = 1700001800000L,
            pendingCalculation = calculation
        )

        repository.saveSession(session)

        val retrieved = repository.getSession()
        assertNotNull(retrieved)
        assertEquals(session, retrieved)
        assertEquals(calculation, retrieved?.pendingCalculation)
    }

    @Test
    fun saveSession_twice_secondOverwritesFirst() = runTest {
        val dao = FakeActiveFocusSessionDao()
        val repository = FocusSessionRepository(dao)

        val firstSession = PersistedFocusSession(
            config = FocusSessionConfig(
                selectedSkillIdx = 0,
                isWildernessChecked = false,
                isDungeonMode = false,
                dungeonSessions = 0
            ),
            durationMinutes = 25,
            endTimeMillis = 1700000000000L,
            pendingCalculation = null
        )

        val secondSession = PersistedFocusSession(
            config = FocusSessionConfig(
                selectedSkillIdx = 2,
                isWildernessChecked = true,
                isDungeonMode = false,
                dungeonSessions = 1
            ),
            durationMinutes = 50,
            endTimeMillis = 1700003000000L,
            pendingCalculation = null
        )

        repository.saveSession(firstSession)
        repository.saveSession(secondSession)

        val retrieved = repository.getSession()
        assertNotNull(retrieved)
        assertEquals(secondSession, retrieved)
    }

    @Test
    fun clearSession_afterSaving_returnsNull() = runTest {
        val dao = FakeActiveFocusSessionDao()
        val repository = FocusSessionRepository(dao)

        val session = PersistedFocusSession(
            config = FocusSessionConfig(
                selectedSkillIdx = 0,
                isWildernessChecked = false,
                isDungeonMode = false,
                dungeonSessions = 0
            ),
            durationMinutes = 25,
            endTimeMillis = 1700000000000L,
            pendingCalculation = null
        )

        repository.saveSession(session)
        assertNotNull(repository.getSession())

        repository.clearSession()
        assertNull(repository.getSession())
    }
}
