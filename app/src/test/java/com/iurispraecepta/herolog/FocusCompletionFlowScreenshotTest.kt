package com.iurispraecepta.herolog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.iurispraecepta.herolog.logic.focus.DroppedTitle
import com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation
import com.iurispraecepta.herolog.logic.focus.LootItem
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.ui.focus.CompletionShell
import com.iurispraecepta.herolog.ui.focus.LootDropScreen
import com.iurispraecepta.herolog.ui.focus.SessionNotesScreen
import com.iurispraecepta.herolog.ui.focus.SessionSummaryScreen
import com.iurispraecepta.herolog.ui.focus.StreakCelebrationScreen
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
class FocusCompletionFlowScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.mainClock.autoAdvance = false
    }

    private val baseRewards = FocusRewardsCalculation(
        skillIdx = 0,
        skillName = "Programação",
        xpEarned = 150,
        goldEarned = 45,
        durationMins = 25,
        dungeonClearGoldBonus = 0,
        hasUsedDoubleLoot = false,
        hasUsedFocusElixir = false,
        hasUsedRuneFortune = false,
        hasUsedCrystalClarity = false,
        usedEquipmentIndicesAndCharges = emptyList(),
        lootedItems = emptyList(),
        droppedTitle = null,
        isWildernessChecked = false,
        isDungeonMode = false,
        comboBonusPercent = 0
    )

    @Test
    fun focusCompletionFlow_summary_noCombo_noLoot() {
        composeTestRule.setContent {
            HeroLogTheme {
                CompletionShell(onNext = {}, isLastStep = false) {
                    SessionSummaryScreen(
                        rewardsCalculation = baseRewards,
                        pauseCount = 0,
                        streak = 3
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_completion_flow_summary_no_combo_no_loot.png")
    }

    @Test
    fun focusCompletionFlow_summary_withComboBonus() {
        val rewardsWithCombo = baseRewards.copy(comboBonusPercent = 15)
        composeTestRule.setContent {
            HeroLogTheme {
                CompletionShell(onNext = {}, isLastStep = false) {
                    SessionSummaryScreen(
                        rewardsCalculation = rewardsWithCombo,
                        pauseCount = 1,
                        streak = 5
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_completion_flow_summary_with_combo_bonus.png")
    }

    @Test
    fun focusCompletionFlow_streakCelebration() {
        composeTestRule.setContent {
            HeroLogTheme {
                CompletionShell(onNext = {}, isLastStep = false) {
                    StreakCelebrationScreen(streakPreview = 4)
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_completion_flow_streak_celebration.png")
    }

    @Test
    fun focusCompletionFlow_lootDrop() {
        val itemComum = LootItem(
            name = "Poção de Mana",
            emoji = "🧪",
            desc = "Recupera 50 MP instantaneamente.",
            buff = BuffType.CelestinePotion,
            price = 20,
            isEquipment = false,
            charges = 1,
            maxCharges = 1,
            rarity = Rarity.Comum
        )
        val itemEspecial = LootItem(
            name = "Grimório Arcano",
            emoji = "📖",
            desc = "Aumenta o XP ganho em sessões de foco.",
            buff = BuffType.SilverGrimoire,
            price = 100,
            isEquipment = true,
            charges = 5,
            maxCharges = 5,
            rarity = Rarity.Especial
        )
        val droppedTitle = DroppedTitle(
            id = "focus_master",
            name = "Mestre da Concentração",
            emoji = "👑"
        )

        composeTestRule.setContent {
            HeroLogTheme {
                CompletionShell(onNext = {}, isLastStep = false) {
                    LootDropScreen(
                        lootedItems = listOf(itemComum, itemEspecial),
                        droppedTitle = droppedTitle
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_completion_flow_loot_drop.png")
    }

    @Test
    fun focusCompletionFlow_notes_noSkillTags() {
        composeTestRule.setContent {
            HeroLogTheme {
                CompletionShell(onNext = {}, isLastStep = true) {
                    SessionNotesScreen(
                        completionNotes = "Estudei coroutines em Kotlin e Kotlin Compose.",
                        onNotesChange = {},
                        completionTag = "",
                        onTagChange = {},
                        skillTags = emptyList()
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_completion_flow_notes_no_skill_tags.png")
    }

    @Test
    fun focusCompletionFlow_notes_withSkillTags() {
        composeTestRule.setContent {
            HeroLogTheme {
                CompletionShell(onNext = {}, isLastStep = true) {
                    SessionNotesScreen(
                        completionNotes = "Pratiquei recomposição e gerenciamento de estado.",
                        onNotesChange = {},
                        completionTag = "Jetpack Compose",
                        onTagChange = {},
                        skillTags = listOf("Jetpack Compose", "Coroutines", "Room DB")
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/focus_completion_flow_notes_with_skill_tags.png")
    }
}
