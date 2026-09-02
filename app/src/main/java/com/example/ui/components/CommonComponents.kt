package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SpacedRepetitionStages
import com.example.data.local.StageInfo
import com.example.ui.theme.CoachPurple
import com.example.ui.theme.StageColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StageBadge(
    stageId: Int,
    modifier: Modifier = Modifier,
    isMastered: Boolean = false
) {
    val stageColor = if (isMastered) Color(0xFFF59E0B) else StageColors.getOrElse(stageId) { MaterialTheme.colorScheme.primary }
    val label = if (isMastered) "Mastered" else SpacedRepetitionStages.getStage(stageId).name

    Surface(
        modifier = modifier.testTag("stage_badge_${stageId}"),
        color = stageColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, stageColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(stageColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = stageColor
            )
        }
    }
}

@Composable
fun TypeBadge(
    type: String,
    modifier: Modifier = Modifier
) {
    val (icon, label, color) = when (type) {
        "IMAGE" -> Triple(Icons.Default.Image, "Image", MaterialTheme.colorScheme.secondary)
        "AUDIO" -> Triple(Icons.AutoMirrored.Filled.VolumeUp, "Audio", Color(0xFF0284C7))
        "EXPLANATION" -> Triple(Icons.Default.AutoAwesome, "AI Coach", CoachPurple)
        else -> Triple(Icons.Default.Description, "Text", MaterialTheme.colorScheme.primary)
    }

    Surface(
        modifier = modifier.testTag("type_badge_$type"),
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun TtsAudioButton(
    textToSpeak: String,
    isPlaying: Boolean,
    onPlay: (String) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tts_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    IconButton(
        onClick = {
            if (isPlaying) onStop() else onPlay(textToSpeak)
        },
        modifier = modifier
            .testTag("tts_audio_button")
            .then(if (isPlaying) Modifier.scale(pulseScale) else Modifier)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isPlaying) "Stop audio" else "Play audio",
                    tint = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun formatDueTime(dueTimestamp: Long, currentTime: Long = System.currentTimeMillis()): String {
    val diff = dueTimestamp - currentTime

    return when {
        diff <= 0 -> "Due now"
        diff < 60_000L -> "In ${(diff / 1000).coerceAtLeast(1)}s"
        diff < 3600_000L -> {
            val mins = diff / 60_000L
            val secs = (diff % 60_000L) / 1000L
            if (mins < 5) "In ${mins}m ${secs}s" else "In ${mins}m"
        }
        diff < 86400_000L -> "In ${(diff / 3600_000L)}h"
        diff < 7 * 86400_000L -> "In ${(diff / 86400_000L)}d"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(dueTimestamp))
        }
    }
}
