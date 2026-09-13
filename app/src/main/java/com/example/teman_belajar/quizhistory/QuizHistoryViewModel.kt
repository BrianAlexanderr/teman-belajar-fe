package com.example.teman_belajar.quizhistory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class QuizHistoryStat(
    val totalQuizzes: Int = 3,
    val averageScore: Int = 87,
    val passedQuizzes: Int = 3
)

data class QuizHistoryItem(
    val id: String,
    val title: String,
    val subject: String,
    val score: Int,
    val maxScore: Int = 100,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val durationMin: Int,
    val status: String,
    val date: String
)

data class QuizHistoryUiState(
    val stats: QuizHistoryStat = QuizHistoryStat(),
    val filterCategories: List<String> = listOf("Semua", "Lulus", "Perlu Diperhatikan", "Belum Selesai"),
    val selectedFilter: String = "Semua",
    val historyItems: List<QuizHistoryItem> = emptyList(),
    val selectedItem: QuizHistoryItem? = null
)

sealed class QuizHistoryEvent {
    object NavigateBack : QuizHistoryEvent()
    data class FilterSelected(val filter: String) : QuizHistoryEvent()
    data class HistoryItemClicked(val item: QuizHistoryItem) : QuizHistoryEvent()
}

class QuizHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(QuizHistoryUiState())
    val uiState: StateFlow<QuizHistoryUiState> = _uiState.asStateFlow()

    var onNavigateBack: (() -> Unit)? = null

    var onNavigateToResult: (() -> Unit)? = null

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        val dummyItems = listOf(
            QuizHistoryItem(
                id = "1", title = "Introduction to Calculus", subject = "Matematika",
                score = 100, correctAnswers = 10, totalQuestions = 10, durationMin = 15,
                status = "Sedang berlangsung pada", date = "15 Mei 2026"
            ),
            QuizHistoryItem(
                id = "2", title = "Fuzzy Set", subject = "Kecerdasan Buatan (AI)",
                score = 70, correctAnswers = 7, totalQuestions = 10, durationMin = 20,
                status = "Selesai pada", date = "13 Mei 2026"
            ),
            QuizHistoryItem(
                id = "3", title = "Basic Principle of Biology", subject = "Biologi",
                score = 90, correctAnswers = 9, totalQuestions = 10, durationMin = 10,
                status = "Selesai pada", date = "13 Mei 2026"
            )
        )
        _uiState.update { it.copy(historyItems = dummyItems) }
    }

    fun onEvent(event: QuizHistoryEvent) {
        when (event) {
            QuizHistoryEvent.NavigateBack -> onNavigateBack?.invoke()
            is QuizHistoryEvent.FilterSelected -> {
                _uiState.update { it.copy(selectedFilter = event.filter) }
            }
            is QuizHistoryEvent.HistoryItemClicked -> {
                _uiState.update { it.copy(selectedItem = event.item) }
                onNavigateToResult?.invoke()
            }
        }
    }
}