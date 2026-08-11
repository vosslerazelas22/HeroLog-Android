package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.ui.character.TitleEquipModal
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
class TitleEquipModalScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun titleEquipModal_empty_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                TitleEquipModal(
                    isOpen = true,
                    onClose = {},
                    ownedTitles = emptyList(),
                    equippedTitle = null,
                    onEquipTitle = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/title_equip_modal_empty.png")
    }

    @Test
    fun titleEquipModal_populated_equipped_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                TitleEquipModal(
                    isOpen = true,
                    onClose = {},
                    ownedTitles = listOf("APPRENTICE", "CHAMPION", "IRON_WILL", "BLESSED"),
                    equippedTitle = "CHAMPION",
                    onEquipTitle = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/title_equip_modal_populated_equipped.png")
    }
}
