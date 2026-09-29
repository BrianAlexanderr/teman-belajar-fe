package com.example.teman_belajar.quiz

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import com.example.teman_belajar.fetch.model.CheckQuizRequest
import com.example.teman_belajar.fetch.model.QuestionAttemptRequest
import com.example.teman_belajar.fetch.model.SubmitQuizRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizQuestion(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctOptionIndex: Int = -1,
    val explanation: String = ""
)

data class QuizUiState(
    val quizId: String = "",
    val isLoading: Boolean = false,
    val isCheckingAnswer: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val correctAnswersCount: Int = 0,
    val isQuizFinished: Boolean = false,
    val userAnswers: List<Int> = emptyList(),
    val userCorrectness: List<Boolean> = emptyList(),
    val currentExplanationIndex: Int = 0,
    val errorMessage: String? = null,
    val showExitConfirmation: Boolean = false
)

sealed class QuizEvent {
    object NavigateBack : QuizEvent()
    data class OptionSelected(val index: Int) : QuizEvent()
    object SubmitAnswer : QuizEvent()
    object NavigateToHome : QuizEvent()
    object ViewExplanation : QuizEvent()
    object NextExplanation : QuizEvent()
    object ShowExitConfirmation : QuizEvent()
    object DismissExitConfirmation : QuizEvent()
    object ConfirmExitAndSave : QuizEvent()
    object ConfirmExitNoSave : QuizEvent()
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService.create(application)

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    var onNavigateBack: (() -> Unit)? = null
    var onNavigateToResult: (() -> Unit)? = null
    var onNavigateToHome: (() -> Unit)? = null
    var onNavigateToExplanation: (() -> Unit)? = null

     private fun mapLabelToIndex(label: String?): Int {
        val l = label?.trim()?.uppercase() ?: return -1
        return when {
            l == "A" || l == "ANSWERA" || l == "OPTIONA" || l.endsWith(".A") || l.startsWith("A.") -> 0
            l == "B" || l == "ANSWERB" || l == "OPTIONB" || l.endsWith(".B") || l.startsWith("B.") -> 1
            l == "C" || l == "ANSWERC" || l == "OPTIONC" || l.endsWith(".C") || l.startsWith("C.") -> 2
            l == "D" || l == "ANSWERD" || l == "OPTIOND" || l.endsWith(".D") || l.startsWith("D.") -> 3
            else -> -1
        }
    }

    private fun findOptionIndex(options: List<String>, target: String?, fieldLabel: String? = null): Int {
        val indexFromLabel = mapLabelToIndex(fieldLabel)
        if (indexFromLabel != -1) return indexFromLabel

        val t = target?.trim() ?: return -1
        if (t.isEmpty()) return -1

        val exactMatch = options.indexOfFirst { it.trim().equals(t, ignoreCase = true) }
        if (exactMatch != -1) return exactMatch

        return mapLabelToIndex(t)
    }

    fun fetchQuiz(quizId: String) {
        _uiState.update { 
            it.copy(
                quizId = quizId,
                isLoading = true, 
                errorMessage = null, 
                isQuizFinished = false, 
                currentQuestionIndex = 0,
                questions = emptyList(),
                userAnswers = emptyList(),
                userCorrectness = emptyList(),
                correctAnswersCount = 0,
                showExitConfirmation = false
            ) 
        }
        viewModelScope.launch {
            try {
                val response = apiService.getQuizById(quizId)
                if (response.isSuccessful) {
                    val quizItems = response.body() ?: emptyList()
                    val mappedQuestions = quizItems.map { item ->
                        QuizQuestion(
                            id = item.id ?: "",
                            text = item.question ?: "Pertanyaan tidak tersedia",
                            options = listOf(
                                item.answerA ?: "",
                                item.answerB ?: "",
                                item.answerC ?: "",
                                item.answerD ?: ""
                            )
                        )
                    }
                    _uiState.update { it.copy(isLoading = false, questions = mappedQuestions) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat kuis.") }
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Fetch quiz error", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Kesalahan jaringan.") }
            }
        }
    }

