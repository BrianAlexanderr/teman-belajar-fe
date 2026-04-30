package com.example.teman_belajar.Register.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
//import androidx.compose.material3.internal.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.teman_belajar.theme.RegistrationColors
import com.example.teman_belajar.theme.RegistrationShapes
import com.example.teman_belajar.theme.RegistrationSpacing
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    var isFocused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = when {
            error != null -> RegistrationColors.Error
            isFocused     -> RegistrationColors.InputFocused
            else          -> RegistrationColors.InputBorder
        },
        animationSpec = tween(200),
        label = "text_field_border"
    )

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RegistrationColors.TextSecondary
                    )
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isFocused) RegistrationColors.Purple
                    else RegistrationColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility
                            else Icons.Outlined.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password"
                            else "Show password",
                            tint = RegistrationColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            isError = error != null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(RegistrationShapes.fieldRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = borderColor,
                unfocusedBorderColor = borderColor,
                errorBorderColor     = borderColor,
                focusedContainerColor   = RegistrationColors.Surface,
                unfocusedContainerColor = RegistrationColors.Surface,
                errorContainerColor     = RegistrationColors.ErrorSurface,
                cursorColor = RegistrationColors.Purple
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = RegistrationColors.TextPrimary
            )
        )

        if (error != null) {
            Row(
                modifier = Modifier.padding(
                    start = RegistrationSpacing.md,
                    top   = RegistrationSpacing.xs
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = RegistrationColors.Error,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(RegistrationSpacing.xs))
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = RegistrationColors.Error
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable

private fun AppTextFieldPreview() {
            MaterialTheme {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AppTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = "First Name",
                        leadingIcon = Icons.Outlined.Person,
                    )
                    AppTextField(
                        value = "john@example.com",
                        onValueChange = {},
                        placeholder = "Email",
                        leadingIcon = Icons.Outlined.Email,
                        error = "Invalid email address"
                    )
                    AppTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = "Password",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true
                    )
                }
            }
}