package com.example.teman_belajar.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun QuizScreen(
    uiState: QuizUiState,
    onEvent: (QuizEvent) -> Unit
) {
    val totalQuestions = uiState.questions.size
    val currentIndex = uiState.currentQuestionIndex
    val progress = if (totalQuestions > 0) (currentIndex + 1).toFloat() / totalQuestions else 0f

    val currentQuestion = uiState.questions.getOrNull(currentIndex)

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Button(
                    onClick = { onEvent(QuizEvent.SubmitAnswer) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Purple,
                        disabledContainerColor = Color(0xFFD1D5DB)
                    ),
                    enabled = uiState.selectedOptionIndex != null
                ) {
                    Text("Kirim Jawaban", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { onEvent(QuizEvent.NavigateBack) },
                    modifier = Modifier
                        .background(Color(0xFFF3F4F6), CircleShape)
                        .size(40.dp)
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
                Text(
                    text = currentQuestion.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                val letters = listOf("A", "B", "C", "D")

                currentQuestion.options.forEachIndexed { index, option ->
                    val isSelected = uiState.selectedOptionIndex == index

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable { onEvent(QuizEvent.OptionSelected(index)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFF5F3FF) else Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) AppColors.Purple else Color(0xFFE5E7EB)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) AppColors.Purple else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) AppColors.Purple else Color(0xFFD1D5DB),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letters.getOrElse(index) { "" },
                                    color = if (isSelected) Color.White else Color(0xFF4B5563),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = option,
                                color = if (isSelected) Color.Black else Color(0xFF374151),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}