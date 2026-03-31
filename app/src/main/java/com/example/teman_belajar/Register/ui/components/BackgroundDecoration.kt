package com.example.teman_belajar.Register.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.teman_belajar.theme.RegistrationColors


@Composable
fun BackgroundDecoration(
    modifier: Modifier = Modifier,
    topColor: Color = RegistrationColors.DecorationTop,
    bottomColor: Color = RegistrationColors.DecorationBot,
) {
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(topColor, Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.1f),
                radius = size.width * 0.5f
            ),
            radius = size.width * 0.5f,
            center = Offset(size.width * 0.85f, size.height * 0.1f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(bottomColor, Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 0.85f),
                radius = size.width * 0.45f
            ),
            radius = size.width * 0.45f,
            center = Offset(size.width * 0.1f, size.height * 0.85f)
        )
    }
}