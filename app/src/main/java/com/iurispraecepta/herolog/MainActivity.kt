package com.iurispraecepta.herolog

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.iurispraecepta.herolog.logic.DeleteSkillEligibility
import com.iurispraecepta.herolog.logic.InventoryLogic
import com.iurispraecepta.herolog.logic.SkillLogic
import com.iurispraecepta.herolog.logic.SkillOperationResult
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterSummary
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.ui.character.CharacterScreen
import com.iurispraecepta.herolog.ui.focus.FocusModeScreen
import com.iurispraecepta.herolog.ui.focus.FocusOrb
import com.iurispraecepta.herolog.ui.focus.FocusOrbSize
import com.iurispraecepta.herolog.ui.focus.RaidModeInfoBox
import com.iurispraecepta.herolog.ui.focus.RaidModeSegmentedControl
import com.iurispraecepta.herolog.ui.focus.lootChancePercentFrom
import com.iurispraecepta.herolog.ui.focus.raidModeFrom
import com.iurispraecepta.herolog.ui.focus.toLegacyFlags
import com.iurispraecepta.herolog.ui.inventory.InventoryScreen
import com.iurispraecepta.herolog.ui.skills.SkillsScreen
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.HeroLogTheme
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iurispraecepta.herolog.logic.toSummary
import com.iurispraecepta.herolog.ui.HeroLogViewModel
import com.iurispraecepta.herolog.ui.HeroLogViewModelFactory
import com.iurispraecepta.herolog.ui.focus.FocusCompletionFlow
import com.iurispraecepta.herolog.logic.quests.QuestLogic
import com.iurispraecepta.herolog.logic.focus.FocusSessionConfig
import com.iurispraecepta.herolog.model.CharacterState
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HeroLogTheme {
                var selectedTab by remember { mutableStateOf(0) }
                var isCreateModalOpen by remember { mutableStateOf(false) }

                val application = LocalContext.current.applicationContext as HeroLogApplication
                val heroLogViewModel: HeroLogViewModel = viewModel(factory = HeroLogViewModelFactory(application))
                val characterState by heroLogViewModel.characterState.collectAsState()
                var inspectingItem by remember { mutableStateOf<InventoryItem?>(null) }

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_STOP -> heroLogViewModel.onAppBackgrounded()
                            Lifecycle.Event.ON_START -> heroLogViewModel.onAppForegrounded()
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = when (selectedTab) {
                                            0 -> "HeroLog — Habilidades"
                                            1 -> "HeroLog — Personagem"
                                            2 -> "HeroLog — Inventário"
                                            else -> "HeroLog — Foco (Preview)"
                                        },
                                        color = Amber400
                                    )
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Stone900,
                                    titleContentColor = Amber400
                                )
                            )
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Stone900,
                                contentColor = Amber400,
                                indicator = { tabPositions ->
                                    if (selectedTab < tabPositions.size) {
                                        TabRowDefaults.SecondaryIndicator(
                                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                            color = Amber400
                                        )
                                    }
                                }
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = {
                                        Text(
                                            "Habilidades",
                                            color = if (selectedTab == 0) Amber400 else Color.Gray
                                        )
                                    }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = {
                                        Text(
                                            "Personagem",
                                            color = if (selectedTab == 1) Amber400 else Color.Gray
                                        )
                                    }
                                )
                                Tab(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    text = {
                                        Text(
                                            "Inventário",
                                            color = if (selectedTab == 2) Amber400 else Color.Gray
                                        )
                                    }
                                )
                                Tab(
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 },
                                    text = {
                                        Text(
                                            "Foco (Preview)",
                                            color = if (selectedTab == 3) Amber400 else Color.Gray
                                        )
                                    }
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (selectedTab == 0) {
                            FloatingActionButton(
                                onClick = { isCreateModalOpen = true },
                                containerColor = Amber400,
                                contentColor = Stone950
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Adicionar Habilidade"
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando habilidades...", color = Amber400)
                                    }
                                } else {
                                    SkillsScreen(
                                        skills = state.skills,
                                        onAddTagToSkill = { skillIdx, newTag ->
                                            heroLogViewModel.addTagToSkill(skillIdx, newTag)
                                        },
                                        onRemoveTagFromSkill = { skillIdx, tagIdx ->
                                            heroLogViewModel.removeTagFromSkill(skillIdx, tagIdx)
                                        },
                                        onAddCustomSkill = { name, emoji ->
                                            when (val result = heroLogViewModel.addCustomSkill(name, emoji)) {
                                                is SkillOperationResult.Success -> {
                                                    isCreateModalOpen = false
                                                }
                                                is SkillOperationResult.Error -> {
                                                    Log.d("HeroLog", "Falha ao adicionar skill: ${result.reason}")
                                                }
                                            }
                                        },
                                        onDeleteSkill = { idx ->
                                            val eligibility = heroLogViewModel.deleteSkill(idx)
                                            if (eligibility != DeleteSkillEligibility.Eligible) {
                                                Log.d("HeroLog", "Falha ao deletar skill: $eligibility")
                                            }
                                        },
                                        onPrestigeSkill = { idx ->
                                            heroLogViewModel.prestigeSkill(idx)
                                        },
                                        onRenameSkill = { idx, newName ->
                                            when (val result = heroLogViewModel.renameSkill(idx, newName)) {
                                                is SkillOperationResult.Success -> {
                                                    // Success state automatically flow via characterState
                                                }
                                                is SkillOperationResult.Error -> {
                                                    Log.d("HeroLog", "Falha ao renomear skill: ${result.reason}")
                                                }
                                            }
                                        },
                                        isCreateModalOpen = isCreateModalOpen,
                                        onCreateModalOpenChange = { isCreateModalOpen = it }
                                    )
                                }
                            }
                            1 -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando personagem...", color = Amber400)
                                    }
                                } else {
                                    CharacterScreen(
                                        character = state.toSummary(),
                                        equippedEquipment = state.equippedEquipment ?: listOf(null, null, null),
                                        activeBuffs = InventoryLogic.activeBuffs(state.inventory),
                                        onUnequipItem = { slotIdx -> heroLogViewModel.unequipItem(slotIdx) },
                                        ownedTitles = state.ownedTitles ?: emptyList(),
                                        onEquipTitle = { titleId -> heroLogViewModel.equipTitle(titleId) }
                                    )
                                }
                            }
                            2 -> {
                                val state = characterState
                                if (state == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Carregando inventario...", color = Amber400)
                                    }
                                } else {
                                    InventoryScreen(
                                        inventory = state.inventory,
                                        inspectingItem = inspectingItem,
                                        onInspectItem = { item -> inspectingItem = item },
                                        onCloseInspection = { inspectingItem = null },
                                        onEquipItem = { item, slotIdx ->
                                            heroLogViewModel.equipItem(item, slotIdx)
                                            inspectingItem = null
                                        },
                                        onSellItem = { item ->
                                            heroLogViewModel.sellItem(item)
                                            inspectingItem = null
                                        },
                                        onDiscardItem = { item ->
                                            heroLogViewModel.discardItem(item)
                                            inspectingItem = null
                                        }
                                    )
                                }
                            }
                            else -> {
                                FocusOrbPreviewScreen(
                                    viewModel = heroLogViewModel,
                                    characterState = characterState
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FocusOrbPreviewScreen(
    viewModel: HeroLogViewModel,
    characterState: CharacterState?,
    modifier: Modifier = Modifier
) {
    if (characterState == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Carregando personagem...", color = Amber400)
        }
        return
    }

    val selectedSkill = characterState.skills.firstOrNull()
    if (selectedSkill == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Por favor, crie uma habilidade primeiro para poder iniciar o Foco!",
                color = Amber400,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    var isDungeonModePreview by remember { mutableStateOf(false) }
    var isWildernessPreview by remember { mutableStateOf(false) }

    val focusState by viewModel.focusSessionState.collectAsState()
    val dungeonSessionsProgress by viewModel.dungeonSessionsProgress.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (focusState.isFocusCompleted) {
            val rewards = focusState.pendingRewardsCalculation
            if (rewards != null) {
                val streak = characterState.streak
                val todayString = QuestLogic.toDateStringJs(java.util.Date())
                val shouldShowStreakCelebration = characterState.lastStudyDate != todayString
                val selectedSkillForSession = characterState.skills.getOrNull(rewards.skillIdx)
                val skillTags = selectedSkillForSession?.tags ?: emptyList()

                FocusCompletionFlow(
                    rewardsCalculation = rewards,
                    pauseCount = focusState.pauseCount,
                    streak = streak,
                    shouldShowStreakCelebration = shouldShowStreakCelebration,
                    skillTags = skillTags,
                    onConfirm = { editedNotes, selectedTag ->
                        viewModel.confirmFocusSession(editedNotes, selectedTag)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Erro: Cálculo de recompensa pendente ausente.", color = Amber400)
                }
            }
        } else if (focusState.isRunning || focusState.isPlayerDead) {
            val config = focusState.config
            val selectedSkillForSession = characterState.skills.getOrNull(config?.selectedSkillIdx ?: 0)
            val skillName = selectedSkillForSession?.name ?: "Habilidade"
            val skillEmoji = selectedSkillForSession?.emoji ?: "💻"

            FocusModeScreen(
                skillName = skillName,
                skillEmoji = skillEmoji,
                isDungeonMode = config?.isDungeonMode ?: false,
                dungeonSessions = config?.dungeonSessions ?: 0,
                isWildernessChecked = config?.isWildernessChecked ?: false,
                timeLeft = focusState.timeLeft,
                totalSeconds = focusState.totalSeconds,
                isRunning = focusState.isRunning,
                isPaused = focusState.isPaused,
                onTogglePause = { viewModel.togglePauseQuest() },
                onExit = { viewModel.cancelSession() },
                isGraceActive = focusState.isGraceActive,
                graceSecondsLeft = focusState.graceSecondsLeft,
                isPlayerDead = focusState.isPlayerDead,
                onReturnToFocusCap = { viewModel.returnToFocusFromGrace() },
                onRespawn = { viewModel.respawnHero() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val currentRaidMode = raidModeFrom(isDungeonModePreview, isWildernessPreview)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RaidModeSegmentedControl(
                    mode = currentRaidMode,
                    isRunning = false,
                    onModeSelected = { newMode ->
                        val (dungeon, wilderness) = newMode.toLegacyFlags()
                        isDungeonModePreview = dungeon
                        isWildernessPreview = wilderness
                    },
                    onLog = {}
                )

                Spacer(modifier = Modifier.height(8.dp))

                RaidModeInfoBox(
                    mode = currentRaidMode,
                    dungeonSessions = dungeonSessionsProgress,
                    dungeonOnCooldown = false,
                    lootChancePercent = lootChancePercentFrom(
                        studiedMinutes = 25,
                        isDungeon = isDungeonModePreview,
                        equippedTitleId = characterState.equippedTitle
                    ),
                    onShowDungeonHelp = {},
                    onShowWildernessHelp = {},
                    onShowStandardHelp = {}
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val skillIdx = characterState.skills.indexOf(selectedSkill)
                        val config = FocusSessionConfig(
                            selectedSkillIdx = skillIdx,
                            isWildernessChecked = isWildernessPreview,
                            isDungeonMode = isDungeonModePreview,
                            dungeonSessions = dungeonSessionsProgress
                        )
                        viewModel.startSession(config, durationMinutes = 25)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entrar em Modo Foco (25 min)")
                }

                Spacer(modifier = Modifier.height(24.dp))

                FocusOrb(
                    timeLeft = 1500,
                    totalSeconds = 1500,
                    isRunning = false,
                    isPaused = false,
                    isBreakActive = false,
                    size = FocusOrbSize.STANDARD
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
