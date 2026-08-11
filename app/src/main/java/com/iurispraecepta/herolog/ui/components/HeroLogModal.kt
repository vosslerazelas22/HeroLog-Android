package com.iurispraecepta.herolog.ui.components

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

enum class ModalVariant { Amber, Purple, Red }

// Hex tokens
private val Stone950 = Color(0xFF0C0A09)
private val Stone900 = Color(0xFF1C1917)
private val Stone800 = Color(0xFF292524)
private val Stone400 = Color(0xFFA8A29E)

private val Amber600 = Color(0xFFD97706)
private val Amber500 = Color(0xFFF59E0B)
private val Amber400 = Color(0xFFFBBF24)

private val Purple600 = Color(0xFF9333EA)
private val Purple500 = Color(0xFFA855F7)
private val Purple400 = Color(0xFFC084FC)

private val Red600 = Color(0xFFDC2626)
private val Red500 = Color(0xFFEF4444)
private val Red400 = Color(0xFFF87171)

@Composable
fun HeroLogModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    title: String,
    variant: ModalVariant = ModalVariant.Amber,
    hideHeader: Boolean = false,
    allowBackdropClose: Boolean = true,
    disableEscClose: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var shouldRenderDialog by remember { mutableStateOf(isOpen) }
    var animatedVisible by remember { mutableStateOf(isOpen) }

    DisposableEffect(isOpen) {
        if (isOpen) {
            ModalCountRegistry.increment()
        }
        onDispose {
            if (isOpen) {
                ModalCountRegistry.decrement()
            }
        }
    }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            shouldRenderDialog = true
            animatedVisible = true
        } else {
            animatedVisible = false
            delay(220L) // Duração da animação de saída (tween 220ms)
            shouldRenderDialog = false
        }
    }

    if (!shouldRenderDialog) return

    BackHandler(enabled = shouldRenderDialog && !disableEscClose) {
        onClose()
    }

    val (borderColor, topGradient, glowColor, titleColor) = when (variant) {
        ModalVariant.Amber -> Quadruple(
            Color(0x4DF59E0B), // amber-500/30
            listOf(Amber600, Amber400, Amber600),
            Color(0x05F59E0B), // amber-500/[0.02]
            Amber400
        )
        ModalVariant.Purple -> Quadruple(
            Color(0x4DA855F7), // purple-500/30
            listOf(Purple600, Purple400, Purple600),
            Color(0x05A855F7), // purple-500/[0.02]
            Purple400
        )
        ModalVariant.Red -> Quadruple(
            Color(0x4DEF4444), // red-500/30
            listOf(Red600, Red400, Red600),
            Color(0x05EF4444), // red-500/[0.02]
            Red400
        )
    }

    Dialog(
        onDismissRequest = {
            if (allowBackdropClose) onClose()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false, // BackHandler manual é a única fonte de verdade
            dismissOnClickOutside = allowBackdropClose
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Backdrop
            var backdropModifier = Modifier
                .fillMaxSize()
                .background(Stone950.copy(alpha = 0.8f))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backdropModifier = backdropModifier.blur(2.dp)
            }

            if (allowBackdropClose) {
                backdropModifier = backdropModifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onClose()
                }
            }

            Box(modifier = backdropModifier)

            // Animated Modal Container
            AnimatedVisibility(
                visible = animatedVisible,
                enter = fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                        slideInVertically(initialOffsetY = { 15 }, animationSpec = tween(220, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(220, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.95f, animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                        slideOutVertically(targetOffsetY = { 15 }, animationSpec = tween(220, easing = FastOutSlowInEasing))
            ) {
                val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .widthIn(max = 448.dp)
                        .fillMaxWidth()
                        .heightIn(max = screenHeight * 0.8f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Stone900)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                ) {
                    // Ambient glow
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(glowColor, Color.Transparent)))
                    )

                    // Top Accent Border Line (4dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Brush.horizontalGradient(topGradient))
                            .align(Alignment.TopCenter)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        if (!hideHeader) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawBehind {
                                        drawLine(
                                            color = Stone800,
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title.uppercase(),
                                    modifier = Modifier.weight(1f),
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = titleColor,
                                        letterSpacing = 0.8.sp
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(
                                            role = Role.Button,
                                            onClick = onClose
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fechar",
                                        tint = Stone400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            content()
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
