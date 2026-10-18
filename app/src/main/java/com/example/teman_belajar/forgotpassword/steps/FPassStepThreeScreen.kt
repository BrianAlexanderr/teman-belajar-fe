package com.example.teman_belajar.forgotpassword.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teman_belajar.register.ui.components.AppTextField
import com.example.teman_belajar.forgotpassword.ForgotPasswordEvent
import com.example.teman_belajar.forgotpassword.ForgotPasswordUiState
import com.example.teman_belajar.theme.AppColors

@Composable
fun FPassStepThreeScreen(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Buat kata sandi yang kuat untuk mengamankan akun Anda.",
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Kata Sandi Baru", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = uiState.newPassword,
                onValueChange = { onEvent(ForgotPasswordEvent.NewPasswordChanged(it)) },
                placeholder = "Masukkan Kata Sandi Baru",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                enabled = !uiState.isLoading
            )

            if (uiState.passwordError != null) {
                Text(
                    text = uiState.passwordError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Konfirmasi Kata Sandi Baru", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = uiState.confirmPassword,
                onValueChange = { onEvent(ForgotPasswordEvent.ConfirmPasswordChanged(it)) },
                placeholder = "Konfirmasi Kata Sandi Baru",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                enabled = !uiState.isLoading
            )

            if (uiState.confirmPasswordError != null) {
                Text(
                    text = uiState.confirmPasswordError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { onEvent(ForgotPasswordEvent.ResetPasswordClicked) },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Purple,
                disabledContainerColor = AppColors.Purple.copy(alpha = 0.5f)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Atur Ulang Kata Sandi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
