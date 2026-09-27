package com.example.teman_belajar.quizhistory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teman_belajar.components.Navbar
import com.example.teman_belajar.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHistoryScreen(
    uiState: QuizHistoryUiState,
    onEvent: (QuizHistoryEvent) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    LaunchedEffect(Unit) {
        onEvent(QuizHistoryEvent.RefreshHistory)
    }

    val filteredItems = when (uiState.selectedFilter) {
        "Lulus" -> uiState.historyItems.filter { it.score != null && it.score >= 70 }
        "Perlu Diperhatikan" -> uiState.historyItems.filter { it.score != null && it.score < 70 }
        "Draft" -> uiState.historyItems.filter { it.score == null }
        else -> uiState.historyItems
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Kuis", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(QuizHistoryEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Navbar(
                currentRoute = "quiz_history",
                onItemClick = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.EmojiEvents,
                        iconBgColor = Color(0xFFE9D5FF).copy(alpha = 0.4f),
                        iconColor = Color(0xFF9333EA),
                        value = uiState.stats.totalQuizzes.toString(),
                        label = "Jumlah"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.TrackChanges,
                        iconBgColor = Color(0xFFBBF7D0).copy(alpha = 0.4f),
                        iconColor = Color(0xFF16A34A),
                        value = "${uiState.stats.averageScore / 10}/10",
                        label = "Rata-rata"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.WorkspacePremium,
                        iconBgColor = Color(0xFFBAE6FD).copy(alpha = 0.4f),
                        iconColor = Color(0xFF0284C7),
                        value = uiState.stats.passedQuizzes.toString(),
                        label = "Lulus"
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.filterCategories) { filter ->
                        val isSelected = uiState.selectedFilter == filter
                        val filterBg = if (isSelected) AppColors.Purple else MaterialTheme.colorScheme.surfaceVariant
                        val filterText = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(filterBg)
                                .clickable { onEvent(QuizHistoryEvent.FilterSelected(filter)) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter,
                                color = filterText,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Purple)
                    }
                }
            } else if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat kuis dengan kategori ini",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredItems) { item ->
                    QuizHistoryCard(
                        item = item,
                        onClick = { onEvent(QuizHistoryEvent.HistoryItemClicked(item)) },
                        onContinueQuiz = { onEvent(QuizHistoryEvent.ContinueQuizClicked(quizId = item.quizId, attemptedQuizId = item.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    value: String,
    label: String
) {
    Card(
        modifier = modifier.aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuizHistoryCard(
    item: QuizHistoryItem,
    onContinueQuiz: () -> Unit,
    onClick: () -> Unit
) {
    val isFinished = item.score != null
    val score = item.score ?: 0
    val isHighScore = score >= 80
    val badgeBgColor = if (isHighScore) Color(0xFFBBF7D0).copy(alpha = 0.5f) else Color(0xFFE9D5FF).copy(alpha = 0.5f)
    val badgeTextColor = if (isHighScore) Color(0xFF16A34A) else Color(0xFF9333EA)
    val displayScore = score / 10

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = item.subject, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (isFinished) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(badgeBgColor).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "$displayScore/10", color = badgeTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFEDD5)).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Draft", color = Color(0xFFEA580C), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.TrackChanges, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isFinished) "$displayScore/10 soal Benar" else "${item.totalQuestions} Soal",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            if (isFinished) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.status} ${item.date}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Button(
                    onClick = onContinueQuiz,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
                ) {
                    Text(text = "Lanjutkan Quiz", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
