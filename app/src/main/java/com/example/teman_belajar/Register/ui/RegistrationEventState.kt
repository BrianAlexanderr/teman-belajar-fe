package com.example.teman_belajar.Register.ui
data class RegistrationUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val agreedToTerms: Boolean = false,
    val currentStep: Int = 1,
    val isLoading: Boolean = false,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,
)

sealed class RegistrationEvent {
    data class FirstNameChanged(val value: String)       : RegistrationEvent()
    data class LastNameChanged(val value: String)        : RegistrationEvent()
    data class EmailChanged(val value: String)           : RegistrationEvent()
    data class PasswordChanged(val value: String)        : RegistrationEvent()
    data class ConfirmPasswordChanged(val value: String) : RegistrationEvent()
    data class TermsChecked(val checked: Boolean)        : RegistrationEvent()
    object ContinueClicked : RegistrationEvent()
    object ConfirmClicked  : RegistrationEvent()
    object BackClicked     : RegistrationEvent()
    object LoginClicked    : RegistrationEvent()
}