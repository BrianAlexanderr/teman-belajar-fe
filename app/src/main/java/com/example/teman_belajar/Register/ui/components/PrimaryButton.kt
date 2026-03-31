package com.example.teman_belajar.Register.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teman_belajar.theme.RegistrationColors
import com.example.teman_belajar.theme.RegistrationShapes

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    val scale by animateFloatAsState(
        targetValue = if (isLoading) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "primary_btn_scale"
    )

    Button(
        onClick = { if (!isLoading) onClick() },
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(RegistrationShapes.buttonHeight)
            .scale(scale),
        shape = RoundedCornerShape(RegistrationShapes.buttonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor         = RegistrationColors.Purple,
            contentColor           = Color.White,
            disabledContainerColor = RegistrationColors.PurpleLight,
            disabledContentColor   = RegistrationColors.Purple,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp
        )
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(tween(150)) togetherWith fadeOut(tween(150))
            },
            label = "primary_btn_content"
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(text = "Continue", onClick = {})
            PrimaryButton(text = "Loading...", onClick = {}, isLoading = true)
            PrimaryButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}