package com.iurispraecepta.herolog.ui.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant

private val Stone950 = Color(0xFF0C0A09)
private val Stone900 = Color(0xFF1C1917)
private val Stone800 = Color(0xFF292524)
private val Stone700 = Color(0xFF44403C)
private val Stone400 = Color(0xFFA8A29E)

private val Amber500 = Color(0xFFF59E0B)
private val Amber400 = Color(0xFFFBBF24)
private val Amber300 = Color(0xFFFCD34D)
private val Amber100 = Color(0xFFFEF3C7)

private val Red500 = Color(0xFFEF4444)
private val Red400 = Color(0xFFF87171)

private val PRESETS = listOf(25, 50, 90)

@Composable
fun TimerSettingsModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    pomodoroSettings: PomodoroSettings,
    isRunning: Boolean,
    isBreakActive: Boolean,
    onSavePresetDuration: (Int) -> Unit,
    onSaveCustomSettings: (focus: Int, shortBreak: Int, longBreak: Int) -> Unit,
    onToggleAutoStartBreak: () -> Unit,
    onToggleAutoStartFocus: () -> Unit,
) {
    val isCustomActiveInitial = pomodoroSettings.focusDuration !in PRESETS
    var isCustomTime by remember(isOpen, pomodoroSettings) { mutableStateOf(isCustomActiveInitial) }
    var customFocusInput by remember(isOpen, pomodoroSettings) {
        mutableStateOf(pomodoroSettings.focusDuration.toString())
    }
    var customShortBreakInput by remember(isOpen, pomodoroSettings) {
        mutableStateOf(pomodoroSettings.shortBreakDuration.toString())
    }
    var customLongBreakInput by remember(isOpen, pomodoroSettings) {
        mutableStateOf(pomodoroSettings.longBreakDuration.toString())
    }

    val isCustomActive = isCustomTime || (pomodoroSettings.focusDuration !in PRESETS)
    val isControlsDisabled = isRunning || isBreakActive

    val focusInt = customFocusInput.toIntOrNull()
    val shortInt = customShortBreakInput.toIntOrNull()
    val longInt = customLongBreakInput.toIntOrNull()

    val isFocusValid = focusInt != null && focusInt in 1..180
    val isShortValid = shortInt != null && shortInt in 1..60
    val isLongValid = longInt != null && longInt in 1..60
    val isCustomValid = isFocusValid && isShortValid && isLongValid

    HeroLogModal(
        isOpen = isOpen,
        onClose = onClose,
        title = "Ajustes do Timer",
        variant = ModalVariant.Amber
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Presets de Duração
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "PRESETS DE DURAÇÃO",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = Amber400
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESETS.forEach { preset ->
                        val isActive = !isCustomActive && pomodoroSettings.focusDuration == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isActive) Amber500.copy(alpha = 0.2f) else Stone900
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isActive) Amber400 else Stone800,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable(
                                    enabled = !isControlsDisabled,
                                    onClick = {
                                        onSavePresetDuration(preset)
                                        onClose()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$preset MIN",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = when {
                                        isControlsDisabled -> Stone400.copy(alpha = 0.5f)
                                        isActive -> Amber300
                                        else -> Stone400
                                    }
                                )
                            )
                        }
                    }
                }
            }

            // 2. Duração Personalizada
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Stone900.copy(alpha = 0.6f))
                    .border(1.dp, if (isCustomActive) Amber500.copy(alpha = 0.3f) else Stone800, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Duração Personalizada",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isCustomActive) Amber300 else Amber100
                            )
                        )
                        Text(
                            text = "Defina tempos sob medida para foco e descanso",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = Stone400
                            )
                        )
                    }

                    HeroLogToggleSwitch(
                        checked = isCustomActive,
                        enabled = !isControlsDisabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                isCustomTime = true
                            } else {
                                isCustomTime = false
                                onSavePresetDuration(25)
                            }
                        }
                    )
                }

                AnimatedVisibility(
                    visible = isCustomActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Foco (1-180 min)
                            CustomDurationInputField(
                                label = "Foco",
                                limitHint = "1-180m",
                                value = customFocusInput,
                                onValueChange = { customFocusInput = it.filter { c -> c.isDigit() } },
                                isValid = isFocusValid,
                                enabled = !isControlsDisabled,
                                modifier = Modifier.weight(1f)
                            )

                            // Pausa Curta (1-60 min)
                            CustomDurationInputField(
                                label = "P. Curta",
                                limitHint = "1-60m",
                                value = customShortBreakInput,
                                onValueChange = { customShortBreakInput = it.filter { c -> c.isDigit() } },
                                isValid = isShortValid,
                                enabled = !isControlsDisabled,
                                modifier = Modifier.weight(1f)
                            )

                            // Pausa Longa (1-60 min)
                            CustomDurationInputField(
                                label = "P. Longa",
                                limitHint = "1-60m",
                                value = customLongBreakInput,
                                onValueChange = { customLongBreakInput = it.filter { c -> c.isDigit() } },
                                isValid = isLongValid,
                                enabled = !isControlsDisabled,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (!isCustomValid) {
                            Text(
                                text = "Por favor, insira valores dentro dos limites indicados.",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Red400
                                ),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (isCustomValid && focusInt != null && shortInt != null && longInt != null) {
                                    onSaveCustomSettings(focusInt, shortInt, longInt)
                                    onClose()
                                }
                            },
                            enabled = isCustomValid && !isControlsDisabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Amber500,
                                contentColor = Stone950,
                                disabledContainerColor = Stone800,
                                disabledContentColor = Stone400.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "Salvar Personalizado",
                                style = TextStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            // 3. Opções Adicionais
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "OPÇÕES ADICIONAIS",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = Amber400
                    )
                )

                // Toggle 1: Início automático de descanso
                OptionToggleRow(
                    title = "Iniciar descanso automaticamente",
                    description = "Inicia o timer de descanso ao concluir uma sessão de foco",
                    checked = pomodoroSettings.autoStartBreak,
                    onToggle = onToggleAutoStartBreak
                )

                // Toggle 2: Início automático de foco
                OptionToggleRow(
                    title = "Iniciar foco automaticamente",
                    description = "Inicia a próxima sessão ao concluir o descanso",
                    checked = pomodoroSettings.autoStartFocus,
                    onToggle = onToggleAutoStartFocus
                )
            }
        }
    }
}

