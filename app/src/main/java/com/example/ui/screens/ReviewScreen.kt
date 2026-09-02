package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.FlashcardEntity
import com.example.data.local.SpacedRepetitionStages
import com.example.ui.components.StageBadge
import com.example.ui.components.TtsAudioButton
import com.example.ui.components.TypeBadge
import com.example.ui.theme.CoachPurple
import com.example.ui.theme.MasteredGold
import com.example.ui.theme.StageColors
import com.example.ui.viewmodel.ReviewAction
import com.example.ui.viewmodel.ReviewSessionState

@Composable
fun ReviewScreen(
    sessionState: ReviewSessionState,
    onFlip: () -> Unit,
    onAction: (ReviewAction, Int?) -> Unit,
    onCloseSession: () -> Unit,
    onStartAiCoach: (FlashcardEntity) -> Unit,
    onSpeak: (String) -> Unit,
    onStopTts: () -> Unit,
    isTtsPlaying: Boolean,
    fontScale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    if (sessionState.isFinished || sessionState.currentCard == null) {
        ReviewCompletedView(
            sessionState = sessionState,
            onClose = onCloseSession,
            fontScale = fontScale,
            modifier = modifier
        )
        return
    }

    val card = sessionState.currentCard!!
    val isInputCard = card.cardCategory == "INPUT"
    val isAiCoach = card.outputSubtype == "AI_COACH" || card.itemType == "EXPLANATION"
    val isSoundToWriting = card.outputSubtype == "SOUND_TO_WRITING"
    val isWordToSound = card.outputSubtype == "WORD_TO_SOUND"

    val currentStage = SpacedRepetitionStages.getStage(card.currentStageId)
    val nextStageId = SpacedRepetitionStages.getNextStageId(card.currentStageId)
    val nextStage = nextStageId?.let { SpacedRepetitionStages.getStage(it) }

    var showStagePickerMenu by remember { mutableStateOf(false) }
    var userSpellingInput by remember(card.id) { mutableStateOf("") }

    LaunchedEffect(card.id, isAiCoach) {
        if (isAiCoach) {
            onStartAiCoach(card)
        }
    }

    // Auto-read aloud pronunciation when flipping a Pronunciation card
    LaunchedEffect(card.id, sessionState.isFlipped) {
        if (sessionState.isFlipped && isWordToSound) {
            val textToSpeak = card.audioPronunciationText ?: card.titleContent
            if (textToSpeak.isNotBlank()) {
                onSpeak(textToSpeak)
            }
        }
    }

    // 3D Flip animation
    val rotation by animateFloatAsState(
        targetValue = if (sessionState.isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardFlipAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(16.dp)
            .testTag("review_session_screen")
    ) {
        // Top session control bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCloseSession,
                modifier = Modifier.testTag("close_review_session_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit Session",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Card ${sessionState.currentIndex + 1} of ${sessionState.totalCards}",
                    fontSize = (15 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${currentStage.name} (${currentStage.shortName})",
                    fontSize = (12 * fontScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StageColors.getOrElse(card.currentStageId) { MaterialTheme.colorScheme.primary }
                )
            }

            Box {
                IconButton(onClick = { showStagePickerMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Select Stage",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showStagePickerMenu,
                    onDismissRequest = { showStagePickerMenu = false }
                ) {
                    SpacedRepetitionStages.STAGES.forEach { stg ->
                        DropdownMenuItem(
                            text = { Text("Move to ${stg.name} (${stg.shortName})") },
                            onClick = {
                                showStagePickerMenu = false
                                onAction(ReviewAction.PROMOTE_NEXT, stg.id)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Mark as Mastered ⭐") },
                        onClick = {
                            showStagePickerMenu = false
                            onAction(ReviewAction.MARK_MASTERED, null)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { (sessionState.currentIndex + 1).toFloat() / sessionState.totalCards.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // If card is AI Coach, show prominent launcher banner
        if (isAiCoach) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CoachPurple.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, CoachPurple.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CoachPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (card.aiDomain == "KNOWLEDGE") "AI Knowledge Coach" else "AI Language Coach",
                            fontSize = (13 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = CoachPurple
                        )
                        val subContext = when (card.linguisticContext) {
                            "GIRLFRIEND" -> "💖 Girlfriend register"
                            "CLASS" -> "🎓 Classroom register"
                            else -> "🤝 Friend register"
                        }
                        Text(
                            text = if (card.aiDomain == "KNOWLEDGE") "Adaptive sequence & conceptual recall" else "$subContext • Roleplay scenario",
                            fontSize = (11 * fontScale).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { onStartAiCoach(card) },
                        colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("launch_ai_coach_button")
                    ) {
                        Text("Start Chat", fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // CARD DISPLAY AREA
        if (isInputCard) {
            // SINGLE-SIDED INPUT CARD (No flip required)
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("input_single_sided_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "📥 Input (Passive Review)",
                                fontSize = (11 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        StageBadge(stageId = card.currentStageId)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!card.imageUriOrBase64.isNullOrBlank()) {
                            AsyncImage(
                                model = card.imageUriOrBase64,
                                contentDescription = "Card Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = card.titleContent,
                            fontSize = (22 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = (28 * fontScale).sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = card.descriptionContent,
                                fontSize = (15 * fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = (22 * fontScale).sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        if (!card.audioPronunciationText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TtsAudioButton(
                                textToSpeak = card.audioPronunciationText,
                                isPlaying = isTtsPlaying,
                                onPlay = onSpeak,
                                onStop = onStopTts
                            )
                        }
                    }
                }
            }
        } else {
            // OUTPUT CARD (Active Recall with 3D Flip & Specialized Tests)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onFlip()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // FRONT FACE
                    FlashcardFrontFace(
                        card = card,
                        isSoundToWriting = isSoundToWriting,
                        isWordToSound = isWordToSound,
                        userSpellingInput = userSpellingInput,
                        onSpellingChange = { userSpellingInput = it },
                        onFlip = onFlip,
                        onSpeak = onSpeak,
                        onStopTts = onStopTts,
                        isTtsPlaying = isTtsPlaying,
                        fontScale = fontScale
                    )
                } else {
                    // BACK FACE (Rotate back by 180 so text is not mirrored)
                    Box(
                        modifier = Modifier.graphicsLayer {
                            rotationY = 180f
                        }
                    ) {
                        FlashcardBackFace(
                            card = card,
                            userSpellingInput = userSpellingInput,
                            isSoundToWriting = isSoundToWriting,
                            onSpeak = onSpeak,
                            onStopTts = onStopTts,
                            isTtsPlaying = isTtsPlaying,
                            fontScale = fontScale
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons at Bottom
        if (isInputCard || sessionState.isFlipped || isAiCoach) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary 3 Straightforward Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Forgot / Reset Button
                    Button(
                        onClick = { onAction(ReviewAction.RESET_TIMER, null) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("review_action_forgot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Forgot",
                                fontSize = (14 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reset Timer",
                                fontSize = (10 * fontScale).sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 2. Remembered / Next Stage Button
                    Button(
                        onClick = { onAction(ReviewAction.PROMOTE_NEXT, null) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp)
                            .testTag("review_action_promote_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Remembered",
                                fontSize = (14 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (nextStage != null) "→ ${nextStage.name}" else "Next Stage ★",
                                fontSize = (10 * fontScale).sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // 3. Mastered Button
                    Button(
                        onClick = { onAction(ReviewAction.MARK_MASTERED, null) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MasteredGold,
                            contentColor = Color(0xFF451A03)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("review_action_master_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Mastered",
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Archive ⭐",
                                fontSize = (10 * fontScale).sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Free-Floating Edge-to-Edge Stage Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpacedRepetitionStages.STAGES.forEach { stg ->
                        val isCurrent = card.currentStageId == stg.id
                        val color = StageColors.getOrElse(stg.id) { CoachPurple }
                        Surface(
                            onClick = { onAction(ReviewAction.PROMOTE_NEXT, stg.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCurrent) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            border = BorderStroke(1.2.dp, if (isCurrent) color else color.copy(alpha = 0.35f)),
                            shadowElevation = if (isCurrent) 2.dp else 1.dp,
                            modifier = Modifier.testTag("review_stage_btn_${stg.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = stg.shortName,
                                    fontSize = (11.5 * fontScale).sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isCurrent) color else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Front state of Output Card: Button to Flip / Check Answer
            Button(
                onClick = onFlip,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("flip_card_button")
            ) {
                Icon(imageVector = Icons.Default.Flip, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSoundToWriting) "Check Spelling / Flip" else "Flip Card / Show Answer",
                    fontSize = (16 * fontScale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FlashcardFrontFace(
    card: FlashcardEntity,
    isSoundToWriting: Boolean,
    isWordToSound: Boolean,
    userSpellingInput: String,
    onSpellingChange: (String) -> Unit,
    onFlip: () -> Unit,
    onSpeak: (String) -> Unit,
    onStopTts: () -> Unit,
    isTtsPlaying: Boolean,
    fontScale: Float
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxSize()
            .testTag("flashcard_front"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = when (card.outputSubtype) {
                            "WORD_TO_SOUND" -> "🗣️ Word → Sound"
                            "SOUND_TO_WRITING" -> "✍️ Sound → Writing"
                            "IMAGE_TO_WORD" -> "🖼️ Image → Word"
                            "AI_COACH" -> "🎓 AI Practice Coach"
                            else -> "📤 Active Recall"
                        },
                        fontSize = (11 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                StageBadge(stageId = card.currentStageId)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body depending on output subtype
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                if (isSoundToWriting) {
                    // SOUND TO WRITING (SPELLING) TEST: Voice audio on front, spelling input, no front text
                    Text(
                        text = "🎧 Listen carefully and write the correct spelling:",
                        fontSize = (13 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TtsAudioButton(
                        textToSpeak = card.audioPronunciationText ?: card.titleContent,
                        isPlaying = isTtsPlaying,
                        onPlay = onSpeak,
                        onStop = onStopTts
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    OutlinedTextField(
                        value = userSpellingInput,
                        onValueChange = onSpellingChange,
                        placeholder = { Text("Type spelling here...", fontSize = (14 * fontScale).sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("spelling_input_field"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                } else if (card.outputSubtype == "IMAGE_TO_WORD") {
                    // OUTPUT VISUAL (IMAGE -> WORD): Image is on FRONT face, target word is hidden until flip
                    if (!card.imageUriOrBase64.isNullOrBlank()) {
                        AsyncImage(
                            model = card.imageUriOrBase64,
                            contentDescription = "Visual Clue",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    Text(
                        text = "🖼️ What is the target word or concept for this visual?",
                        fontSize = (14 * fontScale).sp,
                        color = Color(0xFFE11D48),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    // WORD TO SOUND OR ACTIVE RECALL
                    Text(
                        text = card.titleContent,
                        fontSize = (24 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = (30 * fontScale).sp
                    )

                    if (isWordToSound) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "🗣️ Say this word aloud, then flip to check pronunciation & phonetics.",
                            fontSize = (13 * fontScale).sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tap card to flip",
                fontSize = (11 * fontScale).sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun FlashcardBackFace(
    card: FlashcardEntity,
    userSpellingInput: String,
    isSoundToWriting: Boolean,
    onSpeak: (String) -> Unit,
    onStopTts: () -> Unit,
    isTtsPlaying: Boolean,
    fontScale: Float
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxSize()
            .testTag("flashcard_back"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "REVEALED ANSWER",
                        fontSize = (11 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                StageBadge(stageId = card.currentStageId)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // If Spelling test, compare typed vs target
                if (isSoundToWriting && userSpellingInput.isNotBlank()) {
                    val isMatch = userSpellingInput.trim().equals(card.titleContent.trim(), ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isMatch) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isMatch) "✅ Perfect Spelling Match!" else "❌ Spelling Check",
                                fontWeight = FontWeight.Bold,
                                color = if (isMatch) Color(0xFF047857) else Color(0xFFDC2626),
                                fontSize = (13 * fontScale).sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your typing: \"$userSpellingInput\"",
                                fontSize = (12 * fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Target Word / Concept
                Text(
                    text = card.titleContent,
                    fontSize = (22 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Back Face Image (for Visual cards or cards with images)
                if (!card.imageUriOrBase64.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = card.imageUriOrBase64,
                        contentDescription = "Flashcard Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                if (card.descriptionContent.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Meaning & Definition
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = card.descriptionContent,
                            fontSize = (15 * fontScale).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = (22 * fontScale).sp,
                            modifier = Modifier.padding(14.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Audio TTS
                TtsAudioButton(
                    textToSpeak = card.audioPronunciationText ?: card.titleContent,
                    isPlaying = isTtsPlaying,
                    onPlay = onSpeak,
                    onStop = onStopTts
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Assess your recall below to adjust spaced interval",
                fontSize = (11 * fontScale).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReviewCompletedView(
    sessionState: ReviewSessionState,
    onClose: () -> Unit,
    fontScale: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Review Session Completed!",
            fontSize = (22 * fontScale).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "All scheduled interval reviews have been processed.",
            fontSize = (14 * fontScale).sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Breakdown stats
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatRow(label = "Cards Completed", value = "${sessionState.completedCount}", color = MaterialTheme.colorScheme.primary, fontScale = fontScale)
                StatRow(label = "Promoted to Next Stage", value = "${sessionState.promotedCount}", color = Color(0xFF10B981), fontScale = fontScale)
                StatRow(label = "Timers Reset (Forgot)", value = "${sessionState.resetCount}", color = Color(0xFFEF4444), fontScale = fontScale)
                StatRow(label = "Mastered", value = "${sessionState.masteredCount}", color = MasteredGold, fontScale = fontScale)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onClose,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("finish_review_done_button")
        ) {
            Text("Back to Dashboard", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color, fontScale: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = (13 * fontScale).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold, color = color)
    }
}
