package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import com.example.ui.components.CustomInAppKeyboard
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.cos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.ripple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ZoomIn
import androidx.core.content.ContextCompat
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.heightIn
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.local.MemoraSettings
import com.example.data.remote.CoachFeedbackParser
import com.example.data.remote.GeminiClient
import com.example.data.remote.ImageSearchService
import com.example.data.remote.SearchImageResult
import com.example.data.speech.AudioRecordState
import com.example.data.speech.SpeechRecognitionState
import com.example.ui.components.FormattedQuotedText
import com.example.ui.components.QuotedHighlightPurple
import com.example.ui.components.QuotedHighlightRed
import com.example.ui.components.QuotedHighlightYellow
import com.example.ui.theme.CoachPurple
import com.example.ui.viewmodel.ChatMessage

/**
 * Custom modifier to draw tilted/diagonal white hatching lines across a composable
 * when selected, indicating selection without altering size, margins, or introducing checkboxes.
 */
fun Modifier.diagonalHatchOverlay(
    isSelected: Boolean,
    stripeColor: Color = Color.White.copy(alpha = 0.28f),
    stripeWidthDp: Dp = 1.5.dp,
    stripeSpacingDp: Dp = 10.dp
): Modifier = if (!isSelected) this else this.drawWithContent {
    drawContent()
    val width = size.width
    val height = size.height
    val strokePx = stripeWidthDp.toPx()
    val stepPx = stripeSpacingDp.toPx()

    var offset = -height
    while (offset < width + height) {
        drawLine(
            color = stripeColor,
            start = Offset(offset, 0f),
            end = Offset(offset + height, height),
            strokeWidth = strokePx
        )
        offset += stepPx
    }
}

