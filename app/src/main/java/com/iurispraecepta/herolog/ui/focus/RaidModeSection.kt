package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.focus.LootConfig
import com.iurispraecepta.herolog.logic.focus.formatDungeonCooldown

/**
 * Port de "Segmented Control for Raid Modes" (fonte: App.tsx, tela de configuração pré-sessão,
 * localização exata do arquivo React ainda não confirmada — JSX renderizado quando
 * sessionConfig.isFocusMode === false).
 *
 * FONTE REAL: trecho colado por Bruno em auditoria de 08/08/2026 (bloco de exclusividade
 * Masmorra/Wilderness). Cores traduzidas 1:1 das classes Tailwind reais que apareciam no JSX
 * (amber-500, purple-950, purple-200/300/400, red-950/500/400, stone-950/900/400) — hex oficiais
 * da paleta Tailwind CSS, não fabricados.
 *
 * DECISÃO DE PORT (registrada em PARIDADE.md): o React usa 2 booleans soltos
 * (isDungeonMode, isWildernessChecked), cada handler de clique zerando o outro manualmente pra
 * garantir exclusão mútua "por disciplina". Aqui modelamos como enum de 3 estados —
 * exclusão mútua garantida PELO TIPO, não por convenção de handler. `raidModeFrom` /
 * `RaidMode.toLegacyFlags()` convertem nos dois sentidos para não alterar o formato real de
 * SessionConfig/ActiveSession (que devem continuar persistindo os dois booleans, não o enum).
 *
 * RISCO EM ABERTO (ver PARIDADE.md, "Legenda de risco") — NÃO resolvido neste arquivo:
 * - Cancelamento do bônus Selvagem ao minimizar a aba → na verdade é um subsistema inteiro de
 *   Morte Cognitiva (grace period, streak/combo, respawn), não um simples cancelamento de
 *   bônus. Lógica pura já portada em CognitiveDeathLogic.kt (Bloco 23); UI/wiring de lifecycle
 *   ainda pendentes — fora de escopo deste arquivo (é lógica de sessão ativa, não de tela de
 *   configuração pré-sessão).
 *
 * RESOLVIDO NESTE ARQUIVO (histórico, não é mais risco): cooldown de Masmorra — fonte real
 * confirmada e portada em DungeonCooldownLogic.kt (`resolveDungeonCooldownRemainingMs` +
 * `formatDungeonCooldown`, formato HH:MM:SS). Divergência consciente aprovada por Bruno: o
 * Android persiste `lastDungeonClearedTime` de verdade (React não persiste, reseta a cada
 * reload — comportamento não replicado de propósito).
 *
 * NÃO INCLUÍDO NESTE ARQUIVO (dependências externas não auditadas):
 * - Wiring com `LootConfig.calculateLootChance` real (assinatura exata não confirmada nesta
 *   sessão) — `lootChancePercent` é recebido pronto de fora, quem chama este composable calcula.
 * - `ModeDescriptionModal` (componente de modal de ajuda) — os 3 callbacks `onShow*Help` só
 *   disparam a intenção de abrir; o modal em si (React: `ModeDescriptionModal.tsx`, mencionado
 *   em Sprint 6 do v1.1.0) precisa ter seu status de port em Android confirmado à parte.
 */

enum class RaidMode {
    PADRAO,
    MASMORRA,
    SELVAGEM,
}

/** Deriva o RaidMode a partir dos dois booleans legados persistidos em SessionConfig/ActiveSession. */
fun raidModeFrom(isDungeonMode: Boolean, isWildernessChecked: Boolean): RaidMode = when {
    isDungeonMode -> RaidMode.MASMORRA
    isWildernessChecked -> RaidMode.SELVAGEM
    else -> RaidMode.PADRAO
}

/** Inverso: converte RaidMode para o par de booleans, para escrever sem alterar o formato real
 * de dado (SessionConfig/ActiveSession continuam com isDungeonMode/isWildernessChecked). */
fun RaidMode.toLegacyFlags(): Pair<Boolean, Boolean> = when (this) {
    RaidMode.PADRAO -> false to false
    RaidMode.MASMORRA -> true to false
    RaidMode.SELVAGEM -> false to true
}

private object RaidColors {
    val AmberBgActive = Color(0xFFF59E0B).copy(alpha = 0.15f)     // amber-500/15
    val AmberTextActive = Color(0xFFFCD34D)                        // amber-300
    val AmberText = Color(0xFFFBBF24)                              // amber-400
    val AmberLabel = Color(0xFFFBBF24).copy(alpha = 0.40f)         // amber-100/40 (aprox.)

