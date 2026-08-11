package com.iurispraecepta.herolog.ui.skills

import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.SkillLogic
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalCountRegistry
import com.iurispraecepta.herolog.ui.components.ModalVariant

private val Stone950 = Color(0xFF0C0A09)
private val Stone900 = Color(0xFF1C1917)

private val Amber500 = Color(0xFFF59E0B)
private val Amber400 = Color(0xFFFBBF24)
private val Amber300 = Color(0xFFFCD34D)
private val Amber200 = Color(0xFFFDE68A)
private val Amber100 = Color(0xFFFEF3C7)

private val Yellow500 = Color(0xFFEAB308)
private val Yellow400 = Color(0xFFFACC15)

private val Red400 = Color(0xFFF87171)
private val Emerald400 = Color(0xFF34D399)

private data class SkillSuggestion(val name: String, val emoji: String)

private val SKILL_SUGGESTIONS = listOf(
    SkillSuggestion("Estudos", "📚"),
    SkillSuggestion("Foco Profundo", "🧠"),
    SkillSuggestion("Pesquisa", "🔬"),
    SkillSuggestion("Escrita", "✍️"),
    SkillSuggestion("Idiomas", "🗣️"),
    SkillSuggestion("Leitura", "📖"),
    SkillSuggestion("Programação", "💻"),
    SkillSuggestion("Exercícios", "🏋️"),
    SkillSuggestion("Meditação", "🧘"),
    SkillSuggestion("Artes & Pintura", "🎨"),
    SkillSuggestion("Culinária", "🍳"),
    SkillSuggestion("Finanças", "💰"),
    SkillSuggestion("Música", "🎵"),
    SkillSuggestion("Organização", "📅"),
    SkillSuggestion("Jogos & Estratégia", "🎮"),
    SkillSuggestion("Trabalho", "💼")
)

