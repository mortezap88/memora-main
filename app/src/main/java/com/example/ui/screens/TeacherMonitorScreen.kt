package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExamEntity
import com.example.data.local.ExamSubmissionEntity
import com.example.data.repository.StudentStatsReport
import com.example.ui.theme.CoachPurple
import com.example.ui.viewmodel.MemoraViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TeacherMonitorScreen(
    viewModel: MemoraViewModel,
    modifier: Modifier = Modifier
) {
    val studentReports by viewModel.allStudentReports.collectAsState()
    val allExams by viewModel.allExams.collectAsState()
    val allSubmissions by viewModel.allSubmissions.collectAsState()
    val cloudSyncState by viewModel.cloudSyncState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Students, 1 = Exams & Tasks, 2 = Answers & AI Digest
    var showCreateExamDialog by remember { mutableStateOf(false) }
    var selectedSubmissionForDetail by remember { mutableStateOf<ExamSubmissionEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Teacher Hub Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = CoachPurple.copy(alpha = 0.15f)
            ),
            border = BorderStroke(1.dp, CoachPurple.copy(alpha = 0.35f))
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CoachPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Mentor Hub",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Mentor Hub",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${studentReports.size} students • ${allSubmissions.size} submissions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when (cloudSyncState) {
                                    com.example.data.remote.supabase.CloudSyncState.SYNCING -> "⚡ Syncing..."
                                    com.example.data.remote.supabase.CloudSyncState.SYNCED -> "☁️ Cloud Synced"
                                    com.example.data.remote.supabase.CloudSyncState.ERROR -> "⚠️ Offline / Cached"
                                    com.example.data.remote.supabase.CloudSyncState.OFFLINE -> "Offline"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (cloudSyncState == com.example.data.remote.supabase.CloudSyncState.SYNCED) CoachPurple else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.triggerCloudSync() },
                        modifier = Modifier.testTag("mentor_cloud_sync_button")
                    ) {
                        if (cloudSyncState == com.example.data.remote.supabase.CloudSyncState.SYNCING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = CoachPurple
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Cloud Submissions",
                                tint = CoachPurple
                            )
                        }
                    }

                    Button(
                        onClick = { showCreateExamDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                        modifier = Modifier.testTag("create_exam_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Exam", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Secondary Tabs
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            contentColor = CoachPurple,
            modifier = Modifier.clip(RoundedCornerShape(14.dp)),
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Students (${studentReports.size})", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Exams (${allExams.size})", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Answers & AI (${allSubmissions.size})", fontWeight = FontWeight.SemiBold) }
            )
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> StudentsListView(studentReports = studentReports)
                1 -> ExamsListView(
                    exams = allExams,
                    onDeleteExam = { viewModel.deleteExam(it) },
                    onCreateClick = { showCreateExamDialog = true }
                )
                2 -> SubmissionsListView(
                    submissions = allSubmissions,
                    onDigestClick = { viewModel.digestStudentSubmission(it) },
                    onSendFeedback = { sub, fb -> viewModel.sendTeacherFeedback(sub, fb) },
                    onSelectDetail = { selectedSubmissionForDetail = it }
                )
            }
        }
    }

    if (showCreateExamDialog) {
        CreateExamDialog(
            studentReports = studentReports,
            onDismiss = { showCreateExamDialog = false },
            onCreate = { title, desc, topic, questionsJson, assignedTo ->
                viewModel.createExam(title, desc, topic, questionsJson, assignedTo)
                showCreateExamDialog = false
            }
        )
    }

    selectedSubmissionForDetail?.let { sub ->
        SubmissionDetailDialog(
            submission = sub,
            onDismiss = { selectedSubmissionForDetail = null },
            onDigestClick = {
                viewModel.digestStudentSubmission(sub)
                selectedSubmissionForDetail = null
            },
            onSendFeedback = { feedback ->
                viewModel.sendTeacherFeedback(sub, feedback)
                selectedSubmissionForDetail = null
            }
        )
    }
}

