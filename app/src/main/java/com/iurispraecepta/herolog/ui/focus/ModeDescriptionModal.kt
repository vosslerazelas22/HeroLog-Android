package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant

typealias ModeDescriptionBlock = RaidModeHelpBlock

private val Stone950 = Color(0xFF0C0A09)
private val Stone300 = Color(0xFFD6D3D1)

private val Amber500 = Color(0xFFF59E0B)
private val Amber400 = Color(0xFFFBBF24)
private val Amber950 = Color(0xFF451A03)

private val Purple600 = Color(0xFF9333EA)
private val Purple500 = Color(0xFFA855F7)
private val Purple400 = Color(0xFFC084FC)
private val Purple950 = Color(0xFF3B0764)

private val Red600 = Color(0xFFDC2626)
private val Red500 = Color(0xFFEF4444)
private val Red400 = Color(0xFFF87171)
private val Red950 = Color(0xFF450A0A)

@Composable
fun ModeDescriptionModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    title: String,
    variant: ModalVariant,
    blocks: List<RaidModeHelpBlock>,
) {
    val (cardBorder, cardBg, labelColor, buttonBg, buttonText) = when (variant) {
        ModalVariant.Amber -> Quintuple(
            Amber500.copy(alpha = 0.25f),
            Amber950.copy(alpha = 0.20f),
            Amber400,
            Amber500,
            Stone950
        )
        ModalVariant.Purple -> Quintuple(
            Purple500.copy(alpha = 0.25f),
            Purple950.copy(alpha = 0.25f),
            Purple400,
            Purple600,
            Color.White
        )
        ModalVariant.Red -> Quintuple(
            Red500.copy(alpha = 0.25f),
            Red950.copy(alpha = 0.25f),
            Red400,
            Red600,
            Color.White
        )
    }

    HeroLogModal(
        isOpen = isOpen,
        onClose = onClose,
        title = title,
        variant = variant
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            blocks.forEach { block ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Stone950)
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = block.label.uppercase(),
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = labelColor,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = block.text,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = Stone300
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonBg,
                    contentColor = buttonText
                )
            ) {
                Text(
                    text = "ENTENDIDO",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
