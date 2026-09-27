package com.example.teman_belajar.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.utils.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "Michael Wijaya",
    val email: String = "michael.wijaya024@binus.ac.id",
    val totalQuizzes: Int = 23,
    val averageScore: Int = 90,
    val isDarkMode: Boolean = false,
    val subscriptionPlan: String = "Paket Gratis"
)

sealed class ProfileEvent {
    object NavigateBack : ProfileEvent()
    object EditProfile : ProfileEvent()
    object Subscription : ProfileEvent()
    data class ToggleDarkMode(val enabled: Boolean) : ProfileEvent()
    object Logout : ProfileEvent()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    var onNavigateBack: (() -> Unit)? = null
    var onLogout: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            userPreferences.isDarkModeFlow.collect { isDark ->
                _uiState.update { it.copy(isDarkMode = isDark) }
            }
        }

        viewModelScope.launch {
            userPreferences.userNameFlow.collect { fullName ->
                if (!fullName.isNullOrBlank()) {
                    _uiState.update { it.copy(name = fullName) }
                }
            }
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.NavigateBack -> onNavigateBack?.invoke()
            ProfileEvent.EditProfile -> { /* Handle Edit Profile */ }
            ProfileEvent.Subscription -> { /* Handle Subscription */ }
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
        }
    }
}
