package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.focus.formatDungeonCooldown
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant

private val Stone950 = Color(0xFF0C0A09)
private val Stone900 = Color(0xFF1C1917)
private val Stone800 = Color(0xFF292524)
private val Stone400 = Color(0xFFA8A29E)
private val Stone300 = Color(0xFFD6D3D1)

private val Amber600 = Color(0xFFD97706)
private val Amber500 = Color(0xFFF59E0B)
private val Amber400 = Color(0xFFFBBF24)
private val Amber300 = Color(0xFFFCD34D)
private val Amber950 = Color(0xFF451A03)

private val Purple600 = Color(0xFF9333EA)
private val Purple500 = Color(0xFFA855F7)
private val Purple400 = Color(0xFFC084FC)
private val Purple300 = Color(0xFFD8B4FE)
private val Purple200 = Color(0xFFE9D5FF)
private val Purple950 = Color(0xFF3B0764)

private val Red600 = Color(0xFFDC2626)
private val Red500 = Color(0xFFEF4444)
private val Red400 = Color(0xFFF87171)
private val Red950 = Color(0xFF450A0A)

@Composable
fun IncursionModeModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    currentMode: RaidMode,
    dungeonCooldownRemainingMs: Long = 0L,
    onSelectMode: (RaidMode) -> Unit,
) {
    val isDungeonOnCooldown = dungeonCooldownRemainingMs > 0L

    HeroLogModal(
        isOpen = isOpen,
        onClose = onClose,
        title = "Modos de Incursão",
        variant = ModalVariant.Amber
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Card 1: Padrão
            IncursionModeCard(
                title = "PADRÃO",
                description = "Chance de saque baseada na duração da sessão.",
                icon = Icons.Default.AutoAwesome,
                iconTint = Amber400,
                isActive = currentMode == RaidMode.PADRAO,
                activeBadgeText = "ATIVO",
                activeBadgeBg = Amber400,
                activeBadgeTextColor = Stone950,
                activeBorderColor = Amber400,
                baseBorderColor = Amber500.copy(alpha = 0.20f),
                baseBgColor = Amber950.copy(alpha = 0.15f),
                titleColor = Amber300,
                enabled = true,
                onClick = {
                    onSelectMode(RaidMode.PADRAO)
                    onClose()
                }
            )

            // Card 2: Masmorra
            IncursionModeCard(
                title = "MASMORRA",
                description = "4 sessões seguidas sem abandonar. +2.500 GP e Quad Loot ao concluir.",
                icon = Icons.Default.Shield,
                iconTint = Purple400,
                isActive = currentMode == RaidMode.MASMORRA && !isDungeonOnCooldown,
                activeBadgeText = if (isDungeonOnCooldown) "⏳ ${formatDungeonCooldown(dungeonCooldownRemainingMs)}" else "ATIVO",
                activeBadgeBg = if (isDungeonOnCooldown) Purple950 else Purple500,
                activeBadgeTextColor = if (isDungeonOnCooldown) Purple200 else Color.White,
                activeBadgeBorderColor = if (isDungeonOnCooldown) Purple500.copy(alpha = 0.40f) else null,
                alwaysShowBadge = isDungeonOnCooldown,
                activeBorderColor = Purple400,
                baseBorderColor = Purple500.copy(alpha = 0.20f),
                baseBgColor = Purple950.copy(alpha = 0.25f),
                titleColor = Purple300,
                enabled = !isDungeonOnCooldown,
                onClick = {
                    onSelectMode(RaidMode.MASMORRA)
                    onClose()
                }
            )

            // Card 3: Selvagem
            IncursionModeCard(
                title = "TERRA SELVAGEM",
                description = "+25% XP & GP. Minimizar a aba cancela o bônus.",
                icon = Icons.Default.Warning,
                iconTint = Red400,
                isActive = currentMode == RaidMode.SELVAGEM,
                activeBadgeText = "ATIVO",
                activeBadgeBg = Red500,
                activeBadgeTextColor = Color.White,
                activeBorderColor = Red500,
                baseBorderColor = Red500.copy(alpha = 0.25f),
                baseBgColor = Red950.copy(alpha = 0.25f),
                titleColor = Red400,
                enabled = true,
                onClick = {
                    onSelectMode(RaidMode.SELVAGEM)
                    onClose()
                }
            )
        }
    }
}

@Composable
private fun IncursionModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    isActive: Boolean,
    activeBadgeText: String,
    activeBadgeBg: Color,
    activeBadgeTextColor: Color,
    activeBorderColor: Color,
    baseBorderColor: Color,
    baseBgColor: Color,
    titleColor: Color,
    enabled: Boolean,
    activeBadgeBorderColor: Color? = null,
    alwaysShowBadge: Boolean = false,
    onClick: () -> Unit,
) {
    val borderColor = if (isActive) activeBorderColor else baseBorderColor
    val showBadge = isActive || alwaysShowBadge

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Stone950)
            .background(baseBgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .then(if (!enabled) Modifier.alpha(0.55f) else Modifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Stone900)
                    .border(1.dp, baseBorderColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = titleColor,
                            letterSpacing = 0.8.sp
                        )
                    )

                    if (showBadge) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(activeBadgeBg)
                                .then(
                                    if (activeBadgeBorderColor != null) {
                                        Modifier.border(1.dp, activeBadgeBorderColor, RoundedCornerShape(4.dp))
                                    } else Modifier
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = activeBadgeText,
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = activeBadgeTextColor,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }

                Text(
                    text = description,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Stone300
                    )
                )
            }
        }
    }
}
