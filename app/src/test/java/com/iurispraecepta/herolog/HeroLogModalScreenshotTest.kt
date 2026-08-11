package com.iurispraecepta.herolog

import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant
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
class HeroLogModalScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun heroLogModal_amber_variant_opened_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                HeroLogModal(
                    isOpen = true,
                    onClose = {},
                    title = "Título do Modal",
                    variant = ModalVariant.Amber
                ) {
                    Text("Este é um conteúdo de teste dentro do HeroLogModal.", color = Color.White)
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/hero_log_modal_amber_opened.png")
    }

    @Test
    fun heroLogModal_closed_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                androidx.compose.foundation.layout.Box {
                    HeroLogModal(
                        isOpen = false,
                        onClose = {},
                        title = "Modal Fechado",
                        variant = ModalVariant.Amber
                    ) {
                        Text("Conteúdo não visível", color = Color.White)
                    }
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/hero_log_modal_closed.png")
    }
}
