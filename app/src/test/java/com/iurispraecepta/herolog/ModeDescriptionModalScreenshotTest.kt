package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.data.TITLE_CATALOG
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.focus.ModeDescriptionModal
import com.iurispraecepta.herolog.ui.focus.RaidModeHelpContent
import com.iurispraecepta.herolog.ui.focus.TitleDisplay
import com.iurispraecepta.herolog.ui.focus.buildStandardLootHelpBlocks
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
class ModeDescriptionModalScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun modeDescriptionModal_standard_amber_screenshot() {
        val blocks = buildStandardLootHelpBlocks(
            studiedMinutes = 25,
            equippedTitleId = "CHAMPION",
            titleLookup = { id ->
                TITLE_CATALOG.find { it.id == id }?.let {
                    TitleDisplay(it.emoji, it.name)
                }
            }
        )

        composeTestRule.setContent {
            HeroLogTheme {
                ModeDescriptionModal(
                    isOpen = true,
                    onClose = {},
                    title = "Modo Padrão & Saques",
                    variant = ModalVariant.Amber,
                    blocks = blocks
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/mode_description_modal_standard_amber.png")
    }

    @Test
    fun modeDescriptionModal_dungeon_purple_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ModeDescriptionModal(
                    isOpen = true,
                    onClose = {},
                    title = "Incursão por Masmorra",
                    variant = ModalVariant.Purple,
                    blocks = RaidModeHelpContent.MASMORRA
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/mode_description_modal_dungeon_purple.png")
    }

    @Test
    fun modeDescriptionModal_wilderness_red_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                ModeDescriptionModal(
                    isOpen = true,
                    onClose = {},
                    title = "Terra Selvagem",
                    variant = ModalVariant.Red,
                    blocks = RaidModeHelpContent.SELVAGEM
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/mode_description_modal_wilderness_red.png")
    }

    @Test
    fun modeDescriptionModal_closed_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                androidx.compose.foundation.layout.Box {
                    ModeDescriptionModal(
                        isOpen = false,
                        onClose = {},
                        title = "Modo Fechado",
                        variant = ModalVariant.Amber,
                        blocks = emptyList()
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/mode_description_modal_closed.png")
    }
}
