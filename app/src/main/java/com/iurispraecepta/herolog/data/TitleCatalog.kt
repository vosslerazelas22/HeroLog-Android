package com.iurispraecepta.herolog.data

import androidx.compose.ui.graphics.Color
import com.iurispraecepta.herolog.model.CharacterState

enum class TitleCategory { Common, Rare, Epic, Legendary, Achievement, Drop }

data class TitleVisualStyle(
    val borderColor: Color,
    val backgroundColor: Color,
    val textColor: Color,
    val glowTextColor: Color
)

data class TitleItem(
    val id: String,
    val name: String,
    val emoji: String,
    val category: TitleCategory,
    val price: Int? = null,
    val perks: List<String>? = null,
    val requirementText: String? = null,
    val dropChanceText: String? = null,
    val visualStyle: TitleVisualStyle,
    val checkUnlocked: ((CharacterState) -> Boolean)? = null
)

// --- Estilos reutilizáveis por categoria ---

private val CommonStyle = TitleVisualStyle(
    borderColor = Color(0xFF44403C).copy(alpha = 0.5f),   // stone-700/50
    backgroundColor = Color(0xFF1C1917).copy(alpha = 0.4f), // stone-900/40
    textColor = Color(0xFFD6D3D1),                          // stone-300
    glowTextColor = Color(0xFFFDE68A).copy(alpha = 0.8f)    // amber-200/80
)

private val RareStyle = TitleVisualStyle(
    borderColor = Color(0xFFD97706).copy(alpha = 0.3f),     // amber-600/30
    backgroundColor = Color(0xFF451A03).copy(alpha = 0.1f), // amber-950/10
    textColor = Color(0xFFFEF3C7),                           // amber-100
    glowTextColor = Color(0xFFFCD34D)                        // amber-300
)

private val EpicStyle = TitleVisualStyle(
    borderColor = Color(0xFFA855F7).copy(alpha = 0.2f),     // purple-500/20
    backgroundColor = Color(0xFF3B0764).copy(alpha = 0.1f), // purple-950/10
    textColor = Color(0xFFF3E8FF),                           // purple-100
    glowTextColor = Color(0xFFD8B4FE)                         // purple-300
)

private val LegendaryWatcherStyle = TitleVisualStyle(
    borderColor = Color(0xFFF97316).copy(alpha = 0.3f),     // orange-500/30
    backgroundColor = Color(0xFF431407).copy(alpha = 0.1f), // orange-950/10
    textColor = Color(0xFFFFEDD5),                            // orange-100
    glowTextColor = Color(0xFFFDBA74)                         // orange-300
)

private val LegendaryEternalScholarStyle = TitleVisualStyle(
    borderColor = Color(0xFFCA8A04).copy(alpha = 0.3f),     // yellow-600/30
    backgroundColor = Color(0xFF422006).copy(alpha = 0.1f), // yellow-950/10
    textColor = Color(0xFFFEF9C3),                            // yellow-100 (corrigindo o typo yellow-105 do React)
    glowTextColor = Color(0xFFFACC15)                         // yellow-400
)

private val AchievementStyle = TitleVisualStyle(
    borderColor = Color(0xFF4E3C28).copy(alpha = 0.7f),
    backgroundColor = Color(0xFF251E16),
    textColor = Color(0xFFCCBDA8),
    glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.9f) // fallback padrão (amber-100/90)
)

