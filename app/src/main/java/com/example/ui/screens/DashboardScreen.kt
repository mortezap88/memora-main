package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppStats
import com.example.data.local.FlashcardEntity
import com.example.data.local.SpacedRepetitionStages
import com.example.data.local.StageInfo
import com.example.ui.theme.CoachPurple
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    stats: AppStats = AppStats(),
    activeCards: List<FlashcardEntity>,
    masteredCards: List<FlashcardEntity>,
    studentExams: List<com.example.data.local.ExamEntity> = emptyList(),
    studentSubmissions: List<com.example.data.local.ExamSubmissionEntity> = emptyList(),
    onOpenExam: (com.example.data.local.ExamEntity) -> Unit = {},
    onStartGlobalReview: () -> Unit,
    onStartStageReview: (Int) -> Unit,
    onAddNewCard: () -> Unit,
    currentTime: Long = System.currentTimeMillis(),
    fontScale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    // 6-day forecast computation matching the reference design (Sun, Mon, Tue, Wed, Thu, Fri)
    val forecast = remember(activeCards) {
        computeForecastDays(activeCards, daysCount = 6)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Teacher Assigned Exams banner if available
            if (studentExams.isNotEmpty()) {
                item {
                    TeacherExamsBannerCard(
                        exams = studentExams,
                        submissions = studentSubmissions,
                        onOpenExam = onOpenExam,
                        fontScale = fontScale
                    )
                }
            }

            // 1. Upcoming Reviews Forecast Section (Glossy Glassmorphic Card)
            item {
                UpcomingReviewsForecastCard(
                    forecast = forecast,
                    fontScale = fontScale
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            // 2. Exact 8 Stage Pill Bars with Radiant Colored Halo Backlights
            items(SpacedRepetitionStages.STAGES) { stage ->
                val stageCards = activeCards.filter { it.currentStageId == stage.id }
                val stageDueCards = stageCards.filter { it.dueTimestamp <= currentTime }

                StagePillBar(
                    stage = stage,
                    totalCount = stageCards.size,
                    dueCount = stageDueCards.size,
                    onStartReview = { onStartStageReview(stage.id) },
                    fontScale = fontScale
                )
            }
        }

        // 3. Floating Action Button (FAB) to Add Card / Create Note
        // Positioned comfortably above the bottom navigation island with generous space
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 126.dp)
        ) {
            // Radiant Glow halo behind FAB
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                CoachPurple.copy(alpha = 0.65f),
                                Color(0xFF818CF8).copy(alpha = 0.30f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Surface(
                shape = CircleShape,
                color = CoachPurple,
                shadowElevation = 10.dp,
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.45f)),
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White)
                    ) {
                        onAddNewCard()
                    }
                    .testTag("dashboard_add_card_fab")
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add New Card",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

data class DayForecast(
    val dayLabel: String,
    val count: Int,
    val isToday: Boolean
)

fun computeForecastDays(activeCards: List<FlashcardEntity>, daysCount: Int = 6): List<DayForecast> {
    val sdf = SimpleDateFormat("EEE", Locale.getDefault())
    val result = mutableListOf<DayForecast>()

    for (i in 0 until daysCount) {
        val targetDayCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, i)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = targetDayCal.timeInMillis
        val endMillis = startMillis + (24 * 60 * 60 * 1000L)

        val count = if (i == 0) {
            // Today: cards due now or due before the end of today
            val todayEnd = targetDayCal.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            activeCards.count { it.dueTimestamp <= todayEnd }
        } else {
            activeCards.count { it.dueTimestamp in startMillis until endMillis }
        }

        // Use "Today" for current day, abbreviated day name for future days
        val label = if (i == 0) "Today" else sdf.format(targetDayCal.time)
        result.add(DayForecast(label, count, i == 0))
    }
    return result
}

