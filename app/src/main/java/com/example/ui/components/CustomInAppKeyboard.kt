package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoachPurple
import kotlinx.coroutines.delay

enum class KeyboardLayoutMode {
    LETTERS,
    NUMBERS,
    SYMBOLS
}

@Composable
fun CustomInAppKeyboard(
    visible: Boolean,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onSend: () -> Unit,
    isSendable: Boolean,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onHideKeyboard: () -> Unit = {},
    fontScale: Float = 1f,
    modifier: Modifier = Modifier
) {
    InAppKeyboardGrid(
        visible = visible,
        onKeyPress = onKeyPress,
        onBackspace = onBackspace,
        onClearAll = onClearAll,
        onSend = onSend,
        isSendable = isSendable,
        isRecording = isRecording,
        onStartRecording = onStartRecording,
        onStopRecording = onStopRecording,
        fontScale = fontScale,
        modifier = modifier
    )
}

@Composable
fun InAppKeyboardGrid(
    visible: Boolean,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onSend: () -> Unit,
    isSendable: Boolean,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    fontScale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var layoutMode by remember { mutableStateOf(KeyboardLayoutMode.LETTERS) }
    var isShifted by remember { mutableStateOf(false) }
    var isCapsLock by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)
        ) + fadeIn(animationSpec = tween(150)),
        exit = shrinkVertically(
            animationSpec = tween(180, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(120)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // KEYBOARD ROWS BASED ON CURRENT LAYOUT MODE
            when (layoutMode) {
                KeyboardLayoutMode.LETTERS -> {
                    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
                    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
                    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")

                    // ROW 1
                    KeyboardRow(
                        keys = row1,
                        isShifted = isShifted || isCapsLock,
                        isDark = isDark,
                        fontScale = fontScale,
                        onKeyPress = { key ->
                            onKeyPress(if (isShifted || isCapsLock) key.uppercase() else key)
                            if (isShifted && !isCapsLock) isShifted = false
                        }
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // ROW 2
                    KeyboardRow(
                        keys = row2,
                        isShifted = isShifted || isCapsLock,
                        isDark = isDark,
                        fontScale = fontScale,
                        sidePadding = 14.dp,
                        onKeyPress = { key ->
                            onKeyPress(if (isShifted || isCapsLock) key.uppercase() else key)
                            if (isShifted && !isCapsLock) isShifted = false
                        }
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // ROW 3: SHIFT + KEYS + BACKSPACE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // SHIFT / CAPS KEY
                        KeyCap(
                            weight = 1.35f,
                            isDark = isDark,
                            isSpecial = true,
                            isHighlighted = isShifted || isCapsLock,
                            onClick = {
                                if (isShifted) {
                                    isCapsLock = true
                                    isShifted = false
                                } else if (isCapsLock) {
                                    isCapsLock = false
                                    isShifted = false
                                } else {
                                    isShifted = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowUpward,
                                contentDescription = "Shift",
                                tint = if (isShifted || isCapsLock) Color.White else if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF334155),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        row3.forEach { key ->
                            val displayKey = if (isShifted || isCapsLock) key.uppercase() else key
                            KeyCap(
                                text = displayKey,
                                isDark = isDark,
                                fontScale = fontScale,
                                onClick = {
                                    onKeyPress(displayKey)
                                    if (isShifted && !isCapsLock) isShifted = false
                                }
                            )
                        }

                        // BACKSPACE KEY (continuous deletion on hold)
                        var isPressingBackspace by remember { mutableStateOf(false) }
                        LaunchedEffect(isPressingBackspace) {
                            if (isPressingBackspace) {
                                onBackspace()
                                delay(380)
                                while (isPressingBackspace) {
                                    onBackspace()
                                    delay(65)
                                }
                            }
                        }

                        KeyCap(
                            weight = 1.35f,
                            isDark = isDark,
                            isSpecial = true,
                            onLongPress = { onClearAll() },
                            onPressChange = { isPressingBackspace = it },
                            onClick = { onBackspace() }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Backspace,
                                contentDescription = "Backspace",
                                tint = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF334155),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                KeyboardLayoutMode.NUMBERS -> {
                    val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                    val row2 = listOf("@", "#", "$", "%", "&", "*", "-", "+", "(", ")")
                    val row3 = listOf("!", "\"", "'", ":", ";", "/", "?")

                    KeyboardRow(keys = row1, isShifted = false, isDark = isDark, fontScale = fontScale, onKeyPress = onKeyPress)
                    Spacer(modifier = Modifier.height(5.dp))
                    KeyboardRow(keys = row2, isShifted = false, isDark = isDark, fontScale = fontScale, onKeyPress = onKeyPress)
                    Spacer(modifier = Modifier.height(5.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KeyCap(
                            text = "=\\<",
                            weight = 1.35f,
                            isDark = isDark,
                            isSpecial = true,
                            fontScale = fontScale,
                            onClick = { layoutMode = KeyboardLayoutMode.SYMBOLS }
                        )

                        row3.forEach { key ->
                            KeyCap(text = key, isDark = isDark, fontScale = fontScale, onClick = { onKeyPress(key) })
                        }

                        KeyCap(
                            weight = 1.35f,
                            isDark = isDark,
                            isSpecial = true,
                            onClick = { onBackspace() }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Backspace,
                                contentDescription = "Backspace",
                                tint = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF334155),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                KeyboardLayoutMode.SYMBOLS -> {
                    val row1 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")
                    val row2 = listOf("£", "€", "¥", "¢", "^", "°", "=", "{", "}", "\\")
                    val row3 = listOf("%", "_", "<", ">", "[", "]", "§")

                    KeyboardRow(keys = row1, isShifted = false, isDark = isDark, fontScale = fontScale, onKeyPress = onKeyPress)
                    Spacer(modifier = Modifier.height(5.dp))
                    KeyboardRow(keys = row2, isShifted = false, isDark = isDark, fontScale = fontScale, onKeyPress = onKeyPress)
                    Spacer(modifier = Modifier.height(5.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KeyCap(
                            text = "?123",
                            weight = 1.35f,
                            isDark = isDark,
                            isSpecial = true,
                            fontScale = fontScale,
                            onClick = { layoutMode = KeyboardLayoutMode.NUMBERS }
                        )

                        row3.forEach { key ->
                            KeyCap(text = key, isDark = isDark, fontScale = fontScale, onClick = { onKeyPress(key) })
                        }

                        KeyCap(
                            weight = 1.35f,
                            isDark = isDark,
                            isSpecial = true,
                            onClick = { onBackspace() }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Backspace,
                                contentDescription = "Backspace",
                                tint = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF334155),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // BOTTOM ACTION ROW: [?123] [,] [SPACE • HOLD TO SPEAK] [.] [SEND ACTION]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // MODE TOGGLE (?123 / ABC)
                KeyCap(
                    text = if (layoutMode == KeyboardLayoutMode.LETTERS) "?123" else "ABC",
                    weight = 1.4f,
                    isDark = isDark,
                    isSpecial = true,
                    fontScale = fontScale,
                    onClick = {
                        layoutMode = if (layoutMode == KeyboardLayoutMode.LETTERS) {
                            KeyboardLayoutMode.NUMBERS
                        } else {
                            KeyboardLayoutMode.LETTERS
                        }
                    }
                )

                // COMMA KEY
                KeyCap(
                    text = ",",
                    weight = 0.9f,
                    isDark = isDark,
                    fontScale = fontScale,
                    onClick = { onKeyPress(",") }
                )

                // EXPANSIVE SPACEBAR & HOLD-TO-SPEAK ZONE
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = when {
                        isRecording -> Color(0xFFDC2626)
                        isDark -> Color(0xFF262B3F)
                        else -> Color(0xFFFFFFFF)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isRecording -> Color(0xFFFCA5A5)
                            isDark -> Color.White.copy(alpha = 0.12f)
                            else -> Color(0xFFCBD5E1)
                        }
                    ),
                    shadowElevation = if (isRecording) 6.dp else 2.dp,
                    modifier = Modifier
                        .weight(4.4f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    onKeyPress(" ")
                                },
                                onPress = {
                                    onStartRecording()
                                    tryAwaitRelease()
                                    onStopRecording()
                                }
                            )
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isRecording) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "Recording",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Listening... release to send",
                                    fontSize = (12 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "space  •  hold to speak",
                                    fontSize = (11.5 * fontScale).sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color.White.copy(alpha = 0.45f) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                // PERIOD KEY
                KeyCap(
                    text = ".",
                    weight = 0.9f,
                    isDark = isDark,
                    fontScale = fontScale,
                    onClick = { onKeyPress(".") }
                )

                // SEND ACTION BUTTON
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = if (isSendable) CoachPurple else if (isDark) Color(0xFF1E2235) else Color(0xFFE2E8F0),
                    border = BorderStroke(
                        1.dp,
                        if (isSendable) Color.White.copy(alpha = 0.4f) else if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFCBD5E1)
                    ),
                    shadowElevation = if (isSendable) 6.dp else 1.dp,
                    modifier = Modifier
                        .weight(1.6f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .clickable(
                            enabled = isSendable,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White)
                        ) {
                            onSend()
                        }
                        .testTag("keyboard_action_send")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send",
                            tint = if (isSendable) Color.White else if (isDark) Color.White.copy(alpha = 0.35f) else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardRow(
    keys: List<String>,
    isShifted: Boolean,
    isDark: Boolean,
    fontScale: Float,
    sidePadding: androidx.compose.ui.unit.Dp = 0.dp,
    onKeyPress: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sidePadding + 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        keys.forEach { key ->
            val displayKey = if (isShifted) key.uppercase() else key
            KeyCap(
                text = displayKey,
                isDark = isDark,
                fontScale = fontScale,
                onClick = { onKeyPress(displayKey) }
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.KeyCap(
    text: String? = null,
    weight: Float = 1f,
    isDark: Boolean,
    isSpecial: Boolean = false,
    isHighlighted: Boolean = false,
    fontScale: Float = 1f,
    onLongPress: (() -> Unit)? = null,
    onPressChange: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit,
    content: (@Composable () -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(11.dp),
        color = when {
            isHighlighted -> CoachPurple
            isSpecial && isDark -> Color(0xFF1E2235)
            isSpecial && !isDark -> Color(0xFFE2E8F0)
            isDark -> Color(0xFF262B3F)
            else -> Color(0xFFFFFFFF)
        },
        border = BorderStroke(
            1.dp,
            when {
                isHighlighted -> Color.White.copy(alpha = 0.5f)
                isDark -> Color.White.copy(alpha = 0.12f)
                else -> Color(0xFFCBD5E1)
            }
        ),
        shadowElevation = 2.dp,
        modifier = Modifier
            .weight(weight)
            .height(44.dp)
            .clip(RoundedCornerShape(11.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange?.invoke(true)
                        tryAwaitRelease()
                        onPressChange?.invoke(false)
                    },
                    onTap = { onClick() },
                    onLongPress = { onLongPress?.invoke() }
                )
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (content != null) {
                content()
            } else if (text != null) {
                Text(
                    text = text,
                    fontSize = (17 * fontScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isHighlighted -> Color.White
                        isSpecial && isDark -> Color.White.copy(alpha = 0.9f)
                        isSpecial && !isDark -> Color(0xFF334155)
                        isDark -> Color.White
                        else -> Color(0xFF0F172A)
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