@Composable
private fun CustomDurationInputField(
    label: String,
    limitHint: String,
    value: String,
    onValueChange: (String) -> Unit,
    isValid: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Amber100
                )
            )
            Text(
                text = limitHint,
                style = TextStyle(
                    fontSize = 9.sp,
                    color = Stone400
                )
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = if (isValid) Color.White else Red400
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Stone950,
                unfocusedContainerColor = Stone950,
                disabledContainerColor = Stone950.copy(alpha = 0.5f),
                focusedBorderColor = if (isValid) Amber400 else Red500,
                unfocusedBorderColor = if (isValid) Stone800 else Red500,
                disabledBorderColor = Stone800.copy(alpha = 0.5f),
                cursorColor = Amber400
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OptionToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Stone900.copy(alpha = 0.4f))
            .border(1.dp, Stone800, RoundedCornerShape(10.dp))
            .clickable(
                role = Role.Switch,
                onClick = onToggle
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Amber100
                )
            )
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = Stone400
                )
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        HeroLogToggleSwitch(
            checked = checked,
            enabled = true,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
fun HeroLogToggleSwitch(
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = if (checked) Amber500 else Stone800
    val thumbColor = if (checked) Stone950 else Stone400

    Box(
        modifier = modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(CircleShape)
            .background(trackColor.copy(alpha = if (enabled) 1f else 0.5f))
            .clickable(
                enabled = enabled,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}
