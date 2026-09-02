package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.FlashcardEntity
import com.example.data.local.MemoraSettings
import com.example.data.local.SpacedRepetitionStages
import com.example.data.remote.CoachFeedbackParser
import com.example.data.remote.ParsedCoachResponse
import com.example.data.speech.AudioRecordState
import com.example.data.speech.SpeechRecognitionState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.ui.components.StageBadge
import com.example.ui.components.FormattedQuotedText
import com.example.ui.components.QuotedHighlightPurple
import com.example.ui.components.QuotedHighlightRed
import com.example.ui.components.QuotedHighlightYellow
import com.example.ui.theme.CoachPurple
import com.example.ui.theme.MasteredGold
import com.example.ui.theme.StageColors
import com.example.ui.viewmodel.AiCoachSessionState
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.ReviewAction

@Composable
fun ExplanationAiSessionScreen(
    sessionState: AiCoachSessionState,
    onSendMessage: (String) -> Unit,
    onCompleteSession: (ReviewAction, Int?) -> Unit,
    onSaveCreationCard: (title: String, meaning: String) -> Unit = { _, _ -> },
    onCloseSession: () -> Unit,
    onSpeak: (String) -> Unit,
    onStopTts: () -> Unit,
    isTtsPlaying: Boolean,
    speechState: SpeechRecognitionState = SpeechRecognitionState.Idle,
    speechPartialText: String = "",
    speechSoundLevel: Float = 0f,
    onStartSpeech: () -> Unit = {},
    onStopSpeech: () -> Unit = {},
    onResetSpeech: () -> Unit = {},
    audioRecordState: AudioRecordState = AudioRecordState.Idle,
    audioRecordDuration: Int = 0,
    audioRecordAmplitude: Float = 0f,
    onStartAudioRecording: () -> Unit = {},
    onStopAudioRecording: ((String) -> Unit) -> Unit = {},
    onCancelAudioRecording: () -> Unit = {},
    onResetAudioRecording: () -> Unit = {},
    onAddCardFromChat: ((domain: String, context: String, title: String, meaning: String, transcript: String) -> Unit)? = null,
    settings: MemoraSettings = MemoraSettings(),
    fontScale: Float = 1.0f,
    onUpdateApiKey: (String) -> Unit = {},
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val card = sessionState.card
    val isCreation = sessionState.isCreationMode
    var inputText by remember { mutableStateOf("") }
    var customTitle by remember(sessionState.suggestedTitle) { mutableStateOf(sessionState.suggestedTitle) }
    var customMeaning by remember(sessionState.suggestedMeaning) { mutableStateOf(sessionState.suggestedMeaning) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var tempApiKey by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Granular Bubble Selection state (tap/long-tap bubbles to add them as new review items)
    var selectedBubbleIds by remember { mutableStateOf(setOf<String>()) }
    var showAddInteractiveDialog by remember { mutableStateOf(false) }
    val isSelectionMode = selectedBubbleIds.isNotEmpty()

    LaunchedEffect(sessionState.messages.size) {
        if (sessionState.messages.isNotEmpty() && !isSelectionMode) {
            listState.animateScrollToItem(sessionState.messages.size - 1)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartAudioRecording()
        }
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun dismissKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    fun toggleBubble(bubbleId: String) {
        selectedBubbleIds = if (bubbleId in selectedBubbleIds) {
            selectedBubbleIds - bubbleId
        } else {
            selectedBubbleIds + bubbleId
        }
    }

    fun longClickBubble(bubbleId: String) {
        selectedBubbleIds = if (bubbleId in selectedBubbleIds) {
            selectedBubbleIds - bubbleId
        } else {
            selectedBubbleIds + bubbleId
        }
    }

    BackHandler {
        if (isSelectionMode) {
            selectedBubbleIds = emptySet()
        } else {
            onCloseSession()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                dismissKeyboard()
            }
            .safeDrawingPadding()
            .testTag("ai_coach_session_screen")
    ) {
        // LAYER 1: Chat Transcript Messages (Scrolls full screen beneath floating controls)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = if (isSelectionMode) 58.dp else 16.dp,
                bottom = if (isCreation) 120.dp else 160.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(sessionState.messages) { index, message ->
                ChatMessageBubble(
                    messageIndex = index,
                    message = message,
                    card = card,
                    selectedBubbleIds = selectedBubbleIds,
                    onToggleBubble = { id -> toggleBubble(id) },
                    onLongClickBubble = { id -> longClickBubble(id) },
                    onSpeak = onSpeak,
                    onStopTts = onStopTts,
                    isTtsPlaying = isTtsPlaying,
                    fontScale = fontScale
                )
            }

            if (sessionState.isLoading) {
                val pastSessionsCount = card.countPastSessions()
                val loadingMessage = if (isCreation) {
                    "Analyzing context & preparing initial scenario..."
                } else if (pastSessionsCount > 0) {
                    "Considering our $pastSessionsCount past ${if (pastSessionsCount == 1) "conversation" else "conversations"}..."
                } else {
                    "Reviewing target expression & context..."
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CoachPurple.copy(alpha = 0.1f),
                            modifier = Modifier.padding(8.dp)
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
                                    text = loadingMessage,
                                    fontSize = (12 * fontScale).sp,
                                    color = CoachPurple,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            if (sessionState.errorMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("ai_coach_error_notice")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Connection Notice",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = (13 * fontScale).sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = sessionState.errorMessage ?: "Failed to generate AI response.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = (12 * fontScale).sp,
                                lineHeight = (16 * fontScale).sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { showApiKeyDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("🔑 Enter API Key", fontSize = (11 * fontScale).sp)
                                }
                                OutlinedButton(
                                    onClick = { onRetry() },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("🔄 Retry", fontSize = (11 * fontScale).sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // LAYER 2: Text Message Input Bar & Stage Selector (Overlaid Transparently at Bottom)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            val handleSendMessage = {
                if (inputText.isNotBlank() && !sessionState.isLoading) {
                    val msg = inputText.trim()
                    inputText = ""
                    onSendMessage(msg)
                }
            }

            // UNIFIED FLOATING BUBBLE TEXT INPUT BOX
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
                border = BorderStroke(
                    1.2.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Text Field inside the bubble
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = (15 * fontScale).sp,
                            lineHeight = (21 * fontScale).sp
                        ),
                        cursorBrush = SolidColor(CoachPurple),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp, vertical = 8.dp)
                            .testTag("ai_coach_message_input"),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { handleSendMessage() }
                        ),
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text(
                                    text = if (card.aiDomain == "KNOWLEDGE") "Answer the question..." else "Type your sentence...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                    fontSize = (13.5 * fontScale).sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Voice Recording & Send Action Buttons
                    val isRecording = audioRecordState is AudioRecordState.Recording
                    val isTranscribing = audioRecordState is AudioRecordState.Transcribing
                    val sendTint = if (inputText.isNotBlank() && !sessionState.isLoading) CoachPurple else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)

                    // Recording button next to Send
                    IconButton(
                        onClick = {
                            if (isRecording) {
                                onStopAudioRecording { transcribedText ->
                                    if (transcribedText.isNotBlank()) {
                                        inputText = if (inputText.isBlank()) transcribedText else "$inputText $transcribedText"
                                    }
                                }
                            } else if (!isTranscribing) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    onStartAudioRecording()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("ai_coach_voice_record_button")
                    ) {
                        if (isTranscribing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(17.dp),
                                color = CoachPurple,
                                strokeWidth = 2.dp
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(
                        onClick = { handleSendMessage() },
                        enabled = inputText.isNotBlank() && !sessionState.isLoading,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("send_ai_coach_message_button")
                    ) {
                        if (sessionState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = CoachPurple,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = sendTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Session Stage Selection Bar (Edge-to-Edge Free Floating Row)
            if (isCreation) {
                Button(
                    onClick = {
                        onSaveCreationCard(customTitle, customMeaning)
                    },
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(46.dp)
                        .testTag("save_discovery_card_button")
                ) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Card & Start Schedule",
                        fontSize = (13 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Free floating edge-to-edge scrollable stages row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(8.dp))

                    // All Spaced-Repetition Stages (2m, 20m, 2h, 1d, 3d, 1w, 2w, 1mo)
                    SpacedRepetitionStages.STAGES.forEach { stg ->
                        val isCurrent = card.currentStageId == stg.id
                        val color = StageColors.getOrElse(stg.id) { CoachPurple }

                        Surface(
                            onClick = { onCompleteSession(ReviewAction.PROMOTE_NEXT, stg.id) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurrent) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            border = BorderStroke(1.2.dp, if (isCurrent) color else color.copy(alpha = 0.35f)),
                            shadowElevation = if (isCurrent) 2.dp else 1.dp,
                            modifier = Modifier.testTag("ai_session_stage_${stg.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stg.shortName,
                                    fontSize = (12 * fontScale).sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isCurrent) color else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Mastered Button in complete form in the same single row
                    val isMastered = card.isMastered
                    Surface(
                        onClick = { onCompleteSession(ReviewAction.MARK_MASTERED, null) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isMastered) MasteredGold.copy(alpha = 0.35f) else MasteredGold.copy(alpha = 0.16f),
                        border = BorderStroke(1.2.dp, MasteredGold.copy(alpha = 0.8f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier.testTag("ai_session_mark_mastered")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mastered",
                                fontSize = (12 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        // LAYER 3: TOP HEADER FOR SELECTION MODE
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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

                // Right side: Purple "Add Item" Button (38dp height)
                Button(
                    onClick = { showAddInteractiveDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                    shape = RoundedCornerShape(19.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("ai_session_add_review_item_button")
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
            }
        }
    }

    // DIALOG: ADD CHAT BUBBLES TO INTERACTIVE ITEM
    if (showAddInteractiveDialog && selectedBubbleIds.isNotEmpty()) {
        val selectedBubblesList = remember(selectedBubbleIds, sessionState.messages) {
            val list = mutableListOf<SelectedBubbleItem>()
            sessionState.messages.forEachIndexed { index, msg ->
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
                                    speakerRole = "Correction",
                                    cardTitle = "Correction Note",
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
                                    speakerRole = "Suggestion",
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

    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Text(
                    text = "🔑 Configure Gemini API Key",
                    fontWeight = FontWeight.Bold,
                    fontSize = (16 * fontScale).sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your Google AI Studio Gemini API key to enable real-time AI Coach conversation and high-accuracy voice transcription.",
                        fontSize = (13 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
                        label = { Text("Gemini API Key (AIzaSy...)") },
                        placeholder = { Text("Paste AIzaSy... key here") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inline_gemini_api_key_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempApiKey.isNotBlank()) {
                            onUpdateApiKey(tempApiKey.trim())
                            showApiKeyDialog = false
                            onRetry()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                    enabled = tempApiKey.isNotBlank()
                ) {
                    Text("Save & Retry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageBubble(
    messageIndex: Int,
    message: ChatMessage,
    card: FlashcardEntity,
    selectedBubbleIds: Set<String>,
    onToggleBubble: (String) -> Unit,
    onLongClickBubble: (String) -> Unit,
    onSpeak: (String) -> Unit,
    onStopTts: () -> Unit,
    isTtsPlaying: Boolean,
    fontScale: Float
) {
    val isUser = message.role == "user"
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    if (isUser) {
        val bubbleId = "$messageIndex:USER"
        val isSelected = bubbleId in selectedBubbleIds

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
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
                    .testTag("user_chat_bubble")
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("coach_response_block"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (parsed.hasStructuredFeedback) {
                // 🔴 BOX 1: RED FRAME BOX (Corrections & Inaccuracies) - INDEPENDENT BUBBLE
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
                            .testTag("red_correction_box")
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

                // 🟡 BOX 2: YELLOW FRAME BOX (Suggestions & Phrasing) - INDEPENDENT BUBBLE
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
                            .testTag("yellow_suggestion_box")
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

                // 🎯💜 BOX 3: CONNECTED PURPLE FRAME BOX (Target Expression Mastery & Coach Reply) - INDEPENDENT BUBBLE
                val targetText = parsed.targetWordFeedback
                val replyText = parsed.conversationalReply
                if (!targetText.isNullOrBlank() || replyText.isNotBlank()) {
                    val replyBubbleId = "$messageIndex:REPLY"
                    val isReplySelected = replyBubbleId in selectedBubbleIds

                    OutlinedCard(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isReplySelected) CoachPurple.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
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
                            .testTag("coach_reply_card")
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
                // 💜 INITIAL SCENARIO / CONVERSATIONAL BUBBLE (Purple Frame) - INDEPENDENT BUBBLE
                if (parsed.conversationalReply.isNotBlank()) {
                    val generalBubbleId = "$messageIndex:GENERAL"
                    val isGeneralSelected = generalBubbleId in selectedBubbleIds

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
                            .testTag("coach_dialogue_bubble")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            FormattedQuotedText(
                                text = parsed.conversationalReply,
                                quoteColor = QuotedHighlightPurple,
                                fontSize = (14 * fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = (20 * fontScale).sp
                            )
                        }
                    }
                }
            }
        }
    }
}
