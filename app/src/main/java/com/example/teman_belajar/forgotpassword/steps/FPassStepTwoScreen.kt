package com.example.teman_belajar.forgotpassword.steps

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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

@Composable
fun FPassStepTwoScreen(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit
) {
    val isCountdownActive = uiState.resendCountdown > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                append("Enter the 6-digit code sent to\n")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                    append(uiState.email.ifEmpty { "john@example.com" })
                }
            },
            color = Color.Black,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        BasicTextField(
            value = uiState.otpCode,
            onValueChange = { newValue ->
                if (newValue.length <= 6 && newValue.all { it.isLetterOrDigit() }) {
                    onEvent(ForgotPasswordEvent.OtpChanged(newValue.uppercase()))
                }
            },
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(6) { index ->
                            val char = when {
                                index < uiState.otpCode.length -> uiState.otpCode[index].toString()
                                else -> ""
                            }

                            val isFocused = index == uiState.otpCode.length

                            val borderColor = if (isFocused || char.isNotEmpty()) AppColors.Purple else Color.LightGray

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = { onEvent(ForgotPasswordEvent.ResendCodeClicked) },
            enabled = !isCountdownActive
        ) {
            Text(
                text = if (isCountdownActive) {
                    "Resend code in ${uiState.resendCountdown}s"
                } else {
                    "Didn't Receive code? Resend Code"
                },
                color = if (isCountdownActive) Color.Gray else AppColors.Purple,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEvent(ForgotPasswordEvent.VerifyClicked) },
            enabled = uiState.otpCode.length == 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Purple,
                disabledContainerColor = AppColors.Purple.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = "Verify",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}
