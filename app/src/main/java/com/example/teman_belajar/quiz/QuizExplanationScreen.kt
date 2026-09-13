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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teman_belajar.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizExplanationScreen(
    uiState: QuizUiState,
    onEvent: (QuizEvent) -> Unit
) {
    val totalQuestions = uiState.questions.size
    val currentIndex = uiState.currentExplanationIndex
    val progress = if (totalQuestions > 0) (currentIndex + 1).toFloat() / totalQuestions else 0f

    val currentQuestion = uiState.questions.getOrNull(currentIndex)
    val userAnswer = uiState.userAnswers.getOrNull(currentIndex) ?: -1

    val isLastQuestion = currentIndex == totalQuestions - 1

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Button(
                    onClick = { onEvent(QuizEvent.NextExplanation) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
                ) {
                    Text(
                        text = if (isLastQuestion) "Lihat Hasil" else "Pertanyaan Berikutnya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
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
                IconButton(
                    onClick = { onEvent(QuizEvent.NavigateBack) },
                    modifier = Modifier.background(Color(0xFFF3F4F6), CircleShape).size(40.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
                Text(
                    text = "Question ${currentIndex + 1}/$totalQuestions",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = AppColors.Purple,
                trackColor = Color(0xFFF3F4F6)
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (currentQuestion != null) {
                Text(text = currentQuestion.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(24.dp))

                val letters = listOf("A", "B", "C", "D")
                val isUserCorrect = userAnswer == currentQuestion.correctOptionIndex

                currentQuestion.options.forEachIndexed { index, option ->
                    val isSelectedByUser = userAnswer == index
                    val isCorrectOption = currentQuestion.correctOptionIndex == index

                    val (borderColor, bgColor, icon) = when {
                        isSelectedByUser && isCorrectOption -> Triple(Color(0xFF10B981), Color(0xFFECFDF5), Icons.Default.CheckCircle)
                        isSelectedByUser && !isCorrectOption -> Triple(Color(0xFFEF4444), Color(0xFFFEF2F2), Icons.Default.Cancel)
                        !isSelectedByUser && isCorrectOption -> Triple(Color(0xFF10B981), Color(0xFFECFDF5), Icons.Default.CheckCircle)
                        else -> Triple(Color(0xFFE5E7EB), Color.White, null)
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
                                    modifier = Modifier.size(32.dp).border(1.dp, Color(0xFFD1D5DB), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = letters.getOrElse(index) { "" }, color = Color(0xFF4B5563), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = option, color = Color(0xFF374151), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isUserCorrect) Color(0xFFECFDF5) else Color(0xFFFEF2F2)),
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
                                text = if (isUserCorrect) "Benar!" else "Salah",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentQuestion.explanation,
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}