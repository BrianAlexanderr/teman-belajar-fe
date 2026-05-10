package com.example.teman_belajar.forgotpassword

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import com.example.teman_belajar.fetch.ChangePasswordRequest
import com.example.teman_belajar.fetch.ForgotPasswordRequest
import com.example.teman_belajar.fetch.verifyOTPRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class ForgotPasswordUiState(
    val currentStep: Int = 1,
    val email: String = "",
    val otpCode: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val resendCountdown: Int = 0,
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val resetToken: String = ""
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
    object ResendCodeClicked : ForgotPasswordEvent()
}

class ForgotPasswordViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    var onNavigateBack: (() -> Unit)? = null

    fun resetState() {
        stopCountdown()
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
                _uiState.update { it.copy(otpCode = event.value, passwordError = null) }
            }
            is ForgotPasswordEvent.NewPasswordChanged -> {
                _uiState.update { it.copy(newPassword = event.value, passwordError = null) }
            }
            is ForgotPasswordEvent.ConfirmPasswordChanged -> {
                _uiState.update { it.copy(confirmPassword = event.value, confirmPasswordError = null) }
            }
            ForgotPasswordEvent.SendCodeClicked -> {
                validateAndSendOtp()
            }
            ForgotPasswordEvent.VerifyClicked -> {
                verifyOtp()
            }
            ForgotPasswordEvent.ResetPasswordClicked -> {
                validateStep3Password()
            }
            ForgotPasswordEvent.SuccessDoneClicked -> {
                resetState()
                onNavigateBack?.invoke()
            }
            ForgotPasswordEvent.ResendCodeClicked -> {
                if (_uiState.value.resendCountdown <= 0 && !_uiState.value.isResending) {
                    resendOtp()
                }
            }
            ForgotPasswordEvent.BackClicked -> {
                if (_uiState.value.currentStep in 2..3) {
                    if (_uiState.value.currentStep == 2) stopCountdown()
                    _uiState.update { it.copy(currentStep = it.currentStep - 1, passwordError = null) }
                } else if (_uiState.value.currentStep == 1) {
                    resetState()
                    onNavigateBack?.invoke()
                }
            }
        }
    }

    private fun startResendCountdown() {
        stopCountdown()
        countdownJob = viewModelScope.launch {
            _uiState.update { it.copy(resendCountdown = 60) }
            while (_uiState.value.resendCountdown > 0) {
                delay(1000)
                _uiState.update { it.copy(resendCountdown = it.resendCountdown - 1) }
            }
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        _uiState.update { it.copy(resendCountdown = 0) }
    }

    private fun validateAndSendOtp() {
        val state = _uiState.value
        val emailError = when {
            state.email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Enter a valid email"
            else -> null
        }

        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
        } else {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, emailError = null) }
                try {
                    val response = ApiService.create().forgotPass(ForgotPasswordRequest(state.email))
                    if (response.isSuccessful) {
                        _uiState.update { it.copy(currentStep = 2, isLoading = false) }
                        startResendCountdown()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            val jsonObject = JSONObject(errorBody ?: "")
                            jsonObject.optString("message").takeIf { it.isNotEmpty() }
                                ?: jsonObject.optString("msg").takeIf { it.isNotEmpty() }
                                ?: "Failed to send code. Please try again."
                        } catch (e: Exception) {
                            "Failed to send code. Please try again."
                        }
                        _uiState.update { it.copy(isLoading = false, emailError = errorMessage) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, emailError = "Check your internet connection") }
                }
            }
        }
    }

    private fun resendOtp() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, passwordError = null) }
            try {
                val response = ApiService.create().forgotPass(ForgotPasswordRequest(state.email))
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isResending = false) }
                    startResendCountdown()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val jsonObject = JSONObject(errorBody ?: "")
                        jsonObject.optString("message").takeIf { it.isNotEmpty() }
                            ?: jsonObject.optString("msg").takeIf { it.isNotEmpty() }
                            ?: "Failed to resend code."
                    } catch (e: Exception) {
                        "Failed to resend code."
                    }
                    _uiState.update { it.copy(isResending = false, passwordError = errorMessage) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isResending = false, passwordError = "Check your internet connection") }
            }
        }
    }

    private fun verifyOtp() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, passwordError = null) }
            try {
                val response = ApiService.create().verifyOTP(verifyOTPRequest(state.email, state.otpCode))
                if (response.isSuccessful) {
                    val token = response.body()?.token ?: ""
                    _uiState.update { it.copy(currentStep = 3, isLoading = false, resetToken = token) }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val jsonObject = JSONObject(errorBody ?: "")
                        jsonObject.optString("message").takeIf { it.isNotEmpty() }
                            ?: jsonObject.optString("msg").takeIf { it.isNotEmpty() }
                            ?: "Invalid OTP code."
                    } catch (e: Exception) {
                        "Invalid OTP code."
                    }
                    _uiState.update { it.copy(isLoading = false, passwordError = errorMessage) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, passwordError = "Check your internet connection") }
            }
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
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, passwordError = null, confirmPasswordError = null) }
            try {
                val request = ChangePasswordRequest(
                    email = state.email,
                    newPassword = state.newPassword,
                    resetToken = state.resetToken
                )
                
                val response = ApiService.create().changePass(request)
                
                if (response.isSuccessful) {
                    _uiState.update { it.copy(currentStep = 4, isLoading = false) }
                    stopCountdown()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val jsonObject = JSONObject(errorBody ?: "")
                        jsonObject.optString("message").takeIf { it.isNotEmpty() }
                            ?: jsonObject.optString("msg").takeIf { it.isNotEmpty() }
                            ?: "Failed to reset password."
                    } catch (e: Exception) {
                        "Failed to reset password."
                    }
                    _uiState.update { it.copy(isLoading = false, passwordError = errorMessage) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, passwordError = "Network error. Please try again.") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCountdown()
    }
}
