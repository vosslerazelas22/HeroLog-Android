package com.iurispraecepta.herolog.logic

import com.iurispraecepta.herolog.model.Skill

sealed class SkillError {
    data object BlankName : SkillError()
    data object DuplicateName : SkillError()
    data object InvalidIndex : SkillError()
}

sealed class SkillOperationResult {
    data class Success(val newSkills: List<Skill>) : SkillOperationResult()
    data class Error(val reason: SkillError) : SkillOperationResult()
}

sealed class DeleteSkillEligibility {
    data object Blocked : DeleteSkillEligibility()
    data object RequiresAtLeastOne : DeleteSkillEligibility()
    data object Eligible : DeleteSkillEligibility()
}

/**
 * Lógica pura e sem efeitos colaterais para o módulo de Habilidades (Skills).
 * Organizada em um `object` singleton para evitar poluição do namespace top-level,
 * mantendo as funções puras, estáticas e facilmente testáveis.
 */
object SkillLogic {

    fun requiredXpForLevel(level: Int): Int {
        return level * 80
    }

    fun addCustomSkill(
        skills: List<Skill>,
        nameInput: String,
        emojiInput: String
    ): SkillOperationResult {
        val trimmed = nameInput.trim()
        if (trimmed.isEmpty()) {
            return SkillOperationResult.Error(SkillError.BlankName)
        }

        if (skills.any { it.name.equals(trimmed, ignoreCase = true) }) {
            return SkillOperationResult.Error(SkillError.DuplicateName)
        }

        val addedSkill = Skill(
            name = trimmed,
            level = 1,
            xp = 0,
            emoji = emojiInput,
            prestige = 0
        )
        return SkillOperationResult.Success(skills + addedSkill)
    }

    fun addTagToSkill(skills: List<Skill>, skillIdx: Int, newTag: String): List<Skill> {
        val trimmed = newTag.trim()
        if (trimmed.isEmpty() || skillIdx !in skills.indices) return skills

        val sk = skills[skillIdx]
        val currentTags = sk.tags ?: emptyList()
        if (currentTags.any { it.equals(trimmed, ignoreCase = true) }) {
            return skills
        }

        val updatedSkill = sk.copy(tags = currentTags + trimmed)
        return skills.toMutableList().apply { this[skillIdx] = updatedSkill }
    }

    fun removeTagFromSkill(skills: List<Skill>, skillIdx: Int, tagIdx: Int): List<Skill> {
        if (skillIdx !in skills.indices) return skills

        val sk = skills[skillIdx]
        val currentTags = sk.tags ?: return skills
        if (tagIdx !in currentTags.indices) return skills

        val newTags = currentTags.toMutableList().apply { removeAt(tagIdx) }
        val updatedSkill = sk.copy(tags = newTags)
        return skills.toMutableList().apply { this[skillIdx] = updatedSkill }
    }

    fun renameSkill(skills: List<Skill>, idx: Int, newName: String): SkillOperationResult {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) {
            return SkillOperationResult.Error(SkillError.BlankName)
        }
        if (idx !in skills.indices) {
            return SkillOperationResult.Error(SkillError.InvalidIndex)
        }

        if (skills.indices.any { sIdx -> sIdx != idx && skills[sIdx].name.equals(trimmed, ignoreCase = true) }) {
            return SkillOperationResult.Error(SkillError.DuplicateName)
        }

        val updatedSkills = skills.toMutableList().apply {
            this[idx] = this[idx].copy(name = trimmed)
        }
        return SkillOperationResult.Success(updatedSkills)
    }

    fun canDeleteSkill(skills: List<Skill>, isFocusSessionRunning: Boolean): DeleteSkillEligibility {
        if (isFocusSessionRunning) {
            return DeleteSkillEligibility.Blocked
        }
        if (skills.size <= 1) {
            return DeleteSkillEligibility.RequiresAtLeastOne
        }
        return DeleteSkillEligibility.Eligible
    }

    fun deleteSkillAt(skills: List<Skill>, idx: Int): List<Skill> {
        if (idx !in skills.indices) return skills
        return skills.toMutableList().apply { removeAt(idx) }
    }

    fun isPrestigeEligible(skill: Skill): Boolean {
        return skill.level >= 99
    }

    fun applyPrestige(skill: Skill): Skill {
        // TODO: multiplicador de +25% XP por prestígio consumido na distribuição de XP de sessão — não localizado ainda, ver PARIDADE.md
        val currentPrestige = skill.prestige ?: 0
        return skill.copy(
            level = 1,
            xp = 0,
            prestige = currentPrestige + 1
        )
    }
}