// ----------------------------------------------------------------------
// 1. STUDENTS LIST VIEW
// ----------------------------------------------------------------------
@Composable
private fun StudentsListView(studentReports: List<StudentStatsReport>) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableIntStateOf(0) } // 0 = Name A-Z, 1 = Username, 2 = Most Active, 3 = Mastery

    val filteredAndSorted = remember(studentReports, searchQuery, sortOption) {
        studentReports
            .filter {
                searchQuery.isBlank() ||
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                it.username.contains(searchQuery, ignoreCase = true)
            }
            .let { list ->
                when (sortOption) {
                    0 -> list.sortedBy { it.displayName.lowercase() }
                    1 -> list.sortedBy { it.username.lowercase() }
                    2 -> list.sortedByDescending { it.lastActiveTimestamp }
                    3 -> list.sortedByDescending { it.masteredCards }
                    else -> list
                }
            }
    }

    if (studentReports.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(54.dp)
                )
                Text(
                    text = "No students registered yet.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "When students create an account in Memora, their full name, activity, and progress will appear here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search & Sort Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or @username...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Sort Pill Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sortLabels = listOf("Name (A-Z)", "Username (@)", "Recent Activity", "Top Mastery")
                sortLabels.forEachIndexed { idx, label ->
                    val isSelected = sortOption == idx
                    Surface(
                        onClick = { sortOption = idx },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CoachPurple else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (filteredAndSorted.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching students found for \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAndSorted, key = { it.username }) { report ->
                        StudentReportCard(report = report)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentReportCard(report: StudentStatsReport) {
    val dateStr = remember(report.lastActiveTimestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(report.lastActiveTimestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                try {
                                    Color(android.graphics.Color.parseColor(report.avatarColor))
                                } catch (_: Exception) {
                                    CoachPurple
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = report.displayName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = report.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "@${report.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "Active $dateStr",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricItem(label = "Flashcards", value = "${report.totalCards}")
                MetricItem(label = "Mastered", value = "${report.masteredCards}")
                MetricItem(label = "Mastery", value = "${report.masteryPercentage}%")
                MetricItem(label = "Memories", value = "${report.memoryEntitiesCount}")
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ----------------------------------------------------------------------
// 2. EXAMS LIST VIEW
// ----------------------------------------------------------------------
@Composable
private fun ExamsListView(
    exams: List<ExamEntity>,
    onDeleteExam: (ExamEntity) -> Unit,
    onCreateClick: () -> Unit
) {
    if (exams.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = CoachPurple.copy(alpha = 0.6f),
                    modifier = Modifier.size(54.dp)
                )
                Text(
                    text = "No exams or prompts created yet.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onCreateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create First Exam")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(exams, key = { it.id }) { exam ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exam.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Topic: ${exam.targetTopic} • Assigned to: ${if (exam.assignedToUsername == "ALL") "All Students" else "@" + exam.assignedToUsername}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { onDeleteExam(exam) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Exam", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        if (exam.description.isNotBlank()) {
                            Text(
                                text = exam.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// 3. SUBMISSIONS & AI DIGEST VIEW
// ----------------------------------------------------------------------
@Composable
private fun SubmissionsListView(
    submissions: List<ExamSubmissionEntity>,
    onDigestClick: (ExamSubmissionEntity) -> Unit,
    onSendFeedback: (ExamSubmissionEntity, String) -> Unit,
    onSelectDetail: (ExamSubmissionEntity) -> Unit
) {
    if (submissions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QuestionAnswer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(54.dp)
                )
                Text(
                    text = "No student submissions yet.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "When students complete assigned exams, their answers will arrive here for AI digestion and grading.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(submissions, key = { it.id }) { sub ->
                var isExpanded by remember { mutableStateOf(false) }
                val hasAiDigest = !sub.aiDigestSummary.isNullOrBlank()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasAiDigest) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (hasAiDigest) CoachPurple.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub.examTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Student: ${sub.studentDisplayName} (@${sub.studentUsername})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (hasAiDigest) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CoachPurple.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = sub.aiRecommendedScore ?: "Digested",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CoachPurple
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Pending AI Digest",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }
                        }

                        // AI Digest Summary Preview
                        if (hasAiDigest) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CoachPurple, modifier = Modifier.size(16.dp))
                                        Text("AI Digested Summary", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CoachPurple)
                                    }
                                    Text(
                                        text = sub.aiDigestSummary ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!hasAiDigest) {
                                Button(
                                    onClick = { onDigestClick(sub) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generate AI Digest")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onSelectDetail(sub) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("View Full Digest & Feedback")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// CREATE EXAM DIALOG
// ----------------------------------------------------------------------
@Composable
private fun CreateExamDialog(
    studentReports: List<StudentStatsReport>,
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, topic: String, questionsJson: String, assignedTo: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetTopic by remember { mutableStateOf("General") }
    var assignedTo by remember { mutableStateOf("ALL") }

    val questionsList = remember {
        mutableStateListOf(
            "Explain the main difference between passive memory review and active recall.",
            "Describe how you would apply this concept in your own words."
        )
    }

    var newQuestionText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = CoachPurple)
                Text("Create New Exam / Task", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Exam Title") },
                        placeholder = { Text("e.g. Midterm Concepts Check") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = targetTopic,
                        onValueChange = { targetTopic = it },
                        label = { Text("Topic / Category") },
                        placeholder = { Text("e.g. Vocabulary, Biology, Philosophy") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Instructions / Objectives") },
                        placeholder = { Text("Instructions for students...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Text(
                        text = "Questions (${questionsList.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(questionsList.size) { index ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Q${index + 1}: ${questionsList[index]}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { questionsList.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newQuestionText,
                            onValueChange = { newQuestionText = it },
                            label = { Text("Add Question") },
                            placeholder = { Text("Type question...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                if (newQuestionText.isNotBlank()) {
                                    questionsList.add(newQuestionText.trim())
                                    newQuestionText = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoachPurple)
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val array = JSONArray()
                    questionsList.forEachIndexed { i, q ->
                        val obj = JSONObject().apply {
                            put("id", "q_${i + 1}")
                            put("question", q)
                        }
                        array.put(obj)
                    }
                    onCreate(title, description, targetTopic, array.toString(), assignedTo)
                },
                enabled = title.isNotBlank() && questionsList.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Publish Exam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ----------------------------------------------------------------------
// SUBMISSION DETAIL & FEEDBACK DIALOG
// ----------------------------------------------------------------------
@Composable
private fun SubmissionDetailDialog(
    submission: ExamSubmissionEntity,
    onDismiss: () -> Unit,
    onDigestClick: () -> Unit,
    onSendFeedback: (String) -> Unit
) {
    var feedbackText by remember { mutableStateOf(submission.teacherFeedback ?: "") }

    val parsedAnswers = remember(submission.answersJson) {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val array = JSONArray(submission.answersJson)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i)
                val q = obj?.optString("question", "Question ${i + 1}") ?: ""
                val a = obj?.optString("answer", "No answer") ?: ""
                list.add(q to a)
            }
        } catch (_: Exception) {}
        list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${submission.examTitle} - @${submission.studentUsername}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // AI Digest Section
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CoachPurple.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, CoachPurple.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CoachPurple)
                                Text("AI Pedagogical Assessment", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = CoachPurple)
                            }
                            Text("Summary: ${submission.aiDigestSummary ?: "Pending evaluation"}", style = MaterialTheme.typography.bodyMedium)
                            if (!submission.aiStrengths.isNullOrBlank()) {
                                Text("Strengths:\n${submission.aiStrengths}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (!submission.aiWeaknesses.isNullOrBlank()) {
                                Text("Gaps to Review:\n${submission.aiWeaknesses}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (!submission.aiRecommendedScore.isNullOrBlank()) {
                                Text("Recommended Score: ${submission.aiRecommendedScore}", fontWeight = FontWeight.Bold, color = CoachPurple)
                            }
                        }
                    }
                }

                // Student Answers
                item {
                    Text("Student's Answers", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                items(parsedAnswers) { (q, a) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Q: $q", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text("A: $a", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Teacher Feedback Input
                item {
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Teacher Feedback & Notes for Student") },
                        placeholder = { Text("Enter constructive feedback...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSendFeedback(feedbackText) },
                colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send Feedback")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
