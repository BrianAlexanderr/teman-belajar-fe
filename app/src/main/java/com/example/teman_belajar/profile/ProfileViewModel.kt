package com.example.teman_belajar.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
    import com.example.teman_belajar.fetch.ApiService
import com.example.teman_belajar.fetch.model.UpdateProfileRequest
import com.example.teman_belajar.utils.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val totalQuizzes: Int = 0,
    val averageScore: Int = 0,
    val isDarkMode: Boolean = false,
    val subscriptionPlan: String = "Paket Gratis",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isEditDialogVisible: Boolean = false,
    val editNameInput: String = "",
    val editEmailInput: String = ""
)

sealed class ProfileEvent {
//    object NavigateBack : ProfileEvent()
    object EditProfile : ProfileEvent()
    object DismissEditDialog : ProfileEvent()
    object SubmitEditProfile : ProfileEvent()
    data class EditNameChanged(val name: String) : ProfileEvent()
    data class EditEmailChanged(val email: String) : ProfileEvent()
    object Subscription : ProfileEvent()
    data class ToggleDarkMode(val enabled: Boolean) : ProfileEvent()
    object Logout : ProfileEvent()
    object ClearMessages : ProfileEvent()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val apiService = ApiService.create(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

//    var onNavigateBack: (() -> Unit)? = null
    var onLogout: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            userPreferences.isDarkModeFlow.collect { isDark ->
                _uiState.update { it.copy(isDarkMode = isDark) }
            }
        }

        fetchProfile()
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val profileResponse = apiService.getUserProfile()

                val historyResponse = apiService.getQuizAttempted()

                var passedQuizzes = 0
                var avgScore = 0

                if (historyResponse.isSuccessful) {
                    val items = historyResponse.body() ?: emptyList()
                    val validScores = items.mapNotNull { it.score?.toIntOrNull() }

                    avgScore = if (validScores.isNotEmpty()) validScores.average().toInt() else 0
                    passedQuizzes = validScores.count { it >= 70 }
                }

                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    val body = profileResponse.body()!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = body.userName ?: "Pelajar",
                            email = body.email ?: "",
                            totalQuizzes = passedQuizzes,
                            averageScore = avgScore
                        )
                    }
                    userPreferences.setUserName(body.userName ?: "Pelajar")
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat profil") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan") }
            }
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
//            ProfileEvent.NavigateBack -> onNavigateBack?.invoke()
            ProfileEvent.EditProfile -> {
                _uiState.update {
                    it.copy(
                        isEditDialogVisible = true,
                        editNameInput = it.name,
                        editEmailInput = it.email
                    )
                }
            }
            ProfileEvent.DismissEditDialog -> {
                _uiState.update { it.copy(isEditDialogVisible = false) }
            }
            is ProfileEvent.EditNameChanged -> {
                _uiState.update { it.copy(editNameInput = event.name) }
            }
            is ProfileEvent.EditEmailChanged -> {
                _uiState.update { it.copy(editEmailInput = event.email) }
            }
            ProfileEvent.SubmitEditProfile -> {
                _uiState.update { it.copy(isEditDialogVisible = false, isLoading = true) }
                viewModelScope.launch {
                    try {
                        val currentState = _uiState.value
                        val request = UpdateProfileRequest(
                            name = currentState.editNameInput,
                            email = currentState.editEmailInput
                        )
                        val response = apiService.updateUserProfile(request)

                        if (response.isSuccessful) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    name = currentState.editNameInput,
                                    email = currentState.editEmailInput,
                                    successMessage = "Profil berhasil diperbarui!"
                                )
                            }
                            userPreferences.setUserName(currentState.editNameInput)
                        } else {
                            _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memperbarui profil") }
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Terjadi kesalahan jaringan") }
                    }
                }
            }
            ProfileEvent.Subscription -> { }
            is ProfileEvent.ToggleDarkMode -> {
                viewModelScope.launch {
                    userPreferences.setDarkMode(event.enabled)
                }
            }
            ProfileEvent.Logout -> {
                viewModelScope.launch {
                    userPreferences.setLoggedIn(false)
                    onLogout?.invoke()
                }
            }
            ProfileEvent.ClearMessages -> {
                _uiState.update { it.copy(errorMessage = null, successMessage = null) }
            }
        }
    }
}