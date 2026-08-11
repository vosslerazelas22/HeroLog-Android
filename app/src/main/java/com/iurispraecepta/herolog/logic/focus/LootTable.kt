package com.iurispraecepta.herolog.logic.focus

import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.Rarity

object LootTable {
    val LOOT_TABLE: List<LootItem> = listOf(
        // Raridade ESPECIAL
        LootItem("Coruja Pixelada", "🦉", "Equipável: +5% de XP em todas as sessões. (8 Cargas)",
            BuffType.PixelOwl, 250, true, 8, 8, Rarity.Especial),
        LootItem("Pena de Dragão", "🪶", "Equipável: +8% de XP em sessões de 45 min+. (8 Cargas)",
            BuffType.DragonQuill, 300, true, 8, 8, Rarity.Especial),
        LootItem("Bola de Cristal", "🔮", "Equipável: +10% de XP em todas as sessões. (10 Cargas)",
            BuffType.CrystalBall, 400, true, 10, 10, Rarity.Especial),
        LootItem("Tomo Antigo", "📖", "Equipável: +15% de XP em sessões de 60 min+. (8 Cargas)",
            BuffType.AncientTome, 500, true, 8, 8, Rarity.Especial),
        // Raridade COMUM
        LootItem("Grimório de Prata", "📚", "Equipável: +1% de XP em todas as sessões. (5 Cargas)",
            BuffType.SilverGrimoire, 80, true, 5, 5, Rarity.Comum),
        LootItem("Pergaminho Antigo", "📜", "Equipável: +1% de Ouro em todas as sessões. (5 Cargas)",
            BuffType.AncientScroll, 80, true, 5, 5, Rarity.Comum),
        LootItem("Poção Celestina", "🧪", "Equipável: +2% de XP em sessões de 30 min+. (4 Cargas)",
            BuffType.CelestinePotion, 100, true, 4, 4, Rarity.Comum),
        LootItem("Fécula de Estrelas", "✨", "Equipável: +2% de Ouro em sessões de 30 min+. (4 Cargas)",
            BuffType.StarPowder, 100, true, 4, 4, Rarity.Comum),
        LootItem("Broche de Ouro", "🏅", "Equipável: +1% de XP e +1% de Ouro em todas as sessões. (5 Cargas)",
            BuffType.GoldBrooch, 90, true, 5, 5, Rarity.Comum),
        LootItem("Grimório Lendário do Caos", "🔮", "Equipável: +2% de XP em todas as sessões. (5 Cargas)",
            BuffType.ChaosGrimoire, 110, true, 5, 5, Rarity.Comum),
        LootItem("Espada do Foco Inabalável", "🗡️", "Equipável: +2% de Ouro em todas as sessões. (5 Cargas)",
            BuffType.UnwaveringSword, 110, true, 5, 5, Rarity.Comum),
        LootItem("Cálice Sagrado da Sabedoria", "🏆", "Equipável: +1% de XP e +1% de Ouro. (5 Cargas)",
            BuffType.SacredChalice, 100, true, 5, 5, Rarity.Comum),
        LootItem("Relíquia Secreta Arcana", "🔱", "Equipável: +3% de XP em sessões de 45 min+. (4 Cargas)",
            BuffType.ArcaneRelic, 130, true, 4, 4, Rarity.Comum),
        LootItem("Pedra Filosofal Rúnica", "💎", "Equipável: +3% de Ouro em sessões de 45 min+. (4 Cargas)",
            BuffType.RunicStone, 130, true, 4, 4, Rarity.Comum)
    )
}