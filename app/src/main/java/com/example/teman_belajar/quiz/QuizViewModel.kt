package com.example.teman_belajar.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

data class QuizUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val correctAnswersCount: Int = 0,
    val isQuizFinished: Boolean = false,
    val userAnswers: List<Int> = emptyList(),
    val currentExplanationIndex: Int = 0
)

sealed class QuizEvent {
    object NavigateBack : QuizEvent()
    data class OptionSelected(val index: Int) : QuizEvent()
    object SubmitAnswer : QuizEvent()
    object NavigateToHome : QuizEvent()
    object ViewExplanation : QuizEvent()
    object NextExplanation : QuizEvent()
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    var onNavigateBack: (() -> Unit)? = null
    var onNavigateToResult: (() -> Unit)? = null
    var onNavigateToHome: (() -> Unit)? = null
    var onNavigateToExplanation: (() -> Unit)? = null

    init {
        loadDummyQuestions()
    }

    private fun loadDummyQuestions() {
        val dummyQuestions = listOf(
            QuizQuestion("Question", listOf("Pilihan 1", "Pilihan 2", "Pilihan 3", "Pilihan 4"), 1, "Penjelasan"),
            QuizQuestion("Question 2", listOf("Pilihan 1", "Pilihan 2", "Pilihan 3", "Pilihan 4"), 1, "Penjelasan"),
            QuizQuestion("Question 3", listOf("Pilihan 1", "Pilihan 2", "Pilihan 3", "Pilihan 4"), 2, "Penjelasan"),
            QuizQuestion("Question 4", listOf("Pilihan 1", "Pilihan 2", "Pilihan 3", "Pilihan 4"), 3, "Penjelasan"),
            QuizQuestion("Question 5", listOf("Pilihan 1", "Pilihan 2", "Pilihan 3", "Pilihan 4"), 0, "Penjelasan")
        )
        _uiState.update { it.copy(questions = dummyQuestions) }
    }

    fun resetQuiz() {
        _uiState.update {
            QuizUiState(
                questions = it.questions,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                correctAnswersCount = 0,
                isQuizFinished = false,
                userAnswers = emptyList(),
                currentExplanationIndex = 0
            )
        }
    }

    fun onEvent(event: QuizEvent) {
        when (event) {
            QuizEvent.NavigateBack -> onNavigateBack?.invoke()
            is QuizEvent.OptionSelected -> {
                _uiState.update { it.copy(selectedOptionIndex = event.index) }
            }
            QuizEvent.SubmitAnswer -> {
                val state = _uiState.value
                val currentQuestion = state.questions[state.currentQuestionIndex]
                val answeredIndex = state.selectedOptionIndex ?: -1

                val isCorrect = answeredIndex == currentQuestion.correctOptionIndex
                val newCorrectCount = if (isCorrect) state.correctAnswersCount + 1 else state.correctAnswersCount


                val updatedAnswers = state.userAnswers + answeredIndex

                if (state.currentQuestionIndex < state.questions.size - 1) {
                    _uiState.update {
                        it.copy(
                            currentQuestionIndex = it.currentQuestionIndex + 1,
                            selectedOptionIndex = null,
                            correctAnswersCount = newCorrectCount,
                            userAnswers = updatedAnswers
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            correctAnswersCount = newCorrectCount,
                            isQuizFinished = true,
                            userAnswers = updatedAnswers
                        )
                    }
                    onNavigateToResult?.invoke()
                }
            }
            QuizEvent.NavigateToHome -> onNavigateToHome?.invoke()
            QuizEvent.ViewExplanation -> {
                _uiState.update { it.copy(currentExplanationIndex = 0) }
                onNavigateToExplanation?.invoke()
            }
            QuizEvent.NextExplanation -> {
                val state = _uiState.value
                if (state.currentExplanationIndex < state.questions.size - 1) {
                    _uiState.update { it.copy(currentExplanationIndex = it.currentExplanationIndex + 1) }
                } else {
                    onNavigateBack?.invoke()
                }
            }
        }
    }
}