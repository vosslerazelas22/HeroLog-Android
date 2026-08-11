package com.iurispraecepta.herolog.ui.character

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.data.TITLE_CATALOG
import com.iurispraecepta.herolog.data.TitleCategory
import com.iurispraecepta.herolog.data.TitleItem
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant

private val Rose400 = Color(0xFFFB7185)
private val Rose500 = Color(0xFFF43F5E)
private val Rose950 = Color(0xFF4C0519)

private val Purple400 = Color(0xFFC084FC)
private val Purple500 = Color(0xFFA855F7)
private val Purple950 = Color(0xFF3B0764)

private val Amber400 = Color(0xFFFBBF24)
private val Amber500 = Color(0xFFF59E0B)
private val Amber950 = Color(0xFF451A03)

private val Stone300 = Color(0xFFD6D3D1)
private val Stone800 = Color(0xFF292524)
private val Stone900 = Color(0xFF1C1917)
private val Stone950 = Color(0xFF0C0A09)

private val Sky400 = Color(0xFF38BDF8)
private val Sky500 = Color(0xFF0EA5E9)
private val Sky950 = Color(0xFF082F49)

private data class TitleCategoryConfig(
    val key: String,
    val label: String,
    val titleColor: Color,
    val borderColor: Color,
    val backgroundColor: Color
)

private val categoriesConfig = listOf(
    TitleCategoryConfig(
        key = "legendary",
        label = "— LENDÁRIOS —",
        titleColor = Rose400,
        borderColor = Rose500.copy(alpha = 0.2f),
        backgroundColor = Rose950.copy(alpha = 0.04f)
    ),
    TitleCategoryConfig(
        key = "epic",
        label = "— ÉPICOS —",
        titleColor = Purple400,
        borderColor = Purple500.copy(alpha = 0.2f),
        backgroundColor = Purple950.copy(alpha = 0.04f)
    ),
    TitleCategoryConfig(
        key = "rare",
        label = "— RAROS —",
        titleColor = Amber400,
        borderColor = Amber500.copy(alpha = 0.2f),
        backgroundColor = Amber950.copy(alpha = 0.04f)
    ),
    TitleCategoryConfig(
        key = "common",
        label = "— COMUNS —",
        titleColor = Stone300,
        borderColor = Stone800,
        backgroundColor = Stone900.copy(alpha = 0.04f)
    ),
    TitleCategoryConfig(
        key = "conquests",
        label = "— CONQUISTAS —",
        titleColor = Sky400,
        borderColor = Sky500.copy(alpha = 0.2f),
        backgroundColor = Sky950.copy(alpha = 0.04f)
    )
)

@Composable
fun TitleEquipModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    ownedTitles: List<String>,
    equippedTitle: String?,
    onEquipTitle: (String?) -> Unit
) {
    HeroLogModal(
        isOpen = isOpen,
        onClose = onClose,
        title = "Equipar Título Honorífico",
        variant = ModalVariant.Amber
    ) {
        val unlockedTitles = TITLE_CATALOG.filter { ownedTitles.contains(it.id) }

        val groupedTitles = mapOf(
            "legendary" to mutableListOf<TitleItem>(),
            "epic" to mutableListOf<TitleItem>(),
            "rare" to mutableListOf<TitleItem>(),
            "common" to mutableListOf<TitleItem>(),
            "conquests" to mutableListOf<TitleItem>()
        )

        unlockedTitles.forEach { title ->
            when (title.category) {
                TitleCategory.Legendary -> groupedTitles["legendary"]?.add(title)
                TitleCategory.Epic -> groupedTitles["epic"]?.add(title)
                TitleCategory.Rare -> groupedTitles["rare"]?.add(title)
                TitleCategory.Common -> groupedTitles["common"]?.add(title)
                TitleCategory.Achievement, TitleCategory.Drop -> groupedTitles["conquests"]?.add(title)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Selecione qual brasão ou título honorífico você deseja carregar em sua ficha de herói:",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    color = Color(0xFFFEF3C7).copy(alpha = 0.6f)
                )
            )

            if (unlockedTitles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Stone950.copy(alpha = 0.4f))
                        .border(1.dp, Color(0xFF292524), RoundedCornerShape(8.dp))
                        .padding(horizontal = 20.dp, vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "award_pulse")
                        val awardAlpha by infiniteTransition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = 0.5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "award_alpha"
                        )

                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = Amber500.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(40.dp)
                                .alpha(awardAlpha)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "SUA ESTANTE DE BRASÕES ESTÁ VAZIA!",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Amber400,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Nenhum título honorífico foi conquistado ainda. Cultive seu foco nas missões ou compre brasões no Bazar!",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = 10.sp,
                                color = Color(0xFFFEF3C7).copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    categoriesConfig.forEach { cat ->
                        val list = groupedTitles[cat.key].orEmpty()
                        if (list.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(cat.backgroundColor)
                                        .border(1.dp, cat.borderColor, RoundedCornerShape(4.dp))
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.label,
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = cat.titleColor,
                                            letterSpacing = 1.5.sp
                                        )
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    list.forEach { title ->
                                        val isEquipped = equippedTitle == title.id

                                        val cardModifier = if (isEquipped) {
                                            Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, Amber400, RoundedCornerShape(8.dp))
                                                .background(Stone950)
                                                .background(Amber500.copy(alpha = 0.04f))
                                        } else {
                                            Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, title.visualStyle.borderColor, RoundedCornerShape(8.dp))
                                                .background(title.visualStyle.backgroundColor)
                                        }

                                        Box(
                                            modifier = cardModifier
                                                .clickable {
                                                    if (isEquipped) {
                                                        onEquipTitle(null)
                                                    } else {
                                                        onEquipTitle(title.id)
                                                    }
                                                    onClose()
                                                }
                                                .padding(14.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 80.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = title.emoji,
                                                        fontSize = 16.sp
                                                    )
                                                    Text(
                                                        text = title.name.uppercase(),
                                                        style = TextStyle(
                                                            fontFamily = FontFamily.Serif,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = title.visualStyle.glowTextColor,
                                                            letterSpacing = 0.8.sp
                                                        )
                                                    )
                                                }

                                                if (!title.perks.isNullOrEmpty()) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        title.perks.forEach { perk ->
                                                            Text(
                                                                text = "⚡ $perk",
                                                                style = TextStyle(
                                                                    fontFamily = FontFamily.Monospace,
                                                                    fontSize = 9.5.sp,
                                                                    color = Sky400
                                                                )
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Text(
                                                        text = "Sem efeitos adicionais ativos.",
                                                        style = TextStyle(
                                                            fontFamily = FontFamily.SansSerif,
                                                            fontStyle = FontStyle.Italic,
                                                            fontSize = 9.sp,
                                                            color = Color(0xFF78716C)
                                                        )
                                                    )
                                                }
                                            }

                                            if (isEquipped) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Amber400)
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = Stone950,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Text(
                                                            text = "EQUIPADO",
                                                            style = TextStyle(
                                                                fontFamily = FontFamily.Serif,
                                                                fontWeight = FontWeight.Black,
                                                                fontSize = 9.sp,
                                                                color = Stone950
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
                    }
                }
            }
        }
    }
}