data class SelectedBubbleItem(
    val id: String,
    val messageIndex: Int,
    val bubbleType: String,
    val speakerRole: String,
    val cardTitle: String,
    val textContent: String
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun FreeChatScreen(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onSpeak: (String) -> Unit,
    onStopTts: () -> Unit,
    isTtsPlaying: Boolean,
    speechState: SpeechRecognitionState,
    speechPartialText: String,
    audioRecordState: AudioRecordState,
    onStartVoiceRecording: () -> Boolean,
    onStopVoiceRecording: ((String) -> Unit) -> Unit,
    onCancelVoiceRecording: () -> Unit,
    onResetVoiceRecording: (() -> Unit)? = null,
    onAddCardFromChat: ((domain: String, context: String, title: String, meaning: String, transcript: String) -> Unit)? = null,
    onInsertImageToNotepad: ((imageUrl: String, caption: String) -> Unit)? = null,
    onCreateCardWithImage: ((imageUrl: String, title: String) -> Unit)? = null,
    onRequestImageForMessage: ((messageIndex: Int, query: String) -> Unit)? = null,
    onCycleNextImage: ((messageIndex: Int) -> Unit)? = null,
    settings: MemoraSettings = MemoraSettings(),
    isKeyboardExpanded: Boolean = false,
    onKeyboardExpandedChange: ((Boolean) -> Unit)? = null,
    fontScale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isRecordingActive by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    var internalKeyboardVisible by remember { mutableStateOf(false) }
    val isCustomKeyboardVisible = if (onKeyboardExpandedChange != null) isKeyboardExpanded else internalKeyboardVisible
    var selectedWordText by remember { mutableStateOf<String?>(null) }

    fun setKeyboardVisible(visible: Boolean) {
        if (onKeyboardExpandedChange != null) {
            onKeyboardExpandedChange(visible)
        } else {
            internalKeyboardVisible = visible
        }
    }

    // Granular Bubble Selection state (supports selecting individual cards inside one message)
    var selectedBubbleIds by remember { mutableStateOf(setOf<String>()) }
    var showAddInteractiveDialog by remember { mutableStateOf(false) }
    var showTokenWarningDialog by remember { mutableStateOf(false) }

    val isTokenLimitReached = messages.size >= 100

    val isSelectionMode = selectedBubbleIds.isNotEmpty()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty() && !isSelectionMode) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Permission launcher for audio
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecordingActive = onStartVoiceRecording()
        }
    }

    val isImeVisible = WindowInsets.isImeVisible
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun dismissKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
        setKeyboardVisible(false)
    }

    fun toggleBubble(bubbleId: String) {
        if (isSelectionMode) {
            selectedBubbleIds = if (bubbleId in selectedBubbleIds) {
                selectedBubbleIds - bubbleId
            } else {
                selectedBubbleIds + bubbleId
            }
        }
    }

    fun longClickBubble(bubbleId: String) {
        selectedBubbleIds = if (bubbleId in selectedBubbleIds) {
            selectedBubbleIds - bubbleId
        } else {
            selectedBubbleIds + bubbleId
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // LAYER 1: SCROLLING CHAT LIST / EMPTY STATE
        if (messages.isEmpty() && !isLoading) {
            AiIndicatorEmptyState(
                fontScale = fontScale,
                settings = settings,
                onDismissKeyboard = { dismissKeyboard() }
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 56.dp,
                    bottom = if (isImeVisible) 80.dp else 145.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(messages) { index, msg ->
                    FreeChatBubbleItem(
                        messageIndex = index,
                        message = msg,
                        selectedBubbleIds = selectedBubbleIds,
                        onToggleBubble = { id -> toggleBubble(id) },
                        onLongClickBubble = { id -> longClickBubble(id) },
                        onSpeak = onSpeak,
                        onStopTts = onStopTts,
                        isTtsPlaying = isTtsPlaying,
                        onInsertImageToNotepad = onInsertImageToNotepad,
                        onCreateCardWithImage = onCreateCardWithImage,
                        onRequestImageForMessage = onRequestImageForMessage,
                        onCycleNextImage = onCycleNextImage,
                        onPreviewImage = { previewImageUrl = it },
                        fontScale = fontScale
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = CoachPurple
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Thinking...",
                                        fontSize = (13 * fontScale).sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Trailing empty space: tapping below bubbles minimizes keyboard
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                dismissKeyboard()
                            }
                    )
                }
            }
        }

        // LAYER 2: TOP HEADER (Uniform height and vertical alignment for all buttons)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isSelectionMode) {
                // Left side: Cancel selection + count pill (uniform 38dp height)
                Surface(
                    shape = RoundedCornerShape(19.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                    ) {
                        IconButton(
                            onClick = { selectedBubbleIds = emptySet() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Selection",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${selectedBubbleIds.size} selected",
                            fontSize = (13 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Right side: Purple "Add Item" Button + Alert Badge (if >100) + Delete Dustbin (all 38dp height)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // PURPLE ADD BUTTON (38dp height)
                    Button(
                        onClick = { showAddInteractiveDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                        shape = RoundedCornerShape(19.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("chat_add_purple_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Add Item",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Item",
                            fontWeight = FontWeight.Bold,
                            fontSize = (12.5 * fontScale).sp,
                            color = Color.White
                        )
                    }

                    if (isTokenLimitReached) {
                        TokenLimitAlertBadge(
                            messagesCount = messages.size,
                            onClick = { showTokenWarningDialog = true },
                            fontScale = fontScale
                        )
                    }

                    // TRASH / DUSTBIN BUTTON (38dp size, perfectly vertically aligned)
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (messages.isNotEmpty()) {
                                    showClearConfirmDialog = true
                                }
                            }
                            .testTag("clear_chat_dustbin_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Chat",
                                tint = if (messages.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isTokenLimitReached) {
                        TokenLimitAlertBadge(
                            messagesCount = messages.size,
                            onClick = { showTokenWarningDialog = true },
                            fontScale = fontScale
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (messages.isNotEmpty()) {
                                    showClearConfirmDialog = true
                                }
                            }
                            .testTag("clear_chat_dustbin_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Chat",
                                tint = if (messages.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // LAYER 3: UNIFIED INPUT CONTAINER BOX (Text Field, Mic Button & Send Button all in the SAME box)
        val isDarkChat = MaterialTheme.colorScheme.background.luminance() < 0.5f
        val isTranscribing = audioRecordState == AudioRecordState.Transcribing
        val isSendable = inputText.isNotBlank() && !isLoading

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .then(
                    if (!isImeVisible) {
                        Modifier
                            .navigationBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, bottom = 82.dp)
                    } else {
                        Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    }
                )
        ) {
            // Audio recording error feedback banner
            if (audioRecordState is AudioRecordState.Error) {
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = (audioRecordState as AudioRecordState.Error).message,
                            fontSize = (11.5 * fontScale).sp,
                            color = Color(0xFFD97706),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Dismiss",
                            fontSize = (11.5 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            modifier = Modifier
                                .clickable { onResetVoiceRecording?.invoke() }
                                .padding(4.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isDarkChat) Color(0xFF141622).copy(alpha = 0.88f) else Color(0xFFFFFFFF).copy(alpha = 0.92f),
                border = BorderStroke(
                    1.2.dp,
                    if (isDarkChat) Color.White.copy(alpha = 0.18f) else Color(0xFFE2E8F0)
                ),
                shadowElevation = if (isDarkChat) 14.dp else 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("chat_unified_input_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Constant Text Field - does not deform or turn into a red frame when recording
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(
                            color = if (isDarkChat) Color.White else Color(0xFF0F172A),
                            fontSize = (15 * fontScale).sp,
                            lineHeight = (21 * fontScale).sp
                        ),
                        cursorBrush = SolidColor(CoachPurple),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("free_chat_text_input"),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    val msg = inputText.trim()
                                    inputText = ""
                                    onSendMessage(msg)
                                }
                            }
                        ),
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Ask anything or practice...",
                                    color = if (isDarkChat) Color.White.copy(alpha = 0.40f) else Color(0xFF94A3B8),
                                    fontSize = (14.5 * fontScale).sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Action Buttons - Recording Mic & Send Buttons
                    val isRecording = audioRecordState is AudioRecordState.Recording
                    val isTranscribing = audioRecordState is AudioRecordState.Transcribing
                    val sendTint = if (isDarkChat) Color(0xFFCBD5E1) else Color(0xFF64748B)

                    // Recording button next to send button
                    IconButton(
                        onClick = {
                            if (isRecording) {
                                onStopVoiceRecording { transcribedText ->
                                    if (transcribedText.isNotBlank()) {
                                        inputText = if (inputText.isBlank()) transcribedText else "$inputText $transcribedText"
                                    }
                                }
                            } else if (!isTranscribing) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    onStartVoiceRecording()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_voice_record_button")
                    ) {
                        if (isTranscribing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(17.dp),
                                strokeWidth = 2.dp,
                                color = CoachPurple
                            )
                        } else if (isRecording) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Recording",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record Voice",
                                tint = if (isDarkChat) Color(0xFFCBD5E1) else Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                val msg = inputText.trim()
                                inputText = ""
                                onSendMessage(msg)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_send_message_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = sendTint
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                tint = if (inputText.isNotBlank() && !isLoading) sendTint else if (isDarkChat) Color.White.copy(alpha = 0.25f) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // FULLSCREEN IMAGE PREVIEW DIALOG
        if (previewImageUrl != null) {
            FullscreenImageViewerDialog(
                imageUrl = previewImageUrl!!,
                onDismiss = { previewImageUrl = null },
                onInsertImageToNotepad = onInsertImageToNotepad,
                onCreateCardWithImage = onCreateCardWithImage,
                fontScale = fontScale
            )
        }

        // DIALOG: CLEAR CONFIRMATION
        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                title = { Text("Clear Chat Transcript?", fontWeight = FontWeight.Bold) },
                text = { Text("All current conversation messages will be erased and reset. Any long-term memories extracted will remain preserved in your Personal Memory.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showClearConfirmDialog = false
                            selectedBubbleIds = emptySet()
                            onClearChat()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // DIALOG: TOKEN OVERLOAD / HIGH MESSAGE COUNT ALERT
        if (showTokenWarningDialog) {
            AlertDialog(
                onDismissRequest = { showTokenWarningDialog = false },
                icon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                            .border(1.5.dp, Color(0xFFEF4444), CircleShape)
                    ) {
                        Canvas(modifier = Modifier.size(26.dp)) {
                            val strokeWidthPx = 2.2.dp.toPx()
                            val radius = (size.minDimension / 2f) - (strokeWidthPx / 2f)
                            val centerOffset = Offset(size.width / 2f, size.height / 2f)
                            val alertColor = Color(0xFFEF4444)

                            // Circle ring
                            drawCircle(
                                color = alertColor,
                                radius = radius,
                                center = centerOffset,
                                style = Stroke(width = strokeWidthPx)
                            )

                            // Exclamation vertical line
                            val barTop = centerOffset.y - radius * 0.45f
                            val barBottom = centerOffset.y + radius * 0.10f
                            drawLine(
                                color = alertColor,
                                start = Offset(centerOffset.x, barTop),
                                end = Offset(centerOffset.x, barBottom),
                                strokeWidth = strokeWidthPx,
                                cap = StrokeCap.Round
                            )

                            // Exclamation dot
                            val dotCenterY = centerOffset.y + radius * 0.48f
                            drawCircle(
                                color = alertColor,
                                radius = strokeWidthPx * 0.7f,
                                center = Offset(centerOffset.x, dotCenterY)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "High Token Usage Alert",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = (17 * fontScale).sp
                    )
                },
                text = {
                    Text(
                        text = "This conversation currently contains ${messages.size} messages.\n\nConversations exceeding 100 turns consume significantly higher tokens per prompt and may slow down AI responses.\n\nClearing the conversation is recommended for peak speed. If personalization is enabled, all your extracted facts and long-term memory remain safely preserved!",
                        fontSize = (13.5 * fontScale).sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showTokenWarningDialog = false
                            selectedBubbleIds = emptySet()
                            onClearChat()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear & Start Fresh", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTokenWarningDialog = false }) {
                        Text("Keep Chatting")
                    }
                }
            )
        }

        // DIALOG: ADD CHAT BUBBLES TO INTERACTIVE ITEM
        if (showAddInteractiveDialog && selectedBubbleIds.isNotEmpty()) {
            val selectedBubblesList = remember(selectedBubbleIds, messages) {
                val list = mutableListOf<SelectedBubbleItem>()
                messages.forEachIndexed { index, msg ->
                    if (msg.role == "user") {
                        val bubbleId = "$index:USER"
                        if (bubbleId in selectedBubbleIds) {
                            list.add(
                                SelectedBubbleItem(
                                    id = bubbleId,
                                    messageIndex = index,
                                    bubbleType = "USER",
                                    speakerRole = "User",
                                    cardTitle = "User",
                                    textContent = msg.content.trim()
                                )
                            )
                        }
                    } else {
                        val parsed = CoachFeedbackParser.parse(msg.content)
                        if (parsed.hasStructuredFeedback) {
                            val redId = "$index:RED"
                            if (redId in selectedBubbleIds && !parsed.redCorrection.isNullOrBlank()) {
                                list.add(
                                    SelectedBubbleItem(
                                        id = redId,
                                        messageIndex = index,
                                        bubbleType = "RED",
                                        speakerRole = "AI Coach Correction",
                                        cardTitle = "Grammar Correction",
                                        textContent = CoachFeedbackParser.stripAllTags(parsed.redCorrection)
                                    )
                                )
                            }
                            val yellowId = "$index:YELLOW"
                            if (yellowId in selectedBubbleIds && !parsed.yellowSuggestion.isNullOrBlank()) {
                                list.add(
                                    SelectedBubbleItem(
                                        id = yellowId,
                                        messageIndex = index,
                                        bubbleType = "YELLOW",
                                        speakerRole = "AI Coach Suggestion",
                                        cardTitle = "Alternative Suggestion",
                                        textContent = CoachFeedbackParser.stripAllTags(parsed.yellowSuggestion)
                                    )
                                )
                            }
                            val replyId = "$index:REPLY"
                            if (replyId in selectedBubbleIds) {
                                val combinedReply = buildString {
                                    if (!parsed.targetWordFeedback.isNullOrBlank()) {
                                        append(CoachFeedbackParser.stripAllTags(parsed.targetWordFeedback)).append("\n")
                                    }
                                    if (parsed.conversationalReply.isNotBlank()) {
                                        append(CoachFeedbackParser.stripAllTags(parsed.conversationalReply))
                                    }
                                }.trim()
                                if (combinedReply.isNotBlank()) {
                                    list.add(
                                        SelectedBubbleItem(
                                            id = replyId,
                                            messageIndex = index,
                                            bubbleType = "REPLY",
                                            speakerRole = "AI Coach",
                                            cardTitle = "Coach Response",
                                            textContent = combinedReply
                                        )
                                    )
                                }
                            }
                        } else {
                            val generalId = "$index:GENERAL"
                            if (generalId in selectedBubbleIds) {
                                val content = if (parsed.conversationalReply.isNotBlank()) {
                                    CoachFeedbackParser.stripAllTags(parsed.conversationalReply)
                                } else {
                                    CoachFeedbackParser.stripAllTags(msg.content)
                                }
                                list.add(
                                    SelectedBubbleItem(
                                        id = generalId,
                                        messageIndex = index,
                                        bubbleType = "GENERAL",
                                        speakerRole = "AI Coach",
                                        cardTitle = "Coach Response",
                                        textContent = content
                                    )
                                )
                            }
                        }
                    }
                }
                list
            }

            AddInteractiveFromChatDialog(
                selectedBubbles = selectedBubblesList,
                settings = settings,
                onDismiss = { showAddInteractiveDialog = false },
                onConfirm = { domain, contextCode, itemTitle, itemMeaning, transcript ->
                    onAddCardFromChat?.invoke(domain, contextCode, itemTitle, itemMeaning, transcript)
                    showAddInteractiveDialog = false
                    selectedBubbleIds = emptySet()
                },
                fontScale = fontScale
            )
        }
    }
}

// 20 friendly and professional small talk starters chosen randomly on each visit
private val SMALL_TALK_STARTERS = listOf(
    "Hello, how have you been?",
    "Hello, ask me something.",
    "If you have any questions, I'm right here.",
    "Good to see you! What's on your mind today?",
    "Ready to practice? Let's talk about anything you'd like.",
    "Hello! What would you like to explore or discuss today?",
    "I'm all ears. Tell me what you're working on today.",
    "Welcome back! How can I assist you right now?",
    "Curious about a phrase or word? Ask away!",
    "Hey there! Let's chat and practice together.",
    "Got a question about grammar or vocabulary? I'm here to help.",
    "Hello! How is your day going so far?",
    "Whenever you're ready, tell me what you want to learn today.",
    "Hi! Feel free to practice speaking or typing anytime.",
    "What topic would you love to dive into today?",
    "Hello! Share your thoughts with me, and let's practice.",
    "I'm here for you. What would you like to practice first?",
    "Great to have you here! Ask me anything.",
    "Hi there! Ready for some quick, relaxed practice?",
    "Hello! Tell me about something interesting on your mind."
)

@Composable
fun AiIndicatorEmptyState(
    fontScale: Float,
    settings: MemoraSettings? = null,
    onDismissKeyboard: () -> Unit
) {
    val isPersonalized = settings?.personalizationEnabled == true
    val userName = settings?.userName?.trim().orEmpty()

    val starterText = remember(isPersonalized, userName) {
        if (isPersonalized && userName.isNotBlank()) {
            val personalizedStarters = listOf(
                "Hello $userName! How have you been?",
                "Good to see you, $userName! What's on your mind today?",
                "Welcome back, $userName! How can I assist you right now?",
                "Hey $userName! Let's chat and practice together.",
                "Hello $userName! How is your day going so far?",
                "Whenever you're ready, $userName, tell me what you want to learn today.",
                "Hi $userName! Ready for some quick, relaxed practice?"
            )
            personalizedStarters.random()
        } else if (isPersonalized && userName.isBlank()) {
            "Hello! What is your name?"
        } else {
            SMALL_TALK_STARTERS.random()
        }
    }

    // Moving glowing shimmer animation through the text - slowed down for a smooth, captivating feel
    val infiniteTransition = rememberInfiniteTransition(label = "text_shimmer_glow")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -350f,
        targetValue = 1300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onDismissKeyboard()
            }
            .padding(horizontal = 32.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        // The ONLY element in the center: Pure text with moving glowing light inside it
        val shimmerBrush = Brush.linearGradient(
            colors = if (isDark) {
                listOf(
                    Color.White.copy(alpha = 0.70f),
                    Color(0xFFE9D5FF),
                    Color(0xFFC084FC),
                    Color(0xFF38BDF8),
                    Color.White,
                    Color(0xFFE9D5FF),
                    Color.White.copy(alpha = 0.70f)
                )
            } else {
                listOf(
                    Color(0xFF1E293B),
                    Color(0xFF7C3AED),
                    Color(0xFF9333EA),
                    Color(0xFF2563EB),
                    Color(0xFF6B21A8),
                    Color(0xFF1E293B)
                )
            },
            start = Offset(shimmerTranslate - 250f, shimmerTranslate - 250f),
            end = Offset(shimmerTranslate + 250f, shimmerTranslate + 250f)
        )

        Text(
            text = starterText,
            fontSize = (22 * fontScale).sp,
            fontWeight = FontWeight.SemiBold,
            style = TextStyle(
                brush = shimmerBrush,
                textAlign = TextAlign.Center,
                lineHeight = (32 * fontScale).sp,
                letterSpacing = 0.2.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FreeChatBubbleItem(
    messageIndex: Int,
    message: ChatMessage,
    selectedBubbleIds: Set<String>,
    onToggleBubble: (String) -> Unit,
    onLongClickBubble: (String) -> Unit,
    onSpeak: (String) -> Unit = {},
    onStopTts: () -> Unit = {},
    isTtsPlaying: Boolean = false,
    onInsertImageToNotepad: ((imageUrl: String, caption: String) -> Unit)? = null,
    onCreateCardWithImage: ((imageUrl: String, title: String) -> Unit)? = null,
    onRequestImageForMessage: ((messageIndex: Int, query: String) -> Unit)? = null,
    onCycleNextImage: ((messageIndex: Int) -> Unit)? = null,
    onPreviewImage: ((String) -> Unit)? = null,
    fontScale: Float = 1.0f
) {
    val isUser = message.role == "user"
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    if (isUser) {
        val bubbleId = "$messageIndex:USER"
        val isSelected = bubbleId in selectedBubbleIds

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = when {
                        isSelected -> CoachPurple.copy(alpha = 0.28f)
                        isDark -> Color(0xFF181B26).copy(alpha = 0.90f)
                        else -> Color(0xFFFFFFFF).copy(alpha = 0.95f)
                    }
                ),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.5.dp,
                    when {
                        isSelected -> CoachPurple
                        isDark -> Color.White.copy(alpha = 0.85f)
                        else -> Color(0xFFCBD5E1)
                    }
                ),
                elevation = CardDefaults.outlinedCardElevation(
                    defaultElevation = if (isDark) 4.dp else 2.dp
                ),
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .combinedClickable(
                        onClick = { onToggleBubble(bubbleId) },
                        onLongClick = { onLongClickBubble(bubbleId) }
                    )
                    .diagonalHatchOverlay(isSelected)
                    .testTag("user_free_chat_bubble")
            ) {
                FormattedQuotedText(
                    text = message.content,
                    fontSize = (14 * fontScale).sp,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    lineHeight = (20 * fontScale).sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    } else {
        val parsed = remember(message.content) {
            CoachFeedbackParser.parse(message.content)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_free_chat_response_block"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (parsed.hasStructuredFeedback) {
                    // 🔴 RED FRAME BOX (Grammar Correction) - INDEPENDENT BUBBLE
                    if (!parsed.redCorrection.isNullOrBlank()) {
                        val redBubbleId = "$messageIndex:RED"
                        val isRedSelected = redBubbleId in selectedBubbleIds

                        OutlinedCard(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isRedSelected) CoachPurple.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                            ),
                            border = BorderStroke(
                                if (isRedSelected) 2.dp else 1.5.dp,
                                if (isRedSelected) CoachPurple else Color(0xFFEF4444)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .combinedClickable(
                                    onClick = { onToggleBubble(redBubbleId) },
                                    onLongClick = { onLongClickBubble(redBubbleId) }
                                )
                                .diagonalHatchOverlay(isRedSelected)
                                .testTag("free_chat_red_correction_box")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                FormattedQuotedText(
                                    text = parsed.redCorrection,
                                    quoteColor = QuotedHighlightRed,
                                    fontSize = (14 * fontScale).sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = (20 * fontScale).sp
                                )
                            }
                        }
                    }

                    // 🟡 YELLOW FRAME BOX (Alternative Suggestions) - INDEPENDENT BUBBLE
                    if (!parsed.yellowSuggestion.isNullOrBlank()) {
                        val yellowBubbleId = "$messageIndex:YELLOW"
                        val isYellowSelected = yellowBubbleId in selectedBubbleIds

                        OutlinedCard(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isYellowSelected) CoachPurple.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                            ),
                            border = BorderStroke(
                                if (isYellowSelected) 2.dp else 1.5.dp,
                                if (isYellowSelected) CoachPurple else Color(0xFFF59E0B)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .combinedClickable(
                                    onClick = { onToggleBubble(yellowBubbleId) },
                                    onLongClick = { onLongClickBubble(yellowBubbleId) }
                                )
                                .diagonalHatchOverlay(isYellowSelected)
                                .testTag("free_chat_yellow_suggestion_box")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                FormattedQuotedText(
                                    text = parsed.yellowSuggestion,
                                    quoteColor = QuotedHighlightYellow,
                                    fontSize = (14 * fontScale).sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = (20 * fontScale).sp
                                )
                            }
                        }
                    }

                    // 🎯💜 PURPLE TARGET BOX & CONVERSATIONAL REPLY - INDEPENDENT BUBBLE
                    val targetText = parsed.targetWordFeedback
                    val replyText = parsed.conversationalReply
                    if (!targetText.isNullOrBlank() || replyText.isNotBlank()) {
                        val replyBubbleId = "$messageIndex:REPLY"
                        val isReplySelected = replyBubbleId in selectedBubbleIds

                        OutlinedCard(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isReplySelected) CoachPurple.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                            ),
                            border = BorderStroke(
                                if (isReplySelected) 2.dp else 1.5.dp,
                                CoachPurple
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .combinedClickable(
                                    onClick = { onToggleBubble(replyBubbleId) },
                                    onLongClick = { onLongClickBubble(replyBubbleId) }
                                )
                                .diagonalHatchOverlay(isReplySelected)
                                .testTag("free_chat_target_word_box")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                if (!targetText.isNullOrBlank()) {
                                    FormattedQuotedText(
                                        text = targetText,
                                        quoteColor = QuotedHighlightPurple,
                                        fontSize = (14 * fontScale).sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = (20 * fontScale).sp
                                    )
                                }
                                if (!targetText.isNullOrBlank() && replyText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                }
                                if (replyText.isNotBlank()) {
                                    FormattedQuotedText(
                                        text = replyText,
                                        quoteColor = QuotedHighlightPurple,
                                        fontSize = (14 * fontScale).sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = (20 * fontScale).sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // PURE CONVERSATION / ANSWER BUBBLE - INDEPENDENT BUBBLE
                    val generalBubbleId = "$messageIndex:GENERAL"
                    val isGeneralSelected = generalBubbleId in selectedBubbleIds
                    val contentToShow = if (parsed.conversationalReply.isNotBlank()) parsed.conversationalReply else CoachFeedbackParser.stripAllTags(message.content)

                    OutlinedCard(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isGeneralSelected) CoachPurple.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                        ),
                        border = BorderStroke(
                            if (isGeneralSelected) 2.dp else 1.5.dp,
                            CoachPurple
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .combinedClickable(
                                onClick = { onToggleBubble(generalBubbleId) },
                                onLongClick = { onLongClickBubble(generalBubbleId) }
                            )
                            .diagonalHatchOverlay(isGeneralSelected)
                            .testTag("coach_general_bubble")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            FormattedQuotedText(
                                text = contentToShow,
                                quoteColor = QuotedHighlightPurple,
                                fontSize = (14 * fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = (20 * fontScale).sp
                            )
                        }
                    }
                }

                // PURE VISUAL IMAGE DISPLAY (When image is searching, found, or queried)
                if (message.isSearchingImage || !message.imageUrl.isNullOrBlank() || message.candidateImages.isNotEmpty() || !message.imageSearchQuery.isNullOrBlank()) {
                    ChatClosestImageCard(
                        messageIndex = messageIndex,
                        imageUrl = message.imageUrl,
                        candidateImages = message.candidateImages,
                        isSearching = message.isSearchingImage,
                        searchQuery = message.imageSearchQuery,
                        onPreviewImage = onPreviewImage,
                        onRetrySearch = { query ->
                            onRequestImageForMessage?.invoke(messageIndex, query)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatClosestImageCard(
    messageIndex: Int,
    imageUrl: String?,
    candidateImages: List<SearchImageResult> = emptyList(),
    isSearching: Boolean,
    searchQuery: String?,
    onPreviewImage: ((String) -> Unit)? = null,
    onRetrySearch: ((String) -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val conceptTitle = remember(searchQuery) {
        searchQuery?.trim()?.takeIf { it.isNotBlank() } ?: "visual concept"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF141724).copy(alpha = 0.95f) else Color(0xFFF1F5F9),
        border = BorderStroke(
            1.2.dp,
            if (isDark) Color.White.copy(alpha = 0.16f) else Color(0xFFCBD5E1)
        ),
        shadowElevation = if (isDark) 6.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .testTag("chat_closest_image_card_$messageIndex")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: "This image is about <concept>" with Google/Web Search indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = CoachPurple,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "This image is about $conceptTitle",
                        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                // Source indicator badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CoachPurple.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text(
                        text = if (candidateImages.any { it.source.contains("Google", ignoreCase = true) }) "Google Images" else "Web Images",
                        color = CoachPurple,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0xFF1B1E2E) else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = CoachPurple
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Searching Google & Web Images for \"$conceptTitle\"...",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }
            } else if (candidateImages.size > 1) {
                // Horizontal scrollable carousel of all related images (up to 10)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("chat_images_carousel_$messageIndex"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    itemsIndexed(candidateImages) { index, item ->
                        val targetUrl = item.fullUrl.ifEmpty { item.thumbnailUrl }
                        Box(
                            modifier = Modifier
                                .width(260.dp)
                                .height(190.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) Color(0xFF141724) else Color(0xFFE2E8F0))
                                .combinedClickable(
                                    onClick = {
                                        onPreviewImage?.invoke(targetUrl)
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        if (clipboard != null) {
                                            val clip = ClipData.newPlainText("Image", targetUrl)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Image copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                .testTag("chat_image_item_${messageIndex}_$index")
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(targetUrl)
                                    .addHeader("User-Agent", ImageSearchService.BROWSER_USER_AGENT)
                                    .addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                loading = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(if (isDark) Color(0xFF161926) else Color(0xFFE2E8F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = CoachPurple,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                error = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(if (isDark) Color(0xFF1B1E2E) else Color(0xFFE2E8F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = null,
                                            tint = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            )

                            // Index indicator badge (e.g. 1/10)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.60f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "${index + 1}/${candidateImages.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                val targetUrl = imageUrl ?: candidateImages.firstOrNull()?.let { it.fullUrl.ifEmpty { it.thumbnailUrl } } ?: ""
                if (targetUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0xFF1B1E2E) else Color(0xFFE2E8F0))
                            .combinedClickable(
                                onClick = {
                                    onPreviewImage?.invoke(targetUrl)
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    if (clipboard != null) {
                                        val clip = ClipData.newPlainText("Image", targetUrl)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Image link copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(targetUrl)
                                .addHeader("User-Agent", ImageSearchService.BROWSER_USER_AGENT)
                                .addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Image result",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (isDark) Color(0xFF1B1E2E) else Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = CoachPurple,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (isDark) Color(0xFF1B1E2E) else Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        )
                    }
                } else {
                    // Fallback when search finished and 0 images were returned (never abruptly disappear)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0xFF1B1E2E) else Color(0xFFE2E8F0))
                            .padding(vertical = 16.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No web images found for \"$conceptTitle\"",
                                fontSize = 12.5.sp,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                            if (onRetrySearch != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { onRetrySearch(conceptTitle) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = CoachPurple
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry Google Search", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullscreenImageViewerDialog(
    imageUrl: String,
    onDismiss: () -> Unit,
    onInsertImageToNotepad: ((imageUrl: String, caption: String) -> Unit)? = null,
    onCreateCardWithImage: ((imageUrl: String, title: String) -> Unit)? = null,
    fontScale: Float = 1.0f
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.8f, 5f)
        offset += panChange
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // Transformable & hold-to-copy Image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state = transformState)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            if (clipboard != null) {
                                val clip = ClipData.newPlainText("Image", imageUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Image copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .addHeader("User-Agent", ImageSearchService.BROWSER_USER_AGENT)
                        .addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full View Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                        }
                    }
                )
            }

            // Top Header: Close button
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.20f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddInteractiveFromChatDialog(
    selectedBubbles: List<SelectedBubbleItem>,
    settings: MemoraSettings,
    onDismiss: () -> Unit,
    onConfirm: (domain: String, contextCode: String, title: String, meaning: String, transcript: String) -> Unit,
    fontScale: Float = 1.0f
) {
    // Intelligent heuristic preview extracted directly from structured feedback
    val (heuristicTitle, heuristicMeaning) = remember(selectedBubbles) {
        var foundTitle = ""
        val meaningNotes = mutableListOf<String>()

        for (bubble in selectedBubbles) {
            val cleanContent = CoachFeedbackParser.stripAllTags(bubble.textContent)
            when (bubble.bubbleType) {
                "RED" -> {
                    // Extract corrected target and grammar rule
                    val arrowMatch = Regex("[\"']([^\"']+)[\"']\\s*(?:→|->)\\s*[\"']([^\"']+)[\"']").find(cleanContent)
                    if (arrowMatch != null) {
                        val wrong = arrowMatch.groupValues[1].trim()
                        val correct = arrowMatch.groupValues[2].trim()
                        if (foundTitle.isBlank()) foundTitle = correct
                        meaningNotes.add("Corrected usage: \"$correct\" (avoid \"$wrong\")")
                    }
                    val reason = Regex("(?:❓|Why:|Reason:)\\s*(.+)").find(cleanContent)?.groupValues?.get(1)?.trim()
                    if (!reason.isNullOrBlank()) {
                        meaningNotes.add("Grammar Rule: $reason")
                    }
                }
                "YELLOW" -> {
                    // Extract natural alternative and nuance
                    val arrowMatch = Regex("[\"']([^\"']+)[\"']\\s*(?:→|->)\\s*[\"']([^\"']+)[\"']").find(cleanContent)
                    if (arrowMatch != null) {
                        val orig = arrowMatch.groupValues[1].trim()
                        val better = arrowMatch.groupValues[2].trim()
                        if (foundTitle.isBlank()) foundTitle = better
                        meaningNotes.add("Natural alternative: \"$better\" (instead of \"$orig\")")
                    }
                    val nuance = Regex("(?:❓|Why:|Reason:)\\s*(.+)").find(cleanContent)?.groupValues?.get(1)?.trim()
                    if (!nuance.isNullOrBlank()) {
                        meaningNotes.add("Nuance Note: $nuance")
                    }
                }
                "REPLY", "GENERAL" -> {
                    val firstBold = Regex("\\*\\*([^*]+)\\*\\*").find(cleanContent)?.groupValues?.get(1)?.trim()
                    if (!firstBold.isNullOrBlank() && foundTitle.isBlank()) {
                        foundTitle = firstBold
                    }
                    if (meaningNotes.isEmpty() && cleanContent.isNotBlank()) {
                        meaningNotes.add(cleanContent.take(180))
                    }
                }
                "USER" -> {
                    if (meaningNotes.isEmpty()) {
                        meaningNotes.add("Practice context: \"$cleanContent\"")
                    }
                }
            }
        }

        if (foundTitle.isBlank()) {
            val firstLine = selectedBubbles.firstOrNull()?.textContent?.lines()?.firstOrNull { it.isNotBlank() }?.trim() ?: "Practice Item"
            foundTitle = firstLine.take(45)
        }

        Pair(foundTitle, meaningNotes.joinToString("\n\n"))
    }

    val formattedTranscript = remember(selectedBubbles) {
        selectedBubbles.joinToString("\n\n") { bubble ->
            "${bubble.speakerRole}: ${bubble.textContent}"
        }
    }

    var isDistilling by remember { mutableStateOf(true) }
    var selectedDomain by remember { mutableStateOf("LINGUISTIC") }
    var linguisticContext by remember { mutableStateOf("FRIEND") }
    var titleText by remember { mutableStateOf(heuristicTitle) }
    var meaningText by remember { mutableStateOf(heuristicMeaning) }

    // Automatically distill the selected chat using AI on launch with full user settings
    LaunchedEffect(selectedBubbles, settings) {
        isDistilling = true
        val distillationResult = GeminiClient.distillConversationToCard(
            conversationTranscript = formattedTranscript,
            modelName = settings.geminiModel,
            thinkingLevel = settings.thinkingLevel,
            customApiKey = settings.geminiApiKey
        )
        distillationResult.onSuccess { distilled ->
            if (distilled.title.isNotBlank()) {
                titleText = distilled.title
            }
            if (distilled.meaning.isNotBlank()) {
                meaningText = distilled.meaning
            }
            selectedDomain = distilled.domain
            linguisticContext = distilled.linguisticContext
            isDistilling = false
        }.onFailure {
            isDistilling = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CoachPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Item to Library",
                        fontWeight = FontWeight.Bold,
                        fontSize = (18 * fontScale).sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isDistilling) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoachPurple.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, CoachPurple.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = CoachPurple
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AI is analyzing conversation...",
                                    fontSize = (13 * fontScale).sp,
                                    color = CoachPurple,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Crafting precise title & comprehensive usage notes",
                                    fontSize = (11.5 * fontScale).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Title (appears in your Spaced-Repetition list):",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (13 * fontScale).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    placeholder = { Text("e.g., Hit it off, Present Perfect vs. Past Simple") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_item_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = false,
                    maxLines = 3
                )

                Text(
                    text = "Meaning & Usage Notes:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (13 * fontScale).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = meaningText,
                    onValueChange = { meaningText = it },
                    placeholder = { Text("Grammar rules, clear explanation, nuances, and example sentences...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_item_meaning_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = false,
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedDomain,
                        linguisticContext,
                        titleText.trim().ifEmpty { heuristicTitle },
                        meaningText.trim().ifEmpty { heuristicMeaning },
                        formattedTranscript
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_distilled_card_button")
            ) {
                Text(
                    text = "Save to Library",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Circular red alert indicator that appears beside the Clear Chat button
 * when message count exceeds 100 turns, alerting the user about token overload.
 */
@Composable
fun TokenLimitAlertBadge(
    messagesCount: Int,
    onClick: () -> Unit,
    fontScale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val pulseTransition = rememberInfiniteTransition(label = "alert_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        shape = CircleShape,
        color = Color(0xFFEF4444).copy(alpha = 0.14f),
        border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
        shadowElevation = 2.dp,
        modifier = modifier
            .size(38.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .clickable { onClick() }
            .testTag("chat_token_alert_badge")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.size(20.dp)) {
                val strokeWidthPx = 1.8.dp.toPx()
                val radius = (size.minDimension / 2f) - (strokeWidthPx / 2f)
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val alertColor = Color(0xFFEF4444)

                // Circle ring
                drawCircle(
                    color = alertColor,
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidthPx)
                )

                // Exclamation vertical line
                val barTop = centerOffset.y - radius * 0.46f
                val barBottom = centerOffset.y + radius * 0.10f
                drawLine(
                    color = alertColor,
                    start = Offset(centerOffset.x, barTop),
                    end = Offset(centerOffset.x, barBottom),
                    strokeWidth = strokeWidthPx,
                    cap = StrokeCap.Round
                )

                // Exclamation dot
                val dotCenterY = centerOffset.y + radius * 0.50f
                drawCircle(
                    color = alertColor,
                    radius = strokeWidthPx * 0.65f,
                    center = Offset(centerOffset.x, dotCenterY)
                )
            }
        }
    }
}
