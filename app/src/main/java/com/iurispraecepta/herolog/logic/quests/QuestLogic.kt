package com.iurispraecepta.herolog.logic.quests

import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor

object QuestLogic {

    fun todayLocalStr(referenceDate: Date = Date()): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).format(referenceDate)
    }

    fun toDateStringJs(referenceDate: Date = Date()): String {
        return SimpleDateFormat("EEE MMM dd yyyy", Locale.US).format(referenceDate)
    }

    fun todaySessions(state: CharacterState, today: String = todayLocalStr()): List<HistoryEntry> {
        return state.history.filter { it.date.startsWith(today) }
    }

    fun todaySkillNames(state: CharacterState, today: String = todayLocalStr()): List<String> {
        return todaySessions(state, today).map { it.skillName }.distinct()
    }

    fun weakestSkill(state: CharacterState): String? {
        if (state.skills.isEmpty()) return null
        return state.skills.reduce { a, b ->
            if (a.level <= b.level) a else b
        }.name
    }

    fun rotateDailyQuests(
        catalog: List<QuestDef>,
        count: Int = 3,
        referenceDate: Date = Date()
    ): List<QuestDef> {
        if (catalog.isEmpty()) return emptyList()
        val str = toDateStringJs(referenceDate)
        var s: Int = str.sumOf { it.code }
        val arr = catalog.toMutableList()

        for (i in arr.size - 1 downTo 1) {
            s = s * 1664525 + 1013904223
            val rand = (s.toLong() and 0xFFFFFFFFL).toDouble() / 4294967295.0
            val j = floor(rand * (i + 1)).toInt()
            val temp = arr[i]
            arr[i] = arr[j]
            arr[j] = temp
        }

        return arr.take(count)
    }

    fun getRotatingDailyQuests(
        count: Int = 3,
        referenceDate: Date = Date()
    ): List<QuestDef> {
        return rotateDailyQuests(QuestCatalog.DAILY_QUEST_CATALOG, count, referenceDate)
    }

    fun getQuestClaimId(questId: String, referenceDate: Date = Date()): String {
        return if (questId.startsWith("daily_")) {
            "claimed_${questId}_${toDateStringJs(referenceDate)}"
        } else {
            "claimed_$questId"
        }
    }

    fun isQuestClaimed(
        state: CharacterState,
        questId: String,
        referenceDate: Date = Date()
    ): Boolean {
        if (!questId.startsWith("daily_")) {
            return state.achievements.contains("claimed_$questId")
        }
        val claimId = getQuestClaimId(questId, referenceDate)
        val todayJs = toDateStringJs(referenceDate)
        val legacyClaimed = (state.todayDate == todayJs) && state.achievements.contains("claimed_$questId")
        return state.achievements.contains(claimId) || legacyClaimed
    }
}
