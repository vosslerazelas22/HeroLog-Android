package com.iurispraecepta.herolog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.ui.focus.RaidMode
import com.iurispraecepta.herolog.ui.focus.RaidModeInfoBox
import com.iurispraecepta.herolog.ui.focus.RaidModeSegmentedControl
import com.iurispraecepta.herolog.ui.focus.lootChancePercentFrom
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
class RaidModeSectionScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.mainClock.autoAdvance = false
    }

    @Composable
    private fun RaidModePreviewContainer(
        mode: RaidMode,
        isRunning: Boolean = false,
        dungeonCooldownRemainingMs: Long = 0L,
        dungeonSessions: Int = 2
    ) {
        HeroLogTheme {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1C1917))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RaidModeSegmentedControl(
                    mode = mode,
                    isRunning = isRunning,
                    dungeonCooldownRemainingMs = dungeonCooldownRemainingMs,
                    onModeSelected = {},
                    onLog = {}
                )
                RaidModeInfoBox(
                    mode = mode,
                    dungeonSessions = dungeonSessions,
                    dungeonOnCooldown = dungeonCooldownRemainingMs > 0L,
                    lootChancePercent = lootChancePercentFrom(
                        studiedMinutes = 25,
                        isDungeon = mode == RaidMode.MASMORRA,
                        equippedTitleId = null
                    ),
                    onShowDungeonHelp = {},
                    onShowWildernessHelp = {},
                    onShowStandardHelp = {}
                )
            }
        }
    }

    @Test
    fun raidModeSection_padraoActive() {
        composeTestRule.setContent {
            RaidModePreviewContainer(mode = RaidMode.PADRAO)
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/raid_mode_section_padrao_active.png")
    }

    @Test
    fun raidModeSection_masmorraActive() {
        composeTestRule.setContent {
            RaidModePreviewContainer(mode = RaidMode.MASMORRA)
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/raid_mode_section_masmorra_active.png")
    }

    @Test
    fun raidModeSection_selvagemActive() {
        composeTestRule.setContent {
            RaidModePreviewContainer(mode = RaidMode.SELVAGEM)
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/raid_mode_section_selvagem_active.png")
    }

    @Test
    fun raidModeSection_disabledRunning() {
        composeTestRule.setContent {
            RaidModePreviewContainer(mode = RaidMode.PADRAO, isRunning = true)
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/raid_mode_section_disabled_running.png")
    }

    @Test
    fun raidModeSection_masmorraCooldown() {
        composeTestRule.setContent {
            RaidModePreviewContainer(
                mode = RaidMode.PADRAO,
                dungeonCooldownRemainingMs = 3600000L
            )
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/raid_mode_section_masmorra_cooldown.png")
    }
}
