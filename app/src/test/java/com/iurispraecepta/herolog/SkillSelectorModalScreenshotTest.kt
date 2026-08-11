package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.ui.skills.SkillSelectorModal
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
class SkillSelectorModalScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun skillSelectorModal_screenshot() {
        val sampleSkills = listOf(
            Skill(
                name = "Meditação",
                level = 5,
                xp = 120,
                emoji = "🧘"
            ),
            Skill(
                name = "Programação",
                level = 12,
                xp = 450,
                emoji = "💻"
            ),
            Skill(
                name = "Foco",
                level = 1,
                xp = 0,
                emoji = "🎯",
                prestige = 2,
                tags = listOf("Kotlin", "Compose")
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                SkillSelectorModal(
                    isOpen = true,
                    onClose = {},
                    skills = sampleSkills,
                    selectedSkillIdx = 1,
                    onSelectSkill = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/skill_selector_modal.png")
    }
}
