package com.example.teman_belajar.Register.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.teman_belajar.theme.RegistrationColors
import com.example.teman_belajar.theme.RegistrationShapes
import kotlinx.coroutines.delay

data class StepItem(
    val label: String,
    val icon: ImageVector,
)

@Composable
fun StepProgressIndicator(
    currentStep: Int,
    steps: List<StepItem>,
    modifier: Modifier = Modifier,
    dotCount: Int = 6,
    dotSize: Dp = 7.dp,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val stepNum     = index + 1
            val isCompleted = currentStep > stepNum
            val isActive    = currentStep == stepNum

            StepNode(
                label       = step.label,
                icon        = step.icon,
                isActive    = isActive,
                isCompleted = isCompleted,
            )

            if (index < steps.size - 1) {
                AnimatedDotConnector(
                    filled    = currentStep > stepNum,
                    dotCount  = dotCount,
                    dotSize   = dotSize,
                )
            }
        }
    }
}

@Composable
private fun RowScope.AnimatedDotConnector(
    filled: Boolean,
    dotCount: Int,
    dotSize: Dp,
) {
    var filledDots by remember { mutableIntStateOf(if (filled) dotCount else 0) }

    LaunchedEffect(filled) {
        if (filled) {
            for (i in 1..dotCount) {
                filledDots = i
                delay(60L)
            }
        } else {
            for (i in dotCount - 1 downTo 0) {
                filledDots = i
                delay(60L)
            }
        }
    }

    Row(
        modifier = Modifier
            .weight(1f)
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(dotCount) { index ->
            val isFilled = index < filledDots

            val dotColor by animateColorAsState(
                targetValue = if (isFilled) RegistrationColors.Purple
                else RegistrationColors.PurpleLight,
                animationSpec = tween(durationMillis = 200),
                label = "dot_color_$index"
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(dotColor, CircleShape)
            )
        }
    }
}

@Composable
private fun StepNode(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    isCompleted: Boolean,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isCompleted || isActive) RegistrationColors.Purple
        else Color(0xFFEEEEEE),
        animationSpec = tween(300),
        label = "node_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isCompleted || isActive) RegistrationColors.Purple
        else Color(0xFFDDDDDD),
        animationSpec = tween(300),
        label = "node_border"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isCompleted || isActive) Color.White
        else RegistrationColors.Purple,
        animationSpec = tween(300),
        label = "node_icon"
    )
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "node_scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(RegistrationShapes.stepSize)
                .scale(scale)
                .border(2.dp, borderColor, CircleShape)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.Check else icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isActive || isCompleted) RegistrationColors.Purple
                else RegistrationColors.TextSecondary,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}