package com.example.teman_belajar.folderdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.AutoAwesome
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

data class SummaryItem(
    val keyPoint: String,
    val description: String
)
data class SummaryDetailUiState(
    val title: String = "Judul Rangkuman",
    val items: List<SummaryItem> = listOf(
        SummaryItem("Difusi Gas", "Proses pertukaran oksigen dan karbon dioksida terjadi di alveolus melalui membran respirasi yang sangat tipis."),
        SummaryItem("Hemoglobin", "Protein dalam sel darah merah yang bertugas mengikat O2 dari paru-paru untuk diedarkan ke seluruh jaringan tubuh."),
        SummaryItem("Faktor Respirasi", "Kecepatan pernapasan dipengaruhi oleh aktivitas fisik, umur, jenis kelamin, dan suhu tubuh."),
        SummaryItem("Volume Residu", "Udara yang tetap berada di dalam paru-paru meskipun telah melakukan ekspirasi maksimal (sekitar 1000 mL).")
    ),
    val quizQuestionCount: Int = 5
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryDetailScreen(
    uiState: SummaryDetailUiState = SummaryDetailUiState(),
    onBackClick: () -> Unit = {},
    onStartQuizClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ringkasan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F3FF))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = AppColors.Purple,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Hasil Scan",
                    color = AppColors.Purple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = AppColors.Purple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    uiState.items.forEach { item ->
                        SummaryBulletItem(item)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                border = BorderStroke(1.dp, Color(0xFFEDE9FE))
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Siap untuk Uji Diri?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4C1D95)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI telah membuat ${uiState.quizQuestionCount} pertanyaan kuis.",
                            fontSize = 12.sp,
                            color = Color(0xFF6D28D9)
                        )
                    }
                    
                    Button(
                        onClick = onStartQuizClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text("Mulai Kuis", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SummaryBulletItem(item: SummaryItem) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(AppColors.Purple)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "${item.keyPoint}: ${item.description}",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF374151)
            )
        }
    }
}
