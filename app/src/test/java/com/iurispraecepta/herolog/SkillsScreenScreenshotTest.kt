package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.ui.skills.SkillsScreen
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
class SkillsScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun skillsScreen_screenshot() {
        val sampleSkills = listOf(
            Skill(
                name = "Estudos",
                level = 10,
                xp = 320,
                emoji = "📚",
                tags = listOf("Direito", "Matemática")
            ),
            Skill(
                name = "Foco Maximo",
                level = 99,
                xp = 7920,
                emoji = "🧠",
                prestige = 1,
                tags = listOf("Pomodoro")
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                SkillsScreen(
                    skills = sampleSkills,
                    onAddTagToSkill = { _, _ -> },
                    onRemoveTagFromSkill = { _, _ -> },
                    onAddCustomSkill = { _, _ -> },
                    onDeleteSkill = {},
                    onPrestigeSkill = {},
                    onRenameSkill = { _, _ -> },
                    isCreateModalOpen = false,
                    onCreateModalOpenChange = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/skills_screen.png")
    }

    @Test
    fun skillsScreen_editingState_screenshot() {
        val sampleSkills = listOf(
            Skill(
                name = "Estudos",
                level = 10,
                xp = 320,
                emoji = "📚",
                tags = listOf("Direito", "Matemática")
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                SkillsScreen(
                    skills = sampleSkills,
                    onAddTagToSkill = { _, _ -> },
                    onRemoveTagFromSkill = { _, _ -> },
                    onAddCustomSkill = { _, _ -> },
                    onDeleteSkill = {},
                    onPrestigeSkill = {},
                    onRenameSkill = { _, _ -> },
                    isCreateModalOpen = false,
                    onCreateModalOpenChange = {},
                    initialEditingIdx = 0
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/skills_screen_editing.png")
    }

    @Test
    fun skillsScreen_createModalOpen_screenshot() {
        val sampleSkills = listOf(
            Skill(
                name = "Estudos",
                level = 10,
                xp = 320,
                emoji = "📚",
                tags = listOf("Direito", "Matemática")
            )
        )

        composeTestRule.setContent {
            HeroLogTheme {
                SkillsScreen(
                    skills = sampleSkills,
                    onAddTagToSkill = { _, _ -> },
                    onRemoveTagFromSkill = { _, _ -> },
                    onAddCustomSkill = { _, _ -> },
                    onDeleteSkill = {},
                    onPrestigeSkill = {},
                    onRenameSkill = { _, _ -> },
                    isCreateModalOpen = true,
                    onCreateModalOpenChange = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/skills_screen_create_modal.png")
    }
}
