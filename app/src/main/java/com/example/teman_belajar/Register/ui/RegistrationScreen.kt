package com.example.teman_belajar.Register.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.teman_belajar.Register.ui.components.BackgroundDecoration
import com.example.teman_belajar.Register.ui.components.StepItem
import com.example.teman_belajar.Register.ui.components.StepProgressIndicator
import com.example.teman_belajar.Register.ui.steps.StepOneScreen
import com.example.teman_belajar.Register.ui.steps.StepThreeScreen
import com.example.teman_belajar.Register.ui.steps.StepTwoScreen
import com.example.teman_belajar.theme.RegistrationColors
import com.example.teman_belajar.theme.RegistrationSpacing

@Composable
fun RegistrationScreen(
    uiState: RegistrationUiState,
    onEvent: (RegistrationEvent) -> Unit,
) {
    val steps = listOf(
        StepItem("Details", Icons.Outlined.Person),
        StepItem("Account", Icons.Outlined.AccountCircle),
        StepItem("Done",    Icons.Default.CheckCircle)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RegistrationColors.Background)
    ) {
        BackgroundDecoration(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            AnimatedVisibility(
                visible = uiState.currentStep == 2,
                enter = fadeIn() + slideInHorizontally(),
                exit  = fadeOut() + slideOutHorizontally()
            ) {
                IconButton(
                    onClick = { onEvent(RegistrationEvent.BackClicked) },
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = RegistrationColors.TextPrimary
                    )
                }
            }

            if (uiState.currentStep != 2) {
                Spacer(modifier = Modifier.height(52.dp))
            }

            Text(
                text = "Registration",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = RegistrationColors.TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RegistrationSpacing.screenHorizontal),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(RegistrationSpacing.md + 4.dp))

            StepProgressIndicator(
                currentStep = uiState.currentStep,
                steps = steps,
                modifier = Modifier.padding(horizontal = RegistrationSpacing.xxl)
            )

            Spacer(modifier = Modifier.height(RegistrationSpacing.xl))

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
                when (step) {
                    1 -> StepOneScreen(uiState = uiState, onEvent = onEvent)
                    2 -> StepTwoScreen(uiState = uiState, onEvent = onEvent)
                    3 -> StepThreeScreen(onEvent = onEvent)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Step 1")
@Composable
private fun Step1Preview() {
    MaterialTheme { RegistrationScreen(uiState = RegistrationUiState(currentStep = 1), onEvent = {}) }
}

@Preview(showBackground = true, showSystemUi = true, name = "Step 2")
@Composable
private fun Step2Preview() {
    MaterialTheme { RegistrationScreen(uiState = RegistrationUiState(currentStep = 2), onEvent = {}) }
}

@Preview(showBackground = true, showSystemUi = true, name = "Step 3")
@Composable
private fun Step3Preview() {
    MaterialTheme { RegistrationScreen(uiState = RegistrationUiState(currentStep = 3), onEvent = {}) }
}