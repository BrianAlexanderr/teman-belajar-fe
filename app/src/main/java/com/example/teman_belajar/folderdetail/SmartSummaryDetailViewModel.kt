package com.example.teman_belajar.folderdetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import com.example.teman_belajar.fetch.model.GenerateQuizRequest
import com.example.teman_belajar.fetch.model.SummaryDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class SummaryDetailUiState(
    val isLoading: Boolean = true,
    val isGeneratingQuiz: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorDialogMessage: String? = null,
    val title: String = "",
    val keyPoints: List<String> = emptyList(),
    val content: String = "",
    val quizQuestionCount: Int = 5
)

sealed class SummaryDetailEvent {
    object NavigateBack : SummaryDetailEvent()
    object StartQuizClicked : SummaryDetailEvent()
    object DismissErrorDialog : SummaryDetailEvent()
}

class SmartSummaryDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiService.create(application)

    private val _uiState = MutableStateFlow(SummaryDetailUiState())
    val uiState: StateFlow<SummaryDetailUiState> = _uiState.asStateFlow()

    private var currentSummary: SummaryDetailResponse? = null

    var onNavigateBack: (() -> Unit)? = null
    var onNavigateToQuiz: (() -> Unit)? = null

    fun fetchSummary(summaryId: String) {
        if (summaryId.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, showErrorDialog = false) }

        viewModelScope.launch {
            try {
                val response = apiService.getSummaryDetail(summaryId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        currentSummary = body
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                title = body.title ?: "",
                                keyPoints = body.keyPoint ?: emptyList(),
                                content = body.content ?: ""
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showErrorDialog = true,
                                errorDialogMessage = "Data ringkasan tidak ditemukan."
                            )
                        }
                    }
                } else {
                    val errorMsg = parseError(response.errorBody()?.string()) ?: "Gagal memuat ringkasan (${response.code()})"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showErrorDialog = true,
                            errorDialogMessage = errorMsg
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showErrorDialog = true,
                        errorDialogMessage = "Kesalahan Jaringan: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun generateQuiz() {
        val summary = currentSummary ?: return

        _uiState.update { it.copy(isGeneratingQuiz = true, showErrorDialog = false) }

        viewModelScope.launch {
            try {
                val requestBody = GenerateQuizRequest(summary = summary)
                val response = apiService.generateQuizFromSummary(requestBody)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isGeneratingQuiz = false) }
                    onNavigateToQuiz?.invoke()
                } else {
                    val errorMsg = parseError(response.errorBody()?.string()) ?: "Gagal membuat kuis (${response.code()})"
                    _uiState.update {
                        it.copy(
                            isGeneratingQuiz = false,
                            showErrorDialog = true,
                            errorDialogMessage = errorMsg
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SummaryDetail", "Generate quiz exception", e)
                _uiState.update {
                    it.copy(
                        isGeneratingQuiz = false,
                        showErrorDialog = true,
                        errorDialogMessage = "Kesalahan Sistem: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun parseError(json: String?): String? {
        if (json.isNullOrEmpty()) return null
        return try {
            val obj = JSONObject(json)
            obj.optString("message").takeIf { it.isNotBlank() }
                ?: obj.optString("msg").takeIf { it.isNotBlank() }
                ?: obj.optString("error").takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    fun onEvent(event: SummaryDetailEvent) {
        when (event) {
            SummaryDetailEvent.NavigateBack -> onNavigateBack?.invoke()
            SummaryDetailEvent.StartQuizClicked -> generateQuiz()
            SummaryDetailEvent.DismissErrorDialog -> {
                _uiState.update { it.copy(showErrorDialog = false, errorDialogMessage = null) }
            }
        }
    }
}
