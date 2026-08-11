package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterSummary
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.ui.character.CharacterScreen
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class CharacterScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun characterScreen_equipped_title_equipment_buffs_screenshot() {
        val character = CharacterSummary(
            charName = "Aethelgard",
            charClass = CharClass.Warrior,
            equippedTitle = "CHAMPION",
            streak = 12,
            bestStreak = 45,
            totalMinutes = 125,
            combatLevel = 8,
            combatXP = 340,
            hp = 85,
            maxHp = 100
        )

        val equippedEquipment = listOf(
            InventoryItem(
                id = "eq_1",
                name = "Espada Flamejante",
                emoji = "⚔️",
                buff = BuffType.UnwaveringSword,
                price = 500,
                desc = "Espada encantada com chamas purificadoras.",
                isEquipment = true,
                charges = 6,
                maxCharges = 8,
                rarity = Rarity.Especial
            ),
            InventoryItem(
                id = "eq_2",
                name = "Escudo Rúnico",
                emoji = "🛡️",
                buff = BuffType.RunicStone,
                price = 450,
                desc = "Escudo gravado com runas antigas de proteção.",
                isEquipment = true,
                charges = 4,
                maxCharges = 8,
                rarity = Rarity.Comum
            ),
            InventoryItem(
                id = "eq_3",
                name = "Cálice Sagrado",
                emoji = "🏆",
                buff = BuffType.SacredChalice,
                price = 600,
                desc = "Cálice lendário de purificação.",
                isEquipment = true,
                charges = 8,
                maxCharges = 8,
                rarity = Rarity.Especial
            )
        )

        val activeBuffs = listOf(
            InventoryItem(
                id = "buff_1",
                name = "Elixir de Foco",
                emoji = "🧪",
                buff = BuffType.FocusElixir,
                price = 200,
                desc = "Garante clareza mental durante o estudo."
            ),
            InventoryItem(
                id = "buff_2",
                name = "Runa da Fortuna",
                emoji = "✨",
                buff = BuffType.RuneFortune,
                price = 300,
                desc = "Aumenta os ganhos de ouro temporariamente."
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                CharacterScreen(
                    character = character,
                    equippedEquipment = equippedEquipment,
                    activeBuffs = activeBuffs,
                    onUnequipItem = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/character_screen_equipped.png")
    }

    @Test
    fun characterScreen_zeroed_state_screenshot() {
        val character = CharacterSummary(
            charName = "Novato",
            charClass = CharClass.Mage,
            equippedTitle = null,
            streak = 0,
            bestStreak = 0,
            totalMinutes = 0,
            combatLevel = 1,
            combatXP = 0,
            hp = 100,
            maxHp = 100
        )

        composeTestRule.setContent {
            HeroLogTheme {
                CharacterScreen(
                    character = character,
                    equippedEquipment = listOf(null, null, null),
                    activeBuffs = emptyList(),
                    onUnequipItem = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/character_screen_zeroed.png")
    }
}
