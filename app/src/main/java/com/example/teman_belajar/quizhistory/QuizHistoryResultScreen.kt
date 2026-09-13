package com.example.teman_belajar.quizhistory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
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

@Composable
fun QuizHistoryResultScreen(
    uiState: QuizHistoryUiState,
    onEvent: (QuizHistoryEvent) -> Unit
) {
    val item = uiState.selectedItem
    val scorePercentage = item?.score ?: 0
    val correctAnswers = item?.correctAnswers ?: 0
    val totalQuestions = item?.totalQuestions ?: 10
    val progress = correctAnswers.toFloat() / totalQuestions

    Scaffold(containerColor = Color.White) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(AppColors.Purple),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.EmojiEvents, contentDescription = "Trophy", tint = Color.White, modifier = Modifier.size(60.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Hasil Kuis", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item?.title ?: "Materi", fontSize = 14.sp, color = Color(0xFF6B7280))

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Skor Kamu", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Text(text = "$scorePercentage%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppColors.Purple)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = AppColors.Purple,
                        trackColor = Color(0xFFF3F4F6)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$correctAnswers dari $totalQuestions benar", fontSize = 12.sp, color = Color(0xFF6B7280))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { onEvent(QuizHistoryEvent.NavigateBack) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AppColors.Purple),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Purple)
            ) {
                Text("Kembali ke Riwayat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}