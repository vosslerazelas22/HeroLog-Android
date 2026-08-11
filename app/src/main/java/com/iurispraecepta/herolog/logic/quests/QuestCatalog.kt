package com.iurispraecepta.herolog.logic.quests

import kotlin.math.min

object QuestCatalog {

    val DAILY_QUEST_CATALOG: List<QuestDef> = listOf(
        // ── Foco Básico ──────────────────────────────────────────────
        QuestDef(
            id = "daily_first_torch",
            name = "Acender a Primeira Tocha",
            summary = "1 foco hoje",
            desc = "Conclua 1 sessão de foco hoje.",
            target = 1, rewardGold = 80, rewardXp = 40,
            getProgress = { s -> min(QuestLogic.todaySessions(s).size, 1) }
        ),
        QuestDef(
            id = "daily_rite_25",
            name = "Rito dos 25 Minutos",
            summary = "25 min de foco",
            desc = "Acumule 25 minutos de foco hoje.",
            target = 25, rewardGold = 100, rewardXp = 50,
            getProgress = { s -> min(s.todayMinutes, 25) }
        ),
        QuestDef(
            id = "daily_apprentice_vigil",
            name = "Vigília do Aprendiz",
            summary = "40 min de foco",
            desc = "Acumule 40 minutos de foco hoje.",
            target = 40, rewardGold = 130, rewardXp = 65,
            getProgress = { s -> min(s.todayMinutes, 40) }
        ),
        QuestDef(
            id = "daily_two_bells",
            name = "O Sino Tocou Duas Vezes",
            summary = "2 sessões hoje",
            desc = "Conclua 2 sessões de foco hoje.",
            target = 2, rewardGold = 150, rewardXp = 75,
            getProgress = { s -> min(QuestLogic.todaySessions(s).size, 2) }
        ),
        QuestDef(
            id = "daily_three_incursions",
            name = "Três Investidas ao Santuário",
            summary = "3 sessões hoje",
            desc = "Conclua 3 sessões de foco hoje.",
            target = 3, rewardGold = 200, rewardXp = 100,
            getProgress = { s -> min(QuestLogic.todaySessions(s).size, 3) }
        ),

        // ── Tempo Total ──────────────────────────────────────────────
        QuestDef(
            id = "daily_sacred_hour",
            name = "Hora Sagrada",
            summary = "60 min de foco",
            desc = "Acumule 60 minutos de foco hoje.",
            target = 60, rewardGold = 200, rewardXp = 100,
            getProgress = { s -> min(s.todayMinutes, 60) }
        ),
        QuestDef(
            id = "daily_concentration_circle",
            name = "Círculo de Concentração",
            summary = "90 min de foco",
            desc = "Acumule 90 minutos de foco hoje.",
            target = 90, rewardGold = 270, rewardXp = 135,
            getProgress = { s -> min(s.todayMinutes, 90) }
        ),
        QuestDef(
            id = "daily_half_heroic_journey",
            name = "Meia Jornada Heroica",
            summary = "120 min de foco",
            desc = "Acumule 120 minutos de foco hoje.",
            target = 120, rewardGold = 350, rewardXp = 175,
            getProgress = { s -> min(s.todayMinutes, 120) }
        ),
        QuestDef(
            id = "daily_wall_150",
            name = "Muralha dos 150 Minutos",
            summary = "150 min de foco",
            desc = "Acumule 150 minutos de foco hoje.",
            target = 150, rewardGold = 420, rewardXp = 210,
            getProgress = { s -> min(s.todayMinutes, 150) }
        ),
        QuestDef(
            id = "daily_great_vigil",
            name = "Grande Vigília",
            summary = "180 min de foco",
            desc = "Acumule 180 minutos de foco hoje.",
            target = 180, rewardGold = 500, rewardXp = 250,
            getProgress = { s -> min(s.todayMinutes, 180) }
        ),

        // ── Wilderness ───────────────────────────────────────────────
        QuestDef(
            id = "daily_wilderness_step",
            name = "Passo na Terra Selvagem",
            summary = "1 Wilderness",
            desc = "Conclua 1 sessão Wilderness hoje.",
            target = 1, rewardGold = 150, rewardXp = 80,
            getProgress = { s -> min(QuestLogic.todaySessions(s).count { it.wilderness }, 1) }
        ),
        QuestDef(
            id = "daily_no_looking_back",
            name = "Sem Olhar Para Trás",
            summary = "Wilderness 25 min",
            desc = "Conclua 1 sessão Wilderness de pelo menos 25 minutos hoje.",
            target = 1, rewardGold = 200, rewardXp = 100,
            getProgress = { s ->
                min(QuestLogic.todaySessions(s).count { it.wilderness && it.duration >= 25 }, 1)
            }
        ),
        QuestDef(
            id = "daily_border_scout",
            name = "Batedor da Fronteira",
            summary = "40 min Wilderness",
            desc = "Acumule 40 minutos em Wilderness hoje.",
            target = 40, rewardGold = 250, rewardXp = 120,
            getProgress = { s ->
                val total = QuestLogic.todaySessions(s).filter { it.wilderness }.sumOf { it.duration }
                min(total, 40)
            }
        ),
        QuestDef(
            id = "daily_survive_wilds",
            name = "Sobreviva ao Ermo",
            summary = "2 Wilderness",
            desc = "Conclua 2 sessões Wilderness hoje.",
            target = 2, rewardGold = 300, rewardXp = 150,
            getProgress = { s -> min(QuestLogic.todaySessions(s).count { it.wilderness }, 2) }
        ),

        // ── Skills e Variedade ───────────────────────────────────────
        QuestDef(
            id = "daily_two_mastery_paths",
            name = "Duas Trilhas de Maestria",
            summary = "2 skills hoje",
            desc = "Estude 2 skills diferentes hoje.",
            target = 2, rewardGold = 150, rewardXp = 75,
            getProgress = { s -> min(QuestLogic.todaySkillNames(s).size, 2) }
        ),
        QuestDef(
            id = "daily_triad_of_knowledge",
            name = "Tríade de Saberes",
            summary = "3 skills hoje",
            desc = "Estude 3 skills diferentes hoje.",
            target = 3, rewardGold = 220, rewardXp = 110,
            getProgress = { s -> min(QuestLogic.todaySkillNames(s).size, 3) }
        ),
        QuestDef(
            id = "daily_weakest_link",
            name = "Reforço do Elo Fraco",
            summary = "Skill menor nível",
            desc = "Faça uma sessão na skill de menor nível hoje.",
            target = 1, rewardGold = 170, rewardXp = 85,
            getProgress = { s ->
                val weak = QuestLogic.weakestSkill(s)
                if (weak != null && QuestLogic.todaySessions(s).any { it.skillName == weak }) 1 else 0
            }
        ),
        QuestDef(
            id = "daily_arcane_polish",
            name = "Lapidação Arcana",
            summary = "Ganhar XP hoje",
            desc = "Ganhe XP em qualquer skill hoje.",
            target = 1, rewardGold = 80, rewardXp = 40,
            getProgress = { s -> if (s.todayXP > 0) 1 else 0 }
        ),

        // ── Hábitos, Dailies e Todos ─────────────────────────────────
        QuestDef(
            id = "daily_chapel_order",
            name = "Capela em Ordem",
            summary = "1 Daily feita",
            desc = "Complete 1 Daily da aba de tarefas.",
            target = 1, rewardGold = 100, rewardXp = 50,
            getProgress = { s -> min(s.dailies.count { it.completed }, 1) }
        ),
        QuestDef(
            id = "daily_morning_ritual",
            name = "Ritual Matinal",
            summary = "2 Dailies feitas",
            desc = "Complete 2 Dailies hoje.",
            target = 2, rewardGold = 160, rewardXp = 80,
            getProgress = { s -> min(s.dailies.count { it.completed }, 2) }
        )
    )

    val GUILD_QUESTS: List<QuestDef> = listOf(
        QuestDef(
            id = "guild_1",
            name = "Iniciado da Guilda",
            desc = "Atinja Combat Level 5 ou superior.",
            target = 5, rewardGold = 400, rewardXp = 200,
            getProgress = { s -> min(s.combatLevel, 5) }
        ),
        QuestDef(
            id = "guild_2",
            name = "Maratona Mágica",
            desc = "Conclua um total de 12 sessões acumuladas.",
            target = 12, rewardGold = 500, rewardXp = 300,
            getProgress = { s -> min(s.totalSessions, 12) }
        ),
        QuestDef(
            id = "guild_3",
            name = "Campeão da Constância",
            desc = "Atinja ou supere uma série recorde de 3 dias de estudo.",
            target = 3, rewardGold = 350, rewardXp = 150,
            getProgress = { s -> min(s.bestStreak, 3) }
        )
    )
}