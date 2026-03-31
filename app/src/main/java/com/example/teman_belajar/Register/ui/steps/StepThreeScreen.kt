package com.example.teman_belajar.Register.ui.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.teman_belajar.Register.ui.RegistrationEvent
import com.example.teman_belajar.Register.ui.components.PrimaryButton
import com.example.teman_belajar.Register.ui.components.SuccessCheckmark
import com.example.teman_belajar.theme.RegistrationColors
import com.example.teman_belajar.theme.RegistrationSpacing

@Composable
fun StepThreeScreen(
    onEvent: (RegistrationEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = RegistrationSpacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        SuccessCheckmark()

        Spacer(modifier = Modifier.height(RegistrationSpacing.xl))

        Text(
            text = "All Done !!!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = RegistrationColors.TextPrimary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(RegistrationSpacing.sm))

        Text(
            text = "Thank You For Registering",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = RegistrationColors.TextSecondary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Login",
            onClick = { onEvent(RegistrationEvent.LoginClicked) }
        )

        Spacer(modifier = Modifier.height(RegistrationSpacing.xl))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StepThreePreview() {
    MaterialTheme {
        StepThreeScreen(onEvent = {})
    }
}