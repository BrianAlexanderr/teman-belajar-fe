package com.example.teman_belajar.forgotpassword

import android.util.Patterns
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ForgotPasswordUiState(
    val currentStep: Int = 1,
    val email: String = "",
    val otpCode: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

sealed class ForgotPasswordEvent {
    object BackClicked : ForgotPasswordEvent()
    data class EmailChanged(val value: String) : ForgotPasswordEvent()
    object SendCodeClicked : ForgotPasswordEvent()
    data class OtpChanged(val value: String) : ForgotPasswordEvent()
    object VerifyClicked : ForgotPasswordEvent()
    data class NewPasswordChanged(val value: String) : ForgotPasswordEvent()
    data class ConfirmPasswordChanged(val value: String) : ForgotPasswordEvent()
    object ResetPasswordClicked : ForgotPasswordEvent()
    object SuccessDoneClicked : ForgotPasswordEvent()
}

class ForgotPasswordViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    var onNavigateBack: (() -> Unit)? = null

    fun resetState() {
        _uiState.update {
            ForgotPasswordUiState()
        }
    }

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.value, emailError = null) }
            }
            is ForgotPasswordEvent.OtpChanged -> {
                _uiState.update { it.copy(otpCode = event.value) }
            }
            is ForgotPasswordEvent.NewPasswordChanged -> {
                _uiState.update { it.copy(newPassword = event.value, passwordError = null) }
            }
            is ForgotPasswordEvent.ConfirmPasswordChanged -> {
                _uiState.update { it.copy(confirmPassword = event.value, confirmPasswordError = null) }
            }
            ForgotPasswordEvent.SendCodeClicked -> {
                validateStep1Email()
            }
            ForgotPasswordEvent.VerifyClicked -> {
                _uiState.update { it.copy(currentStep = 3) }
            }
            ForgotPasswordEvent.ResetPasswordClicked -> {
                validateStep3Password()
            }
            ForgotPasswordEvent.SuccessDoneClicked -> {
                resetState()
                onNavigateBack?.invoke()
            }
            ForgotPasswordEvent.BackClicked -> {
                if (_uiState.value.currentStep in 2..3) {
                    _uiState.update { it.copy(currentStep = it.currentStep - 1) }
                } else if (_uiState.value.currentStep == 1) {
                    resetState()
                    onNavigateBack?.invoke()
                }
            }
        }
    }

    private fun validateStep1Email() {
        val state = _uiState.value
        val emailError = when {
            state.email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Enter a valid email"
            else -> null
        }

        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
        } else {
            _uiState.update { it.copy(currentStep = 2, emailError = null) }
        }
    }

    private fun validateStep3Password() {
        val state = _uiState.value

        val passwordError = when {
            state.newPassword.isBlank() -> "Password is required"
            state.newPassword.length < 8 -> "At least 8 characters"
            !state.newPassword.any { it.isDigit() } -> "Must contain a number"
            else -> null
        }

        val confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Please confirm your password"
            state.confirmPassword != state.newPassword -> "Passwords do not match"
            else -> null
        }

        if (passwordError != null || confirmPasswordError != null) {
            _uiState.update {
                it.copy(
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    currentStep = 4,
                    passwordError = null,
                    confirmPasswordError = null
                )
            }
        }
    }
}