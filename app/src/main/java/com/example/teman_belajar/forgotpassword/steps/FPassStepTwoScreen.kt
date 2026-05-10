package com.example.teman_belajar.forgotpassword.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teman_belajar.forgotpassword.ForgotPasswordEvent
import com.example.teman_belajar.forgotpassword.ForgotPasswordUiState
import com.example.teman_belajar.theme.AppColors
import com.example.teman_belajar.theme.RegistrationSpacing
import com.example.teman_belajar.theme.RegistrationShapes

@Composable
fun FPassStepTwoScreen(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit
) {
    val isCountdownActive = uiState.resendCountdown > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RegistrationSpacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                append("Enter the 6-digit code sent to\n")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)) {
                    append(uiState.email.ifEmpty { "john@example.com" })
                }
            },
            color = AppColors.TextPrimary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = RegistrationSpacing.xl)
        )

        BasicTextField(
            value = uiState.otpCode,
            onValueChange = { newValue ->
                if (newValue.length <= 6 && newValue.all { it.isLetterOrDigit() }) {
                    onEvent(ForgotPasswordEvent.OtpChanged(newValue.uppercase()))
                }
            },
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                capitalization = KeyboardCapitalization.Characters
            ),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.alpha(0f)) {
                        innerTextField()
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(RegistrationSpacing.sm),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(6) { index ->
                            val char = when {
                                index < uiState.otpCode.length -> uiState.otpCode[index].toString()
                                else -> ""
                            }

                            val isFocused = index == uiState.otpCode.length

                            val borderColor = if (isFocused || char.isNotEmpty()) AppColors.Purple else AppColors.InputBorder

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(RegistrationSpacing.sm)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        )

        if (uiState.passwordError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = RegistrationSpacing.md),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.ErrorSurface
                ),
                shape = RoundedCornerShape(RegistrationShapes.fieldRadius),
                border = BorderStroke(1.dp, AppColors.Error.copy(alpha = 0.2f))
            ) {
                Text(
                    text = uiState.passwordError,
                    color = AppColors.Error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(RegistrationSpacing.md)
                )
            }
        }

        Spacer(modifier = Modifier.height(RegistrationSpacing.lg))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = { onEvent(ForgotPasswordEvent.ResendCodeClicked) },
                enabled = !isCountdownActive && !uiState.isResending && !uiState.isLoading
            ) {
                if (uiState.isResending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = AppColors.Purple
                    )
                    Spacer(modifier = Modifier.width(RegistrationSpacing.sm))
                    Text(
                        text = "Sending...",
                        color = AppColors.Purple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = if (isCountdownActive) {
                            "Resend code in ${uiState.resendCountdown}s"
                        } else {
                            "Didn't Receive code? Resend Code"
                        },
                        color = if (isCountdownActive) AppColors.TextSecondary else AppColors.Purple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(RegistrationSpacing.md))

        Button(
            onClick = { onEvent(ForgotPasswordEvent.VerifyClicked) },
            enabled = uiState.otpCode.length == 6 && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(RegistrationShapes.buttonHeight),
            shape = RoundedCornerShape(RegistrationShapes.buttonRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Purple,
                disabledContainerColor = AppColors.Purple.copy(alpha = 0.5f)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AppColors.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Verify",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.White
                )
            }
        }
    }
}
