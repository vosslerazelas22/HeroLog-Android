package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.ui.inventory.InventoryScreen
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
class InventoryScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun inventoryScreen_withItems_screenshot() {
        val sampleItems = listOf(
            InventoryItem(
                id = "sword_1",
                name = "Espada Inabalável",
                emoji = "⚔️",
                buff = BuffType.UnwaveringSword,
                price = 150,
                desc = "Uma lâmina lendária gravada com runas de coragem e determinação.",
                isEquipment = true,
                charges = 8,
                maxCharges = 8,
                rarity = Rarity.Especial
            ),
            InventoryItem(
                id = "relic_1",
                name = "Relíquia Arcana",
                emoji = "🔮",
                buff = BuffType.ArcaneRelic,
                price = 100,
                desc = "Um artefato antigo emitindo um brilho azul misterioso.",
                isEquipment = false,
                rarity = Rarity.Comum
            ),
            // Virtual buff - filtered out from physical items view
            InventoryItem(
                id = "buff_1",
                name = "Elixir de Foco",
                emoji = "🧪",
                buff = BuffType.FocusElixir,
                price = 50,
                desc = "Item virtual.",
                isEquipment = false
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                InventoryScreen(
                    inventory = sampleItems,
                    inspectingItem = null,
                    onInspectItem = {},
                    onCloseInspection = {},
                    onEquipItem = { _, _ -> },
                    onSellItem = {},
                    onDiscardItem = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/inventory_screen.png")
    }

    @Test
    fun inventoryScreen_empty_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                InventoryScreen(
                    inventory = emptyList(),
                    inspectingItem = null,
                    onInspectItem = {},
                    onCloseInspection = {},
                    onEquipItem = { _, _ -> },
                    onSellItem = {},
                    onDiscardItem = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/inventory_screen_empty.png")
    }
}
