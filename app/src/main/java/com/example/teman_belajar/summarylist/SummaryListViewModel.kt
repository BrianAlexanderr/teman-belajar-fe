package com.example.teman_belajar.summarylist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import com.example.teman_belajar.fetch.model.SummaryListItemResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SummaryListUiState(
    val isLoading: Boolean = false,
    val summaries: List<SummaryListItemResponse> = emptyList(),
    val errorMessage: String? = null
)

sealed class SummaryListEvent {
    object FetchSummaries : SummaryListEvent()
    object NavigateBack : SummaryListEvent()
    data class SummaryClicked(val summaryId: String) : SummaryListEvent()
}

class SummaryListViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiService.create(application)

    private val _uiState = MutableStateFlow(SummaryListUiState())
    val uiState: StateFlow<SummaryListUiState> = _uiState.asStateFlow()

    var onNavigateBack: (() -> Unit)? = null
    var onNavigateToSummaryDetail: ((String) -> Unit)? = null

    fun fetchAllSummaries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = apiService.getSummaryListAll()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, summaries = response.body() ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat daftar ringkasan.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan.") }
            }
        }
    }

    fun onEvent(event: SummaryListEvent) {
        when (event) {
            SummaryListEvent.FetchSummaries -> fetchAllSummaries()
            SummaryListEvent.NavigateBack -> onNavigateBack?.invoke()
            is SummaryListEvent.SummaryClicked -> onNavigateToSummaryDetail?.invoke(event.summaryId)
        }
    }
}