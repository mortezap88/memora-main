package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ExamEntity
import com.example.data.local.ExamSubmissionEntity
import com.example.ui.theme.CoachPurple
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun StudentExamDialog(
    exam: ExamEntity,
    existingSubmission: ExamSubmissionEntity?,
    onDismiss: () -> Unit,
    onSubmitAnswers: (answersJson: String) -> Unit
) {
    val questions = remember(exam.questionsJson) {
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(exam.questionsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(obj.optString("question", "Question ${i + 1}"))
            }
        } catch (_: Exception) {
            list.add("Please answer the exam prompt.")
        }
        list
    }

    val answers = remember(existingSubmission, questions.size) {
        val list = mutableStateListOf<String>()
        if (existingSubmission != null) {
            try {
                val array = JSONArray(existingSubmission.answersJson)
                for (i in 0 until questions.size) {
                    val ans = if (i < array.length()) array.getJSONObject(i).optString("answer", "") else ""
                    list.add(ans)
                }
            } catch (_: Exception) {
                repeat(questions.size) { list.add("") }
            }
        } else {
            repeat(questions.size) { list.add("") }
        }
        list
    }

    val isSubmitted = existingSubmission != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = CoachPurple
                )
                Column {
                    Text(
                        text = exam.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Topic: ${exam.targetTopic}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (exam.description.isNotBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = exam.description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // If already submitted and feedback exists, show teacher feedback banner
                if (existingSubmission != null && !existingSubmission.teacherFeedback.isNullOrBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CoachPurple.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, CoachPurple.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CoachPurple, modifier = Modifier.size(18.dp))
                                    Text("Teacher & AI Feedback", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = CoachPurple)
                                }
                                Text(
                                    text = existingSubmission.teacherFeedback ?: "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (!existingSubmission.aiRecommendedScore.isNullOrBlank()) {
                                    Text(
                                        text = "Grade: ${existingSubmission.aiRecommendedScore}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = CoachPurple
                                    )
                                }
                            }
                        }
                    }
                }

                // Questions and Answers inputs
                itemsIndexed(questions) { index, question ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Q${index + 1}: $question",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            if (isSubmitted) {
                                Text(
                                    text = "Your Answer: ${answers.getOrNull(index) ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                OutlinedTextField(
                                    value = answers.getOrElse(index) { "" },
                                    onValueChange = { newVal ->
                                        if (index < answers.size) {
                                            answers[index] = newVal
                                        }
                                    },
                                    placeholder = { Text("Type your answer...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isSubmitted) {
                Button(
                    onClick = {
                        val array = JSONArray()
                        questions.forEachIndexed { i, q ->
                            val obj = JSONObject().apply {
                                put("question", q)
                                put("answer", answers.getOrElse(i) { "" })
                            }
                            array.put(obj)
                        }
                        onSubmitAnswers(array.toString())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoachPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("submit_exam_answers_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Submit Answers")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (!isSubmitted) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