private val SKILL_EMOJIS = listOf(
    "📚", "💻", "🧠", "✍️", "🗣️", "🏋️", "🎨", "🍳",
    "🔬", "🧘", "🎵", "💰", "💼", "🧪", "🛡️", "🎯"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsScreen(
    skills: List<Skill>,
    onAddTagToSkill: (skillIdx: Int, newTag: String) -> Unit,
    onRemoveTagFromSkill: (skillIdx: Int, tagIdx: Int) -> Unit,
    onAddCustomSkill: (name: String, emoji: String) -> Unit,
    onDeleteSkill: (idx: Int) -> Unit,
    onPrestigeSkill: (idx: Int) -> Unit,
    onRenameSkill: (idx: Int, newName: String) -> Unit,
    isCreateModalOpen: Boolean,
    onCreateModalOpenChange: (Boolean) -> Unit,
    initialEditingIdx: Int? = null,
    modifier: Modifier = Modifier
) {
    var editingIdx by remember(initialEditingIdx) { mutableStateOf<Int?>(initialEditingIdx) }
    var editNameValue by remember(initialEditingIdx) {
        mutableStateOf(initialEditingIdx?.let { skills.getOrNull(it)?.name } ?: "")
    }

    var newSkillNameInput by remember { mutableStateOf("") }
    var selectedNewSkillEmoji by remember { mutableStateOf("📚") }

    val handleSaveRename: (Int) -> Unit = { idx ->
        val trimmed = editNameValue.trim()
        if (trimmed.isNotEmpty()) {
            onRenameSkill(idx, trimmed)
        }
        editingIdx = null
    }

    val handleAddCustom: () -> Unit = {
        val trimmed = newSkillNameInput.trim()
        if (trimmed.isNotEmpty()) {
            onAddCustomSkill(trimmed, selectedNewSkillEmoji)
            newSkillNameInput = ""
            onCreateModalOpenChange(false)
        }
    }

    val handleAddSuggestion: (String, String) -> Unit = { name, emoji ->
        onAddCustomSkill(name, emoji)
        onCreateModalOpenChange(false)
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            skills.forEachIndexed { idx, sk ->
                key(sk.name) {
                    val reqXP = SkillLogic.requiredXpForLevel(sk.level)
                val percent = (sk.xp.toFloat() / reqXP.toFloat() * 100f).coerceIn(0f, 100f)
                val isEditing = editingIdx == idx

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x660C0A09)) // bg-stone-950/40
                        .border(1.dp, Color(0x1AF59E0B), RoundedCornerShape(8.dp)) // border-amber-500/10
                        .padding(12.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header Row
                        if (isEditing) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = sk.emoji ?: "🎯",
                                    fontSize = 20.sp
                                )

                                BasicTextField(
                                    value = editNameValue,
                                    onValueChange = { editNameValue = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Stone950, RoundedCornerShape(4.dp))
                                        .border(1.dp, Color(0x4DF59E0B), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    textStyle = TextStyle(
                                        color = Amber100,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(Amber400),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        handleSaveRename(idx)
                                        keyboardController?.hide()
                                    })
                                )

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x3310B981))
                                        .clickable { handleSaveRename(idx) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Salvar Nome",
                                        tint = Emerald400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x33EF4444))
                                        .clickable { editingIdx = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancelar",
                                        tint = Red400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = sk.emoji ?: "🎯",
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = sk.name,
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Amber200,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (sk.prestige != null && sk.prestige > 0) {
                                                Text(
                                                    text = "👑" + "★".repeat(sk.prestige),
                                                    color = Yellow400,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .clickable {
                                                        editingIdx = idx
                                                        editNameValue = sk.name
                                                    }
                                                    .padding(2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Renomear Habilidade",
                                                    tint = Color(0x4DFEF3C7),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0x1AF59E0B))
                                                .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Nível ${sk.level}",
                                                color = Amber400,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x0AEF4444))
                                        .border(1.dp, Color(0x1AEF4444), RoundedCornerShape(4.dp))
                                        .clickable { onDeleteSkill(idx) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ESQUECER",
                                        color = Color(0xB3F87171),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        // Progress Bar Section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(Stone950)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(percent / 100f)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(Amber500)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Progresso: ${sk.xp} / $reqXP XP",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = Color(0x4DFEF3C7) // amber-100/30
                                )
                                if (sk.prestige != null && sk.prestige > 0) {
                                    Text(
                                        text = "Bônus: +${sk.prestige * 25}% XP",
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 9.sp,
                                        color = Yellow500
                                    )
                                }
                            }
                        }

                        // Subskills Section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .drawBehind {
                                    drawLine(
                                        color = Color(0x0DF59E0B),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = "SUBSKILLS:",
                                fontFamily = FontFamily.Serif,
                                fontSize = 9.sp,
                                color = Color(0x66FEF3C7),
                                letterSpacing = 0.5.sp
                            )

                            if (sk.tags.isNullOrEmpty()) {
                                Text(
                                    text = "Nenhuma subskill cadastrada para esta habilidade.",
                                    fontSize = 9.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0x40FEF3C7)
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    sk.tags.forEachIndexed { tIdx, tg ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0x1AF59E0B))
                                                .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tg,
                                                fontSize = 9.sp,
                                                color = Amber200,
                                                fontFamily = FontFamily.SansSerif
                                            )
                                            Text(
                                                text = "×",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0x66FEF3C7),
                                                modifier = Modifier.clickable { onRemoveTagFromSkill(idx, tIdx) }
                                            )
                                        }
                                    }
                                }
                            }

                            // Subskill Input Row
                            var subskillInput by remember(idx) { mutableStateOf("") }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                BasicTextField(
                                    value = subskillInput,
                                    onValueChange = { subskillInput = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0x800C0A09), RoundedCornerShape(4.dp))
                                        .border(1.dp, Color(0x1AF59E0B), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    textStyle = TextStyle(
                                        color = Amber100,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(Amber400),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        val trimmed = subskillInput.trim()
                                        if (trimmed.isNotEmpty()) {
                                            onAddTagToSkill(idx, trimmed)
                                            subskillInput = ""
                                        }
                                    }),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (subskillInput.isEmpty()) {
                                                Text(
                                                    text = "Criar subskill (ex: Direito Processual, React, CSS...)",
                                                    color = Color(0x26FEF3C7),
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.SansSerif
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x26F59E0B))
                                        .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(4.dp))
                                        .clickable {
                                            val trimmed = subskillInput.trim()
                                            if (trimmed.isNotEmpty()) {
                                                onAddTagToSkill(idx, trimmed)
                                                subskillInput = ""
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+",
                                        color = Amber300,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Prestige Button
                        if (sk.level >= 99) {
                            val infiniteTransition = rememberInfiniteTransition(label = "prestigePulse")
                            val alphaPulse by infiniteTransition.animateFloat(
                                initialValue = 1.0f,
                                targetValue = 0.5f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "prestigeAlpha"
                            )
                            val effectiveAlpha = if (ModalCountRegistry.isAnyModalOpen) 1f else alphaPulse

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Amber500, Yellow400, Amber400)
                                        )
                                    )
                                    .alpha(effectiveAlpha)
                                    .clickable { onPrestigeSkill(idx) }
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "👑 Alcançar Prestígio (Resetar a Nível 1 & Ganhar +25% XP Definitivo)",
                                    color = Stone950,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Criar Nova Habilidade
    HeroLogModal(
        isOpen = isCreateModalOpen,
        onClose = { onCreateModalOpenChange(false) },
        title = "Criar Nova Habilidade",
        variant = ModalVariant.Amber
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(end = 4.dp)
        ) {
            // Emoji Picker Section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SELECIONE UM ÍCONE/EMOJI PARA A HABILIDADE:",
                    fontFamily = FontFamily.Serif,
                    fontSize = 10.sp,
                    color = Color(0x66FEF3C7),
                    letterSpacing = 1.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x4D0C0A09))
                        .border(1.dp, Color(0x1AF59E0B), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    SKILL_EMOJIS.forEach { em ->
                        val isSelected = selectedNewSkillEmoji == em
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (isSelected) {
                                        Modifier.shadow(
                                            elevation = 4.dp,
                                            shape = RoundedCornerShape(4.dp),
                                            ambientColor = Amber500.copy(alpha = 0.2f),
                                            spotColor = Amber500.copy(alpha = 0.2f)
                                        )
                                    } else Modifier
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) Color(0x33F59E0B) else Color(0x731C1917))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Amber400 else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { selectedNewSkillEmoji = em },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = em,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Custom Skill Name Section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NOME DA HABILIDADE DE FOCO:",
                    fontFamily = FontFamily.Serif,
                    fontSize = 10.sp,
                    color = Color(0x66FEF3C7),
                    letterSpacing = 1.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = newSkillNameInput,
                        onValueChange = { if (it.length <= 30) newSkillNameInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xCC0C0A09), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        textStyle = TextStyle(
                            color = Amber100,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Amber400),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            handleAddCustom()
                        }),
                        decorationBox = { innerTextField ->
                            Box {
                                if (newSkillNameInput.isEmpty()) {
                                    Text(
                                        text = "Ex: Alquimia de Dados, Exercícios Físicos...",
                                        color = Color(0x26FEF3C7),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x26F59E0B))
                            .border(1.dp, Amber400, RoundedCornerShape(4.dp))
                            .clickable { handleAddCustom() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GRAVAR",
                            color = Amber300,
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Quick Suggestions Section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .drawBehind {
                        drawLine(
                            color = Color(0x1AF59E0B),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "SUGESTÕES RÁPIDAS:",
                    fontFamily = FontFamily.Serif,
                    fontSize = 10.sp,
                    color = Color(0x66FEF3C7),
                    letterSpacing = 1.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SKILL_SUGGESTIONS.forEach { sug ->
                        val alreadyHas = skills.any { it.name.equals(sug.name, ignoreCase = true) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (alreadyHas) Color(0x401C1917) else Color(0x800C0A09))
                                .border(
                                    width = 1.dp,
                                    color = if (alreadyHas) Color(0x66292524) else Color(0x0DF59E0B),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable(enabled = !alreadyHas) {
                                    handleAddSuggestion(sug.name, sug.emoji)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sug.emoji,
                                fontSize = 14.sp
                            )
                            Text(
                                text = sug.name,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Serif,
                                color = if (alreadyHas) Color(0x40FEF3C7) else Amber200
                            )
                        }
                    }
                }
            }
        }
    }
}
}