val TITLE_CATALOG: List<TitleItem> = listOf(

    // ============ COMMON (150-500 GP) — 8 títulos ============
    TitleItem(
        id = "APPRENTICE", name = "Aprendiz", emoji = "⭐",
        category = TitleCategory.Common, price = 150, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "SCHOLAR", name = "Erudito", emoji = "📖",
        category = TitleCategory.Common, price = 200, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "GRINDER", name = "Incansável", emoji = "⚔️",
        category = TitleCategory.Common, price = 200, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "SCRIBE", name = "Escriba", emoji = "📝",
        category = TitleCategory.Common, price = 250, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "ARCANE", name = "Arcanista", emoji = "🔮",
        category = TitleCategory.Common, price = 300, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "WANDERER", name = "Andarilho", emoji = "🌿",
        category = TitleCategory.Common, price = 300, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "SEEKER", name = "Buscador", emoji = "🔍",
        category = TitleCategory.Common, price = 400, visualStyle = CommonStyle
    ),
    TitleItem(
        id = "NIGHT_OWL", name = "Coruja Noturna", emoji = "🦉",
        category = TitleCategory.Common, price = 500, visualStyle = CommonStyle
    ),

    // ============ RARE (1.500-6.000 GP) — 6 títulos ============
    TitleItem(
        id = "CHAMPION", name = "Campeão", emoji = "👑",
        category = TitleCategory.Rare, price = 1500, visualStyle = RareStyle
    ),
    TitleItem(
        id = "DUELIST", name = "Duelista", emoji = "🗡️",
        category = TitleCategory.Rare, price = 2000, visualStyle = RareStyle
    ),
    TitleItem(
        id = "STORMBORN", name = "Nascido da Tempestade", emoji = "⛈️",
        category = TitleCategory.Rare, price = 2500, visualStyle = RareStyle
    ),
    TitleItem(
        id = "ALCHEMIST", name = "Alquimista", emoji = "🧪",
        category = TitleCategory.Rare, price = 3000, visualStyle = RareStyle
    ),
    TitleItem(
        id = "ILLUMINATED", name = "Iluminado", emoji = "💡",
        category = TitleCategory.Rare, price = 4000, visualStyle = RareStyle
    ),
    TitleItem(
        id = "WARLORD", name = "Senhor da Guerra", emoji = "🔱",
        category = TitleCategory.Rare, price = 6000, visualStyle = RareStyle
    ),

    // ============ EPIC (10.000-50.000 GP) — 5 títulos ============
    TitleItem(
        id = "LEGEND_GP", name = "Lenda", emoji = "💀",
        category = TitleCategory.Epic, price = 10000,
        perks = listOf("+5% de XP em todas as sessões"),
        visualStyle = EpicStyle
    ),
    TitleItem(
        id = "INFERNO", name = "Inferno", emoji = "🔥",
        category = TitleCategory.Epic, price = 15000,
        perks = listOf("+8% de XP · Eventos multiplicadores duram 15% mais"),
        visualStyle = EpicStyle
    ),
    TitleItem(
        id = "STARBOUND", name = "Nascido das Estrelas", emoji = "☀️",
        category = TitleCategory.Epic, price = 22000,
        perks = listOf("+10% de XP · +8% de ouro de todas as fontes"),
        visualStyle = EpicStyle
    ),
    TitleItem(
        id = "DRAGONBORN", name = "Dragonborn", emoji = "🐉",
        category = TitleCategory.Epic, price = 35000,
        perks = listOf("+15% de XP · Bônus de XP de Prestígio 25% mais fortes"),
        visualStyle = EpicStyle
    ),
    TitleItem(
        id = "VOIDWALKER", name = "Caminhante do Vazio", emoji = "🌌",
        category = TitleCategory.Epic, price = 50000,
        perks = listOf("+20% de XP · Chance de drop de saques e títulos +50%"),
        visualStyle = EpicStyle
    ),

    // ============ LEGENDARY (100.000-500.000 GP) — 3 títulos ============
    TitleItem(
        id = "THE_WATCHER", name = "O Vigilante", emoji = "👁️",
        category = TitleCategory.Legendary, price = 100000,
        perks = listOf("+25% de XP · +15% de ouro de todas as fontes"),
        visualStyle = LegendaryWatcherStyle
    ),
    TitleItem(
        id = "TRANSCENDENT", name = "Transcendente", emoji = "♾️",
        category = TitleCategory.Legendary, price = 250000,
        perks = listOf("+30% de XP · +20% de ouro · Taxas de drop de saques duplicadas"),
        visualStyle = LegendaryWatcherStyle
    ),
    TitleItem(
        id = "THE_ETERNAL_SCHOLAR", name = "O Erudito Eterno", emoji = "🌠",
        category = TitleCategory.Legendary, price = 500000,
        perks = listOf("+50% de XP · +30% de ouro · Bônus de Prestígio 50% mais fortes"),
        visualStyle = LegendaryEternalScholarStyle
    ),

    // ============ ACHIEVEMENT (marcos de conquista) — 18 títulos ============
    // Nota: nomes e perks destes títulos estão em inglês no arquivo fonte original — mantido
    // literalmente, não traduzido, diferente das outras categorias.
    TitleItem(
        id = "IRON_WILL", name = "IRON WILL", emoji = "🔥",
        category = TitleCategory.Achievement,
        perks = listOf("+5% XP from all sessions"),
        requirementText = "30-Day streak",
        checkUnlocked = { state -> state.bestStreak >= 30 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "DIAMOND_MIND", name = "DIAMOND MIND", emoji = "💎",
        category = TitleCategory.Achievement,
        perks = listOf("+8% XP · +5% gold earned"),
        requirementText = "60-Day streak",
        checkUnlocked = { state -> state.bestStreak >= 60 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "THE_CENTURY", name = "THE CENTURY", emoji = "💯",
        category = TitleCategory.Achievement,
        perks = listOf("+12% XP · +8% gold earned"),
        requirementText = "100-Day streak",
        checkUnlocked = { state -> state.bestStreak >= 100 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "A_FULL_YEAR", name = "A FULL YEAR", emoji = "☀️",
        category = TitleCategory.Achievement,
        perks = listOf("+25% XP · +15% gold earned"),
        requirementText = "365-Day streak",
        checkUnlocked = { state -> state.bestStreak >= 365 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "CENTURION", name = "CENTURION", emoji = "⚔️",
        category = TitleCategory.Achievement,
        perks = listOf("+5% XP from all sessions"),
        requirementText = "100 sessions",
        checkUnlocked = { state -> state.totalSessions >= 100 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "THE_OBSESSED", name = "THE OBSESSED", emoji = "💫",
        category = TitleCategory.Achievement,
        perks = listOf("+15% XP · +10% gold earned"),
        requirementText = "1,000 sessions",
        checkUnlocked = { state -> state.totalSessions >= 1000 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "IMMORTAL_SCHOLAR", name = "IMMORTAL SCHOLAR", emoji = "🏛️",
        category = TitleCategory.Achievement,
        perks = listOf("+25% XP · +20% gold · Loot rates +50%"),
        requirementText = "2,500 sessions",
        checkUnlocked = { state -> state.totalSessions >= 2500 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "DEATH_PROOF", name = "DEATH-PROOF", emoji = "💀",
        category = TitleCategory.Achievement,
        perks = listOf("+25% XP in Wilderness · Can never die in Wilderness"),
        requirementText = "Survive 50 Wilderness",
        checkUnlocked = { state -> state.wildernessWins >= 50 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "DUNGEON_LORD", name = "DUNGEON LORD", emoji = "🏰",
        category = TitleCategory.Achievement,
        perks = listOf("+15% XP inside dungeons"),
        requirementText = "Clear 10 dungeons",
        checkUnlocked = { state -> state.dungeonProgress >= 10 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "RAID_VETERAN", name = "RAID VETERAN", emoji = "🔱",
        category = TitleCategory.Achievement,
        perks = listOf("+25% XP in dungeons · +20% dungeon gold rewards"),
        requirementText = "Clear 25 dungeons",
        checkUnlocked = { state -> state.dungeonProgress >= 25 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "LEGEND_ACH", name = "LEGEND", emoji = "👑",
        category = TitleCategory.Achievement,
        perks = listOf("+10% XP from all sessions"),
        requirementText = "Level 99 in any skill",
        checkUnlocked = { state -> state.skills.any { it.level >= 99 } },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "PANTHEON", name = "PANTHEON", emoji = "🌠",
        category = TitleCategory.Achievement,
        perks = listOf("+20% XP · Prestige XP bonuses 30% stronger"),
        requirementText = "Level 99 in 5 skills",
        checkUnlocked = { state -> state.skills.count { it.level >= 99 } >= 5 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "MANIAC", name = "MANIAC", emoji = "⚡",
        category = TitleCategory.Achievement,
        perks = listOf("+10% XP · Multiplier events last 30% longer"),
        requirementText = "50-Session combo",
        checkUnlocked = { state -> state.combo >= 50 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "IN_THE_ZONE", name = "IN THE ZONE", emoji = "🌀",
        category = TitleCategory.Achievement,
        perks = listOf("+5% XP · Multiplier events last 20% longer"),
        requirementText = "30-Session combo",
        checkUnlocked = { state -> state.combo >= 30 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "MARATHONER", name = "MARATHONER", emoji = "🏃",
        category = TitleCategory.Achievement,
        perks = listOf("+15% XP for sessions longer than 60 minutes"),
        requirementText = "10 sessions of 90+ min",
        checkUnlocked = { state -> state.history.count { it.duration >= 90 } >= 10 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "XP_GOD", name = "XP GOD", emoji = "💥",
        category = TitleCategory.Achievement,
        perks = listOf("+20% XP · +10% gold earned"),
        requirementText = "5,000,000 total XP",
        checkUnlocked = { state -> state.totalXP >= 5000000 },
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "NOCTURNAL", name = "NOCTURNAL", emoji = "🌙",
        category = TitleCategory.Achievement,
        perks = listOf("+15% XP · Loot & title drop rates +30%"),
        requirementText = "500 hours studied",
        checkUnlocked = { state -> state.totalMinutes >= 30000 }, // 500 horas * 60 min
        visualStyle = AchievementStyle
    ),
    TitleItem(
        id = "ASCENDED", name = "ASCENDED", emoji = "♾️",
        category = TitleCategory.Achievement,
        perks = listOf("+10% XP · Prestige XP bonuses 50% stronger"),
        requirementText = "Prestige 10 times",
        checkUnlocked = { state -> state.skills.sumOf { it.prestige ?: 0 } >= 10 },
        visualStyle = AchievementStyle
    ),

    // ============ DROP (títulos raros de loot) — 7 títulos ============
    // Nota: nomes e dropChanceText em inglês no arquivo fonte original — mantido literalmente.
    TitleItem(
        id = "BLESSED", name = "BLESSED", emoji = "🌸",
        category = TitleCategory.Drop,
        perks = listOf("+8% XP · +10% gold from all sources"),
        dropChanceText = "ULTRA-RARE SESSION DROP",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFF92400E).copy(alpha = 0.35f),
            backgroundColor = Color(0xFF451A03).copy(alpha = 0.30f),
            textColor = Color(0xFFFEF3C7),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "SHADOW", name = "SHADOW", emoji = "🌑",
        category = TitleCategory.Drop,
        perks = listOf("+10% XP · Loot & title drop rates +75%"),
        dropChanceText = "ULTRA-RARE SESSION DROP",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFF1E293B).copy(alpha = 0.70f),
            backgroundColor = Color(0xFF0F172A).copy(alpha = 0.40f),
            textColor = Color(0xFFF1F5F9),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "THE_FORSAKEN", name = "THE FORSAKEN", emoji = "🔮",
        category = TitleCategory.Drop,
        perks = listOf("+15% Wilderness XP · Can never die in the Wilderness"),
        dropChanceText = "EXTREMELY RARE SESSION DROP",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFF581C87).copy(alpha = 0.50f),
            backgroundColor = Color(0xFF3B0764).copy(alpha = 0.30f),
            textColor = Color(0xFFF3E8FF),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "CELESTIAL", name = "CELESTIAL", emoji = "✨",
        category = TitleCategory.Drop,
        perks = listOf("+20% XP · +15% gold · Loot rates doubled"),
        dropChanceText = "RAREST OF ALL DROP TITLES",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFFEAB308).copy(alpha = 0.40f),
            backgroundColor = Color(0xFF422006).copy(alpha = 0.20f),
            textColor = Color(0xFFFEF3C7),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "THUNDERSTRUCK", name = "THUNDERSTRUCK", emoji = "⚡",
        category = TitleCategory.Drop,
        perks = listOf("+25% Wilderness XP · Can never die · +10% gold"),
        dropChanceText = "RARE WILDERNESS-ONLY DROP",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFFF59E0B).copy(alpha = 0.30f),
            backgroundColor = Color(0xFF1E293B).copy(alpha = 0.40f),
            textColor = Color(0xFFFEF3C7),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "HAUNTED", name = "HAUNTED", emoji = "👻",
        category = TitleCategory.Drop,
        perks = listOf("+10% XP · +20% gold from all sources"),
        dropChanceText = "RARE WILDERNESS-ONLY DROP",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFFA855F7).copy(alpha = 0.30f),
            backgroundColor = Color(0xFF0F172A).copy(alpha = 0.30f),
            textColor = Color(0xFFF3E8FF),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    ),
    TitleItem(
        id = "BLOOD_FORGED", name = "BLOOD-FORGED", emoji = "🩸",
        category = TitleCategory.Drop,
        perks = listOf("+20% XP in dungeons · +20% dungeon gold rewards"),
        dropChanceText = "DUNGEON LOOT ONLY",
        visualStyle = TitleVisualStyle(
            borderColor = Color(0xFF4C0519), // sólido, sem alpha — igual ao React
            backgroundColor = Color(0xFF450A0A).copy(alpha = 0.30f),
            textColor = Color(0xFFFFE4E6),
            glowTextColor = Color(0xFFFEF3C7).copy(alpha = 0.90f)
        )
    )
)