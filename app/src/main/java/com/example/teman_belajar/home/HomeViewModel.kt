package com.example.teman_belajar.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.utils.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    var onNavigateToLogin: (() -> Unit)? = null

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.LogoutClicked -> {
                logout()
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            userPreferences.setLoggedIn(false)
            onNavigateToLogin?.invoke()
        }
    }
}
