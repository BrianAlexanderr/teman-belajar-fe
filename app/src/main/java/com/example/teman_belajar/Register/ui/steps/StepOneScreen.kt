package com.example.teman_belajar.Register.ui.steps
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.example.teman_belajar.Register.ui.RegistrationEvent
import com.example.teman_belajar.Register.ui.RegistrationUiState
import com.example.teman_belajar.Register.ui.components.AppTextField
import com.example.teman_belajar.Register.ui.components.PrimaryButton
import com.example.teman_belajar.theme.RegistrationColors
import com.example.teman_belajar.theme.RegistrationSpacing

@Composable
fun StepOneScreen(
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
            text = "What can we call you?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = RegistrationColors.TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(RegistrationSpacing.lg))

        AppTextField(
            value = uiState.firstName,
            onValueChange = { onEvent(RegistrationEvent.FirstNameChanged(it)) },
            placeholder = "First Name",
            leadingIcon = Icons.Outlined.Person,
            error = uiState.firstNameError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(RegistrationSpacing.md))

        AppTextField(
            value = uiState.lastName,
            onValueChange = { onEvent(RegistrationEvent.LastNameChanged(it)) },
            placeholder = "Last Name",
            leadingIcon = Icons.Outlined.Person,
            error = uiState.lastNameError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(RegistrationSpacing.xl))

        PrimaryButton(
            text = "Continue",
            onClick = { onEvent(RegistrationEvent.ContinueClicked) }
        )

        Spacer(modifier = Modifier.height(RegistrationSpacing.xl))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StepOnePreview() {
    MaterialTheme {
        StepOneScreen(uiState = RegistrationUiState(), onEvent = {})
    }
}