    fun resumeQuiz(quizId: String, attemptedQuizId: String) {
        _uiState.update {
            it.copy(
                quizId = quizId,
                isLoading = true,
                errorMessage = null,
                isQuizFinished = false,
                questions = emptyList(),
                userAnswers = emptyList(),
                userCorrectness = emptyList(),
                correctAnswersCount = 0,
                showExitConfirmation = false
            )
        }

        viewModelScope.launch {
            try {
                val quizResponse = apiService.getQuizById(quizId)
                val detailResponse = apiService.getQuizAttemptedDetail(quizId, attemptedQuizId)

                if (quizResponse.isSuccessful && detailResponse.isSuccessful) {
                    val quizItems = quizResponse.body() ?: emptyList()
                    val attemptDetails = detailResponse.body()?.questionAttemptResponse ?: emptyList()

                    val mappedQuestions = quizItems.map { item ->
                        val attempt = attemptDetails.find { it.questionId?.equals(item.id, ignoreCase = true) == true }
                        val options = listOf(
                            item.answerA ?: "",
                            item.answerB ?: "",
                            item.answerC ?: "",
                            item.answerD ?: ""
                        )
                        val correctIdx = findOptionIndex(options, item.answerCorrect)
                        QuizQuestion(
                            id = item.id ?: "",
                            text = item.question ?: "Pertanyaan tidak tersedia",
                            options = options,
                            explanation = attempt?.explanation ?: "",
                            correctOptionIndex = correctIdx
                        )
                    }

                    val userAnswers = mutableListOf<Int>()
                    val userCorrectness = mutableListOf<Boolean>()
                    var correctAnswersCount = 0
                    var firstUnattemptedIndex = 0
                    var foundUnattempted = false

                    quizItems.forEachIndexed { index, item ->
                        val attempt = attemptDetails.find { it.questionId?.equals(item.id, ignoreCase = true) == true }
                        val options = listOf(
                            item.answerA ?: "",
                            item.answerB ?: "",
                            item.answerC ?: "",
                            item.answerD ?: ""
                        )
                        if (attempt != null && !attempt.selectedAnswer.isNullOrEmpty()) {
                            val ansIdx = findOptionIndex(options, attempt.selectedAnswer)
                            if (!foundUnattempted) {
                                userAnswers.add(ansIdx)
                                userCorrectness.add(attempt.isCorrect ?: false)
                                if (attempt.isCorrect == true) {
                                    correctAnswersCount++
                                }
                                firstUnattemptedIndex = index + 1
                            }
                        } else {
                            foundUnattempted = true
                        }
                    }

                    if (firstUnattemptedIndex >= quizItems.size) {
                        firstUnattemptedIndex = quizItems.size - 1
                        if (firstUnattemptedIndex < 0) firstUnattemptedIndex = 0
                        if (userAnswers.size > firstUnattemptedIndex) {
                            userAnswers.removeAt(userAnswers.size - 1)
                            userCorrectness.removeAt(userCorrectness.size - 1)
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            quizId = quizId,
                            questions = mappedQuestions,
                            currentQuestionIndex = firstUnattemptedIndex,
                            userAnswers = userAnswers,
                            userCorrectness = userCorrectness,
                            correctAnswersCount = correctAnswersCount,
                            selectedOptionIndex = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat draft kuis.") }
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Resume quiz error", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Kesalahan jaringan.") }
            }
        }
    }

    fun fetchAttemptDetail(quizId: String, attemptedQuizId: String) {
        _uiState.update { 
            it.copy(
                isLoading = true, 
                errorMessage = null,
                questions = emptyList(),
                userAnswers = emptyList(),
                userCorrectness = emptyList(),
                currentExplanationIndex = 0,
                showExitConfirmation = false
            ) 
        }
        
        viewModelScope.launch {
            try {
                val quizResponse = apiService.getQuizById(quizId)
                val detailResponse = apiService.getQuizAttemptedDetail(quizId, attemptedQuizId)

                if (quizResponse.isSuccessful && detailResponse.isSuccessful) {
                    val quizItems = quizResponse.body() ?: emptyList()
                    val attemptDetails = detailResponse.body()?.questionAttemptResponse ?: emptyList()

                    val mappedQuestions = quizItems.map { item ->
                        val attempt = attemptDetails.find { it.questionId?.equals(item.id, ignoreCase = true) == true }
                        val options = listOf(
                            item.answerA ?: "",
                            item.answerB ?: "",
                            item.answerC ?: "",
                            item.answerD ?: ""
                        )
                        
                        val correctIdx = findOptionIndex(options, item.answerCorrect)

                        QuizQuestion(
                            id = item.id ?: "",
                            text = item.question ?: "Pertanyaan tidak tersedia",
                            options = options,
                            explanation = attempt?.explanation ?: "Tidak ada pembahasan tersedia.",
                            correctOptionIndex = correctIdx
                        )
                    }

                    val userAnswers = quizItems.map { item ->
                        val attempt = attemptDetails.find { it.questionId?.equals(item.id, ignoreCase = true) == true }
                        val options = listOf(
                            item.answerA ?: "",
                            item.answerB ?: "",
                            item.answerC ?: "",
                            item.answerD ?: ""
                        )
                        findOptionIndex(options, attempt?.selectedAnswer)
                    }
                    
                    val userCorrectness = quizItems.map { item ->
                        val attempt = attemptDetails.find { it.questionId?.equals(item.id, ignoreCase = true) == true }
                        attempt?.isCorrect ?: false
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            quizId = quizId,
                            questions = mappedQuestions,
                            userAnswers = userAnswers,
                            userCorrectness = userCorrectness
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Detail pembahasan tidak ditemukan.") }
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Fetch attempt detail error", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat pembahasan: ${e.localizedMessage}") }
            }
        }
    }

    private fun checkCurrentAnswer() {
        val state = _uiState.value
        val currentIndex = state.currentQuestionIndex
        val currentQuestion = state.questions.getOrNull(currentIndex) ?: return
        val selectedIndex = state.selectedOptionIndex ?: return
        val answerText = currentQuestion.options[selectedIndex]

        _uiState.update { it.copy(isCheckingAnswer = true) }

        viewModelScope.launch {
            try {
                val response = apiService.checkQuiz(
                    request = CheckQuizRequest(answer = answerText, questionId = currentQuestion.id)
                )

                if (response.isSuccessful) {
                    val checkResult = response.body()
                    val isCorrect = checkResult?.isCorrect ?: false
                    
                    val correctIdx = findOptionIndex(currentQuestion.options, checkResult?.correctAnswer, checkResult?.correctAnswerField)

                    val updatedQuestions = state.questions.toMutableList()
                    updatedQuestions[currentIndex] = currentQuestion.copy(
                        explanation = checkResult?.explanation ?: "",
                        correctOptionIndex = correctIdx
                    )

                    val updatedAnswers = state.userAnswers + selectedIndex
                    val updatedCorrectness = state.userCorrectness + isCorrect
                    val newCorrectCount = if (isCorrect) state.correctAnswersCount + 1 else state.correctAnswersCount

                    if (currentIndex < state.questions.size - 1) {
                        _uiState.update {
                            it.copy(
                                isCheckingAnswer = false,
                                questions = updatedQuestions,
                                currentQuestionIndex = currentIndex + 1,
                                selectedOptionIndex = null,
                                correctAnswersCount = newCorrectCount,
                                userAnswers = updatedAnswers,
                                userCorrectness = updatedCorrectness
                            )
                        }
                    } else {
                        submitAllAnswers(updatedQuestions, updatedAnswers, updatedCorrectness, newCorrectCount)
                    }
                } else {
                    _uiState.update { it.copy(isCheckingAnswer = false, errorMessage = "Gagal memeriksa jawaban.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCheckingAnswer = false, errorMessage = "Kesalahan jaringan.") }
            }
        }
    }

    private suspend fun submitAllAnswers(
        questions: List<QuizQuestion>,
        userAnswers: List<Int>,
        userCorrectness: List<Boolean>,
        correctCount: Int
    ) {
        val quizId = _uiState.value.quizId
        val questionAttempts = questions.mapIndexed { index, q ->
            val finalSelectedIdx = userAnswers.getOrNull(index) ?: -1
            QuestionAttemptRequest(
                questionId = q.id,
                isCorrect = userCorrectness.getOrNull(index) ?: false,
                selectedAnswer = q.options.getOrNull(finalSelectedIdx) ?: ""
            )
        }

        try {
            apiService.submitQuiz(
                request = SubmitQuizRequest(
                    quizId = quizId,
                    questionAttempts = questionAttempts
                )
            )
        } catch (e: Exception) {
            Log.e("QuizViewModel", "Submit error", e)
        }

        _uiState.update {
            it.copy(
                isCheckingAnswer = false,
                questions = questions,
                correctAnswersCount = correctCount,
                isQuizFinished = true,
                userAnswers = userAnswers,
                userCorrectness = userCorrectness
            )
        }
        onNavigateToResult?.invoke()
    }

    private fun exitAndSave() {
        val state = _uiState.value
        _uiState.update { it.copy(showExitConfirmation = false, isLoading = true) }
        
        viewModelScope.launch {
            // Memperbaiki: Hanya kirim attempt untuk soal yang sudah dijawab user
            val questionAttempts = state.userAnswers.mapIndexedNotNull { index, selectedIdx ->
                if (selectedIdx == -1) return@mapIndexedNotNull null

                val question = state.questions.getOrNull(index) ?: return@mapIndexedNotNull null
                val selectedAnswerText = question.options.getOrNull(selectedIdx) ?: return@mapIndexedNotNull null

                QuestionAttemptRequest(
                    questionId = question.id,
                    isCorrect = state.userCorrectness.getOrNull(index) ?: false,
                    selectedAnswer = selectedAnswerText
                )
            }
            
            if (questionAttempts.isEmpty()) {
                Log.d("QuizViewModel", "exitAndSave: Tidak ada jawaban untuk disimpan.")
                _uiState.update { it.copy(isLoading = false) }
                onNavigateBack?.invoke()
                return@launch
            }

            val requestBody = SubmitQuizRequest(
                quizId = state.quizId,
                questionAttempts = questionAttempts
            )

            Log.d("QuizViewModel", "exitAndSave: Request Payload = $requestBody")

            try {
                val response = apiService.saveQuiz(request = requestBody)
                Log.d("QuizViewModel", "exitAndSave: Response Code = ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("QuizViewModel", "exitAndSave: Success Body = ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("QuizViewModel", "exitAndSave: Error Body = $errorBody")
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "exitAndSave: Exception occurred", e)
            }

            _uiState.update { it.copy(isLoading = false) }
            onNavigateBack?.invoke()
        }
    }

    fun onEvent(event: QuizEvent) {
        when (event) {
            QuizEvent.NavigateBack -> {
                val state = _uiState.value
                if (state.questions.isNotEmpty() && !state.isQuizFinished && state.userAnswers.isNotEmpty()) {
                    _uiState.update { it.copy(showExitConfirmation = true) }
                } else {
                    onNavigateBack?.invoke()
                }
            }
            is QuizEvent.OptionSelected -> {
                if (!_uiState.value.isCheckingAnswer) {
                    _uiState.update { it.copy(selectedOptionIndex = event.index) }
                }
            }
            QuizEvent.SubmitAnswer -> checkCurrentAnswer()
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
            QuizEvent.ShowExitConfirmation -> _uiState.update { it.copy(showExitConfirmation = true) }
            QuizEvent.DismissExitConfirmation -> _uiState.update { it.copy(showExitConfirmation = false) }
            QuizEvent.ConfirmExitAndSave -> exitAndSave()
            QuizEvent.ConfirmExitNoSave -> {
                _uiState.update { it.copy(showExitConfirmation = false) }
                onNavigateBack?.invoke()
            }
        }
    }
}
