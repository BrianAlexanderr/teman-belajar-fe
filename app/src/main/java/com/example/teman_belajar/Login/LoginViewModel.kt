package com.example.teman_belajar.Login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    var onNavigateToRegister: (() -> Unit)? = null

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> onNavigateToRegister?.invoke()
            is LoginEvent.PasswordChanged -> onNavigateToRegister?.invoke()
            LoginEvent.LoginClicked -> onNavigateToRegister?.invoke()
            LoginEvent.RegisterClicked -> onNavigateToRegister?.invoke()
        }
    }
}
