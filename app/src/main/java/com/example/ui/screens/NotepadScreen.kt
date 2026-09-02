package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NotepadEntity
import com.example.ui.theme.CoachPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotepadScreen(
    notepads: List<NotepadEntity>,
    selectedNotepadId: String?,
    onSelectNotepad: (String) -> Unit,
    onCreateNotepad: (String) -> Unit,
    onRenameNotepad: (NotepadEntity, String) -> Unit,
    onUpdateContent: (NotepadEntity, String) -> Unit,
    onDeleteNotepad: (NotepadEntity) -> Unit,
    onSpeak: (String) -> Unit = {},
    onStopTts: () -> Unit = {},
    isTtsPlaying: Boolean = false,
    fontScale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    // Active notepad selection
    val activeNotepad = remember(notepads, selectedNotepadId) {
        notepads.find { it.id == selectedNotepadId } ?: notepads.firstOrNull()
    }

    var textFieldValue by remember(activeNotepad?.id) {
        val initial = activeNotepad?.content ?: ""
        mutableStateOf(TextFieldValue(initial, selection = TextRange(initial.length)))
    }

    // Keep text synchronized if changed externally
    LaunchedEffect(activeNotepad?.content) {
        val externalContent = activeNotepad?.content ?: ""
        if (textFieldValue.text != externalContent) {
            textFieldValue = TextFieldValue(externalContent, selection = TextRange(externalContent.length))
        }
    }

    val focusRequester = remember { FocusRequester() }
    val editorScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newNotepadTitle by remember { mutableStateOf("") }

    // Long press options state
    var notepadForOptions by remember { mutableStateOf<NotepadEntity?>(null) }
    var showOptionsDialog by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameNotepadTitle by remember { mutableStateOf("") }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val isImeVisible = WindowInsets.isImeVisible

    // When the keyboard opens, smoothly scroll the active typing area up with generous breathing room
    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            delay(120)
            editorScrollState.animateScrollTo(editorScrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .then(
                if (!isImeVisible) {
                    Modifier
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 82.dp)
                } else {
                    Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                }
            )
            .testTag("notepad_screen")
    ) {
        // TOP ISLAND BAR: Multi-Notepad Selector & Add (+) Button
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Color(0xFF141622) else Color(0xFFFFFFFF),
            border = BorderStroke(1.2.dp, if (isDark) Color.White.copy(alpha = 0.18f) else Color(0xFFE2E8F0)),
            shadowElevation = if (isDark) 6.dp else 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("notepad_island_header")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Horizontal scrolling notepad tabs with clean underline separation
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        notepads.forEach { pad ->
                            val isSelected = pad.id == activeNotepad?.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .pointerInput(pad.id) {
                                        detectTapGestures(
                                            onTap = {
                                                onSelectNotepad(pad.id)
                                            },
                                            onLongPress = {
                                                notepadForOptions = pad
                                                showOptionsDialog = true
                                            }
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                    .testTag("notepad_tab_${pad.id}")
                            ) {
                                Text(
                                    text = pad.title,
                                    fontSize = (13.5 * fontScale).sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        if (isDark) Color.White else Color(0xFF0F172A)
                                    } else {
                                        if (isDark) Color.White.copy(alpha = 0.45f) else Color(0xFF94A3B8)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(5.dp))

                                // Clear underline indicator: bright white/active when selected, subtle line when inactive
                                Box(
                                    modifier = Modifier
                                        .widthIn(min = 36.dp)
                                        .fillMaxWidth()
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(
                                            if (isSelected) {
                                                if (isDark) Color.White else Color(0xFF0F172A)
                                            } else {
                                                if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Action: Add Notepad (+) button with soft neon aura (matching bottom bar neon style)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable {
                            newNotepadTitle = "Notepad #${notepads.size + 1}"
                            showCreateDialog = true
                        }
                        .testTag("add_notepad_button")
                ) {
                    // Soft neon aura behind the icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        (if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)).copy(alpha = if (isDark) 0.38f else 0.22f),
                                        (if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)).copy(alpha = if (isDark) 0.15f else 0.08f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Notepad",
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // MAIN CLEAN EDITOR CANVAS with Always-Available Extra Blank Lines
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Color(0xFF141622) else Color(0xFFFFFFFF),
            border = BorderStroke(1.2.dp, if (isDark) Color.White.copy(alpha = 0.18f) else Color(0xFFE2E8F0)),
            shadowElevation = if (isDark) 10.dp else 4.dp,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusRequester.requestFocus()
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(textFieldValue.text.length)
                    )
                    coroutineScope.launch {
                        delay(60)
                        editorScrollState.animateScrollTo(editorScrollState.maxValue)
                    }
                }
                .testTag("notepad_editor_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(editorScrollState)
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = "Write your vocabulary notes, grammar observations, idioms, or practice thoughts here...",
                            color = if (isDark) Color.White.copy(alpha = 0.40f) else Color(0xFF94A3B8),
                            fontSize = (15 * fontScale).sp,
                            lineHeight = (22 * fontScale).sp
                        )
                    }

                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            val previousText = textFieldValue.text
                            textFieldValue = newValue
                            if (previousText != newValue.text) {
                                activeNotepad?.let { pad ->
                                    onUpdateContent(pad, newValue.text)
                                }
                                // Auto scroll down when adding new lines so it's always elevated above keyboard
                                if (newValue.text.length > previousText.length) {
                                    coroutineScope.launch {
                                        editorScrollState.animateScrollTo(editorScrollState.maxValue)
                                    }
                                }
                            }
                        },
                        textStyle = TextStyle(
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = (15.5 * fontScale).sp,
                            lineHeight = (23 * fontScale).sp
                        ),
                        cursorBrush = SolidColor(CoachPurple),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("notepad_text_input")
                    )
                }

                // Generous extra lines cushion: ensures you never write on the last visible line
                Spacer(modifier = Modifier.height(280.dp))
            }
        }
    }

    // LONG PRESS OPTIONS DIALOG (Rename & Delete)
    if (showOptionsDialog && notepadForOptions != null) {
        val targetPad = notepadForOptions!!
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = {
                Text(
                    text = targetPad.title,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose an action for this notepad:",
                        fontSize = (13.5 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Option 1: Rename
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    renameNotepadTitle = targetPad.title
                                    showOptionsDialog = false
                                    showRenameDialog = true
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Rename Notepad",
                                fontSize = (14 * fontScale).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Option 2: Delete
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    showOptionsDialog = false
                                    showDeleteConfirmDialog = true
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Delete Notepad",
                                fontSize = (14 * fontScale).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showOptionsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // CREATE NOTEPAD DIALOG
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Notepad") },
            text = {
                Column {
                    Text("Enter a title for this notepad:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newNotepadTitle,
                        onValueChange = { newNotepadTitle = it },
                        label = { Text("Notepad Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNotepadTitle.isNotBlank()) {
                            onCreateNotepad(newNotepadTitle.trim())
                            showCreateDialog = false
                        }
                    },
                    enabled = newNotepadTitle.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // RENAME NOTEPAD DIALOG
    if (showRenameDialog && notepadForOptions != null) {
        val padToRename = notepadForOptions!!
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Notepad") },
            text = {
                Column {
                    Text("Enter a new title for '${padToRename.title}':")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameNotepadTitle,
                        onValueChange = { renameNotepadTitle = it },
                        label = { Text("New Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameNotepadTitle.isNotBlank()) {
                            onRenameNotepad(padToRename, renameNotepadTitle.trim())
                            showRenameDialog = false
                        }
                    },
                    enabled = renameNotepadTitle.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DELETE NOTEPAD DIALOG
    if (showDeleteConfirmDialog && notepadForOptions != null) {
        val padToDelete = notepadForOptions!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Notepad?") },
            text = {
                Text("Are you sure you want to delete '${padToDelete.title}'? All notes inside will be permanently removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNotepad(padToDelete)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
