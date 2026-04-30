package com.example.teman_belajar.login

import org.json.JSONObject
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import com.example.teman_belajar.fetch.LoginRequest
import com.example.teman_belajar.utils.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService: ApiService = ApiService.create()
    private val userPreferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    var onNavigateToRegister: (() -> Unit)? = null
    var onLoginSuccess: (() -> Unit)? = null

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.value, emailError = null, errorMessage = null) }
            }
            is LoginEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.value, passwordError = null, errorMessage = null) }
            }
            LoginEvent.LoginClicked -> {
                validateAndLogin()
            }
            LoginEvent.RegisterClicked -> {
                onNavigateToRegister?.invoke()
            }
        }
    }

    private fun validateAndLogin() {
        val state = _uiState.value

        val emailError = if (state.email.isBlank()) "Email is required" else null
        val passwordError = if (state.password.isBlank()) "Password is required" else null

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        performLogin()
    }

    private fun performLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val state = _uiState.value

            try {
                val request = LoginRequest(
                    email = state.email.trim().lowercase(),
                    password = state.password
                )

                val response = apiService.login(request)

                if (response.isSuccessful) {
                    val token = response.body()?.token
                    val refreshToken = response.body()?.refreshToken
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            email = "",
                            password = ""
                        )
                    }

                    viewModelScope.launch {
                        userPreferences.setLoggedIn(true, token, refreshToken)
                    }

                    onLoginSuccess?.invoke()
                } else {
                    val errorJson = response.errorBody()?.string()

                    Log.e("LoginError", "Isi errorJson: $errorJson")
                    val serverError = try {
                        JSONObject(errorJson ?: "{}").getString("msg")
                    } catch (_: Exception) {
                        "Invalid email or password"
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = serverError) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Connection failed. Please check your internet.") }
            }
        }
    }
}