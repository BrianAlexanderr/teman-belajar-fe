package com.example.teman_belajar.forgotpassword

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.teman_belajar.forgotpassword.ui.components.FPassStepProgressIndicator
import com.example.teman_belajar.forgotpassword.ui.components.ForgotPasswordStep
import com.example.teman_belajar.forgotpassword.steps.FPassStepOneScreen
import com.example.teman_belajar.forgotpassword.steps.FPassStepTwoScreen
import com.example.teman_belajar.forgotpassword.steps.FPassStepThreeScreen
import com.example.teman_belajar.forgotpassword.steps.FPassStepFourScreen

@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit,
) {
    val steps = listOf(
        ForgotPasswordStep("Data Diri", Icons.Outlined.Badge),
        ForgotPasswordStep("Verifikasi", Icons.Outlined.Lock),
        ForgotPasswordStep("Atur Ulang", Icons.Outlined.Person),
        ForgotPasswordStep("Selesai", Icons.Filled.CheckCircle)
    )

    val screenTitle = when (uiState.currentStep) {
        1 -> "Lupa Kata Sandi?"
        2 -> "Verifikasi Kode"
        3 -> "Buat Kata Sandi Baru"
        4 -> "Berhasil!"
        else -> "Lupa Kata Sandi?"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            IconButton(
                onClick = { onEvent(ForgotPasswordEvent.BackClicked) },
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = screenTitle,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            FPassStepProgressIndicator(
                currentStep = uiState.currentStep,
                steps = steps,
                modifier = Modifier.padding(horizontal = 48.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "step_content"
            ) { step ->

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when (step) {
                        1 -> FPassStepOneScreen(uiState = uiState, onEvent = onEvent)
                        2 -> FPassStepTwoScreen(uiState = uiState, onEvent = onEvent)
                        3 -> FPassStepThreeScreen(uiState = uiState, onEvent = onEvent)
                        4 -> FPassStepFourScreen(onEvent = onEvent)
                    }
                }
            }
        }
    }
}