@Composable
fun UpcomingReviewsForecastCard(
    forecast: List<DayForecast>,
    fontScale: Float
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = if (isDark) Color(0xFF161823).copy(alpha = 0.78f) else Color(0xFFFFFFFF).copy(alpha = 0.85f),
        border = BorderStroke(1.2.dp, if (isDark) Color.White.copy(alpha = 0.18f) else Color(0xFFE2E8F0).copy(alpha = 0.85f)),
        shadowElevation = if (isDark) 12.dp else 6.dp,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .testTag("forecast_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            // Header: "Upcoming Reviews" title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Reviews",
                    fontSize = (19 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row of 6 Day Pills matching the reference screenshot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                forecast.forEach { day ->
                    val hasItems = day.count > 0

                    val pillBg = when {
                        hasItems && isDark -> Color(0xFF26183B).copy(alpha = 0.90f)
                        hasItems && !isDark -> Color(0xFFF3E8FF)
                        !hasItems && isDark -> Color(0xFF1E212B).copy(alpha = 0.55f)
                        else -> Color(0xFFF8FAFC).copy(alpha = 0.85f)
                    }

                    val pillBorder = when {
                        hasItems && isDark -> BorderStroke(1.5.dp, Color(0xFFA78BFA))
                        hasItems && !isDark -> BorderStroke(1.5.dp, Color(0xFF9333EA))
                        !hasItems && isDark -> BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        else -> BorderStroke(1.dp, Color(0xFFE2E8F0))
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = pillBg,
                        border = pillBorder,
                        shadowElevation = if (hasItems) 6.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.5.dp)
                            .height(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = day.dayLabel,
                                fontSize = (12 * fontScale).sp,
                                fontWeight = if (hasItems) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    hasItems && isDark -> Color(0xFFA78BFA)
                                    hasItems && !isDark -> Color(0xFF7E22CE)
                                    !hasItems && isDark -> Color(0xFFD1D5DB)
                                    else -> Color(0xFF64748B)
                                }
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = day.count.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = (19 * fontScale).sp,
                                color = when {
                                    hasItems && isDark -> Color(0xFFA78BFA)
                                    hasItems && !isDark -> Color(0xFF6B21A8)
                                    !hasItems && isDark -> Color.White
                                    else -> Color(0xFF1E293B)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getStageAccentColor(stageId: Int, isDark: Boolean): Color {
    return when (stageId) {
        0 -> if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48) // 2 Minutes (Red)
        1 -> if (isDark) Color(0xFFFB923C) else Color(0xFFEA580C) // 20 Minutes (Orange)
        2 -> if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706) // 2 Hours (Golden Amber)
        3 -> if (isDark) Color(0xFF34D399) else Color(0xFF059669) // 1 Day (Emerald Green)
        4 -> if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488) // 3 Days (Teal)
        5 -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7) // 1 Week (Sky Blue)
        6 -> if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5) // 2 Weeks (Indigo)
        else -> if (isDark) Color(0xFFC084FC) else Color(0xFF9333EA) // 1 Month (Purple)
    }
}

@Composable
fun StagePillBar(
    stage: StageInfo,
    totalCount: Int,
    dueCount: Int,
    onStartReview: () -> Unit,
    fontScale: Float
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val isDue = dueCount > 0
    val hasCards = totalCount > 0
    val stageColor = remember(stage.id, isDark) { getStageAccentColor(stage.id, isDark) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        // Frosted Stadium Capsule Bar
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Color(0xFF161824).copy(alpha = 0.78f) else Color(0xFFFFFFFF).copy(alpha = 0.88f),
            border = BorderStroke(
                1.2.dp,
                if (isDark) Color.White.copy(alpha = 0.18f) else Color(0xFFE2E8F0)
            ),
            shadowElevation = if (isDark) 8.dp else 4.dp,
            tonalElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("stage_card_${stage.id}")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LEFT SIDE: Stage Name aligned to the left
                Text(
                    text = stage.name,
                    fontSize = (17 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = stageColor
                )

                // RIGHT SIDE: Standardized RED Review Button
                if (hasCards) {
                    Button(
                        onClick = onStartReview,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("stage_review_btn_${stage.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (dueCount > 0) "Review ($dueCount)" else "Review",
                            fontSize = (13 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeacherExamsBannerCard(
    exams: List<com.example.data.local.ExamEntity>,
    submissions: List<com.example.data.local.ExamSubmissionEntity>,
    onOpenExam: (com.example.data.local.ExamEntity) -> Unit,
    fontScale: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoachPurple.copy(alpha = 0.12f)
        ),
        border = BorderStroke(1.dp, CoachPurple.copy(alpha = 0.35f))
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CoachPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Teacher Exams & Prompts (${exams.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                exams.forEach { exam ->
                    val submission = submissions.firstOrNull { it.examId == exam.id }
                    val isDone = submission != null

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenExam(exam) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exam.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Topic: ${exam.targetTopic}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (isDone) {
                                val hasFeedback = !submission?.teacherFeedback.isNullOrBlank()
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (hasFeedback) CoachPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (hasFeedback) "Feedback Ready ✨" else "Submitted",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (hasFeedback) CoachPurple else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onOpenExam(exam) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Take Exam", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


