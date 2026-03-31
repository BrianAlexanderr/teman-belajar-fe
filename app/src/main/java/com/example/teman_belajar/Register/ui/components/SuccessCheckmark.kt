package com.example.teman_belajar.Register.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.teman_belajar.theme.RegistrationColors

@Composable
fun SuccessCheckmark(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    autoAnimate: Boolean = true,
) {
    var triggered by remember { mutableStateOf(!autoAnimate) }

    LaunchedEffect(Unit) {
        if (autoAnimate) triggered = true
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "success_checkmark_scale"
    )

    val innerSize = size * 0.8125f
    val iconSize  = size * 0.45f

    Box(
        modifier = modifier
            .size(size)
            .scale(animatedScale),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .border(
                    width = 6.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            RegistrationColors.CheckRingOuter,
                            RegistrationColors.Purple,
                            RegistrationColors.CheckRingOuter
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(innerSize)
                .border(6.dp, RegistrationColors.CheckRingInner, CircleShape)
                .background(RegistrationColors.CheckBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = RegistrationColors.Success,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun SuccessCheckmarkPreview() {
    Box(
        modifier = androidx.compose.ui.Modifier
            .size(220.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        SuccessCheckmark(autoAnimate = false)
    }
}