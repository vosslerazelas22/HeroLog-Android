package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.ui.components.ItemInspectAction
import com.iurispraecepta.herolog.ui.components.ItemInspectModal
import com.iurispraecepta.herolog.ui.components.ItemInspectVariant
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
class ItemInspectModalScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val equipmentItem = InventoryItem(
        id = "item_sword",
        name = "Espada Inabalável",
        emoji = "⚔️",
        buff = BuffType.UnwaveringSword,
        price = 500,
        desc = "Uma lâmina lendária forjada para combates intensos e foco inabalável.",
        isEquipment = true,
        charges = 5,
        maxCharges = 8,
        rarity = Rarity.Especial
    )

    private val collectibleItem = InventoryItem(
        id = "item_relic",
        name = "Relíquia Arcana",
        emoji = "🏺",
        buff = BuffType.ArcaneRelic,
        price = 1000,
        desc = "Uma relíquia misteriosa com aura encantada de tempos antigos.",
        isEquipment = false
    )

    @Test
    fun itemInspectModal_equipmentWithCharges_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = equipmentItem,
                    onClose = {},
                    actions = listOf(
                        ItemInspectAction(
                            label = "Equipar",
                            onClick = {},
                            variant = ItemInspectVariant.Primary
                        )
                    )
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_equipment_charges.png")
    }

    @Test
    fun itemInspectModal_nonEquipment_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = collectibleItem,
                    onClose = {},
                    actions = listOf(
                        ItemInspectAction(
                            label = "Vender (50 GP)",
                            onClick = {},
                            variant = ItemInspectVariant.Danger
                        )
                    )
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_non_equipment.png")
    }

    @Test
    fun itemInspectModal_slotSelector_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = equipmentItem,
                    onClose = {},
                    showSlotSelector = true,
                    onSelectSlot = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_slot_selector.png")
    }

    @Test
    fun itemInspectModal_asymmetricActions_successAndDanger_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ItemInspectModal(
                    item = equipmentItem,
                    onClose = {},
                    actions = listOf(
                        ItemInspectAction(
                            label = "Equipar",
                            onClick = {},
                            variant = ItemInspectVariant.Success
                        ),
                        ItemInspectAction(
                            label = "Descartar",
                            onClick = {},
                            variant = ItemInspectVariant.Danger
                        )
                    )
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_inspect_modal_asymmetric_actions.png")
    }
}
