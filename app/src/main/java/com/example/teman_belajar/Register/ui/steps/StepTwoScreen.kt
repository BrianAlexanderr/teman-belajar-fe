package com.example.teman_belajar.Register.ui.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.teman_belajar.Register.ui.RegistrationEvent
import com.example.teman_belajar.Register.ui.RegistrationUiState
import com.example.teman_belajar.Register.ui.components.AppTextField
import com.example.teman_belajar.Register.ui.components.PrimaryButton
import com.example.teman_belajar.Register.ui.components.TermsCheckbox
import com.example.teman_belajar.theme.RegistrationColors
import com.example.teman_belajar.theme.RegistrationSpacing

@Composable
fun StepTwoScreen(
    uiState: RegistrationUiState,
    onEvent: (RegistrationEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = RegistrationSpacing.screenHorizontal)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "One Step Forward",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = RegistrationColors.TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(RegistrationSpacing.lg))

        AppTextField(
            value = uiState.email,
            onValueChange = { onEvent(RegistrationEvent.EmailChanged(it)) },
            placeholder = "Email",
            leadingIcon = Icons.Outlined.Email,
            error = uiState.emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(RegistrationSpacing.md))

        AppTextField(
            value = uiState.password,
            onValueChange = { onEvent(RegistrationEvent.PasswordChanged(it)) },
            placeholder = "Password",
            leadingIcon = Icons.Outlined.Lock,
            error = uiState.passwordError,
            isPassword = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(RegistrationSpacing.md))

        AppTextField(
            value = uiState.confirmPassword,
            onValueChange = { onEvent(RegistrationEvent.ConfirmPasswordChanged(it)) },
            placeholder = "Confirm Password",
            leadingIcon = Icons.Outlined.Lock,
            error = uiState.confirmPasswordError,
            isPassword = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Spacer(modifier = Modifier.height(RegistrationSpacing.md))

        TermsCheckbox(
            checked = uiState.agreedToTerms,
            onCheckedChange = { onEvent(RegistrationEvent.TermsChecked(it)) },
            error = uiState.termsError
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(RegistrationSpacing.xl))

        PrimaryButton(
            text = "Confirm",
            isLoading = uiState.isLoading,
            onClick = { onEvent(RegistrationEvent.ConfirmClicked) }
        )

        Spacer(modifier = Modifier.height(RegistrationSpacing.xl))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StepTwoPreview() {
    MaterialTheme {
        StepTwoScreen(uiState = RegistrationUiState(currentStep = 2), onEvent = {})
    }
}