package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.ui.focus.FocusOrb
import com.iurispraecepta.herolog.ui.focus.FocusOrbSize
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class FocusOrbScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.mainClock.autoAdvance = false
    }

    @Test
    fun focusOrb_running_highProgress() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_running_high_progress.png")
    }

    @Test
    fun focusOrb_running_urgent() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 45,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_running_urgent.png")
    }

    @Test
    fun focusOrb_paused() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 800,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = true,
                    isBreakActive = false,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_paused.png")
    }

    @Test
    fun focusOrb_breakActive() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 200,
                    totalSeconds = 300,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = true,
                    size = FocusOrbSize.STANDARD
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_break_active.png")
    }

    @Test
    fun focusOrb_fullscreenSize() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusOrb(
                    timeLeft = 1000,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    isBreakActive = false,
                    size = FocusOrbSize.FULLSCREEN
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_orb_fullscreen_size.png")
    }
}
