package com.example.teman_belajar.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.utils.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    var onNavigateToRegister: (() -> Unit)? = null
    var onLoginSuccess: (() -> Unit)? = null

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.value) }
            }
            is LoginEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.value) }
            }
            LoginEvent.LoginClicked -> {
                performLogin()
            }
            LoginEvent.RegisterClicked -> {
                onNavigateToRegister?.invoke()
            }
        }
    }

    private fun performLogin() {
        viewModelScope.launch {
            userPreferences.setLoggedIn(true)
            onLoginSuccess?.invoke()
        }
    }
}