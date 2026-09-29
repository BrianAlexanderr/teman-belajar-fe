package com.example.teman_belajar.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teman_belajar.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizExplanationScreen(
    uiState: QuizUiState,
    onEvent: (QuizEvent) -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.Purple)
        }
        return
    }

    if (uiState.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = uiState.errorMessage, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onEvent(QuizEvent.NavigateBack) }) {
                    Text("Kembali", color = Color.White)
                }
            }
        }
        return
    }

    val totalQuestions = uiState.questions.size
    val currentIndex = uiState.currentExplanationIndex
    val progress = if (totalQuestions > 0) (currentIndex + 1).toFloat() / totalQuestions else 0f

    val currentQuestion = uiState.questions.getOrNull(currentIndex)
    val userAnswer = uiState.userAnswers.getOrNull(currentIndex) ?: -1
    val isUserCorrect = uiState.userCorrectness.getOrNull(currentIndex) ?: false
    val isLastQuestion = currentIndex == totalQuestions - 1

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Pembahasan", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(QuizEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (totalQuestions > 0) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Button(
                        onClick = { onEvent(QuizEvent.NextExplanation) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
                    ) {
                        Text(
                            text = if (isLastQuestion) "Selesai" else "Pertanyaan Berikutnya",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (totalQuestions == 0) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Tidak ada data pembahasan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pertanyaan ${currentIndex + 1}/$totalQuestions",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = AppColors.Purple,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))

                if (currentQuestion != null) {
                    Text(text = currentQuestion.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(24.dp))

                    val letters = listOf("A", "B", "C", "D")

                    currentQuestion.options.forEachIndexed { index, option ->
                        val isSelectedByUser = userAnswer == index
                        val isCorrectOption = if (isUserCorrect) isSelectedByUser else index == currentQuestion.correctOptionIndex

                        val (borderColor, bgColor, icon) = when {
                            isSelectedByUser && isCorrectOption -> Triple(Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.1f), Icons.Default.CheckCircle)
                            isSelectedByUser && !isCorrectOption -> Triple(Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.1f), Icons.Default.Cancel)
                            !isSelectedByUser && isCorrectOption -> Triple(Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.1f), Icons.Default.CheckCircle)
                            else -> Triple(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.surface, null)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (icon != null) {
                                    Icon(imageVector = icon, contentDescription = null, tint = borderColor, modifier = Modifier.size(32.dp))
                                } else {
                                    Box(
                                        modifier = Modifier.size(32.dp).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = letters.getOrElse(index) { "" }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = option, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUserCorrect) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, if (isUserCorrect) Color(0xFF10B981) else Color(0xFFEF4444))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isUserCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isUserCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isUserCorrect) "Jawabanmu Benar" else "Jawabanmu Salah",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentQuestion.explanation,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
