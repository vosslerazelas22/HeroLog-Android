package com.iurispraecepta.herolog.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

enum class FocusOrbSize { COMPACT, STANDARD, FULLSCREEN }

// Paradas de gradiente transcritas 1:1 do FocusOrb.tsx real (defs SVG: liquid-work,
// liquid-urgent, liquid-break, liquid-paused, cada uma com variante "-back")
private object OrbColors {
    val WorkTop = Color(0xFFFCD34D).copy(alpha = 0.85f)
    val WorkMid = Color(0xFFD97706).copy(alpha = 0.9f)
    val WorkBottom = Color(0xFF78350F).copy(alpha = 0.95f)
    val WorkBackTop = Color(0xFFB45309).copy(alpha = 0.5f)
    val WorkBackBottom = Color(0xFF451A03).copy(alpha = 0.6f)

    val UrgentTop = Color(0xFFEF4444).copy(alpha = 0.9f)
    val UrgentMid = Color(0xFFDC2626).copy(alpha = 0.95f)
    val UrgentBottom = Color(0xFF7F1D1D).copy(alpha = 0.98f)
    val UrgentBackTop = Color(0xFFB91C1C).copy(alpha = 0.6f)
    val UrgentBackBottom = Color(0xFF450A0A).copy(alpha = 0.7f)

    val BreakTop = Color(0xFF34D399).copy(alpha = 0.85f)
    val BreakMid = Color(0xFF10B981).copy(alpha = 0.9f)
    val BreakBottom = Color(0xFF064E3B).copy(alpha = 0.95f)
    val BreakBackTop = Color(0xFF059669).copy(alpha = 0.5f)
    val BreakBackBottom = Color(0xFF022C22).copy(alpha = 0.6f)

    val PausedTop = Color(0xFFF59E0B).copy(alpha = 0.75f)
    val PausedMid = Color(0xFFD97706).copy(alpha = 0.8f)
    val PausedBottom = Color(0xFF78350F).copy(alpha = 0.85f)
    val PausedBackTop = Color(0xFFB45309).copy(alpha = 0.45f)
    val PausedBackBottom = Color(0xFF451A03).copy(alpha = 0.55f)

    val BgCircle = Color(0xFF130D0A)
    val RingBreak = Color(0xFF10B981)
    val RingUrgent = Color(0xFFEF4444)
    val RingDefault = Color(0xFFD97706)

    val TextEmerald300 = Color(0xFF6EE7B7)
    val TextRed400 = Color(0xFFF87171)
    val TextAmber500 = Color(0xFFF59E0B)
    val TextAmber100 = Color(0xFFFEF3C7)
    val TextAmber200 = Color(0xFFFDE68A)
}

