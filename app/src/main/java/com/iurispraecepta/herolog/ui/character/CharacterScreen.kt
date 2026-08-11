package com.iurispraecepta.herolog.ui.character

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.R
import com.iurispraecepta.herolog.data.TITLE_CATALOG
import com.iurispraecepta.herolog.logic.CombatLogic
import com.iurispraecepta.herolog.model.CharClass
import com.iurispraecepta.herolog.model.CharacterSummary
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ItemInspectAction
import com.iurispraecepta.herolog.ui.components.ItemInspectModal
import com.iurispraecepta.herolog.ui.components.ItemInspectVariant

private val Rose400 = Color(0xFFFB7185)
private val Red600 = Color(0xFFDC2626)
private val Emerald500 = Color(0xFF10B981)
private val Purple950 = Color(0xFF3B0764)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterScreen(
    character: CharacterSummary,
    equippedEquipment: List<InventoryItem?>,
    activeBuffs: List<InventoryItem>,
    onUnequipItem: (Int) -> Unit,
    ownedTitles: List<String> = emptyList(),
    onEquipTitle: (String?) -> Unit = {}
) {
    var isTitleModalOpen by remember { mutableStateOf(false) }
    var inspectingItem by remember { mutableStateOf<InventoryItem?>(null) }
    var inspectingSlotIdx by remember { mutableStateOf<Int?>(null) }

    val modalActions = remember(inspectingItem, inspectingSlotIdx) {
        if (inspectingItem != null && inspectingSlotIdx != null) {
            val slotIdx = inspectingSlotIdx!!
            listOf(
                ItemInspectAction(
                    label = "Desequipar",
                    onClick = {
                        onUnequipItem(slotIdx)
                        inspectingItem = null
                        inspectingSlotIdx = null
                    },
                    variant = ItemInspectVariant.Danger
                )
            )
        } else {
            emptyList()
        }
    }

    val avatarRes = when (character.charClass) {
        CharClass.Mage -> R.drawable.mage_idle
        CharClass.Warrior -> R.drawable.warrior_idle
        CharClass.Ranger -> R.drawable.ranger_idle
    }

    val classLabel = when (character.charClass) {
        CharClass.Mage -> "🧙 Mago d'Arraia"
        CharClass.Warrior -> "🛡️ Guerreiro de Aço"
        CharClass.Ranger -> "🏹 Patrulheiro Silvestre"
    }

    val titleItem = if (character.equippedTitle != null) {
        TITLE_CATALOG.firstOrNull { it.id == character.equippedTitle }
    } else {
        null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0C0A09).copy(alpha = 0.2f))
                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Avatar + Identity
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar Box (115x115)
                Box(
                    modifier = Modifier
                        .size(115.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C0A09))
                        .border(2.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = character.charName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Purple950.copy(alpha = 0.4f))
                                )
                            )
                    )
                }

                // Identity Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = character.charName,
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFFFDE68A), // amber-200
                            letterSpacing = 0.02.em,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Equipped Title or "+ Equipar Título" button
                    if (titleItem != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.1f))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .clickable { isTitleModalOpen = true }
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${titleItem.emoji} ${titleItem.name}",
                                style = TextStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFCD34D), // amber-300
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1C1917).copy(alpha = 0.6f))
                                .border(1.dp, Color(0xFF292524), RoundedCornerShape(4.dp))
                                .clickable { isTitleModalOpen = true }
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ Equipar Título",
                                style = TextStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 11.sp,
                                    color = Color(0xFFA8A29E), // stone-400
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }

                    Text(
                        text = classLabel.uppercase(),
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFC084FC), // purple-400
                            letterSpacing = 0.1.em,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            // 2 Tiles Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Streak Tile
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C0A09).copy(alpha = 0.4f))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🔥 SEQUÊNCIA",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 10.sp,
                            color = Color(0xFFFEF3C7).copy(alpha = 0.5f),
                            letterSpacing = 0.05.em
                        )
                    )
                    Text(
                        text = "${character.streak} dia(s)",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFFDE68A)
                        )
                    )
                    Text(
                        text = "(Recorde: ${character.bestStreak}d)",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 10.sp,
                            color = Color(0xFFFEF3C7).copy(alpha = 0.4f)
                        )
                    )
                }

                // Combat Level Tile
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C0A09).copy(alpha = 0.4f))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⚔️ NÍVEL DE COMBATE",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 10.sp,
                            color = Color(0xFFFEF3C7).copy(alpha = 0.5f),
                            letterSpacing = 0.05.em
                        )
                    )
                    Text(
                        text = "${character.combatLevel}",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFE2B054)
                        )
                    )
                    val reqXp = CombatLogic.requiredXpForCombatLevel(character.combatLevel)
                    val progress = (character.combatXP.toFloat() / reqXp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                    Text(
                        text = "${character.combatXP} / ${reqXp} XP",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color(0xFF34D399) // emerald-400
                        )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF1C1917))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Emerald500)
                        )
                    }
                }
            }

            // Compact Row Foco Total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0C0A09).copy(alpha = 0.4f))
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⏱️ Foco Total:",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp,
                        color = Color(0xFFFEF3C7).copy(alpha = 0.8f)
                    )
                )
                val hours = character.totalMinutes / 60
                val mins = character.totalMinutes % 60
                val formattedMins = String.format("%02d", mins)
                Text(
                    text = "${hours}h${formattedMins}m",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFFDE68A)
                    )
                )
            }

            // Compact Row HP
            val infiniteTransition = rememberInfiniteTransition(label = "hp_heart_pulse")
            val heartAlpha by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "heart_alpha"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0C0A09).copy(alpha = 0.4f))
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "HP",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier
                                .size(14.dp)
                                .alpha(heartAlpha)
                        )
                        Text(
                            text = "HP (PONTOS DE VIDA)",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = 12.sp,
                                color = Rose400
                            )
                        )
                    }
                    Text(
                        text = "${character.hp} / ${character.maxHp}",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Rose400
                        )
                    )
                }
                val hpProgress = (character.hp.toFloat() / character.maxHp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1C1917))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(hpProgress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Red600, Rose400))
                            )
                    )
                }
            }
        }

        // SLOTS DE EQUIPAMENTO (3 SLOTS)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🛡️ EQUIPAMENTOS EQUIPADOS (3 SLOTS)",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFFCD34D), // amber-300
                    letterSpacing = 0.05.em
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (slotIdx in 0..2) {
                    val item = equippedEquipment.getOrNull(slotIdx)
                    if (item != null) {
                        // Occupied Slot
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1C1917))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable {
                                    inspectingItem = item
                                    inspectingSlotIdx = slotIdx
                                }
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "×",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFFF87171) // red-400
                                        ),
                                        modifier = Modifier.clickable {
                                            onUnequipItem(slotIdx)
                                        }
                                    )
                                }
                                Text(
                                    text = item.emoji,
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = item.name,
                                    style = TextStyle(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFFFCD34D),
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.isEquipment == true) {
                                    val charges = item.charges ?: 0
                                    val maxCharges = item.maxCharges ?: 8
                                    Text(
                                        text = "$charges/$maxCharges Cargas",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = Color(0xFF34D399) // emerald-400
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty Slot
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0C0A09).copy(alpha = 0.2f))
                                .border(
                                    1.dp,
                                    Color(0xFF44403C).copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Slot ${slotIdx + 1}\nVazio",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color(0xFFA8A29E), // stone-400
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }

        // BÊNÇÃOS & ELIXIRES ATIVOS
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "BÊNÇÃOS & ELIXIRES ATIVOS",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFFCD34D), // amber-300
                    letterSpacing = 0.05.em
                )
            )

            if (activeBuffs.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    activeBuffs.forEach { buff ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Purple950.copy(alpha = 0.4f))
                                .border(
                                    1.dp,
                                    Color(0xFFA855F7).copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    inspectingItem = buff
                                    inspectingSlotIdx = null
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${buff.emoji} ${buff.name}",
                                style = TextStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE9D5FF) // purple-200
                                )
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Não há bênçãos ativas. Vá ao Bazar de Mystara",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 11.sp,
                        color = Color(0xFFFEF3C7).copy(alpha = 0.4f)
                    )
                )
            }
        }
    }

    // ITEM INSPECT MODAL
    ItemInspectModal(
        item = inspectingItem,
        onClose = {
            inspectingItem = null
            inspectingSlotIdx = null
        },
        actions = modalActions
    )

    // TITLE EQUIP MODAL
    TitleEquipModal(
        isOpen = isTitleModalOpen,
        onClose = { isTitleModalOpen = false },
        ownedTitles = ownedTitles,
        equippedTitle = character.equippedTitle,
        onEquipTitle = onEquipTitle
    )
}
