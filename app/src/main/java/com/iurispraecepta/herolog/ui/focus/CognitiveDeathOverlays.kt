package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Port dos overlays de UI da "Morte Cognitiva" (Terra Selvagem / Wilderness), auditados a partir
 * da fonte real de `App.tsx` (React).
 *
 * DECISÕES CONSCIENTES DE PARIDADE:
 * - As animações de `animate-pulse` / `animate-ping` do Tailwind/Framer Motion no React foram
 *   simplificadas para visualização estática/limpa em Compose sem timers extras, mantendo 100% de
 *   fidelidade de cores, tipografia, bordas e textos.
 * - `CognitiveDeathOverlay` é totalmente agnóstico sobre a causa da morte, recebendo apenas a
 *   ação de `onRespawn`, preservando o padrão da fonte real.
 */

private val RedOverlayBgGrace = Color(0xFF280505).copy(alpha = 0.70f)
private val RedOverlayBgDead = Color(0xFF280505).copy(alpha = 0.95f)
private val QuestPanelBg = Color(0xFF1C1A17)
private val RedBorder500 = Color(0xFFEF4444)
private val RedText500 = Color(0xFFEF4444)
private val AmberText100 = Color(0xFFFEF3C7).copy(alpha = 0.90f)
private val AmberText200 = Color(0xFFFDE68A)
private val StoneText400 = Color(0xFFA8A29E)
private val DarkBtnBg = Color(0xFF1C1917)
private val LightRedBtnText = Color(0xFFFCA5A5)
private val RedGradientStart = Color(0xFFB91C1C)
private val RedGradientEnd = Color(0xFF7F1D1D)
private val RespawnText = Color(0xFFFEE2E2)

/**
 * Overlay 1 — Grace Period (aviso de 3 segundos antes da morte cognitiva se confirmar).
 */
@Composable
fun WildernessGracePeriodOverlay(
    graceSecondsLeft: Int,
    onReturnToFocusCap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RedOverlayBgGrace)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(8.dp), spotColor = RedBorder500)
                .background(QuestPanelBg, RoundedCornerShape(8.dp))
                .border(2.dp, RedBorder500, RoundedCornerShape(8.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "SOMBRA DE DISTRAÇÃO DETECTADA!",
                    color = RedText500,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "A Terra Selvagem (Wilderness) detectou sua distração mental! Acesse esta interface imediatamente antes do colapso do portal!",
                    color = AmberText100,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "$graceSecondsLeft",
                    color = RedText500,
                    fontSize = 48.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onReturnToFocusCap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBtnBg,
                        contentColor = LightRedBtnText
                    ),
                    border = BorderStroke(1.dp, RedBorder500.copy(alpha = 0.50f))
                ) {
                    Text(
                        text = "SELAR PORTAL DE FOCO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

/**
 * Overlay 2 — Morte Cognitiva confirmada.
 */
@Composable
fun CognitiveDeathOverlay(
    onRespawn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RedOverlayBgDead)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "💀",
                fontSize = 64.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "VOCÊ MORREU",
                color = RedText500,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Você quebrou o sagrado compasso do foco mental ao abandonar temporariamente estes perímetros na Terra Selvagem (Wilderness)!",
                color = AmberText200,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Sua mente vagou, e a força das distrações colapsou sua série de foco acumulada. XP e moedas acumuladas nesta jornada evaporaram nas brumas.",
                color = StoneText400,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRespawn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = RespawnText
                ),
                border = BorderStroke(2.dp, RedBorder500),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(RedGradientStart, RedGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RESSURGIR (RESPAWN)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
