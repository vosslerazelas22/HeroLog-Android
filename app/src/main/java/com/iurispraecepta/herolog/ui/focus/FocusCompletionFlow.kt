package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.focus.DroppedTitle
import com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation
import com.iurispraecepta.herolog.logic.focus.LootItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.ui.theme.Stone950

fun getRank(isWildernessChecked: Boolean, pauseCount: Int): String = when {
    isWildernessChecked && pauseCount == 0 -> "S+"
    pauseCount == 0 -> "S"
    pauseCount == 1 -> "A"
    pauseCount <= 2 -> "B"
    pauseCount <= 4 -> "C"
    else -> "F"
}

fun getRankDescription(isWildernessChecked: Boolean, pauseCount: Int): String = when {
    isWildernessChecked && pauseCount == 0 -> "Sobrevivente Cognitivo — Lenda"
    pauseCount == 0 -> "Sem Pausas — Lendário"
    pauseCount == 1 -> "Pausa Única — Heróico"
    pauseCount <= 2 -> "Foco Estável — Exquisito"
    pauseCount <= 4 -> "Distração Parcial — Comum"
    else -> "Pausas Constantes — Instável"
}

@Composable
fun CompletionShell(
    onNext: () -> Unit,
    isLastStep: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Stone950)
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC29544),
                    contentColor = Stone950
                ),
                border = BorderStroke(1.dp, Color(0xFFE9C37A))
            ) {
                Text(
                    text = if (isLastStep) "RECEBER RECOMPENSAS" else "CONTINUAR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    color = Stone950
                )
            }
        }
    }
}

