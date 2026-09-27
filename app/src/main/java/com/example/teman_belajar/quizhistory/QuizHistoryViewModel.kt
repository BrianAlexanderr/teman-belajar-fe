package com.example.teman_belajar.quizhistory

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizHistoryStat(
    val totalQuizzes: Int = 0,
    val averageScore: Int = 0,
    val passedQuizzes: Int = 0
)

data class QuizHistoryItem(
    val id: String,
    val quizId: String,
    val title: String,
    val subject: String,
    val score: Int?,
    val maxScore: Int = 100,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val durationMin: Int,
    val status: String,
    val date: String
)

data class QuizHistoryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val stats: QuizHistoryStat = QuizHistoryStat(),
    val filterCategories: List<String> = listOf("Semua", "Lulus", "Perlu Diperhatikan", "Draft"),
    val selectedFilter: String = "Semua",
    val historyItems: List<QuizHistoryItem> = emptyList(),
    val selectedItem: QuizHistoryItem? = null
)

sealed class QuizHistoryEvent {
    object NavigateBack : QuizHistoryEvent()
    object RefreshHistory : QuizHistoryEvent()
    object ViewExplanation : QuizHistoryEvent()
    data class FilterSelected(val filter: String) : QuizHistoryEvent()
    data class HistoryItemClicked(val item: QuizHistoryItem) : QuizHistoryEvent()
    data class ContinueQuizClicked(val quizId: String, val attemptedQuizId: String) : QuizHistoryEvent()
}

class QuizHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiService.create(application)

    private val _uiState = MutableStateFlow(QuizHistoryUiState())
    val uiState: StateFlow<QuizHistoryUiState> = _uiState.asStateFlow()

    var onNavigateBack: (() -> Unit)? = null
    var onNavigateToResult: (() -> Unit)? = null
    var onNavigateToExplanation: ((String, String) -> Unit)? = null
    var onNavigateToQuizSession: ((String, String) -> Unit)? = null

    init {
        fetchHistory()
    }

    private fun fetchHistory() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = apiService.getQuizAttempted()
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    val mappedItems = items.map {
                        // Handled Nullable fields
                        val scoreInt = it.score?.toIntOrNull()
                        val totalQuestions = it.countAttemptedQuestion?.toIntOrNull() ?: 0
                        val estimatedCorrect = if (totalQuestions > 0 && scoreInt != null) (scoreInt * totalQuestions) / 100 else 0

                        QuizHistoryItem(
                            id = it.attemptedQuizId ?: "",
                            quizId = it.quizId ?: "",
                            title = it.quizTitle ?: "Tanpa Judul",
                            subject = it.folderName ?: "Umum",
                            score = scoreInt,
                            correctAnswers = estimatedCorrect,
                            totalQuestions = totalQuestions,
                            durationMin = 0,
                            status = "Selesai pada",
                            date = it.attemptedAt ?: "-"
                        )
                    }
                    
                    val validScores = mappedItems.mapNotNull { it.score }
                    val total = mappedItems.size
                    val avg = if (validScores.isNotEmpty()) validScores.average().toInt() else 0
                    val passed = mappedItems.count { it.score != null && it.score >= 70 }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            historyItems = mappedItems,
                            stats = QuizHistoryStat(total, avg, passed)
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat riwayat kuis.") }
                }
            } catch (e: Exception) {
                Log.e("QuizHistoryVM", "Error fetch history", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Kesalahan jaringan.") }
            }
        }
    }

    fun onEvent(event: QuizHistoryEvent) {
        when (event) {
            QuizHistoryEvent.NavigateBack -> onNavigateBack?.invoke()
            QuizHistoryEvent.RefreshHistory -> fetchHistory()
            QuizHistoryEvent.ViewExplanation -> {
                _uiState.value.selectedItem?.let {
                    onNavigateToExplanation?.invoke(it.quizId, it.id)
                }
            }
            is QuizHistoryEvent.FilterSelected -> {
                _uiState.update { it.copy(selectedFilter = event.filter) }
            }
            is QuizHistoryEvent.HistoryItemClicked -> {
                if (event.item.score == null) {
                    onNavigateToQuizSession?.invoke(event.item.quizId, event.item.id)
                } else {
                    _uiState.update { it.copy(selectedItem = event.item) }
                    onNavigateToResult?.invoke()
                }
            }
            is QuizHistoryEvent.ContinueQuizClicked -> {
                onNavigateToQuizSession?.invoke(event.quizId, event.attemptedQuizId)
            }
        }
    }
}