    val PurpleBgActive = Color(0xFF3B0764).copy(alpha = 0.90f)     // purple-950/90
    val PurpleTextActive = Color(0xFFE9D5FF)                       // purple-200
    val PurpleText = Color(0xFFC084FC).copy(alpha = 0.80f)         // purple-400/80
    val PurpleTextHover = Color(0xFFD8B4FE)                        // purple-300

    val RedBgActive = Color(0xFF450A0A)                            // red-950
    val RedBorderActive = Color(0xFFEF4444).copy(alpha = 0.40f)    // red-500/40
    val RedTextActive = Color(0xFFEF4444)                          // red-500
    val RedText = Color(0xFFF87171).copy(alpha = 0.80f)            // red-400/80

    val StoneBg = Color(0xFF0C0A09).copy(alpha = 0.60f)            // stone-950/60
    val StoneBorder = Color(0xFFF59E0B).copy(alpha = 0.15f)        // amber-500/15
    val StoneTextInactive = Color(0xFFA8A29E)                      // stone-400

    val InfoBoxBg = Color(0xFF0C0A09).copy(alpha = 0.40f)          // stone-950/40
    val InfoBoxBorder = Color(0xFFF59E0B).copy(alpha = 0.05f)      // amber-500/5
}

// ---------------------------------------------------------------------------------------------
// Segmented control
// ---------------------------------------------------------------------------------------------

@Composable
fun RaidModeSegmentedControl(
    mode: RaidMode,
    isRunning: Boolean,
    dungeonCooldownRemainingMs: Long = 0L, // STUB — ver nota de risco #1 no topo do arquivo
    onModeSelected: (RaidMode) -> Unit,
    onLog: (String) -> Unit, // equivalente a addSystemLog(...) no React
    modifier: Modifier = Modifier,
) {
    val dungeonOnCooldown = dungeonCooldownRemainingMs > 0L

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text10Uppercase(text = "Modo de Incursão:", color = RaidColors.AmberLabel)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(RaidColors.StoneBg)
                .border(1.dp, RaidColors.StoneBorder, RoundedCornerShape(8.dp))
                .padding(2.dp),
        ) {
            RaidModeButton(
                label = "Padrão",
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp),
                enabled = !isRunning,
                active = mode == RaidMode.PADRAO,
                activeBg = RaidColors.AmberBgActive,
                activeText = RaidColors.AmberTextActive,
                inactiveText = RaidColors.StoneTextInactive,
                onClick = {
                    onModeSelected(RaidMode.PADRAO)
                    onLog("⚔️ Modo de Incursão Padrão selecionado.")
                },
            )

            RaidModeButton(
                label = "Masmorra ⚔️",
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(0.dp),
                enabled = !isRunning && !dungeonOnCooldown,
                active = mode == RaidMode.MASMORRA,
                activeBg = RaidColors.PurpleBgActive,
                activeText = RaidColors.PurpleTextActive,
                inactiveText = RaidColors.PurpleText,
                disabledText = RaidColors.PurpleText.copy(alpha = 0.50f),
                cooldownLabel = if (dungeonOnCooldown) {
                    "⏳ " + formatDungeonCooldown(dungeonCooldownRemainingMs)
                } else null,
                onClick = {
                    onModeSelected(RaidMode.MASMORRA)
                    onLog(
                        "⚔️ Incursão por Masmorra Ativada! Comprometa-se a realizar 4 focos " +
                            "seguidos sem abandonar para adquirir GP bônus.",
                    )
                },
            )

            RaidModeButton(
                label = "Selvagem 💀",
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp),
                enabled = !isRunning,
                active = mode == RaidMode.SELVAGEM,
                activeBg = RaidColors.RedBgActive,
                activeBorder = RaidColors.RedBorderActive,
                activeText = RaidColors.RedTextActive,
                inactiveText = RaidColors.RedText,
                onClick = {
                    onModeSelected(RaidMode.SELVAGEM)
                    onLog("🛡️ Ajuste: Terra Selvagem selecionada para a próxima Missão!")
                },
            )
        }
    }
}

