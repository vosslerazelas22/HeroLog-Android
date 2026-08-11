package com.iurispraecepta.herolog.ui.focus

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Cores transcritas do FocusModeScreen.tsx real
private val Stone950 = Color(0xFF0C0A09)
private val StoneBlack = Color(0xFF000000)
private val Amber500 = Color(0xFFF59E0B)
private val AmberHeader = Color(0xFFE2B054)
private val Amber100 = Color(0xFFFEF3C7)
private val PurpleBg = Color(0xFF3B0764).copy(alpha = 0.20f)
private val PurpleBorder = Color(0xFFA855F7).copy(alpha = 0.30f)
private val PurpleText = Color(0xFFD8B4FE)
private val RedBg = Color(0xFF450A0A).copy(alpha = 0.20f)
private val RedBorder = Color(0xFFEF4444).copy(alpha = 0.30f)
private val RedText = Color(0xFFFCA5A5)
private val PausedBg = Color(0xFF3B0764).copy(alpha = 0.40f)
private val PausedText = Color(0xFFE9D5FF)
private val RunningBorder = Amber500.copy(alpha = 0.30f)
private val StoneExitBorder = Color(0xFF292524)
private val StoneExitText = Color(0xFF78716C)

@Composable
fun FocusModeScreen(
    skillName: String,
    skillEmoji: String,
    isDungeonMode: Boolean,
    dungeonSessions: Int,
    isWildernessChecked: Boolean,
    timeLeft: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onExit: () -> Unit,
    isGraceActive: Boolean = false,
    graceSecondsLeft: Int = 3,
    isPlayerDead: Boolean = false,
    onReturnToFocusCap: () -> Unit = {},
    onRespawn: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Pulso radial de fundo — reflexo mágico. Ver PARIDADE.md: chuva de partículas cintilantes
    // (15 '✦' subindo) conscientemente omitida, mesma categoria de decisão do Bloco 18.
    val infiniteTransition = rememberInfiniteTransition(label = "auraPulse")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Stone950, StoneBlack, Stone950))
            )
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Amber500.copy(alpha = auraAlpha * 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.maxDimension * 0.6f
                    )
                )
            }
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = skillEmoji, fontSize = 24.sp)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = "CÂMARA DE FOCO ATIVA",
                            color = AmberHeader,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = skillName,
                            color = Amber100.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isDungeonMode) {
                        ModeTag(
                            text = "⚔️ MASMORRA ($dungeonSessions/4)",
                            bg = PurpleBg,
                            border = PurpleBorder,
                            textColor = PurpleText
                        )
                    }
                    if (isWildernessChecked) {
                        ModeTag(
                            text = "💀 TERRA SELVAGEM",
                            bg = RedBg,
                            border = RedBorder,
                            textColor = RedText
                        )
                    }
                }
            }

            // Centro: Orb
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FocusOrb(
                    timeLeft = timeLeft,
                    totalSeconds = totalSeconds,
                    isRunning = isRunning,
                    isPaused = isPaused,
                    size = FocusOrbSize.FULLSCREEN
                )
            }

            // Bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        border = BorderStroke(1.dp, Amber500.copy(alpha = 0.10f)),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isPaused) {
                    Button(
                        onClick = onTogglePause,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PausedBg,
                            contentColor = PausedText
                        )
                    ) {
                        Text("RETOMAR", fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onTogglePause,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber500),
                        border = BorderStroke(1.dp, RunningBorder)
                    ) {
                        Text("PAUSAR", fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StoneExitText),
                    border = BorderStroke(1.dp, StoneExitBorder)
                ) {
                    Text("SAIR", fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontSize = 12.sp)
                }
            }
        }

        if (isGraceActive) {
            WildernessGracePeriodOverlay(
                graceSecondsLeft = graceSecondsLeft,
                onReturnToFocusCap = onReturnToFocusCap
            )
        }

        if (isPlayerDead) {
            CognitiveDeathOverlay(
                onRespawn = onRespawn
            )
        }
    }
}

@Composable
private fun ModeTag(text: String, bg: Color, border: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}