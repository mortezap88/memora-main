package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.AiDomain
import com.example.data.local.FlashcardEntity
import com.example.data.local.LinguisticContext
import com.example.data.local.OutputSubtype
import com.example.data.local.SpacedRepetitionStages
import com.example.ui.theme.CoachPurple
import com.example.ui.theme.StageColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardBottomSheet(
    cardToEdit: FlashcardEntity? = null,
    onDismiss: () -> Unit,
    onSave: (FlashcardEntity) -> Unit,
    onTestSpeak: (String) -> Unit,
    onStartDiscoverySession: (domain: String, context: String) -> Unit = { _, _ -> },
    fontScale: Float = 1.0f
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    val isEditing = cardToEdit != null

    // Practice type (Pronounce, Spelling, Visual, Interactive)
    var outputSubtype by remember {
        mutableStateOf(
            cardToEdit?.outputSubtype ?: when (cardToEdit?.itemType) {
                "EXPLANATION" -> "AI_COACH"
                "IMAGE" -> "IMAGE_TO_WORD"
                "AUDIO" -> "WORD_TO_SOUND"
                else -> "WORD_TO_SOUND"
            }
        )
    }

    // AI Coach domain: "LINGUISTIC" ("Word / Grammar") vs "KNOWLEDGE" ("Knowledge")
    var aiDomain by remember {
        mutableStateOf(cardToEdit?.aiDomain ?: "LINGUISTIC")
    }
    // Word / Grammar Context: "CLASS", "FRIEND", "GIRLFRIEND"
    var linguisticContext by remember {
        mutableStateOf(cardToEdit?.linguisticContext ?: "FRIEND")
    }

    // Card content states
    var title by remember { mutableStateOf(cardToEdit?.titleContent ?: "") }
    var description by remember { mutableStateOf(cardToEdit?.descriptionContent ?: "") }
    var selectedStageId by remember {
        mutableIntStateOf(
            cardToEdit?.currentStageId ?: if (outputSubtype == "AI_COACH" && aiDomain == "KNOWLEDGE") 2 else 0
        )
    }
    var imageUriOrUrl by remember { mutableStateOf(cardToEdit?.imageUriOrBase64 ?: "") }
    var audioText by remember { mutableStateOf(cardToEdit?.audioPronunciationText ?: "") }

    var isImageSavedFeedback by remember { mutableStateOf(cardToEdit?.imageUriOrBase64?.isNotBlank() == true) }
    var showStageDropdown by remember { mutableStateOf(false) }
    var showImageBrowserSheet by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_edit_card_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (cardToEdit == null) "New Practice Card" else "Edit Flashcard",
                        fontSize = (20 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Spaced repetition practice card",
                        fontSize = (12 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. PRACTICE TYPE SELECTION
            Text(
                text = "PRACTICE TYPE",
                fontSize = (11 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutputSubtypeChip(
                    title = "Word → Sound",
                    subtitle = "Pronounce",
                    icon = Icons.Default.RecordVoiceOver,
                    isSelected = outputSubtype == "WORD_TO_SOUND",
                    color = Color(0xFF0284C7),
                    onClick = {
                        outputSubtype = "WORD_TO_SOUND"
                        selectedStageId = 0
                    },
                    modifier = Modifier.weight(1f)
                )
                OutputSubtypeChip(
                    title = "Sound → Writing",
                    subtitle = "Spelling",
                    icon = Icons.Default.Edit,
                    isSelected = outputSubtype == "SOUND_TO_WRITING",
                    color = Color(0xFF0D9488),
                    onClick = {
                        outputSubtype = "SOUND_TO_WRITING"
                        selectedStageId = 0
                    },
                    modifier = Modifier.weight(1f)
                )
                OutputSubtypeChip(
                    title = "Image → Word",
                    subtitle = "Visual",
                    icon = Icons.Default.Image,
                    isSelected = outputSubtype == "IMAGE_TO_WORD",
                    color = Color(0xFFE11D48),
                    onClick = {
                        outputSubtype = "IMAGE_TO_WORD"
                        selectedStageId = 0
                    },
                    modifier = Modifier.weight(1f)
                )
                OutputSubtypeChip(
                    title = "AI Coach",
                    subtitle = "Interactive",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = outputSubtype == "AI_COACH",
                    color = CoachPurple,
                    onClick = {
                        outputSubtype = "AI_COACH"
                        selectedStageId = if (aiDomain == "KNOWLEDGE") 2 else 0
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -------------------------------------------------------------
            // TYPE 1: PRONUNCIATION (WORD_TO_SOUND) — Single Box: Word / Sentence
            // -------------------------------------------------------------
            if (outputSubtype == "WORD_TO_SOUND") {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        audioText = it
                    },
                    label = { Text("Word / Sentence") },
                    placeholder = { Text("e.g. ubiquitous, serendipity, How have you been?...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🔊 Front displays the word/sentence to recall sound. Flipping the card automatically reads it aloud.",
                    fontSize = (11 * fontScale).sp,
                    color = Color(0xFF0284C7)
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // -------------------------------------------------------------
            // TYPE 2: SPELLING (SOUND_TO_WRITING) — Single Box: Word / Sentence
            // -------------------------------------------------------------
            if (outputSubtype == "SOUND_TO_WRITING") {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        audioText = it
                    },
                    label = { Text("Word / Sentence") },
                    placeholder = { Text("e.g. rhythm, accommodate, fluorescent...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "✍️ Front plays audio aloud so you can guess & type the spelling. Back reveals correct spelling.",
                    fontSize = (11 * fontScale).sp,
                    color = Color(0xFF0D9488)
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // -------------------------------------------------------------
            // TYPE 3: VISUAL (IMAGE_TO_WORD) — Image Clue + Target Word / Concept
            // -------------------------------------------------------------
            if (outputSubtype == "IMAGE_TO_WORD") {
                Text(
                    text = "FRONT SIDE IMAGE (VISUAL)",
                    fontSize = (11 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE11D48),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (imageUriOrUrl.isNotBlank()) {
                            AsyncImage(
                                model = imageUriOrUrl,
                                contentDescription = "Pasted Front Visual",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Button 1: Search / Find in Image Browser
                            Button(
                                onClick = { showImageBrowserSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Find Image", fontWeight = FontWeight.Bold)
                            }

                            // Button 2: Paste
                            Button(
                                onClick = {
                                    val clipText = clipboardManager.getText()?.text?.trim()
                                    if (!clipText.isNullOrBlank()) {
                                        imageUriOrUrl = clipText
                                        isImageSavedFeedback = true
                                    } else {
                                        imageUriOrUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=600"
                                        isImageSavedFeedback = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isImageSavedFeedback && imageUriOrUrl.isNotBlank()) Color(0xFF10B981) else Color(0xFFE11D48)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isImageSavedFeedback && imageUriOrUrl.isNotBlank()) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Saved", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Paste", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (imageUriOrUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        imageUriOrUrl = ""
                                        isImageSavedFeedback = false
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear image", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Target Word / Concept (Back Side Answer)") },
                    placeholder = { Text("e.g. Photosynthesis, Espresso, Architecture...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // -------------------------------------------------------------
            // TYPE 4: INTERACTIVE (AI_COACH) — Word/Grammar vs Knowledge
            // -------------------------------------------------------------
            if (outputSubtype == "AI_COACH") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CoachPurple.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, CoachPurple.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CoachPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Interactive Practice",
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = CoachPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1. PRACTICE DOMAIN: Word / Grammar vs Knowledge
                        Text(
                            text = "DOMAIN",
                            fontSize = (10 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (aiDomain == "LINGUISTIC") CoachPurple else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (aiDomain == "LINGUISTIC") CoachPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        aiDomain = "LINGUISTIC"
                                        selectedStageId = 0 // 2 Minutes for Word / Grammar
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🗣️ Word / Grammar",
                                        fontSize = (12 * fontScale).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (aiDomain == "LINGUISTIC") Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Initial interval: 2 Minutes",
                                        fontSize = (10 * fontScale).sp,
                                        color = if (aiDomain == "LINGUISTIC") Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (aiDomain == "KNOWLEDGE") CoachPurple else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (aiDomain == "KNOWLEDGE") CoachPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        aiDomain = "KNOWLEDGE"
                                        selectedStageId = 2 // 2 Hours for Knowledge
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🧠 Knowledge",
                                        fontSize = (12 * fontScale).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (aiDomain == "KNOWLEDGE") Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Initial interval: 2 Hours",
                                        fontSize = (10 * fontScale).sp,
                                        color = if (aiDomain == "KNOWLEDGE") Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // 2. If Word / Grammar: Context options (Classroom, Friend, Girlfriend)
                        if (aiDomain == "LINGUISTIC") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "CONTEXT / REGISTER",
                                fontSize = (10 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val contexts = listOf(
                                    Triple("CLASS", "🎓", "Classroom"),
                                    Triple("FRIEND", "🤝", "Friend"),
                                    Triple("GIRLFRIEND", "💖", "Girlfriend")
                                )

                                contexts.forEach { (code, emoji, label) ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (linguisticContext == code) CoachPurple else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (linguisticContext == code) CoachPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { linguisticContext = code }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = emoji, fontSize = 14.sp)
                                            Text(
                                                text = label,
                                                fontSize = (11 * fontScale).sp,
                                                fontWeight = if (linguisticContext == code) FontWeight.Bold else FontWeight.Normal,
                                                color = if (linguisticContext == code) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onStartDiscoverySession(aiDomain, linguisticContext)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_ai_discovery_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (aiDomain == "KNOWLEDGE") "Start AI Conversation (Knowledge)" else "Start AI Conversation (Expression)",
                                fontWeight = FontWeight.Bold,
                                fontSize = (14 * fontScale).sp
                            )
                        }
                    }
                }

                if (isEditing) {
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (aiDomain == "KNOWLEDGE") "Knowledge / Concept Title" else "Target Word or Grammar Structure") },
                        placeholder = { Text(if (aiDomain == "KNOWLEDGE") "e.g. Cognitive Load Theory" else "e.g. hit the books, Present Perfect...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_title_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Details, Meaning or Clues (Optional)") },
                        placeholder = { Text("Provide notes or context to guide the AI Coach...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .testTag("card_description_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // -------------------------------------------------------------
            // 2. INITIAL SPACED REPETITION SCHEDULE
            // -------------------------------------------------------------
            Text(
                text = "INITIAL SPACED REPETITION INTERVAL",
                fontSize = (11 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            val currentSelectedStage = SpacedRepetitionStages.getStage(selectedStageId)
            val stageColor = StageColors.getOrElse(selectedStageId) { MaterialTheme.colorScheme.primary }

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = stageColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, stageColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStageDropdown = true }
                        .testTag("select_stage_picker")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(stageColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${currentSelectedStage.name} (${currentSelectedStage.shortName})",
                                    fontSize = (14 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentSelectedStage.description,
                                    fontSize = (11 * fontScale).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "Change",
                            fontSize = (12 * fontScale).sp,
                            color = stageColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                DropdownMenu(
                    expanded = showStageDropdown,
                    onDismissRequest = { showStageDropdown = false }
                ) {
                    SpacedRepetitionStages.STAGES.forEach { stg ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(StageColors.getOrElse(stg.id) { Color.Gray })
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${stg.name} — ${stg.description}")
                                }
                            },
                            onClick = {
                                selectedStageId = stg.id
                                showStageDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // -------------------------------------------------------------
            // 3. ACTION BUTTONS
            // -------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Cancel")
                }

                val canSave = title.isNotBlank() || (outputSubtype == "AI_COACH" && !isEditing)

                Button(
                    onClick = {
                        if (canSave) {
                            val now = System.currentTimeMillis()
                            val stageInfo = SpacedRepetitionStages.getStage(selectedStageId)
                            val resolvedLegacyType = when (outputSubtype) {
                                "AI_COACH" -> "EXPLANATION"
                                "IMAGE_TO_WORD" -> "IMAGE"
                                "WORD_TO_SOUND", "SOUND_TO_WRITING" -> "AUDIO"
                                else -> "TEXT"
                            }

                            val resolvedAudio = if (audioText.isNotBlank()) audioText.trim() else title.trim()

                            val newCard = FlashcardEntity(
                                id = cardToEdit?.id ?: UUID.randomUUID().toString(),
                                titleContent = title.trim(),
                                descriptionContent = description.trim(),
                                itemType = resolvedLegacyType,
                                cardCategory = "OUTPUT",
                                outputSubtype = outputSubtype,
                                aiDomain = if (outputSubtype == "AI_COACH") aiDomain else null,
                                linguisticContext = if (outputSubtype == "AI_COACH" && aiDomain == "LINGUISTIC") linguisticContext else null,
                                linguisticScenarioType = "COMMUNICATIVE",
                                currentStageId = selectedStageId,
                                dueTimestamp = cardToEdit?.dueTimestamp ?: (now + stageInfo.intervalMillis),
                                createdAt = cardToEdit?.createdAt ?: now,
                                isMastered = cardToEdit?.isMastered ?: false,
                                masteredAt = cardToEdit?.masteredAt,
                                imageUriOrBase64 = if (outputSubtype == "IMAGE_TO_WORD" && imageUriOrUrl.isNotBlank()) imageUriOrUrl.trim() else null,
                                audioPronunciationText = resolvedAudio,
                                previousSessions = cardToEdit?.previousSessions ?: "[]",
                                totalReviewsCount = cardToEdit?.totalReviewsCount ?: 0,
                                lastReviewedAt = cardToEdit?.lastReviewedAt
                            )
                            onSave(newCard)
                            onDismiss()
                        }
                    },
                    enabled = canSave,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_card_button")
                ) {
                    Text("Save Card", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showImageBrowserSheet) {
        ImageBrowserScreen(
            isOpen = showImageBrowserSheet,
            onClose = { showImageBrowserSheet = false },
            onSelectImage = { url ->
                imageUriOrUrl = url
                isImageSavedFeedback = true
                showImageBrowserSheet = false
            },
            initialQuery = title.ifBlank { "flashcard illustration" }
        )
    }
}

@Composable
fun OutputSubtypeChip(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier
            .clickable { onClick() }
            .testTag("subtype_chip_$title")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
