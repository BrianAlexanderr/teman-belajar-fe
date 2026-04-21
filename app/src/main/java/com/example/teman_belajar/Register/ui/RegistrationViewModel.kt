package com.example.teman_belajar.Register.ui
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.Fetch.ApiService
import com.example.teman_belajar.Fetch.RegisterRequest
import com.example.teman_belajar.Fetch.RegisterResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val apiService: ApiService = ApiService.create()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    var onNavigateToLogin: (() -> Unit)? = null

    fun onEvent(event: RegistrationEvent) {
        when (event) {
            is RegistrationEvent.FirstNameChanged ->
                _uiState.update { it.copy(firstName = event.value, firstNameError = null) }

            is RegistrationEvent.LastNameChanged ->
                _uiState.update { it.copy(lastName = event.value, lastNameError = null) }

            is RegistrationEvent.EmailChanged ->
                _uiState.update { it.copy(email = event.value, emailError = null) }

            is RegistrationEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.value, passwordError = null) }

            is RegistrationEvent.ConfirmPasswordChanged ->
                _uiState.update { it.copy(confirmPassword = event.value, confirmPasswordError = null) }

            is RegistrationEvent.TermsChecked ->
                _uiState.update { it.copy(agreedToTerms = event.checked, termsError = null) }

            RegistrationEvent.ContinueClicked -> validateStep1()
            RegistrationEvent.ConfirmClicked  -> validateStep2()
            RegistrationEvent.BackClicked     ->
                _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1)) }

            RegistrationEvent.LoginClicked    -> {
                onNavigateToLogin?.invoke()
                resetState()
            }
            RegistrationEvent.ResetRegistration -> resetState()
        }
    }

    private fun resetState() {
        _uiState.value = RegistrationUiState()
    }

    private fun validateStep1() {
        val state = _uiState.value
        val firstNameError = when {
            state.firstName.isBlank()    -> "First name is required"
            state.firstName.length < 2   -> "At least 2 characters required"
            else                         -> null
        }
        val lastNameError = when {
            state.lastName.isBlank()     -> "Last name is required"
            state.lastName.length < 2    -> "At least 2 characters required"
            else                         -> null
        }
        if (firstNameError != null || lastNameError != null) {
            _uiState.update { it.copy(firstNameError = firstNameError, lastNameError = lastNameError) }
            return
        }
        _uiState.update { it.copy(currentStep = 2) }
    }

    private fun validateStep2() {
        val state = _uiState.value
        val emailError = when {
            state.email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Enter a valid email"
            else -> null
        }
        val passwordError = when {
            state.password.isBlank()       -> "Password is required"
            state.password.length < 8      -> "At least 8 characters"
            !state.password.any { it.isDigit() } -> "Must contain a number"
            else                           -> null
        }
        val confirmPasswordError = when {
            state.confirmPassword.isBlank()            -> "Please confirm your password"
            state.confirmPassword != state.password    -> "Passwords do not match"
            else                                       -> null
        }
        val termsError = if (!state.agreedToTerms) "You must agree to the terms" else null

        if (emailError != null || passwordError != null || confirmPasswordError != null || termsError != null) {
            _uiState.update {
                it.copy(
                    emailError           = emailError,
                    passwordError        = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    termsError           = termsError
                )
            }
            return
        }
        submitRegistration()
    }

    private fun submitRegistration() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, termsError = null, emailError = null) }
            val state = _uiState.value
            
            try {
                val request = RegisterRequest(
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                    email = state.email.trim().lowercase(),
                    password = state.password
                )
                
                val response = apiService.register(request)
                
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, currentStep = 3) }
                } else {
                    val errorJson = response.errorBody()?.string()
                    val errorMessage =
                        try {
                            Gson().fromJson(errorJson, RegisterResponse::class.java).msg
                        } catch (e: Exception) {
                            "Registration failed"
                        }
                    _uiState.update {
                        it.copy(isLoading = false, emailError = errorMessage)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, termsError = "Connection failed. Please check your internet.")
                }
            }
        }
    }
}
