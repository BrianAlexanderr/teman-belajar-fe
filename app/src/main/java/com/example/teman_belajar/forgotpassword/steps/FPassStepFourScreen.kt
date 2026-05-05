package com.example.teman_belajar.forgotpassword.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teman_belajar.Register.ui.components.SuccessCheckmark
import com.example.teman_belajar.forgotpassword.ForgotPasswordEvent
import com.example.teman_belajar.theme.AppColors

@Composable
fun FPassStepFourScreen(onEvent: (ForgotPasswordEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SuccessCheckmark()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Password Reset Successful!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your password has been updated. You can return to the login screen",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { onEvent(ForgotPasswordEvent.SuccessDoneClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
        ) {
            Text("Back to Login", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }
    }
}