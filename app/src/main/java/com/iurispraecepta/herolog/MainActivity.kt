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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sampleSkills = listOf(
            Skill(
                id = "sample_skill_estudos",
                name = "Estudos",
                level = 12,
                xp = 200,
                emoji = "📚"
            ),
            Skill(
                id = "sample_skill_programacao",
                name = "Programação",
                level = 45,
                xp = 1200,
                emoji = "💻",
                prestige = 1,
                tags = listOf("Kotlin", "Android")
            ),
            Skill(
                id = "sample_skill_academia",
                name = "Academia",
                level = 99,
                xp = 7000,
                emoji = "🏋️"
            )
        )

        setContent {
            HeroLogTheme {
                var selectedTab by remember { mutableStateOf(0) }
                var skills by remember { mutableStateOf(sampleSkills) }
                var isCreateModalOpen by remember { mutableStateOf(false) }

                val application = LocalContext.current.applicationContext as HeroLogApplication
                val heroLogViewModel: HeroLogViewModel = viewModel(factory = HeroLogViewModelFactory(application))
                val characterState by heroLogViewModel.characterState.collectAsState()
                var inspectingItem by remember { mutableStateOf<InventoryItem?>(null) }

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
                                SkillsScreen(
                                    skills = skills,
                                    onAddTagToSkill = { skillIdx, newTag ->
                                        skills = SkillLogic.addTagToSkill(skills, skillIdx, newTag)
                                    },
                                    onRemoveTagFromSkill = { skillIdx, tagIdx ->
                                        skills = SkillLogic.removeTagFromSkill(skills, skillIdx, tagIdx)
                                    },
                                    onAddCustomSkill = { name, emoji ->
                                        when (val result = SkillLogic.addCustomSkill(skills, name, emoji)) {
                                            is SkillOperationResult.Success -> {
                                                skills = result.newSkills
                                                isCreateModalOpen = false
                                            }
                                            is SkillOperationResult.Error -> {
                                                Log.d("HeroLog", "Falha ao adicionar skill: ${result.reason}")
                                            }
                                        }
                                    },
                                    onDeleteSkill = { idx ->
                                        when (val eligibility = SkillLogic.canDeleteSkill(skills, isFocusSessionRunning = false)) {
                                            DeleteSkillEligibility.Eligible -> {
                                                skills = SkillLogic.deleteSkillAt(skills, idx)
                                            }
                                            else -> {
                                                Log.d("HeroLog", "Falha ao deletar skill: $eligibility")
                                            }
                                        }
                                    },
                                    onPrestigeSkill = { idx ->
                                        if (idx in skills.indices) {
                                            val skill = skills[idx]
                                            if (SkillLogic.isPrestigeEligible(skill)) {
                                                val updatedSkill = SkillLogic.applyPrestige(skill)
                                                skills = skills.toMutableList().apply { this[idx] = updatedSkill }
                                            } else {
                                                Log.d("HeroLog", "Skill não é elegível para prestígio: ${skill.name} (nível ${skill.level})")
                                            }
                                        }
                                    },
                                    onRenameSkill = { idx, newName ->
                                        when (val result = SkillLogic.renameSkill(skills, idx, newName)) {
                                            is SkillOperationResult.Success -> {
                                                skills = result.newSkills
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
                                FocusOrbPreviewScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FocusOrbPreviewScreen(modifier: Modifier = Modifier) {
    var showImmersiveMode by remember { mutableStateOf(false) }
    var isDungeonModePreview by remember { mutableStateOf(false) }
    var isWildernessPreview by remember { mutableStateOf(false) }
    var isPausedImmersive by remember { mutableStateOf(false) }
    var timeLeftImmersive by remember { mutableStateOf(90) }
    val totalSecondsImmersive = 90

    LaunchedEffect(showImmersiveMode, isPausedImmersive) {
        if (showImmersiveMode && !isPausedImmersive) {
            while (isActive && timeLeftImmersive > 0) {
                delay(1000)
                if (timeLeftImmersive > 0) {
                    timeLeftImmersive -= 1
                }
            }
        }
    }

    val totalSeconds = 90
    var timeLeft by remember { mutableStateOf(90) }
    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isBreakActive by remember { mutableStateOf(false) }
    var orbSize by remember { mutableStateOf(FocusOrbSize.STANDARD) }

    LaunchedEffect(isRunning, isPaused) {
        if (isRunning && !isPaused) {
            while (isActive && timeLeft > 0) {
                delay(1000)
                if (timeLeft > 0) {
                    timeLeft -= 1
                }
            }
            if (timeLeft <= 0) {
                isRunning = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val currentRaidMode = raidModeFrom(isDungeonModePreview, isWildernessPreview)

            RaidModeSegmentedControl(
                mode = currentRaidMode,
                isRunning = isRunning || showImmersiveMode,
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
                dungeonSessions = 2,
                dungeonOnCooldown = false,
                lootChancePercent = lootChancePercentFrom(
                    studiedMinutes = 25,
                    isDungeon = false,
                    equippedTitleId = null
                ),
                onShowDungeonHelp = {},
                onShowWildernessHelp = {},
                onShowStandardHelp = {}
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    timeLeftImmersive = 90
                    isPausedImmersive = false
                    showImmersiveMode = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar em Modo Foco")
            }

            Spacer(modifier = Modifier.height(24.dp))

            FocusOrb(
                timeLeft = timeLeft,
                totalSeconds = totalSeconds,
                isRunning = isRunning,
                isPaused = isPaused,
                isBreakActive = isBreakActive,
                size = orbSize
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (!isRunning) {
                            if (timeLeft <= 0) timeLeft = 90
                            isRunning = true
                            isPaused = false
                        } else {
                            isPaused = !isPaused
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Play/Pause")
                }

                Button(
                    onClick = {
                        isRunning = false
                        isPaused = false
                        timeLeft = 90
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { isBreakActive = !isBreakActive },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Alternar Descanso")
                }

                Button(
                    onClick = {
                        orbSize = if (orbSize == FocusOrbSize.STANDARD) FocusOrbSize.FULLSCREEN else FocusOrbSize.STANDARD
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Alternar Tamanho")
                }
            }
        }

        if (showImmersiveMode) {
            FocusModeScreen(
                skillName = "Programação",
                skillEmoji = "💻",
                isDungeonMode = isDungeonModePreview,
                dungeonSessions = 2,
                isWildernessChecked = isWildernessPreview,
                timeLeft = timeLeftImmersive,
                totalSeconds = totalSecondsImmersive,
                isRunning = true,
                isPaused = isPausedImmersive,
                onTogglePause = { isPausedImmersive = !isPausedImmersive },
                onExit = { showImmersiveMode = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
