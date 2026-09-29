package com.example.teman_belajar.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import com.example.teman_belajar.fetch.model.QuizListItemResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizListUiState(
    val isLoading: Boolean = false,
    val quizzes: List<QuizListItemResponse> = emptyList(),
    val errorMessage: String? = null
)

sealed class QuizListEvent {
    object FetchQuizzes : QuizListEvent()
    object NavigateBack : QuizListEvent()
    data class StartQuizClicked(val quizId: String) : QuizListEvent()
}

class QuizListViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiService.create(application)

    private val _uiState = MutableStateFlow(QuizListUiState())
    val uiState: StateFlow<QuizListUiState> = _uiState.asStateFlow()

    var onNavigateBack: (() -> Unit)? = null
    var onNavigateToQuizSession: ((String) -> Unit)? = null

    fun fetchQuizzes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = apiService.getQuizList()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, quizzes = response.body() ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat daftar kuis.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan: ${e.message}") }
            }
        }
    }

    fun onEvent(event: QuizListEvent) {
        when (event) {
            QuizListEvent.FetchQuizzes -> fetchQuizzes()
            QuizListEvent.NavigateBack -> onNavigateBack?.invoke()
            is QuizListEvent.StartQuizClicked -> onNavigateToQuizSession?.invoke(event.quizId)
        }
    }
}
