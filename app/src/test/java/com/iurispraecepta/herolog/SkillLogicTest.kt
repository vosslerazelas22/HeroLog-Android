package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.DeleteSkillEligibility
import com.iurispraecepta.herolog.logic.SkillError
import com.iurispraecepta.herolog.logic.SkillLogic
import com.iurispraecepta.herolog.logic.SkillOperationResult
import com.iurispraecepta.herolog.model.Skill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillLogicTest {

    @Test
    fun requiredXpForLevel_calculates_correctly() {
        assertEquals(80, SkillLogic.requiredXpForLevel(1))
        assertEquals(7920, SkillLogic.requiredXpForLevel(99))
    }

    @Test
    fun addCustomSkill_success_with_new_name() {
        val initialSkills = listOf(Skill(id = "sk_foco", name = "Foco", level = 1, xp = 0))
        val result = SkillLogic.addCustomSkill(initialSkills, "Programação", "💻")

        assertTrue(result is SkillOperationResult.Success)
        val newSkills = (result as SkillOperationResult.Success).newSkills
        assertEquals(2, newSkills.size)
        assertEquals("Programação", newSkills[1].name)
        assertEquals("💻", newSkills[1].emoji)
    }

    @Test
    fun addCustomSkill_fails_on_duplicate_name_case_insensitive() {
        val initialSkills = listOf(Skill(id = "sk_foco", name = "Foco", level = 1, xp = 0))
        val result = SkillLogic.addCustomSkill(initialSkills, "foco", "🎯")

        assertTrue(result is SkillOperationResult.Error)
        assertEquals(SkillError.DuplicateName, (result as SkillOperationResult.Error).reason)
    }

    @Test
    fun addCustomSkill_fails_on_blank_name() {
        val initialSkills = listOf(Skill(id = "sk_foco", name = "Foco", level = 1, xp = 0))
        val result = SkillLogic.addCustomSkill(initialSkills, "   ", "🎯")

        assertTrue(result is SkillOperationResult.Error)
        assertEquals(SkillError.BlankName, (result as SkillOperationResult.Error).reason)
    }

    @Test
    fun renameSkill_success() {
        val initialSkills = listOf(Skill(id = "sk_foco", name = "Foco", level = 1, xp = 0), Skill(id = "sk_treino", name = "Treino", level = 1, xp = 0))
        val result = SkillLogic.renameSkill(initialSkills, 0, "Concentração")

        assertTrue(result is SkillOperationResult.Success)
        val newSkills = (result as SkillOperationResult.Success).newSkills
        assertEquals("Concentração", newSkills[0].name)
        assertEquals("Treino", newSkills[1].name)
    }

    @Test
    fun renameSkill_fails_on_blank_name() {
        val initialSkills = listOf(Skill(id = "sk_foco", name = "Foco", level = 1, xp = 0))
        val result = SkillLogic.renameSkill(initialSkills, 0, "  ")

        assertTrue(result is SkillOperationResult.Error)
        assertEquals(SkillError.BlankName, (result as SkillOperationResult.Error).reason)
    }

    @Test
    fun renameSkill_fails_on_invalid_index() {
        val initialSkills = listOf(Skill(id = "sk_foco", name = "Foco", level = 1, xp = 0))
        val result = SkillLogic.renameSkill(initialSkills, 99, "Novo Nome")

        assertTrue(result is SkillOperationResult.Error)
        assertEquals(SkillError.InvalidIndex, (result as SkillOperationResult.Error).reason)
    }

    @Test
    fun renameSkill_fails_on_duplicate_with_another_skill() {
        val initialSkills = listOf(Skill(id = "sk_foco", name = "Foco", level = 1, xp = 0), Skill(id = "sk_treino", name = "Treino", level = 1, xp = 0))
        val result = SkillLogic.renameSkill(initialSkills, 0, "treino")

        assertTrue(result is SkillOperationResult.Error)
        assertEquals(SkillError.DuplicateName, (result as SkillOperationResult.Error).reason)
    }

    @Test
    fun renameSkill_succeeds_when_renaming_to_same_name() {
        val initialSkills = listOf(Skill(id = "sk_foco", name = "Foco", level = 1, xp = 0), Skill(id = "sk_treino", name = "Treino", level = 1, xp = 0))
        val result = SkillLogic.renameSkill(initialSkills, 0, "Foco")

        assertTrue(result is SkillOperationResult.Success)
        val newSkills = (result as SkillOperationResult.Success).newSkills
        assertEquals("Foco", newSkills[0].name)
    }

    @Test
    fun canDeleteSkill_checks_eligibility() {
        val multipleSkills = listOf(Skill(id = "sk_foco", name = "Foco", level = 1, xp = 0), Skill(id = "sk_treino", name = "Treino", level = 1, xp = 0))
        val singleSkill = listOf(Skill(id = "sk_foco", name = "Foco", level = 1, xp = 0))

        assertEquals(
            DeleteSkillEligibility.Blocked,
            SkillLogic.canDeleteSkill(multipleSkills, isFocusSessionRunning = true)
        )
        assertEquals(
            DeleteSkillEligibility.RequiresAtLeastOne,
            SkillLogic.canDeleteSkill(singleSkill, isFocusSessionRunning = false)
        )
        assertEquals(
            DeleteSkillEligibility.Eligible,
            SkillLogic.canDeleteSkill(multipleSkills, isFocusSessionRunning = false)
        )
    }

    @Test
    fun isPrestigeEligible_and_applyPrestige() {
        val skLevel98 = Skill(id = "sk_98", name = "Foco", level = 98, xp = 500)
        val skLevel99 = Skill(id = "sk_99", name = "Foco", level = 99, xp = 7920)

        assertFalse(SkillLogic.isPrestigeEligible(skLevel98))
        assertTrue(SkillLogic.isPrestigeEligible(skLevel99))

        val prestiged1 = SkillLogic.applyPrestige(skLevel99)
        assertEquals(1, prestiged1.level)
        assertEquals(0, prestiged1.xp)
        assertEquals(1, prestiged1.prestige)

        val prestiged2 = SkillLogic.applyPrestige(prestiged1.copy(level = 99))
        assertEquals(1, prestiged2.level)
        assertEquals(0, prestiged2.xp)
        assertEquals(2, prestiged2.prestige)
    }

    @Test
    fun addTagToSkill_and_removeTagFromSkill() {
        val initialSkills = listOf(Skill(id = "sk_foco", name = "Foco", level = 1, xp = 0, tags = listOf("Kotlin")))

        val withTag = SkillLogic.addTagToSkill(initialSkills, 0, "Compose")
        assertEquals(listOf("Kotlin", "Compose"), withTag[0].tags)

        val duplicateTagAttempt = SkillLogic.addTagToSkill(withTag, 0, "kotlin")
        assertEquals(listOf("Kotlin", "Compose"), duplicateTagAttempt[0].tags)

        val removedTag = SkillLogic.removeTagFromSkill(duplicateTagAttempt, 0, 0)
        assertEquals(listOf("Compose"), removedTag[0].tags)
    }
}