@Composable
fun StreakCelebrationScreen(
    streakPreview: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = Color(0xFFF97316),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Você manteve a chama acesa por mais um dia",
            color = Color(0xFFA8A29E),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "$streakPreview ${if (streakPreview == 1) "dia" else "dias"}",
            color = Color(0xFFFCD34D),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SessionSummaryScreen(
    rewardsCalculation: FocusRewardsCalculation,
    pauseCount: Int,
    streak: Int,
    modifier: Modifier = Modifier
) {
    val rank = getRank(rewardsCalculation.isWildernessChecked, pauseCount)
    val rankDesc = getRankDescription(rewardsCalculation.isWildernessChecked, pauseCount)
    val effectiveStreak = if (streak > 0) streak else 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SESSÃO CONCLUÍDA",
            color = Color(0xFFE2B054),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = rank,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFCD34D)
            )
            Text(
                text = rankDesc,
                fontSize = 14.sp,
                color = Color(0xFFA8A29E),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1917), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF292524), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("DURAÇÃO DA SESSÃO", color = Color(0xFFA8A29E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${rewardsCalculation.durationMins} MIN", color = Color(0xFFF5F5F4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SEQUÊNCIA DE CHAMA", color = Color(0xFFA8A29E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("🔥 $effectiveStreak ${if (effectiveStreak == 1) "DIA" else "DIAS"}", color = Color(0xFFF5F5F4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (rewardsCalculation.comboBonusPercent > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("BÔNUS DE MULTIPLICADOR COMBO", color = Color(0xFFF14D2A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("+${rewardsCalculation.comboBonusPercent}%", color = Color(0xFFF14D2A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1917), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE2B054).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ +${rewardsCalculation.xpEarned} XP",
                    color = Color(0xFF34D399),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "💎 +${rewardsCalculation.goldEarned + rewardsCalculation.dungeonClearGoldBonus} GP",
                    color = Color(0xFFE2B054),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LootDropScreen(
    lootedItems: List<LootItem>,
    droppedTitle: DroppedTitle?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "TESOURO CONQUISTADO",
            color = Color(0xFFC084FC),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )

        lootedItems.forEach { item ->
            val isEspecial = item.rarity == Rarity.Especial
            val borderColor = if (isEspecial) Color(0xFFA855F7) else Color(0xFF44403C)
            val badgeColor = if (isEspecial) Color(0xFFC084FC) else Color(0xFFA8A29E)
            val badgeText = if (isEspecial) "★ ESPECIAL ★" else "COMUM"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1917)),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.emoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = item.name, color = Color(0xFFF5F5F4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = badgeText, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = item.desc, color = Color(0xFFA8A29E), fontSize = 12.sp)
                    }
                }
            }
        }

        droppedTitle?.let { title ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1917)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title.emoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = title.name, color = Color(0xFFFCD34D), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "★ TÍTULO RARO ★", color = Color(0xFFFCD34D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Pode ser equipado na tela de Títulos.", color = Color(0xFFA8A29E), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionNotesScreen(
    completionNotes: String,
    onNotesChange: (String) -> Unit,
    completionTag: String,
    onTagChange: (String) -> Unit,
    skillTags: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📜 CRÔNICA DA MISSÃO",
            color = Color(0xFFE2B054),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )

        OutlinedTextField(
            value = completionNotes,
            onValueChange = onNotesChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            placeholder = {
                Text("O que você aprendeu ou fez nesta sessão?", color = Color(0xFF78716C), fontSize = 14.sp)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1C1917),
                unfocusedContainerColor = Color(0xFF1C1917),
                focusedBorderColor = Color(0xFFE2B054),
                unfocusedBorderColor = Color(0xFF292524),
                focusedTextColor = Color(0xFFF5F5F4),
                unfocusedTextColor = Color(0xFFF5F5F4)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (skillTags.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "VINCULAR SUBSKILL",
                    color = Color(0xFFA8A29E),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    skillTags.forEach { tag ->
                        val isSelected = completionTag == tag
                        val chipBg = if (isSelected) Color(0xFFD97706).copy(alpha = 0.2f) else Color(0xFF1C1917)
                        val chipBorder = if (isSelected) Color(0xFFF59E0B) else Color(0xFF292524)
                        val chipTextColor = if (isSelected) Color(0xFFFCD34D) else Color(0xFFA8A29E)
                        val chipText = if (isSelected) "$tag ✓" else tag

                        Box(
                            modifier = Modifier
                                .background(chipBg, RoundedCornerShape(8.dp))
                                .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isSelected) onTagChange("") else onTagChange(tag)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chipText,
                                color = chipTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FocusCompletionFlow(
    rewardsCalculation: FocusRewardsCalculation,
    pauseCount: Int,
    streak: Int,
    shouldShowStreakCelebration: Boolean,
    skillTags: List<String>,
    initialNotes: String = "",
    onConfirm: (editedNotes: String, selectedTag: String) -> Unit,
    initialStepIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val hasLoot = remember(rewardsCalculation) {
        rewardsCalculation.lootedItems.isNotEmpty() || rewardsCalculation.droppedTitle != null
    }

    val steps = remember(shouldShowStreakCelebration, hasLoot) {
        buildList {
            if (shouldShowStreakCelebration) add("streak")
            add("summary")
            if (hasLoot) add("loot")
            add("notes")
        }
    }

    var stepIndex by remember(initialStepIndex, steps) {
        mutableStateOf(initialStepIndex.coerceIn(0, steps.lastIndex))
    }
    var completionNotes by remember(initialNotes) { mutableStateOf(initialNotes) }
    var completionTag by remember { mutableStateOf("") }

    val streakPreview = if (shouldShowStreakCelebration) streak + 1 else streak
    val currentStep = steps.getOrElse(stepIndex) { "summary" }
    val isLastStep = stepIndex == steps.lastIndex

    CompletionShell(
        onNext = {
            if (isLastStep) {
                onConfirm(completionNotes, completionTag)
            } else {
                stepIndex += 1
            }
        },
        isLastStep = isLastStep,
        modifier = modifier
    ) {
        when (currentStep) {
            "streak" -> StreakCelebrationScreen(streakPreview = streakPreview)
            "summary" -> SessionSummaryScreen(
                rewardsCalculation = rewardsCalculation,
                pauseCount = pauseCount,
                streak = streak
            )
            "loot" -> LootDropScreen(
                lootedItems = rewardsCalculation.lootedItems,
                droppedTitle = rewardsCalculation.droppedTitle
            )
            "notes" -> SessionNotesScreen(
                completionNotes = completionNotes,
                onNotesChange = { completionNotes = it },
                completionTag = completionTag,
                onTagChange = { completionTag = it },
                skillTags = skillTags
            )
        }
    }
}
