package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FlashcardEntity
import com.example.data.local.MemoraSettings
import com.example.data.local.PersonalMemoryEntity
import com.example.data.local.UserEntity
import com.example.data.local.AuthSession
import com.example.ui.theme.CoachPurple
import com.example.ui.theme.MasteredGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    settings: MemoraSettings,
    onUpdateSettings: (MemoraSettings) -> Unit,
    onResetToDefault: () -> Unit,
    onClearAll: () -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportBackup: (String, Boolean) -> Unit,
    onTestTts: (String) -> Unit,
    authSession: AuthSession = AuthSession(),
    onSignOut: () -> Unit = {},
    allUsers: List<UserEntity> = emptyList(),
    onCreateAccount: (username: String, passwordPlain: String, displayName: String, role: String, onResult: (Result<UserEntity>) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onUpdateAccount: (username: String, displayName: String, newPasswordPlain: String?, onResult: (Result<UserEntity>) -> Unit) -> Unit = { _, _, _, _ -> },
    onDeleteAccount: (username: String, onResult: (Result<Unit>) -> Unit) -> Unit = { _, _ -> },
    masteredCards: List<FlashcardEntity> = emptyList(),
    cards: List<FlashcardEntity> = emptyList(),
    searchQuery: String = "",
    onSearchChange: (String) -> Unit = {},
    selectedType: String? = null,
    onTypeSelect: (String?) -> Unit = {},
    selectedStage: Int? = null,
    onStageSelect: (Int?) -> Unit = {},
    onAddNewCard: () -> Unit = {},
    onEditCard: (FlashcardEntity) -> Unit = {},
    onStartReview: (FlashcardEntity) -> Unit = {},
    onResetTimer: (FlashcardEntity) -> Unit = {},
    onUnmasterCard: (FlashcardEntity, Int) -> Unit = { _, _ -> },
    onDeleteCard: (FlashcardEntity) -> Unit = {},
    onSpeak: (String) -> Unit = {},
    onStopTts: () -> Unit = {},
    isTtsPlaying: Boolean = false,
    currentTime: Long = System.currentTimeMillis(),
    memories: List<PersonalMemoryEntity> = emptyList(),
    onDeleteMemory: (String) -> Unit = {},
    onClearAllMemories: () -> Unit = {},
    onSaveMemory: (keyword: String, displayName: String, factsSummary: String) -> Unit = { _, _, _ -> },
    cloudSyncState: com.example.data.remote.supabase.CloudSyncState = com.example.data.remote.supabase.CloudSyncState.SYNCED,
    onTriggerCloudSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMasteredView by remember { mutableStateOf(false) }
    var showLibraryView by remember { mutableStateOf(false) }
    var showModelDropdown by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }

    // Personal Memory Manager Dialog states
    var showMemoryManagerDialog by remember { mutableStateOf(false) }
    var memorySearchQuery by remember { mutableStateOf("") }
    var showAddEditMemoryDialog by remember { mutableStateOf(false) }
    var memoryToEdit by remember { mutableStateOf<PersonalMemoryEntity?>(null) }
    var editMemoryKeyword by remember { mutableStateOf("") }
    var editMemoryName by remember { mutableStateOf("") }
    var editMemoryFacts by remember { mutableStateOf("") }
    var showClearMemoriesConfirmDialog by remember { mutableStateOf(false) }

    // Mentor User Manager Dialog State
    var showUserManagerDialog by remember { mutableStateOf(false) }

    val fontScale = settings.fontSizeScale

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && exportedJsonText.isNotEmpty()) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(exportedJsonText.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Backup file saved successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val content = inputStream.bufferedReader().use { it.readText() }
                    if (content.isNotBlank()) {
                        importJsonInput = content
                        showImportDialog = true
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read backup file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun shareBackup(json: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, "Memora App Raw Backup")
            putExtra(Intent.EXTRA_SUBJECT, "Memora App Raw Backup ($todayStr)")
            putExtra(Intent.EXTRA_TEXT, json)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Send Memora Raw App Backup to...")
        context.startActivity(shareIntent)
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Memora App Backup", text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Raw app backup copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    if (showMasteredView) {
        Column(modifier = modifier.fillMaxSize()) {
            // Header with Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showMasteredView = false }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Settings",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back to Settings", fontSize = (13 * fontScale).sp)
                }
            }

            MasteredScreen(
                masteredCards = masteredCards,
                onUnmasterCard = onUnmasterCard,
                onDeleteCard = onDeleteCard,
                onSpeak = onSpeak,
                onStopTts = onStopTts,
                isTtsPlaying = isTtsPlaying,
                fontScale = fontScale,
                modifier = Modifier.weight(1f)
            )
        }
        return
    }

    if (showLibraryView) {
        Column(modifier = modifier.fillMaxSize()) {
            // Header with Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showLibraryView = false }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Settings",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back to Settings", fontSize = (13 * fontScale).sp)
                }
            }

            CardsManagementScreen(
                cards = cards,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                selectedType = selectedType,
                onTypeSelect = onTypeSelect,
                selectedStage = selectedStage,
                onStageSelect = onStageSelect,
                onAddNewCard = onAddNewCard,
                onEditCard = onEditCard,
                onDeleteCard = onDeleteCard,
                onStartReview = onStartReview,
                onResetTimer = onResetTimer,
                onSpeak = onSpeak,
                onStopTts = onStopTts,
                isTtsPlaying = isTtsPlaying,
                currentTime = currentTime,
                fontScale = fontScale,
                modifier = Modifier.weight(1f)
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen")
    ) {
        Text(
            text = "Settings & Preferences",
            fontSize = (22 * fontScale).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(14.dp))

        // PROFILE & ACCOUNT CARD
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("user_profile_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (authSession.role == "INSTRUCTOR") CoachPurple else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = authSession.displayName.take(1).uppercase().ifBlank { "U" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = (18 * fontScale).sp
                        )
                    }

                    Column {
                        Text(
                            text = authSession.displayName.ifBlank { "Learner" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "@${authSession.username}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * fontScale).sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (authSession.role == "INSTRUCTOR") CoachPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = if (authSession.role == "INSTRUCTOR") "Mentor" else "Student",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (authSession.role == "INSTRUCTOR") CoachPurple else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Out", fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // MENTOR ROSTER & USER MANAGEMENT SECTION
        if (authSession.role == "INSTRUCTOR") {
            Spacer(modifier = Modifier.height(14.dp))
            val studentCount = allUsers.count { it.role != "INSTRUCTOR" }
            val mentorCount = allUsers.count { it.role == "INSTRUCTOR" }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CoachPurple.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, CoachPurple.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mentor_roster_management_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showUserManagerDialog = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CoachPurple.copy(alpha = 0.22f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SupervisorAccount,
                                        contentDescription = null,
                                        tint = CoachPurple,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Manage Mentors & Students",
                                    fontSize = (15 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$studentCount Students • $mentorCount Mentors • Add & Provision",
                                    fontSize = (12 * fontScale).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open User Manager",
                            tint = CoachPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showUserManagerDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SupervisorAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Roster & Logins", fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CLOUD SYNCHRONIZATION CARD
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cloud_sync_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Cloud Synchronization",
                                fontSize = (15 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (cloudSyncState) {
                                    com.example.data.remote.supabase.CloudSyncState.SYNCING -> "Syncing data in background..."
                                    com.example.data.remote.supabase.CloudSyncState.SYNCED -> "Connected • Fully synced"
                                    com.example.data.remote.supabase.CloudSyncState.ERROR -> "Offline cache active • Retrying"
                                    com.example.data.remote.supabase.CloudSyncState.OFFLINE -> "Offline"
                                },
                                fontSize = (12 * fontScale).sp,
                                color = if (cloudSyncState == com.example.data.remote.supabase.CloudSyncState.SYNCED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onTriggerCloudSync,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("trigger_cloud_sync_button")
                    ) {
                        if (cloudSyncState == com.example.data.remote.supabase.CloudSyncState.SYNCING) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Now", fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "Syncs students roster, exams, submissions, and flashcards across mentor and student devices via free cloud backend.",
                    fontSize = (11 * fontScale).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cards_management_hub_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Section 1: Mastered Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMasteredView = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MasteredGold.copy(alpha = 0.18f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MasteredGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Mastered Cards",
                                fontSize = (15 * fontScale).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${masteredCards.size} cards permanently mastered",
                                fontSize = (12 * fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showMasteredView = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("view_mastered_cards_button")
                    ) {
                        Text(
                            text = "View (${masteredCards.size})",
                            fontSize = (12 * fontScale).sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                // Section 2: Library (Active Cards)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLibraryView = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CoachPurple.copy(alpha = 0.18f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Style,
                                    contentDescription = null,
                                    tint = CoachPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Library",
                                fontSize = (15 * fontScale).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${cards.size} active cards in study rotation",
                                fontSize = (12 * fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { showLibraryView = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoachPurple,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("open_library_button")
                    ) {
                        Text(
                            text = "Open (${cards.size})",
                            fontSize = (12 * fontScale).sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Font Size Scaling
        SettingsCard(
            title = "Typography & Font Sizes",
            icon = Icons.Default.FormatSize,
            fontScale = fontScale
        ) {
            Text(
                text = "Adjust text scale across card titles, descriptions, and review views:",
                fontSize = (13 * fontScale).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FontSizeOption(
                    label = "Small",
                    scale = 0.85f,
                    currentScale = settings.fontSizeScale,
                    onSelect = { onUpdateSettings(settings.copy(fontSizeScale = 0.85f)) },
                    modifier = Modifier.weight(1f)
                )
                FontSizeOption(
                    label = "Normal",
                    scale = 1.0f,
                    currentScale = settings.fontSizeScale,
                    onSelect = { onUpdateSettings(settings.copy(fontSizeScale = 1.0f)) },
                    modifier = Modifier.weight(1f)
                )
                FontSizeOption(
                    label = "Large",
                    scale = 1.15f,
                    currentScale = settings.fontSizeScale,
                    onSelect = { onUpdateSettings(settings.copy(fontSizeScale = 1.15f)) },
                    modifier = Modifier.weight(1f)
                )
                FontSizeOption(
                    label = "Extra",
                    scale = 1.3f,
                    currentScale = settings.fontSizeScale,
                    onSelect = { onUpdateSettings(settings.copy(fontSizeScale = 1.3f)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Models Settings (Text Model, Thinking Level, Gemini API Key)
        SettingsCard(
            title = "Models",
            icon = Icons.Default.Psychology,
            accentColor = CoachPurple,
            fontScale = fontScale
        ) {
            // 1. Text Model (Flashlight, Flash, Pro)
            Text(
                text = "1. TEXT MODEL",
                fontSize = (11 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = CoachPurple
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isFlashLite = settings.geminiModel == "gemini-flash-lite-latest" ||
                    (settings.geminiModel.contains("lite") && !settings.geminiModel.contains("pro"))
                val isFlash = (settings.geminiModel == "gemini-flash-latest" ||
                    (!settings.geminiModel.contains("lite") && !settings.geminiModel.contains("pro"))) && !isFlashLite
                val isPro = settings.geminiModel == "gemini-pro-latest" || settings.geminiModel.contains("pro")

                FilterChip(
                    selected = isFlashLite,
                    onClick = { onUpdateSettings(settings.copy(geminiModel = "gemini-flash-lite-latest")) },
                    label = {
                        Text(
                            text = "Flashlight",
                            fontSize = (12 * fontScale).sp,
                            fontWeight = if (isFlashLite) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("model_flashlight_chip")
                )

                FilterChip(
                    selected = isFlash,
                    onClick = { onUpdateSettings(settings.copy(geminiModel = "gemini-flash-latest")) },
                    label = {
                        Text(
                            text = "Flash",
                            fontSize = (12 * fontScale).sp,
                            fontWeight = if (isFlash) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("model_flash_chip")
                )

                FilterChip(
                    selected = isPro,
                    onClick = { onUpdateSettings(settings.copy(geminiModel = "gemini-pro-latest")) },
                    label = {
                        Text(
                            text = "Pro",
                            fontSize = (12 * fontScale).sp,
                            fontWeight = if (isPro) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("model_pro_chip")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Thinking Level (None, Low, Medium, High)
            Text(
                text = "2. THINKING LEVEL",
                fontSize = (11 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = CoachPurple
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "none" to "None",
                    "low" to "Low",
                    "medium" to "Medium",
                    "high" to "High"
                ).forEach { (lvl, lbl) ->
                    FilterChip(
                        selected = settings.thinkingLevel == lvl,
                        onClick = { onUpdateSettings(settings.copy(thinkingLevel = lvl)) },
                        label = {
                            Text(
                                text = lbl,
                                fontSize = (12 * fontScale).sp,
                                fontWeight = if (settings.thinkingLevel == lvl) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Gemini API Key
            Text(
                text = "3. GEMINI API KEY",
                fontSize = (11 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = CoachPurple
            )
            Spacer(modifier = Modifier.height(4.dp))

            var apiKeyInput by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = {
                    apiKeyInput = it
                    onUpdateSettings(settings.copy(geminiApiKey = it))
                },
                placeholder = { Text("Paste AI Studio API Key (AIzaSy...)", fontSize = (12 * fontScale).sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_api_key_field"),
                shape = RoundedCornerShape(10.dp)
            )
            Text(
                text = if (settings.geminiApiKey.isNotBlank()) "✅ Custom key active" else "Uses Secrets panel key or smart dynamic contextual coach",
                fontSize = (11 * fontScale).sp,
                color = if (settings.geminiApiKey.isNotBlank()) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2.5 Personalization & Memory Card
        SettingsCard(
            title = "Personalization & Continuity",
            icon = Icons.Default.Person,
            accentColor = CoachPurple,
            fontScale = fontScale
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Personal Memory",
                        fontSize = (14 * fontScale).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (settings.personalizationEnabled)
                            "Saves facts by keywords (e.g., people, events, favorites) for natural conversation."
                        else
                            "Completely inactive. Zero background processing or memory usage.",
                        fontSize = (12 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = settings.personalizationEnabled,
                    onCheckedChange = { isChecked ->
                        onUpdateSettings(
                            settings.copy(personalizationEnabled = isChecked)
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CoachPurple
                    ),
                    modifier = Modifier.testTag("personalization_switch")
                )
            }

            if (settings.personalizationEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                // Compact, clean interactive box to open Memory Manager
                Surface(
                    onClick = { showMemoryManagerDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, CoachPurple.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manage_memories_box")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(CoachPurple.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = CoachPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Manage Memory Entities",
                                    fontSize = (13.5 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (memories.isEmpty())
                                        "No memories stored yet • Tap to add or manage"
                                    else
                                        "${memories.size} ${if (memories.size == 1) "memory" else "memories"} stored • Tap to view & edit",
                                    fontSize = (11.5 * fontScale).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (memories.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CoachPurple.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${memories.size}",
                                        fontSize = (11.5 * fontScale).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CoachPurple,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Open Memory Manager",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Theme Mode & Aesthetic Palettes
        SettingsCard(
            title = "Display Theme",
            icon = Icons.Default.Brightness4,
            fontScale = fontScale
        ) {
            Text(
                text = "Solid Modes",
                fontSize = (12 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Standard Solid Themes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "SYSTEM" to "📱 System",
                    "LIGHT" to "☀️ Light",
                    "DARK" to "🌙 Dark"
                ).forEach { (mode, label) ->
                    val isSelected = settings.themeMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(themeMode = mode)) },
                        label = { Text(label, fontSize = (12 * fontScale).sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "10 Illustrated Aesthetic Themes",
                fontSize = (12 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Custom illustrated art, matching color palettes, and cozy ambient wallpapers:",
                fontSize = (11.5 * fontScale).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            val nonSolidThemes = listOf(
                "PINK" to "🐰 Cute Bunny",
                "KITTY" to "🐱 Cute Kitty",
                "HONEY" to "🍯 Honey Sunshine",
                "MINT" to "🍃 Matcha Mint",
                "OCEAN" to "🐳 Ocean Breeze",
                "COSMIC" to "🌌 Cosmic Dream",
                "COFFEE" to "☕ Cozy Cafe",
                "SUNSET" to "🌅 Peach Sunset",
                "FOREST" to "🦌 Enchanted Forest",
                "TEDDY" to "🧸 Teddy Bakery"
            )

            nonSolidThemes.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pair.forEach { (mode, label) ->
                        val isSelected = settings.themeMode == mode || (mode == "PINK" && settings.themeMode == "BUNNY") || (mode == "HONEY" && settings.themeMode == "YELLOW")
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateSettings(settings.copy(themeMode = mode)) },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = (11.5 * fontScale).sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Text-To-Speech (TTS) Voice Configuration
        SettingsCard(
            title = "Text-to-Speech Engine",
            icon = Icons.Default.RecordVoiceOver,
            fontScale = fontScale
        ) {
            Text(
                text = "Speech Rate: ${String.format(Locale.US, "%.1fx", settings.ttsSpeechRate)}",
                fontSize = (13 * fontScale).sp,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = settings.ttsSpeechRate,
                onValueChange = { onUpdateSettings(settings.copy(ttsSpeechRate = it)) },
                valueRange = 0.5f..2.0f,
                steps = 15
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Voice Pitch: ${String.format(Locale.US, "%.1fx", settings.ttsPitch)}",
                fontSize = (13 * fontScale).sp,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = settings.ttsPitch,
                onValueChange = { onUpdateSettings(settings.copy(ttsPitch = it)) },
                valueRange = 0.5f..1.5f,
                steps = 10
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    onTestTts("Hello! Memora's offline spaced repetition speech synthesis is working smoothly.")
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Voice Sample", fontSize = (13 * fontScale).sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Data Backup: Import & Export
        SettingsCard(
            title = "Data Backup & Transfer",
            icon = Icons.Default.Share,
            accentColor = MaterialTheme.colorScheme.primary,
            fontScale = fontScale
        ) {
            Text(
                text = "Import or export all your flashcards, mastered items, and notes in complete detail:",
                fontSize = (13 * fontScale).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Export Data Button
                Button(
                    onClick = {
                        onExportBackup { json ->
                            exportedJsonText = json
                            showExportDialog = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_data_button")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export Data",
                        fontWeight = FontWeight.Bold,
                        fontSize = (13 * fontScale).sp
                    )
                }

                // Import Data Button
                OutlinedButton(
                    onClick = { showImportDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("import_data_button")
                ) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Import Data",
                        fontWeight = FontWeight.Bold,
                        fontSize = (13 * fontScale).sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Clear Data
        SettingsCard(
            title = "Clear Data",
            icon = Icons.Default.DeleteForever,
            accentColor = Color(0xFFEF4444),
            fontScale = fontScale
        ) {
            Text(
                text = "Permanently remove all flashcards, mastered items, notepad entries, and chat history from this device:",
                fontSize = (13 * fontScale).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showWipeConfirmDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clear_data_button")
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Clear Data",
                    fontWeight = FontWeight.Bold,
                    fontSize = (13 * fontScale).sp
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Export & Share Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = CoachPurple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Raw App Backup Data")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Your complete snapshot is ready to share, copy, or save:",
                        fontSize = (13 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { copyToClipboard(exportedJsonText) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { shareBackup(exportedJsonText) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import & Restore Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore App Backup")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Choose a backup file or paste the JSON text below:",
                        fontSize = (13 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { openFileLauncher.launch("application/json") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select .json File From Storage")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        placeholder = { Text("Paste raw backup JSON text here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tip: 'Full Restore' replaces all data with this snapshot. 'Merge' adds new items to your existing ones.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (importJsonInput.isNotBlank()) {
                                onImportBackup(importJsonInput.trim(), false)
                                showImportDialog = false
                                importJsonInput = ""
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Merge Items", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            if (importJsonInput.isNotBlank()) {
                                onImportBackup(importJsonInput.trim(), true)
                                showImportDialog = false
                                importJsonInput = ""
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Full Restore", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset to Sample Cards?") },
            text = { Text("This will restore default flashcard items and reset statistics.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToDefault()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("Yes, Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Clear Data Confirm Dialog
    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("Are you sure? This will permanently delete all active and mastered flashcards, notepad memos, and chat history.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showWipeConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Yes, Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Personal Memory Manager Dialog
    if (showMemoryManagerDialog) {
        val filteredMemories = remember(memories, memorySearchQuery) {
            if (memorySearchQuery.isBlank()) {
                memories
            } else {
                val q = memorySearchQuery.trim().lowercase()
                memories.filter {
                    it.keyword.lowercase().contains(q) ||
                    it.displayName.lowercase().contains(q) ||
                    it.factsSummary.lowercase().contains(q)
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showMemoryManagerDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = CoachPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Memory Entities",
                            fontSize = (17 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                memoryToEdit = null
                                editMemoryKeyword = ""
                                editMemoryName = ""
                                editMemoryFacts = ""
                                showAddEditMemoryDialog = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Memory",
                                tint = CoachPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { showMemoryManagerDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                ) {
                    // Search header
                    if (memories.size > 2 || memorySearchQuery.isNotBlank()) {
                        OutlinedTextField(
                            value = memorySearchQuery,
                            onValueChange = { memorySearchQuery = it },
                            placeholder = { Text("Search memories...", fontSize = (12 * fontScale).sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (memorySearchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { memorySearchQuery = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        )
                    }

                    if (filteredMemories.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = CoachPurple.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = if (memorySearchQuery.isNotBlank()) "No matching memories found" else "No personal memories yet",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = (13.5 * fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (memorySearchQuery.isNotBlank())
                                    "Try a different search term"
                                else
                                    "Memories are saved naturally as you chat (people, places, favorites) or you can add them manually.",
                                fontSize = (11.5 * fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    memoryToEdit = null
                                    editMemoryKeyword = ""
                                    editMemoryName = ""
                                    editMemoryFacts = ""
                                    showAddEditMemoryDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Memory", fontSize = (12 * fontScale).sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            items(filteredMemories, key = { it.keyword }) { mem ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bookmark,
                                                    contentDescription = null,
                                                    tint = CoachPurple,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = mem.displayName.ifBlank { mem.keyword },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = (13 * fontScale).sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = mem.factsSummary,
                                                fontSize = (11.5 * fontScale).sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = (16 * fontScale).sp
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    memoryToEdit = mem
                                                    editMemoryKeyword = mem.keyword
                                                    editMemoryName = mem.displayName
                                                    editMemoryFacts = mem.factsSummary
                                                    showAddEditMemoryDialog = true
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit memory",
                                                    tint = CoachPurple,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { onDeleteMemory(mem.keyword) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete memory",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (memories.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearMemoriesConfirmDialog = true }
                        ) {
                            Text("Clear All", color = Color(0xFFEF4444), fontSize = (12 * fontScale).sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = { showMemoryManagerDialog = false },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoachPurple)
                    ) {
                        Text("Done", fontSize = (13 * fontScale).sp)
                    }
                }
            }
        )
    }

    // Add / Edit Memory Item Dialog
    if (showAddEditMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddEditMemoryDialog = false },
            title = {
                Text(
                    text = if (memoryToEdit == null) "Add Memory Entity" else "Edit Memory Entity",
                    fontWeight = FontWeight.Bold,
                    fontSize = (16 * fontScale).sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editMemoryName,
                        onValueChange = { editMemoryName = it },
                        label = { Text("Name / Entity", fontSize = (12 * fontScale).sp) },
                        placeholder = { Text("e.g. Morteza, Sarah, Pizza, Guitar", fontSize = (12 * fontScale).sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editMemoryFacts,
                        onValueChange = { editMemoryFacts = it },
                        label = { Text("Facts / Details", fontSize = (12 * fontScale).sp) },
                        placeholder = { Text("e.g. User's name, or Friend who lives in Toronto and likes hiking", fontSize = (12 * fontScale).sp) },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editMemoryName.isNotBlank() && editMemoryFacts.isNotBlank()) {
                            val kw = editMemoryKeyword.ifBlank { editMemoryName.trim().lowercase() }
                            onSaveMemory(kw, editMemoryName.trim(), editMemoryFacts.trim())
                            showAddEditMemoryDialog = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                    enabled = editMemoryName.isNotBlank() && editMemoryFacts.isNotBlank()
                ) {
                    Text("Save", fontSize = (13 * fontScale).sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditMemoryDialog = false }) {
                    Text("Cancel", fontSize = (13 * fontScale).sp)
                }
            }
        )
    }

    // Clear All Memories Confirm Dialog
    if (showClearMemoriesConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearMemoriesConfirmDialog = false },
            title = { Text("Clear All Personal Memories?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove all saved personal memory facts? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllMemories()
                        showClearMemoriesConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearMemoriesConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Mentor User Manager Dialog
    if (showUserManagerDialog) {
        MentorUserManagerDialog(
            allUsers = allUsers,
            currentUsername = authSession.username,
            fontScale = fontScale,
            onDismiss = { showUserManagerDialog = false },
            onCreateAccount = onCreateAccount,
            onUpdateAccount = onUpdateAccount,
            onDeleteAccount = onDeleteAccount
        )
    }
}

@Composable
fun FontSizeOption(
    label: String,
    scale: Float,
    currentScale: Float,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = kotlin.math.abs(scale - currentScale) < 0.05f
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.clickable { onSelect() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aa",
                fontSize = (14 * scale).sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = (16 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}