@Composable
private fun RaidModeButton(
    label: String,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    enabled: Boolean,
    active: Boolean,
    activeBg: Color,
    activeText: Color,
    inactiveText: Color,
    disabledText: Color = inactiveText.copy(alpha = 0.50f),
    activeBorder: Color? = null,
    cooldownLabel: String? = null,
    onClick: () -> Unit,
) {
    val bg = if (active) activeBg else Color.Transparent
    val textColor = when {
        active -> activeText
        !enabled -> disabledText
        else -> inactiveText
    }

    Column(
        modifier = modifier
            .heightIn(min = 38.dp)
            .clip(shape)
            .background(bg)
            .then(
                if (activeBorder != null && active) {
                    Modifier.border(1.dp, activeBorder, shape)
                } else {
                    Modifier
                },
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        if (cooldownLabel != null) {
            Text(
                text = cooldownLabel,
                fontSize = 8.sp,
                color = RaidColors.PurpleTextHover,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/**
 * `dungeonCooldownRemainingMs` é calculado por quem chama este composable, via
 * `resolveDungeonCooldownRemainingMs` (DungeonCooldownLogic.kt) — este arquivo só recebe o
 * valor pronto e formata com `formatDungeonCooldown` (mesmo arquivo), fonte real confirmada.
 */

// ---------------------------------------------------------------------------------------------
// Info box contextual (texto abaixo do segmented control)
// ---------------------------------------------------------------------------------------------

/**
 * Port de "Mode Context Information / Help Button" — a caixa que muda de conteúdo conforme o
 * modo selecionado. Textos literais copiados do JSX real (não parafraseados — é copy do próprio
 * produto de Bruno, não conteúdo de terceiros).
 *
 * `lootChancePercent` é recebido já calculado de fora (não fabricamos aqui a chamada a
 * `LootConfig.calculateLootChance` — assinatura exata não confirmada nesta sessão, precisa ser
 * conferida contra o arquivo real antes de wiring).
 */
@Composable
fun RaidModeInfoBox(
    mode: RaidMode,
    dungeonSessions: Int,
    dungeonOnCooldown: Boolean,
    lootChancePercent: Int,
    onShowDungeonHelp: () -> Unit,
    onShowWildernessHelp: () -> Unit,
    onShowStandardHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(RaidColors.InfoBoxBg)
            .border(1.dp, RaidColors.InfoBoxBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        when (mode) {
            RaidMode.MASMORRA -> InfoRow(
                mainText = "⚔️ Explorando Masmorra ($dungeonSessions/4)",
                mainColor = RaidColors.PurpleTextActive,
                bonusText = if (dungeonOnCooldown) "⏳ Cooldown" else "Bônus +2.500 GP & Quad Loot",
                bonusColor = if (dungeonOnCooldown) RaidColors.PurpleText else RaidColors.AmberText,
                helpBorderColor = RaidColors.PurpleText,
                onHelpClick = onShowDungeonHelp,
            )

            RaidMode.SELVAGEM -> InfoRow(
                mainText = "💀 Terra Selvagem Ativa",
                mainColor = RaidColors.RedText,
                bonusText = "Bônus +25% XP & GP",
                bonusColor = RaidColors.AmberText,
                helpBorderColor = RaidColors.RedText,
                onHelpClick = onShowWildernessHelp,
            )

            RaidMode.PADRAO -> InfoRow(
                mainText = "🎯 Modo Padrão",
                mainColor = RaidColors.AmberTextActive,
                bonusText = "• Chance de Saque: $lootChancePercent%",
                bonusColor = RaidColors.AmberText,
                helpBorderColor = RaidColors.AmberText,
                onHelpClick = onShowStandardHelp,
            )
        }
    }
}

@Composable
private fun InfoRow(
    mainText: String,
    mainColor: Color,
    bonusText: String,
    bonusColor: Color,
    helpBorderColor: Color,
    onHelpClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = mainText, fontSize = 10.sp, color = mainColor)
            Text(
                text = bonusText,
                fontSize = 9.sp,
                color = bonusColor,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .border(1.dp, helpBorderColor.copy(alpha = 0.20f), RoundedCornerShape(3.dp))
                .clickable(onClick = onHelpClick)
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(text = "?", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = helpBorderColor)
        }
    }
}

@Composable
private fun Text10Uppercase(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
        color = color,
    )
}

// ---------------------------------------------------------------------------------------------
// Conteúdo literal dos 3 modais de ajuda (texto copiado do JSX real, para wiring futuro com o
// componente ModeDescriptionModal — status de port em Android ainda não confirmado)
// ---------------------------------------------------------------------------------------------

data class RaidModeHelpBlock(val label: String, val text: String)

object RaidModeHelpContent {
    val MASMORRA = listOf(
        RaidModeHelpBlock(
            label = "Regras da Jornada",
            text = "Comprometa-se a realizar 4 sessões consecutivas de foco sem abandonar.",
        ),
        RaidModeHelpBlock(
            label = "Recompensas Magnas",
            text = "+50% de XP por minuto em cada sessão, rolos de saque quadruplicados " +
                "(Quad Loot), 40% de chance de saque Lendário e um bônus monumental de " +
                "+2.500 GP ao concluir as 4 sessões.",
        ),
        RaidModeHelpBlock(
            label = "Recarga para Masmorra",
            text = "Tempo de recarga de 2 horas após a conclusão. Não acumulável com o Modo " +
                "Terra Selvagem.",
        ),
    )

    val SELVAGEM = listOf(
        RaidModeHelpBlock(
            label = "Regras da Jornada",
            text = "Regra severa: minimizar a aba durante a sessão cancela o bônus " +
                "automaticamente.",
        ),
        RaidModeHelpBlock(
            label = "Recompensas Magnas",
            text = "Sobreviventes ganham um bônus monumental de +25% de XP & GP extras no " +
                "fechamento do foco.",
        ),
    )

    // Bloco "Padrão" (chance de saque) não incluído aqui: o conteúdo real depende de valores
    // dinâmicos (BASE_LOOT_CHANCE.SHORT/MEDIUM/LONG, TITLE_LOOT_MULTIPLIERS, MAX_LOOT_CHANCE_CAP)
    // que devem vir de LootConfig.kt já portado — montar esse bloco no wiring, não aqui, para não
    // duplicar constantes que já existem em outro arquivo.
}

// ---------------------------------------------------------------------------------------------
// Wiring real com LootConfig.kt (confirmado por Bruno em 08/08/2026 — main, não teste)
// ---------------------------------------------------------------------------------------------

/** Nome de exibição de um título, desacoplado da classe real `Title` de TitleCatalog.kt (cuja
 * assinatura exata não foi confirmada nesta sessão — evita presumir nomes de campo). */
data class TitleDisplay(val emoji: String, val name: String)

/** Chance de loot já arredondada para porcentagem inteira, para RaidModeInfoBox. */
fun lootChancePercentFrom(studiedMinutes: Int, isDungeon: Boolean, equippedTitleId: String?): Int {
    val result = LootConfig.calculateLootChance(studiedMinutes, isDungeon, equippedTitleId)
    return Math.round(result.finalChance * 100).toInt()
}

/**
 * Monta os 4 blocos do modal de ajuda "Padrão" (texto literal do JSX real), calculando os
 * valores dinâmicos via LootConfig.calculateLootChance — sem duplicar as constantes, só lendo
 * delas. `titleLookup` resolve id → (emoji, nome) via TitleCatalog real, ex.:
 * `titleLookup = { id -> TitleCatalog.find(id)?.let { TitleDisplay(it.emoji, it.name) } }`.
 */
fun buildStandardLootHelpBlocks(
    studiedMinutes: Int,
    equippedTitleId: String?,
    titleLookup: (String) -> TitleDisplay?,
): List<RaidModeHelpBlock> {
    val chance = LootConfig.calculateLootChance(studiedMinutes, isDungeon = false, equippedTitleId)
    val basePct = Math.round(chance.baseChance * 100).toInt()
    val finalPct = Math.round(chance.finalChance * 100).toInt()
    val capPct = Math.round(LootConfig.MAX_LOOT_CHANCE_CAP * 100).toInt()

    val equippedTitle = equippedTitleId?.let(titleLookup)
    val titleBonus = equippedTitleId?.let { LootConfig.TITLE_LOOT_MULTIPLIERS[it] }
    val titleLine = if (equippedTitle != null) {
        val label = "${equippedTitle.emoji} ${equippedTitle.name}"
        if (titleBonus != null) {
            "Título equipado: $label (+${Math.round(titleBonus * 100).toInt()}% de bônus)"
        } else {
            "Título equipado: $label (Sem bônus de saque)"
        }
    } else {
        "Título equipado: Nenhum (Sem bônus de saque)"
    }

    val shortPct = Math.round(LootConfig.BASE_LOOT_CHANCE_SHORT * 100).toInt()
    val mediumPct = Math.round(LootConfig.BASE_LOOT_CHANCE_MEDIUM * 100).toInt()
    val longPct = Math.round(LootConfig.BASE_LOOT_CHANCE_LONG * 100).toInt()

    val titleBonusLines = LootConfig.TITLE_LOOT_MULTIPLIERS.entries.joinToString("\n") { (id, mult) ->
        val display = titleLookup(id)
        val label = display?.let { "${it.emoji} ${it.name}" } ?: id
        "$label: +${Math.round(mult * 100).toInt()}%"
    }

    return listOf(
        RaidModeHelpBlock(
            label = "CHANCE DE SAQUE ATUAL",
            text = "Duração selecionada: $studiedMinutes min (Base: $basePct%)\n$titleLine\n" +
                "Chance final de drop: $finalPct% (Teto máximo: $capPct%)",
        ),
        RaidModeHelpBlock(
            label = "CHANCE BASE POR DURAÇÃO",
            text = "Menos de 50 min: $shortPct%\n50 a 89 min: $mediumPct%\n90 min ou mais: $longPct%",
        ),
        RaidModeHelpBlock(
            label = "BÔNUS POR TÍTULO RARO",
            text = titleBonusLines,
        ),
        RaidModeHelpBlock(
            label = "REGRA DE CÁLCULO",
            text = "Os bônus de título multiplicam a chance base da duração selecionada. " +
                "A chance final é limitada a um teto de $capPct% por sessão.",
        ),
    )
}