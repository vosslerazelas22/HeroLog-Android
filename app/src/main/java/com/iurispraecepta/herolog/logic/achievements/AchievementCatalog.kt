package com.iurispraecepta.herolog.logic.achievements

object AchievementCatalog {

    val ACHIEVEMENTS_LIST: List<Achievement> = listOf(
        Achievement(
            id = "first_quest",
            name = "Primeira Incursão",
            desc = "Concluiu com êxito a primeira Missão de Foco.",
            icon = "⚔️",
            check = { s -> s.totalSessions >= 1 }
        ),
        Achievement(
            id = "streak_3",
            name = "Fagulha de Disciplina",
            desc = "Manteve uma série consecutiva de 3 dias de estudo.",
            icon = "🔥",
            check = { s -> s.bestStreak >= 3 }
        ),
        Achievement(
            id = "streak_7",
            name = "Inabalável",
            desc = "Conquistou a lendária marca de 7 dias focando em sequência.",
            icon = "🏆",
            check = { s -> s.bestStreak >= 7 }
        ),
        Achievement(
            id = "xp_1000",
            name = "Mestre Alfabetizado",
            desc = "Acumulou uma soma de 1000 pontos totais de XP.",
            icon = "📚",
            check = { s -> s.totalXP >= 1000 }
        ),
        Achievement(
            id = "xp_10000",
            name = "Sábio Iluminado",
            desc = "Superou a extraordinária marca de 10.000 pontos totais de XP.",
            icon = "🧙",
            check = { s -> s.totalXP >= 10000 }
        ),
        Achievement(
            id = "gp_1000",
            name = "Rico em Espólios",
            desc = "Armazenou nas arcas um patrimônio eterno de 1000 GP gulosamente.",
            icon = "💰",
            check = { s -> s.totalGoldEarned >= 1000 }
        ),
        Achievement(
            id = "sessions_10",
            name = "Veterano de Guerras",
            desc = "Concluiu um total de 10 sessões na Gilda dos Aventureiros.",
            icon = "🎖️",
            check = { s -> s.totalSessions >= 10 }
        ),
        Achievement(
            id = "survive_wilderness",
            name = "Superação Extrema",
            desc = "Sobreviveu à perigosa incursão sob o efeito de Wilderness.",
            icon = "💀",
            check = { s -> s.wildernessWins >= 1 }
        )
    )
}