@Composable
fun FocusOrb(
    timeLeft: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    isBreakActive: Boolean = false,
    size: FocusOrbSize = FocusOrbSize.STANDARD,
    modifier: Modifier = Modifier
) {
    // Breakpoints sm:/lg: do React não se aplicam (app mobile-only, ver handoff item 7).
    // Usa só o breakpoint min-[390px] real do React, que é o único alcançável em celular.
    val isWideScreen = LocalConfiguration.current.screenWidthDp >= 390
    val boxSizeDp = when (size) {
        FocusOrbSize.COMPACT -> 172.dp
        FocusOrbSize.STANDARD -> if (isWideScreen) 218.dp else 172.dp
        FocusOrbSize.FULLSCREEN -> 253.dp
    }

    val progress = if (totalSeconds > 0) timeLeft.toFloat() / totalSeconds.toFloat() else 0f
    val isActiveAnimating = isRunning && !isPaused
    val isUrgent = timeLeft <= 60 && isActiveAnimating

    // Fase da onda controlada manualmente (não rememberInfiniteTransition) para replicar
    // fielmente o comportamento do React de PARAR o loop de animação (não só zerar amplitude)
    // quando pausado/parado — mesmo throttle de ~30fps (33ms) do requestAnimationFrame original.
    var phase by remember { mutableStateOf(0f) }
    LaunchedEffect(isActiveAnimating) {
        if (isActiveAnimating) {
            while (isActive) {
                phase += 0.05f
                if (phase > (2 * PI).toFloat()) phase -= (2 * PI).toFloat()
                delay(33)
            }
        }
    }

    // Rotação do anel rúnico externo — mesma lógica de start/stop manual
    val ringRotation = remember { Animatable(0f) }
    LaunchedEffect(isActiveAnimating) {
        if (isActiveAnimating) {
            while (isActive) {
                ringRotation.animateTo(
                    ringRotation.value + 360f,
                    animationSpec = tween(45000, easing = LinearEasing)
                )
            }
        }
    }

    val amplitude = if (isActiveAnimating && progress > 0.01f && progress < 0.99f) {
        3.8f * sin(progress * PI.toFloat())
    } else 0f

    val baseY = 100f - progress * 100f

    val (gradTop, gradMid, gradBottom) = when {
        isBreakActive -> Triple(OrbColors.BreakTop, OrbColors.BreakMid, OrbColors.BreakBottom)
        isUrgent -> Triple(OrbColors.UrgentTop, OrbColors.UrgentMid, OrbColors.UrgentBottom)
        isPaused -> Triple(OrbColors.PausedTop, OrbColors.PausedMid, OrbColors.PausedBottom)
        else -> Triple(OrbColors.WorkTop, OrbColors.WorkMid, OrbColors.WorkBottom)
    }
    val (gradBackTop, gradBackBottom) = when {
        isBreakActive -> OrbColors.BreakBackTop to OrbColors.BreakBackBottom
        isUrgent -> OrbColors.UrgentBackTop to OrbColors.UrgentBackBottom
        isPaused -> OrbColors.PausedBackTop to OrbColors.PausedBackBottom
        else -> OrbColors.WorkBackTop to OrbColors.WorkBackBottom
    }
    val ringColor = when {
        isBreakActive -> OrbColors.RingBreak
        isUrgent -> OrbColors.RingUrgent
        else -> OrbColors.RingDefault
    }
    val textColor = when {
        isBreakActive -> OrbColors.TextEmerald300
        isUrgent -> OrbColors.TextRed400
        isPaused -> OrbColors.TextAmber500.copy(alpha = 0.8f)
        isRunning -> OrbColors.TextAmber100
        else -> OrbColors.TextAmber200.copy(alpha = 0.75f)
    }
    val textSizeSp = if (size == FocusOrbSize.FULLSCREEN) 44.sp else 26.sp

    Box(
        modifier = modifier.size(boxSizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val scale = min(w, h) / 100f
            val center = Offset(w / 2f, h / 2f)
            val radiusPx = 45f * scale

            // Fundo do "frasco"
            drawCircle(color = OrbColors.BgCircle, radius = radiusPx, center = center)

            // Onda líquida (clipada ao círculo)
            clipPath(buildCirclePath(center, radiusPx)) {
                val backPath = buildWavePath(baseY, -phase * 0.8f + PI.toFloat(), amplitude * 0.8f, scale, w, h)
                drawPath(
                    path = backPath,
                    brush = Brush.verticalGradient(listOf(gradBackTop, gradBackBottom))
                )
                val frontPath = buildWavePath(baseY, phase, amplitude, scale, w, h)
                drawPath(
                    path = frontPath,
                    brush = Brush.verticalGradient(listOf(gradTop, gradMid, gradBottom))
                )
            }

            // Anel rúnico rotativo tracejado
            withTransform({ rotate(ringRotation.value, pivot = center) }) {
                drawCircle(
                    color = ringColor.copy(alpha = 0.45f),
                    radius = 47f * scale,
                    center = center,
                    style = Stroke(
                        width = 1.2f * scale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f * scale, 4f * scale))
                    )
                )
            }

            // Borda de vidro
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = 45.2f * scale,
                center = center,
                style = Stroke(width = 1.6f * scale)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = 44.2f * scale,
                center = center,
                style = Stroke(width = 0.8f * scale)
            )
        }

        Text(
            text = formatOrbTime(timeLeft),
            color = textColor,
            fontWeight = FontWeight.Black,
            fontSize = textSizeSp
        )
    }
}

private fun formatOrbTime(seconds: Int): String {
    val m = (seconds / 60).toString().padStart(2, '0')
    val s = (seconds % 60).toString().padStart(2, '0')
    return "$m:$s"
}

private fun buildCirclePath(center: Offset, radius: Float): Path {
    return Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(center = center, radius = radius))
    }
}

// Gera o path da onda: 12 pontos (N=12), viewBox lógico 0-100 escalado para o Canvas real.
// Réplica de generateWavePath do FocusOrb.tsx — mesma contagem de pontos, mesma frequência
// (~1.2 ciclos ao longo da largura), mesmo fechamento de path (M -10,110 ... L 110,110 Z).
private fun buildWavePath(baseY: Float, phase: Float, amplitude: Float, scale: Float, w: Float, h: Float): Path {
    val n = 12
    val width = 120f
    val startX = -10f
    val path = Path()
    path.moveTo(-10f * scale, 110f * scale)
    for (i in 0..n) {
        val x = startX + (i * width) / n
        val angle = (x / width) * PI.toFloat() * 2f * 1.2f + phase
        val y = baseY + sin(angle) * amplitude
        path.lineTo(x * scale, y * scale)
    }
    path.lineTo(110f * scale, 110f * scale)
    path.close()
    return path
}