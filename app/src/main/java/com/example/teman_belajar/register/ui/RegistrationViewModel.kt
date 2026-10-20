package com.example.teman_belajar.register.ui
import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.fetch.ApiService
import com.example.teman_belajar.fetch.model.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class RegistrationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val apiService: ApiService = ApiService.create(application)

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
            state.firstName.isBlank()    -> "Nama depan wajib diisi"
            state.firstName.length < 2   -> "Minimal 2 karakter"
            else                         -> null
        }
        val lastNameError = when {
            state.lastName.isBlank()     -> "Nama belakang wajib diisi"
            state.lastName.length < 2    -> "Minimal 2 karakter"
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
            state.email.isBlank() -> "Email wajib diisi"
            !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Masukkan email yang valid"
            else -> null
        }
        val passwordError = when {
            state.password.isBlank()       -> "Kata sandi wajib diisi"
            state.password.length < 8      -> "Minimal 8 karakter"
            !state.password.any { it.isDigit() } -> "Harus mengandung angka"
            else                           -> null
        }
        val confirmPasswordError = when {
            state.confirmPassword.isBlank()            -> "Harap konfirmasi kata sandi Anda"
            state.confirmPassword != state.password    -> "Kata sandi tidak cocok"
            else                                       -> null
        }
        val termsError = if (!state.agreedToTerms) "Anda harus menyetujui syarat dan ketentuan" else null

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

                    println("SERVER_ERROR_SPY: $errorJson")

                    val errorMessage = try {
                        val jsonObject = JSONObject(errorJson ?: "")
                        jsonObject.optString("message").takeIf { it.isNotBlank() }
                            ?: jsonObject.optString("msg").takeIf { it.isNotBlank() }
                            ?: jsonObject.optString("error").takeIf { it.isNotBlank() }
                            ?: "Pendaftaran gagal"
                    } catch (_: Exception) {
                        "Pendaftaran gagal"
                    }

                    _uiState.update {
                        it.copy(isLoading = false, emailError = errorMessage)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, termsError = "Koneksi gagal: ${e.localizedMessage}")
                }
            }
        }
    }
}
