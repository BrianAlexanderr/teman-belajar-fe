package com.example.teman_belajar.folderdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SummaryDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val title: String = "",
    val keyPoints: List<String> = emptyList(),
    val content: String = "",
    val quizQuestionCount: Int = 5
)

sealed class SummaryDetailEvent {
    object NavigateBack : SummaryDetailEvent()
    object StartQuizClicked : SummaryDetailEvent()
}

class SmartSummaryDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiService.create(application)

    private val _uiState = MutableStateFlow(SummaryDetailUiState())
    val uiState: StateFlow<SummaryDetailUiState> = _uiState.asStateFlow()

    var onNavigateBack: (() -> Unit)? = null
    var onNavigateToQuiz: (() -> Unit)? = null

    fun fetchSummary(summaryId: String) {
        if (summaryId.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = apiService.getSummaryDetail(summaryId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                title = body.title,
                                keyPoints = body.keyPoint,
                                content = body.content
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Data ringkasan kosong.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat ringkasan.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan.") }
            }
        }
    }

    fun onEvent(event: SummaryDetailEvent) {
        when (event) {
            SummaryDetailEvent.NavigateBack -> {
                onNavigateBack?.invoke()
            }
            SummaryDetailEvent.StartQuizClicked -> {
                onNavigateToQuiz?.invoke()
            }
        }
    }
}