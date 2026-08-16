package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.ui.focus.TimerSettingsModal
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
class TimerSettingsModalScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun timerSettingsModal_preset25_active_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                TimerSettingsModal(
                    isOpen = true,
                    onClose = {},
                    pomodoroSettings = PomodoroSettings(
                        focusDuration = 25,
                        shortBreakDuration = 5,
                        longBreakDuration = 15,
                        autoStartBreak = true,
                        autoStartFocus = false
                    ),
                    isRunning = false,
                    isBreakActive = false,
                    onSavePresetDuration = {},
                    onSaveCustomSettings = { _, _, _ -> },
                    onToggleAutoStartBreak = {},
                    onToggleAutoStartFocus = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/timer_settings_modal_preset25_active.png")
    }

    @Test
    fun timerSettingsModal_preset50_active_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                TimerSettingsModal(
                    isOpen = true,
                    onClose = {},
                    pomodoroSettings = PomodoroSettings(
                        focusDuration = 50,
                        shortBreakDuration = 10,
                        longBreakDuration = 20,
                        autoStartBreak = false,
                        autoStartFocus = false
                    ),
                    isRunning = false,
                    isBreakActive = false,
                    onSavePresetDuration = {},
                    onSaveCustomSettings = { _, _, _ -> },
                    onToggleAutoStartBreak = {},
                    onToggleAutoStartFocus = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/timer_settings_modal_preset50_active.png")
    }

    @Test
    fun timerSettingsModal_custom_valid_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                TimerSettingsModal(
                    isOpen = true,
                    onClose = {},
                    pomodoroSettings = PomodoroSettings(
                        focusDuration = 45, // Custom value outside [25, 50, 90]
                        shortBreakDuration = 10,
                        longBreakDuration = 20,
                        autoStartBreak = true,
                        autoStartFocus = true
                    ),
                    isRunning = false,
                    isBreakActive = false,
                    onSavePresetDuration = {},
                    onSaveCustomSettings = { _, _, _ -> },
                    onToggleAutoStartBreak = {},
                    onToggleAutoStartFocus = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/timer_settings_modal_custom_valid.png")
    }

    @Test
    fun timerSettingsModal_custom_invalid_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                TimerSettingsModal(
                    isOpen = true,
                    onClose = {},
                    pomodoroSettings = PomodoroSettings(
                        focusDuration = 200, // Invalid: exceeds 180m limit
                        shortBreakDuration = 5,
                        longBreakDuration = 15,
                        autoStartBreak = false,
                        autoStartFocus = false
                    ),
                    isRunning = false,
                    isBreakActive = false,
                    onSavePresetDuration = {},
                    onSaveCustomSettings = { _, _, _ -> },
                    onToggleAutoStartBreak = {},
                    onToggleAutoStartFocus = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/timer_settings_modal_custom_invalid.png")
    }

    @Test
    fun timerSettingsModal_toggles_both_active_screenshot() {
        composeTestRule.setContent {
            HeroLogTheme {
                TimerSettingsModal(
                    isOpen = true,
                    onClose = {},
                    pomodoroSettings = PomodoroSettings(
                        focusDuration = 90,
                        shortBreakDuration = 15,
                        longBreakDuration = 30,
                        autoStartBreak = true,
                        autoStartFocus = true
                    ),
                    isRunning = false,
                    isBreakActive = false,
                    onSavePresetDuration = {},
                    onSaveCustomSettings = { _, _, _ -> },
                    onToggleAutoStartBreak = {},
                    onToggleAutoStartFocus = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/timer_settings_modal_toggles_both_active.png")
    }
}
