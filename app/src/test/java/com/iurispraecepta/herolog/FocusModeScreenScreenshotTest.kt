package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.ui.focus.FocusModeScreen
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
class FocusModeScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.mainClock.autoAdvance = false
    }

    @Test
    fun focusModeScreen_running_default() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusModeScreen(
                    skillName = "Programação",
                    skillEmoji = "💻",
                    isDungeonMode = false,
                    dungeonSessions = 0,
                    isWildernessChecked = false,
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    onTogglePause = {},
                    onExit = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_mode_screen_running_default.png")
    }

    @Test
    fun focusModeScreen_paused() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusModeScreen(
                    skillName = "Programação",
                    skillEmoji = "💻",
                    isDungeonMode = false,
                    dungeonSessions = 0,
                    isWildernessChecked = false,
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = true,
                    onTogglePause = {},
                    onExit = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_mode_screen_paused.png")
    }

    @Test
    fun focusModeScreen_dungeonMode() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusModeScreen(
                    skillName = "Programação",
                    skillEmoji = "💻",
                    isDungeonMode = true,
                    dungeonSessions = 2,
                    isWildernessChecked = false,
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    onTogglePause = {},
                    onExit = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_mode_screen_dungeon_mode.png")
    }

    @Test
    fun focusModeScreen_wildernessMode() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusModeScreen(
                    skillName = "Programação",
                    skillEmoji = "💻",
                    isDungeonMode = false,
                    dungeonSessions = 0,
                    isWildernessChecked = true,
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    onTogglePause = {},
                    onExit = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_mode_screen_wilderness_mode.png")
    }

    @Test
    fun focusModeScreen_bothModesActive() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusModeScreen(
                    skillName = "Programação",
                    skillEmoji = "💻",
                    isDungeonMode = true,
                    dungeonSessions = 2,
                    isWildernessChecked = true,
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    onTogglePause = {},
                    onExit = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_mode_screen_both_modes_active.png")
    }

    @Test
    fun focusModeScreen_gracePeriodActive() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusModeScreen(
                    skillName = "Programação",
                    skillEmoji = "💻",
                    isDungeonMode = false,
                    dungeonSessions = 0,
                    isWildernessChecked = true,
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    onTogglePause = {},
                    onExit = {},
                    isGraceActive = true,
                    graceSecondsLeft = 2
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_mode_screen_grace_period_active.png")
    }

    @Test
    fun focusModeScreen_playerDead() {
        composeTestRule.setContent {
            HeroLogTheme {
                FocusModeScreen(
                    skillName = "Programação",
                    skillEmoji = "💻",
                    isDungeonMode = false,
                    dungeonSessions = 0,
                    isWildernessChecked = true,
                    timeLeft = 1200,
                    totalSeconds = 1500,
                    isRunning = true,
                    isPaused = false,
                    onTogglePause = {},
                    onExit = {},
                    isPlayerDead = true
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_mode_screen_player_dead.png")
    }
}
