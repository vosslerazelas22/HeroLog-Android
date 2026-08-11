package com.iurispraecepta.herolog.ui.inventory

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.InventoryLogic
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.ui.components.ItemInspectAction
import com.iurispraecepta.herolog.ui.components.ItemInspectModal
import com.iurispraecepta.herolog.ui.components.ItemInspectVariant

@Composable
fun InventoryScreen(
    inventory: List<InventoryItem>,
    inspectingItem: InventoryItem?,
    onInspectItem: (InventoryItem) -> Unit,
    onCloseInspection: () -> Unit,
    onEquipItem: (InventoryItem, Int) -> Unit,
    onSellItem: (InventoryItem) -> Unit,
    onDiscardItem: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val virtualBuffs = remember {
        setOf(
            BuffType.DoubleLoot,
            BuffType.FocusElixir,
            BuffType.CrystalClarity,
            BuffType.RuneFortune,
            BuffType.StreakShield
        )
    }

    val physicalItems = remember(inventory) {
        inventory.filter { it.buff !in virtualBuffs }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "emptyStatePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (physicalItems.isNotEmpty()) {
            physicalItems.forEach { item ->
                val isEquip = item.isEquipment == true
                val cardBg = if (isEquip) Color(0xFFF59E0B).copy(alpha = 0.04f) else Color(0xFF1C1917).copy(alpha = 0.60f)
                val cardBorder = if (isEquip) Color(0xFFF59E0B).copy(alpha = 0.40f) else Color(0xFFF59E0B).copy(alpha = 0.10f)
                val emojiBorder = if (isEquip) Color(0xFFF59E0B).copy(alpha = 0.30f) else Color(0xFF292524)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                        .clickable { onInspectItem(item) }
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0C0A09).copy(alpha = 0.70f))
                            .border(1.dp, emojiBorder, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.emoji,
                            fontSize = 30.sp
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = item.name,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFEF3C7)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (item.rarity != null) {
                                val isEspecial = item.rarity == Rarity.Especial
                                val badgeBg = if (isEspecial) Color(0xFFF59E0B).copy(alpha = 0.20f) else Color(0xFF292524).copy(alpha = 0.80f)
                                val badgeText = if (isEspecial) Color(0xFFFCD34D) else Color(0xFFA8A29E)
                                val badgeBorder = if (isEspecial) Color(0xFFF59E0B).copy(alpha = 0.40f) else Color(0xFF44403C)

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(badgeBg)
                                        .border(1.dp, badgeBorder, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isEspecial) "Especial" else "Comum",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 9.sp,
                                            color = badgeText,
                                            letterSpacing = 0.05.em
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = item.desc,
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = 11.sp,
                                color = Color(0xFFA8A29E)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1C1917).copy(alpha = 0.30f))
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mochila vazia. Drops ocorrem ao concluir focos ou compre no Bazar.",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFFFEF3C7).copy(alpha = 0.35f * pulseAlpha),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }

    val modalActions = if (inspectingItem != null) {
        val sellPrice = InventoryLogic.calculateSellPrice(inspectingItem)
        listOf(
            ItemInspectAction(
                label = "💰 Vender ($sellPrice GP)",
                onClick = { onSellItem(inspectingItem) },
                variant = ItemInspectVariant.Success
            ),
            ItemInspectAction(
                label = "Descartar",
                onClick = { onDiscardItem(inspectingItem) },
                variant = ItemInspectVariant.Danger
            )
        )
    } else {
        emptyList()
    }

    ItemInspectModal(
        item = inspectingItem,
        onClose = onCloseInspection,
        showSlotSelector = true,
        onSelectSlot = { slotIdx ->
            if (inspectingItem != null) {
                onEquipItem(inspectingItem, slotIdx)
            }
        },
        actions = modalActions
    )
}
