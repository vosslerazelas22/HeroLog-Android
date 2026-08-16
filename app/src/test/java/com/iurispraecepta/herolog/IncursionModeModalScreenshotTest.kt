package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.ui.focus.IncursionModeModal
import com.iurispraecepta.herolog.ui.focus.RaidMode
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
class IncursionModeModalScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun incursionModeModal_padrao_active_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                IncursionModeModal(
                    isOpen = true,
                    onClose = {},
                    currentMode = RaidMode.PADRAO,
                    dungeonCooldownRemainingMs = 0L,
                    onSelectMode = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/incursion_mode_modal_padrao_active.png")
    }

    @Test
    fun incursionModeModal_masmorra_active_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                IncursionModeModal(
                    isOpen = true,
                    onClose = {},
                    currentMode = RaidMode.MASMORRA,
                    dungeonCooldownRemainingMs = 0L,
                    onSelectMode = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/incursion_mode_modal_masmorra_active.png")
    }

    @Test
    fun incursionModeModal_selvagem_active_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                IncursionModeModal(
                    isOpen = true,
                    onClose = {},
                    currentMode = RaidMode.SELVAGEM,
                    dungeonCooldownRemainingMs = 0L,
                    onSelectMode = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/incursion_mode_modal_selvagem_active.png")
    }

    @Test
    fun incursionModeModal_dungeon_on_cooldown_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                IncursionModeModal(
                    isOpen = true,
                    onClose = {},
                    currentMode = RaidMode.PADRAO,
                    dungeonCooldownRemainingMs = 3600000L + 1500000L, // 1h 25m
                    onSelectMode = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/incursion_mode_modal_dungeon_on_cooldown.png")
    }

    @Test
    fun incursionModeModal_closed_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                androidx.compose.foundation.layout.Box {
                    IncursionModeModal(
                        isOpen = false,
                        onClose = {},
                        currentMode = RaidMode.PADRAO,
                        dungeonCooldownRemainingMs = 0L,
                        onSelectMode = {}
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/incursion_mode_modal_closed.png")
    }
}
