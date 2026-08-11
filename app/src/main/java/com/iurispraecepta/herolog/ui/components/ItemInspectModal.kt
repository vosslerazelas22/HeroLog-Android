package com.iurispraecepta.herolog.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import kotlinx.coroutines.delay

enum class ItemInspectVariant { Primary, Danger, Success, Stone, Amber }

data class ItemInspectAction(
    val label: String,
    val onClick: () -> Unit,
    val variant: ItemInspectVariant = ItemInspectVariant.Stone
)

@Composable
fun ItemInspectModal(
    item: InventoryItem?,
    onClose: () -> Unit,
    actions: List<ItemInspectAction> = emptyList(),
    showSlotSelector: Boolean = false,
    onSelectSlot: ((Int) -> Unit)? = null
) {
    var shouldRenderDialog by remember { mutableStateOf(item != null) }
    var animatedVisible by remember { mutableStateOf(item != null) }
    var currentItem by remember { mutableStateOf(item) }

    DisposableEffect(item != null) {
        if (item != null) {
            ModalCountRegistry.increment()
        }
        onDispose {
            if (item != null) {
                ModalCountRegistry.decrement()
            }
        }
    }

    LaunchedEffect(item) {
        if (item != null) {
            currentItem = item
            shouldRenderDialog = true
            animatedVisible = true
        } else {
            animatedVisible = false
            delay(200L)
            shouldRenderDialog = false
        }
    }

    if (!shouldRenderDialog) return
    val displayItem = item ?: currentItem ?: return

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Backdrop
            var backdropModifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backdropModifier = backdropModifier.blur(4.dp)
            }

            backdropModifier = backdropModifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClose()
            }

            Box(modifier = backdropModifier)

            // Animated Modal Container
            AnimatedVisibility(
                visible = animatedVisible,
                enter = fadeIn(tween(200, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(200, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(200, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.95f, animationSpec = tween(200, easing = FastOutSlowInEasing))
            ) {
                val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .widthIn(max = 384.dp)
                        .fillMaxWidth()
                        .heightIn(max = screenHeight * 0.85f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C0A09)) // stone-950
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF1C1917), Color(0xFF0C0A09))
                                    )
                                )
                                .drawBehind {
                                    drawLine(
                                        color = Color(0xFFF59E0B).copy(alpha = 0.1f),
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayItem.emoji,
                                fontSize = 30.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = displayItem.name.uppercase(),
                                    style = TextStyle(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color(0xFFFBBF24), // amber-400
                                        letterSpacing = 0.1.em
                                    )
                                )
                                val subtitleText = if (displayItem.isEquipment == true) {
                                    when (displayItem.rarity) {
                                        Rarity.Especial -> "🛡️ Equipamento Especial"
                                        Rarity.Comum -> "🛡️ Equipamento Comum"
                                        null -> "🛡️ Equipamento"
                                    }
                                } else {
                                    "🎒 Relíquia Colecionável"
                                }
                                Text(
                                    text = subtitleText.uppercase(),
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Color(0xFFFEF3C7).copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }

                        // Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = displayItem.desc,
                                style = TextStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFEF3C7).copy(alpha = 0.8f),
                                    lineHeight = 18.sp
                                )
                            )

                            if (displayItem.isEquipment == true) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = Color(0xFFF59E0B).copy(alpha = 0.04f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "📦 INFORMAÇÕES DE DURABILIDADE",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 10.sp,
                                            color = Color(0xFFFBBF24), // amber-400
                                            letterSpacing = 0.05.em,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    val charges = displayItem.charges ?: 0
                                    val maxCharges = displayItem.maxCharges ?: 8
                                    Text(
                                        text = "🔋 $charges / $maxCharges Cargas",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF34D399), // emerald-400
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    Text(
                                        text = "Perde 1 de durabilidade toda vez que for ativado ao completar uma sessão de foco.",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 9.sp,
                                            color = Color(0xFFFEF3C7).copy(alpha = 0.5f),
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            }

                            if ((showSlotSelector && displayItem.isEquipment == true && onSelectSlot != null) || actions.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (showSlotSelector && displayItem.isEquipment == true && onSelectSlot != null) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "SELECIONE O ESPAÇO PARA EQUIPAR:",
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFFEF3C7).copy(alpha = 0.3f)
                                                )
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                for (slotIdx in 0..2) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFF1C1917))
                                                            .border(
                                                                1.dp,
                                                                Color(0xFFF59E0B).copy(alpha = 0.2f),
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .clickable { onSelectSlot(slotIdx) }
                                                            .padding(vertical = 6.dp, horizontal = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "SLOT ${slotIdx + 1}",
                                                            style = TextStyle(
                                                                fontFamily = FontFamily.Serif,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 10.sp,
                                                                color = Color(0xFFFCD34D),
                                                                letterSpacing = 0.05.em
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (actions.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .drawBehind {
                                                    drawLine(
                                                        color = Color(0xFFF59E0B).copy(alpha = 0.05f),
                                                        start = Offset(0f, 0f),
                                                        end = Offset(size.width, 0f),
                                                        strokeWidth = 1.dp.toPx()
                                                    )
                                                }
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val isSingle = actions.size == 1
                                            actions.forEach { action ->
                                                val isGrowing = when (action.variant) {
                                                    ItemInspectVariant.Success, ItemInspectVariant.Primary, ItemInspectVariant.Amber -> true
                                                    ItemInspectVariant.Danger -> isSingle
                                                    ItemInspectVariant.Stone -> false
                                                }

                                                var buttonModifier: Modifier = Modifier
                                                if (isGrowing) {
                                                    buttonModifier = buttonModifier.weight(1f)
                                                }

                                                val style = getActionStyle(action.variant)

                                                Box(
                                                    modifier = buttonModifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(style.bgColor)
                                                        .border(1.dp, style.borderColor, RoundedCornerShape(4.dp))
                                                        .clickable { action.onClick() }
                                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = action.label.uppercase(),
                                                        style = TextStyle(
                                                            fontFamily = FontFamily.Serif,
                                                            fontWeight = if (style.isBold) FontWeight.Bold else FontWeight.Normal,
                                                            fontSize = 12.sp,
                                                            color = style.textColor,
                                                            letterSpacing = 0.05.em,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Footer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1C1917).copy(alpha = 0.4f))
                                .drawBehind {
                                    drawLine(
                                        color = Color(0xFFF59E0B).copy(alpha = 0.1f),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1C1917))
                                    .border(
                                        1.dp,
                                        Color(0xFFF59E0B).copy(alpha = 0.2f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { onClose() }
                                    .padding(vertical = 6.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "VOLTAR",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFEF3C7).copy(alpha = 0.7f),
                                        letterSpacing = 0.05.em
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class ActionStyle(
    val bgColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val isBold: Boolean
)

private fun getActionStyle(variant: ItemInspectVariant): ActionStyle {
    return when (variant) {
        ItemInspectVariant.Success -> ActionStyle(
            bgColor = Color(0xFF022C22).copy(alpha = 0.4f), // emerald-950/40
            borderColor = Color(0xFF10B981).copy(alpha = 0.3f), // emerald-500/30
            textColor = Color(0xFF6EE7B7), // emerald-300
            isBold = true
        )
        ItemInspectVariant.Danger -> ActionStyle(
            bgColor = Color(0xFF450A0A).copy(alpha = 0.4f), // red-950/40
            borderColor = Color(0xFFEF4444).copy(alpha = 0.3f), // red-500/30
            textColor = Color(0xFFFCA5A5), // red-300
            isBold = false
        )
        ItemInspectVariant.Primary, ItemInspectVariant.Amber -> ActionStyle(
            bgColor = Color(0xFF451A03).copy(alpha = 0.4f), // amber-950/40
            borderColor = Color(0xFFF59E0B).copy(alpha = 0.3f), // amber-500/30
            textColor = Color(0xFFFCD34D), // amber-300
            isBold = false
        )
        ItemInspectVariant.Stone -> ActionStyle(
            bgColor = Color(0xFF1C1917), // stone-900
            borderColor = Color(0xFFF59E0B).copy(alpha = 0.2f), // amber-500/20
            textColor = Color(0xFFFEF3C7).copy(alpha = 0.7f), // amber-100/70
            isBold = false
        )
    }
}
